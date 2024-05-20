package com.swxctx.plex;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.IOException;

/**
 * @Author swxctx
 * @Date 2024-05-20
 * @Describe:
 */
public class PlexTCPService extends Service {
    private PlexSocketManager socketManager;
    private PlexMessageHandler messageHandler;
    private Thread listeningThread;
    private boolean isRunning = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;  // Return null for non-bound service
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startService();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService();
    }

    private void startService() {
        if (!isRunning) {
            isRunning = true;
            socketManager = new PlexSocketManager();
            messageHandler = new PlexMessageHandler();

            // Assume server IP and port are configured or retrieved from intent or shared preferences
            String serverIp = "117.50.198.225";
            int serverPort = 9578;

            try {
                socketManager.connect(serverIp, serverPort);
            } catch (IOException e) {
                PlexLog.e("Failed to connect to server: " + e.getMessage());
                return;
            }

            listeningThread = new Thread(this::listenForMessages);
            listeningThread.start();
        }
    }

    private void stopService() {
        if (isRunning) {
            isRunning = false;
            if (listeningThread != null) {
                try {
                    listeningThread.interrupt();
                    socketManager.disconnect();
                } catch (Exception e) {
                    PlexLog.e("Error stopping service: " + e.getMessage());
                }
            }
        }
    }

    private void listenForMessages() {
        while (isRunning) {
            try {
                byte[] message = socketManager.receiveData();
                messageHandler.handleMessage(message);
            } catch (IOException e) {
                PlexLog.e("Error receiving messages: " + e.getMessage());
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
            }
        }
    }
}