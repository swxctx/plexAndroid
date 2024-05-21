package com.swxctx.plex;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * @Author swxctx
 * @Date 2024-05-20
 * @Describe:
 */
public class PlexManager {
    private static PlexManager instance;
    private Context context;
    private PlexTCPService tcpService;
    private boolean isBound = false;

    private PlexCallbackInterface.OnMessageReceivedListener messageReceivedListener;
    private PlexCallbackInterface.OnConnectionStatusChangedListener connectionStatusChangedListener;

    private PlexManager(Context context) {
        this.context = context;
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = new PlexManager(context);
        }
    }

    public static PlexManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Manager is not initialized, call init first.");
        }
        return instance;
    }

    public void setOnMessageReceivedListener(PlexCallbackInterface.OnMessageReceivedListener listener) {
        PlexLog.d("11133333");
        this.messageReceivedListener = listener;
    }

    public void setOnConnectionStatusChangedListener(PlexCallbackInterface.OnConnectionStatusChangedListener listener) {
        this.connectionStatusChangedListener = listener;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlexLog.d("Service connected");

            PlexTCPService.LocalBinder binder = (PlexTCPService.LocalBinder) service;
            tcpService = binder.getService();
            tcpService.setMessageReceivedListener(new PlexCallbackInterface.OnMessageReceivedListener() {
                @Override
                public void onMessageReceived(PlexMessage message) {
                    PlexLog.d(String.valueOf(messageReceivedListener != null));
                    if (messageReceivedListener != null) {
                        messageReceivedListener.onMessageReceived(message);
                    }
                }
            });
            tcpService.setConnectionStatusChangedListener(new PlexCallbackInterface.OnConnectionStatusChangedListener() {
                @Override
                public void onConnected() {
                    PlexLog.d("TCP Action onConnected");
                    if (connectionStatusChangedListener != null) {
                        connectionStatusChangedListener.onConnected();
                    }
                }

                @Override
                public void onDisconnected() {
                    PlexLog.d("TCP Action onDisconnected");
                    if (connectionStatusChangedListener != null) {
                        connectionStatusChangedListener.onDisconnected();
                    }
                }

                @Override
                public void onConnectionFailed(Exception e) {
                    PlexLog.d("TCP Action onConnectionFailed");
                    if (connectionStatusChangedListener != null) {
                        connectionStatusChangedListener.onConnectionFailed(e);
                    }
                }

                @Override
                public void onConnectAuthFailed() {
                    PlexLog.d("TCP Action onConnectAuthFailed");
                    if (connectionStatusChangedListener != null) {
                        connectionStatusChangedListener.onConnectAuthFailed();
                    }
                }
            });
            tcpService.connect();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            PlexLog.d("Service disconnected");
        }
    };

    public void start() {
        PlexLog.d("Starting service...");
        Intent serviceIntent = new Intent(context, PlexTCPService.class);
        context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void stop() {
        if (isBound && tcpService != null) {
            context.unbindService(serviceConnection);
            isBound = false;
            tcpService = null;
            PlexLog.d("Disconnected from server");
        }
    }

    public void reconnect() {
        // stop
        stop();
        // start
        start();
    }

    public boolean connected() {
        if (isBound && tcpService != null) {
            return tcpService.isConnected();
        }
        return false;
    }
}