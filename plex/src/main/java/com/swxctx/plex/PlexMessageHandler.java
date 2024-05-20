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

    public PlexMessageHandler(PlexTCPService tcpService) {
        this.tcpService = tcpService;
    }

    public void handleMessage(String packMessage) {
        try {
            PlexMessage message = gson.fromJson(packMessage, PlexMessage.class);
            switch (message.getUri()) {
                case "/auth/success":
                    handleAuthSuccess();
                    break;
                case "/heartbeat":
                    handleHeartbeat();
                    break;
                default:
                    handleDefault(message);
                    break;
            }
        } catch (Exception e) {
            PlexLog.e("Error handling message: " + e.getMessage());
        }
    }

    private void handleAuthSuccess() {
        PlexLog.d("Authentication successful.");
        tcpService.startHeartbeat();  // 调用tcpService的startHeartbeat方法以启动心跳
    }

    private void handleHeartbeat() {
        PlexLog.d("Heartbeat received.");
    }

    private void handleDefault(PlexMessage message) {
        PlexLog.d("Received message: " + message.getBody());
    }
}
