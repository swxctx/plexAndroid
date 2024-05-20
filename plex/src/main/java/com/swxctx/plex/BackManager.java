package com.swxctx.plex;

import android.content.Context;
import android.os.Handler;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @Author swxctx
 * @Date 2024-05-20
 * @Describe:
 */
public class BackManager {
    private static BackManager manager;
    private Context context;
    private static String[] serverAddress;


    private static Socket socket;
    private static OutputStream output;
    private static BufferedReader input;

    public BackManager(Context context) {
        this.context = context;
    }

    public static void init(Context context, String[] address) {
        PlexLog.d("Manager init");
        manager = new BackManager(context);
        serverAddress = address;
    }

    public static BackManager getInstance() {
        return manager;
    }

    public void connectToServer() {
        new Thread(() -> {
            try {
                String serverIp = PlexUtil.getServerIp(serverAddress[0]);
                if (serverIp.length() == 0) {
                    PlexLog.e("server ip is nil");
                    return;
                }
                int serverPort = PlexUtil.getServerPort(serverAddress[0]);
                if (serverPort == 0) {
                    PlexLog.e("server port is nil");
                    return;
                }

                socket = new Socket(serverIp, serverPort);
                output = socket.getOutputStream();
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // listen receive
                listenForMessages();

                // auth
                PlexMessage message = new PlexMessage("/auth/server", "1");
                String msg = new Gson().toJson(message);
                sendMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void startHeartbeat() {
        Handler handler = new Handler();
        Runnable heartbeatRunnable = new Runnable() {
            @Override
            public void run() {
                if (output != null) {
                    PlexMessage message = new PlexMessage("/heartbeat");
                    try {
                        sendMessage(new Gson().toJson(message));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    handler.postDelayed(this, 60000); // 60秒
                }
            }
        };
        handler.post(heartbeatRunnable);
    }

    private void listenForMessages() {
        new Thread(() -> {
            try {
                while (true) {  // 循环，直到连接关闭或发生错误
                    String receivedMsg = receiveMessage();  // 接收消息
                    if (receivedMsg == null) {
                        PlexLog.e("No message received or connection closed");
                        break;  // 如果没有消息或连接已关闭，退出循环
                    }
                    PlexLog.d("receive msg-> " + receivedMsg);
                    // 处理接收到的消息
                    PlexMessage message = new Gson().fromJson(receivedMsg, PlexMessage.class);
                    if (message.getUri().equals("/auth/success")) {
                        PlexLog.d("auth success");
                        startHeartbeat();
                    } else if (message.getUri().equals("/auth/failed")) {
                        PlexLog.e("auth failed...");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void sendMessage(String message) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(output);

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
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

        // 读取消息长度
        int messageLength = dataInputStream.readInt();

        // 读取消息体
        if (messageLength > 0) {
            byte[] messageBytes = new byte[messageLength];
            dataInputStream.readFully(messageBytes); // 确保读取所有字节
            return new String(messageBytes, StandardCharsets.UTF_8);
        }
        return null;
    }
}
