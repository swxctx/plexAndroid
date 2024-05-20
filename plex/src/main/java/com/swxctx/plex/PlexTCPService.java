package com.swxctx.plex;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.IOException;

/**
 * @Author swxctx
 * @Date 2024-05-20
 * @Describe:
 */

public class PlexTCPService extends Service {
    private final IBinder binder = new LocalBinder();
    private PlexSocketManager socketManager;
    private PlexMessageHandler messageHandler;
    private Thread listeningThread;
    private boolean isConnected = false;

    public class LocalBinder extends Binder {
        PlexTCPService getService() {
            return PlexTCPService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initializeComponents();
    }

    private void initializeComponents() {
        if (socketManager == null) {
            socketManager = new PlexSocketManager();
            messageHandler = new PlexMessageHandler(this);
        }
    }

    @Override
    public void onDestroy() {
        disconnect();
        super.onDestroy();
    }

    public void connect() {
        PlexLog.d("Attempting to connect to server...");
        socketManager.connect(new PlexConnectionCallback() {
            @Override
            public void onConnectionSuccess() {
                isConnected = true;
                startMessageListening();
                sendAuthentication();
                PlexLog.d("Connected to server successfully.");
            }

            @Override
            public void onConnectionFailed(Exception e) {
                PlexLog.e("Failed to connect to server: " + e.getMessage());
                isConnected = false;
            }
        });
    }

    private void startMessageListening() {
        listeningThread = new Thread(this::listenForMessages);
        listeningThread.start();
    }

    private void sendAuthentication() {
        try {
            String authData = PlexConfig.getInstance().getAuthData();
            PlexMessage message = new PlexMessage("/auth/server", authData);
            String msg = new Gson().toJson(message);
            sendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
            PlexLog.e("Failed to send authentication message: " + e.getMessage());
        }
    }

    public void disconnect() {
        if (!isConnected) {
            return;
        }
        isConnected = false;
        if (socketManager != null) {
            socketManager.disconnect();
        }
        if (listeningThread != null) {
            listeningThread.interrupt();
        }
        stopHeartbeat();
        PlexLog.d("Disconnected from server.");
    }

    private void listenForMessages() {
        PlexLog.d("listenForMessages start");
        while (isConnected) {
            try {
                String receivedMsg = socketManager.receiveMessage();
                if (TextUtils.isEmpty(receivedMsg)) {
                    // PlexLog.e("No message received or connection closed");
                    continue;
                }
                PlexLog.d("Received message: " + receivedMsg);
                messageHandler.handleMessage(receivedMsg);
            } catch (IOException e) {
                PlexLog.e("Error receiving messages: " + e.getMessage());
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
            }
        }
    }

    public void sendMessage(String message) throws IOException {
        if (socketManager != null && isConnected) {
            socketManager.sendData(message);
        }
    }

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable heartbeatRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (isConnected) {
                    PlexMessage message = new PlexMessage("/heartbeat");
                    String msg = new Gson().toJson(message);
                    sendMessage(msg);
                    PlexLog.d("Heartbeat sent.");
                }
            } catch (IOException e) {
                PlexLog.e("Failed to send heartbeat: " + e.getMessage());
            }
            // Schedule the next heartbeat if still connected
            if (isConnected) {
                handler.postDelayed(this, 60000); // Schedule to run after 60 seconds
            }
        }
    };

    public void startHeartbeat() {
        handler.postDelayed(heartbeatRunnable, 60000); // Start the first heartbeat after 60 seconds
        PlexLog.d("Heartbeat scheduling started.");
    }

    public void stopHeartbeat() {
        handler.removeCallbacks(heartbeatRunnable);
        PlexLog.d("Heartbeat scheduling stopped.");
    }
}