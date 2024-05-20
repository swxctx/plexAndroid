package com.swxctx.plex;

import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;

/**
 * @Author swxctx
 * @Date 2024-05-20
 * @Describe:
 */
public class PlexMessageHandler {
    private Gson gson = new Gson();  // 使用Gson进行JSON序列化和反序列化

    public void handleMessage(byte[] messageBytes) {
        try {
            String jsonMessage = new String(messageBytes, StandardCharsets.UTF_8);
            PlexMessage message = gson.fromJson(jsonMessage, PlexMessage.class);  // 将JSON字符串转换为Message对象

            switch (message.getUri()) {
                case "/auth/success":
                    handleAuthSuccess(message);
                    break;
                case "/heartbeat":
                    handleHeartbeat(message);
                    break;
                default:
                    handleDefault(message);
                    break;
            }
        } catch (Exception e) {
            PlexLog.e("Error handling message: " + e.getMessage());
        }
    }

    private void handleAuthSuccess(PlexMessage message) {
        // 处理认证消息
        PlexLog.d("Authentication successful.");
    }

    private void handleHeartbeat(PlexMessage message) {
        // 处理心跳检测消息
        PlexLog.d("Heartbeat received.");
    }

    private void handleDefault(PlexMessage message) {
        // 处理其他类型的消息
        PlexLog.d("Received message: " + message.getBody());
    }
}
