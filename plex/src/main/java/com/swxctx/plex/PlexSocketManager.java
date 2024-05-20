package com.swxctx.plex;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @Author swxctx
 * @Date 2024-05-20
 * @Describe:
 */
public class PlexSocketManager {
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    private volatile boolean isConnected = false;

    public synchronized void connect(final PlexConnectionCallback callback) {
        new Thread(() -> {
            try {
                if (socket != null && socket.isConnected()) {
                    // Already connected
                    return;
                }

                String serverIp = PlexConfig.getInstance().getServerIp();
                int serverPort = PlexConfig.getInstance().getServerPort();

                socket = new Socket();
                // 5000 milliseconds connection timeout
                socket.connect(new InetSocketAddress(serverIp, serverPort), 5000);

                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
                dataOutputStream = new DataOutputStream(outputStream);
                dataInputStream = new DataInputStream(inputStream);

                isConnected = true;
                callback.onConnectionSuccess();
            } catch (IOException e) {
                isConnected = false;
                e.printStackTrace();
                callback.onConnectionFailed(e);
            }
        }).start();
    }

    public synchronized void disconnect() {
        if (socket != null) {
            try {
                dataInputStream.close();
                dataOutputStream.close();
                inputStream.close();
                outputStream.close();
                socket.close();
                socket = null;
                isConnected = false;
            } catch (IOException e) {
                // Log or handle the exception as needed
                PlexLog.e("Error closing network resources: " + e.getMessage());
            }
        }
    }

    public void sendData(String message) throws IOException {
        if (!isConnected) {
            throw new IOException("Not connected to a server");
        }

        // 将String消息转换为字节
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

        // 计算消息长度
        int messageLength = messageBytes.length;

        // 发送消息长度（4字节）
        dataOutputStream.writeInt(messageLength);

        // 发送消息体
        dataOutputStream.write(messageBytes);
        // 确保数据被发送
        dataOutputStream.flush();
    }

    public String receiveMessage() throws IOException {
        int messageLength = dataInputStream.readInt();
        if (messageLength > 0) {
            byte[] messageBytes = new byte[messageLength];
            dataInputStream.readFully(messageBytes);
            return new String(messageBytes, StandardCharsets.UTF_8);
        }
        return null;
    }

    public boolean isConnected() {
        return isConnected;
    }
}