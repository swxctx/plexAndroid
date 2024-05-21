package com.swxctx.plex;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    private PlexCallbackInterface.OnMessageReceivedListener messageReceivedListener;
    private PlexCallbackInterface.OnConnectionStatusChangedListener connectionStatusChangedListener;
    private final String heartbeatMessageJson;
    private final ScheduledExecutorService heartbeatScheduler = Executors.newScheduledThreadPool(1);
    private final ScheduledExecutorService reconnectScheduler = Executors.newScheduledThreadPool(1);

    public PlexTCPService() {
        PlexMessage heartbeatMessage = new PlexMessage(PlexConstant.URI_FOR_HEARTBEAT);
        this.heartbeatMessageJson = new Gson().toJson(heartbeatMessage);
    }

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
    public int onStartCommand(Intent intent, int flags, int startId) {
        PlexLog.d("PlexTCPService on start...");
        initializeComponents();
        return START_STICKY;
    }

    private void initializeComponents() {
        if (socketManager == null) {
            socketManager = new PlexSocketManager();
            messageHandler = new PlexMessageHandler(this);
            messageHandler.setMessageReceivedListener(messageReceivedListener);
            messageHandler.setConnectionStatusChangedListener(connectionStatusChangedListener);
        }
    }

    @Override
    public void onDestroy() {
        PlexLog.w("tcp service onDestroy");
        disconnect();
        super.onDestroy();
    }

    public void setMessageReceivedListener(PlexCallbackInterface.OnMessageReceivedListener listener) {
        this.messageReceivedListener = listener;
        if (messageHandler != null) {
            messageHandler.setMessageReceivedListener(listener);
        }
    }

    public void setConnectionStatusChangedListener(PlexCallbackInterface.OnConnectionStatusChangedListener listener) {
        this.connectionStatusChangedListener = listener;
        if (messageHandler != null) {
            messageHandler.setConnectionStatusChangedListener(listener);
        }
    }


    /**
     * connection
     */
    private synchronized void updateConnectionStatus(boolean status) {
        isConnected = status;
    }

    public void connect() {
        PlexLog.d("Attempting to connect to server...");
        // init check
        initializeComponents();

        // Fetch server IP and port before connecting
        new FetchServerTask(new FetchServerTask.FetchServerCallback() {
            @Override
            public void onFetchSuccess() {
                PlexLog.d("FetchServerTask onFetchSuccess, ip-> " + PlexConfig.getInstance().getServerIp() + ", port-> " + PlexConfig.getInstance().getServerPort());
                startConnection();
            }

            @Override
            public void onFetchFailure(Exception e) {
                PlexLog.e("Failed to fetch server info-> " + e.getMessage());
                if (connectionStatusChangedListener != null) {
                    connectionStatusChangedListener.onConnectionFailed(e);
                }
                scheduleReconnect();
            }
        }).execute();
    }

    private void startConnection() {
        socketManager.connect(new PlexConnectionCallback() {
            @Override
            public void onConnectionSuccess() {
                PlexLog.d("Connected to server successfully.");
                updateConnectionStatus(true);
                // start message listen
                startMessageListening();
                // auth
                sendAuthentication();
            }

            @Override
            public void onConnectionFailed(Exception e) {
                PlexLog.e("Failed to connect to server: " + e.getMessage());
                updateConnectionStatus(false);
                if (connectionStatusChangedListener != null) {
                    connectionStatusChangedListener.onConnectionFailed(e);
                }
                // reconnect
                scheduleReconnect();
            }
        });
    }

    public void disconnect() {
        PlexLog.d("Disconnected from server.");
        if (!isConnected) {
            return;
        }
        updateConnectionStatus(false);
        if (listeningThread != null) {
            listeningThread.interrupt();
        }
        shutdownSchedulers();
        if (socketManager != null) {
            socketManager.disconnect();
        }
        if (connectionStatusChangedListener != null) {
            connectionStatusChangedListener.onDisconnected();
        }
    }

    public boolean isConnected() {
        return isConnected && socketManager != null && socketManager.isConnected();
    }

    private void handleConnectionLoss() {
        updateConnectionStatus(false);
        if (listeningThread != null) {
            listeningThread.interrupt();
        }
        if (socketManager != null) {
            socketManager.disconnect();
        }
        if (connectionStatusChangedListener != null) {
            connectionStatusChangedListener.onDisconnected();
        }
        scheduleReconnect();
    }

    private void scheduleReconnect() {
        if (shouldReconnect()) {
            reconnectScheduler.schedule(this::connect, PlexConfig.getInstance().getReconnectInterval(), TimeUnit.MILLISECONDS);
        }
    }

    private void shutdownSchedulers() {
        heartbeatScheduler.shutdownNow();
        reconnectScheduler.shutdownNow();
    }

    private boolean shouldReconnect() {
        PlexConfig.ReconnectStrategy strategy = PlexConfig.getInstance().getReconnectStrategy();
        switch (strategy) {
            case ALWAYS:
                return true;
            case ON_FAILURE:
                // Only try to reconnect if not connected
                return !isConnected;
            default:
                return false;
        }
    }

    /**
     * message send and listening
     */
    private void sendAuthentication() {
        String authData = PlexConfig.getInstance().getAuthData();
        PlexMessage message = new PlexMessage(PlexConstant.URI_FOR_AUTH, authData);
        String msg = new Gson().toJson(message);
        sendMessage(msg);
    }

    private void startMessageListening() {
        listeningThread = new Thread(this::listenForMessages);
        listeningThread.start();
    }

    private void listenForMessages() {
        PlexLog.d("listenForMessages start");
        while (isConnected) {
            try {
                String receivedMsg = socketManager.receiveMessage();
                if (TextUtils.isEmpty(receivedMsg)) {
                    continue;
                }
                PlexLog.d("Received message: " + receivedMsg);
                messageHandler.handleMessage(receivedMsg);
            } catch (IOException e) {
                e.printStackTrace();
                PlexLog.e("Error receiving messages: " + e.getMessage());
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
                handleConnectionLoss();
            }
        }
    }

    public void sendMessage(String message) {
        if (socketManager != null && isConnected) {
            socketManager.sendData(message);
        }
    }

    /**
     * heartbeat
     */
    private final Runnable heartbeatRunnable = new Runnable() {
        @Override
        public void run() {
            // not connected
            if (!isConnected || socketManager == null || !socketManager.isConnected()) {
                return;
            }
            sendMessage(heartbeatMessageJson);
            PlexLog.d("Heartbeat send.");
        }
    };

    public void startHeartbeat() {
        // Start the first heartbeat after heartbeatInterval ms
        int heartbeatInterval = PlexConfig.getInstance().getHeartbeatInterval();
        heartbeatScheduler.scheduleAtFixedRate(heartbeatRunnable, heartbeatInterval, heartbeatInterval, TimeUnit.MILLISECONDS);
        PlexLog.d("Heartbeat scheduling started...");
    }

    public void stopHeartbeat() {
        heartbeatScheduler.shutdownNow();
        PlexLog.d("Heartbeat scheduling stopped...");
    }
}