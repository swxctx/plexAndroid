package com.swxctx.plex;

/**
 * @Author hu_yang
 * @Date 2024-05-20
 * @Describe:
 */
public interface CallbackInterface {
    interface OnMessageReceivedListener {
        void onMessageReceived(PlexMessage message);
    }

    interface OnConnectionStatusChangedListener {
        void onConnected();
        void onDisconnected();
        void onConnectionFailed(Exception e);
    }
}
