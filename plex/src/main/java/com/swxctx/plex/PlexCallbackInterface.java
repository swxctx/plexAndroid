package com.swxctx.plex;

/**
 * @Author swxctx
 * @Date 2024-05-20
 * @Describe:
 */
public interface PlexCallbackInterface {
    interface OnMessageReceivedListener {
        void onMessageReceived(PlexMessage message);
    }

    interface OnConnectionStatusChangedListener {
        void onConnected();

        void onDisconnected();

        void onConnectionFailed(Exception e);

        void onConnectAuthFailed();
    }
}
