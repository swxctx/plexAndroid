package com.swxctx.plex;

import com.google.gson.Gson;

/**
 * @Author swxctx
 * @Date 2024-05-20
 * @Describe:
 */
public class PlexMessageHandler {
    private PlexTCPService tcpService;
    private Gson gson = new Gson();

    private PlexCallbackInterface.OnMessageReceivedListener messageReceivedListener;
    private PlexCallbackInterface.OnConnectionStatusChangedListener connectionStatusChangedListener;

    public PlexMessageHandler(PlexTCPService tcpService) {
        this.tcpService = tcpService;
    }

    public void setMessageReceivedListener(PlexCallbackInterface.OnMessageReceivedListener listener) {
        this.messageReceivedListener = listener;
    }

    public void setConnectionStatusChangedListener(PlexCallbackInterface.OnConnectionStatusChangedListener listener) {
        this.connectionStatusChangedListener = listener;
    }

    public void handleMessage(String packMessage) {
        try {
            PlexMessage message = gson.fromJson(packMessage, PlexMessage.class);
            switch (message.getUri()) {
                case PlexConstant.URI_FOR_AUTH_SUCCESS:
                    handleAuthSuccess();
                    break;
                case PlexConstant.URI_FOR_HEARTBEAT:
                    handleHeartbeat();
                    break;
                case PlexConstant.URI_FOR_AUTH_FAILED:
                    if (connectionStatusChangedListener != null) {
                        connectionStatusChangedListener.onConnectAuthFailed();
                    }
                    break;
                default:
                    if (messageReceivedListener != null) {
                        messageReceivedListener.onMessageReceived(message);
                    }
                    break;
            }
        } catch (Exception e) {
            PlexLog.e("Error handling message: " + e.getMessage());
        }
    }

    private void handleAuthSuccess() {
        PlexLog.d("Authentication successful.");
        tcpService.startHeartbeat();
        if (connectionStatusChangedListener != null) {
            connectionStatusChangedListener.onConnected();
        }
    }

    private void handleHeartbeat() {
        PlexLog.d("Heartbeat received.");
    }
}
