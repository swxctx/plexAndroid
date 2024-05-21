package com.swxctx.plex;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public synchronized void connect(final PlexConnectionCallback callback) {
        executorService.execute(() -> {
            try {
                if (socket != null && socket.isConnected()) {
                    // Already connected
                    callback.onConnectionSuccess();
                    return;
                }

                String serverIp = PlexConfig.getInstance().getServerIp();
                int serverPort = PlexConfig.getInstance().getServerPort();

                socket = new Socket();
                socket.connect(new InetSocketAddress(serverIp, serverPort), PlexConfig.getInstance().getConnectTimeout());

                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
                dataOutputStream = new DataOutputStream(outputStream);
                dataInputStream = new DataInputStream(inputStream);

                callback.onConnectionSuccess();
            } catch (IOException e) {
                callback.onConnectionFailed(e);
            }
        });
    }

    public synchronized void disconnect() {
        closeResources();
    }

    private void closeResources() {
        try {
            if (dataInputStream != null) dataInputStream.close();
            if (dataOutputStream != null) dataOutputStream.close();
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            PlexLog.e("Error closing network resources: " + e.getMessage());
        } finally {
            socket = null;
            dataInputStream = null;
            dataOutputStream = null;
            inputStream = null;
            outputStream = null;
        }
    }

    public void sendData(String message) {
        if (!isConnected()) {
            PlexLog.w("sendData, Not connected to a server");
            return;
        }

        executorService.execute(() -> {
            try {
                byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

                // msg length
                int messageLength = messageBytes.length;

                // send head
                dataOutputStream.writeInt(messageLength);

                // send msg body
                dataOutputStream.write(messageBytes);
                dataOutputStream.flush();
            } catch (IOException e) {
                PlexLog.e("Error sending data: " + e.getMessage());
            }
        });
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
        return socket != null && socket.isConnected();
    }
}