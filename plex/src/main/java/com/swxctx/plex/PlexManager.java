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

    public void startService() {
        PlexLog.d("Starting service...");
        Intent serviceIntent = new Intent(context, PlexTCPService.class);
        context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlexTCPService.LocalBinder binder = (PlexTCPService.LocalBinder) service;
            tcpService = binder.getService();
            tcpService.connect();
            isBound = true;
            PlexLog.d("Service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            PlexLog.d("Service disconnected");
        }
    };

    public void stopService() {
        if (isBound && tcpService != null) {
            context.unbindService(serviceConnection);
            isBound = false;
            tcpService = null;
            PlexLog.d("Disconnected from server");
        }
    }
}