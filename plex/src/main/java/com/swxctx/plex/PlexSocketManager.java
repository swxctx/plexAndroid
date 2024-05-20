package com.swxctx.plex;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
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
    private boolean isConnected = false;

    public synchronized void connect(String ip, int port) throws IOException {
        if (socket != null && socket.isConnected()) {
            return; // Already connected
        }
        socket = new Socket(ip, port);
        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
        dataOutputStream = new DataOutputStream(outputStream);
        dataInputStream = new DataInputStream(inputStream);
        isConnected = true;
    }

    public synchronized void disconnect() {
        if (socket != null) {
            try {
                dataInputStream.close();
                dataOutputStream.close();
                inputStream.close();
                outputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                socket = null;
                isConnected = false;
            }
        }
    }

    public void sendData(byte[] data) throws IOException {
        if (isConnected()) {
            dataOutputStream.write(data);
            dataOutputStream.flush();
        }
    }

    public byte[] receiveData() throws IOException {
        byte[] data = new byte[1024]; // buffer size
        int length = dataInputStream.read(data);
        if (length == -1) {
            throw new IOException("End of stream reached");
        }
        return Arrays.copyOf(data, length);
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}