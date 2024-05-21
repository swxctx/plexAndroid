package com.swxctx.plexdemo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.swxctx.plex.PlexCallbackInterface;
import com.swxctx.plex.PlexConfig;
import com.swxctx.plex.PlexLog;
import com.swxctx.plex.PlexManager;
import com.swxctx.plex.PlexMessage;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private TextView tvMessage;
    private Button bConnectStatus;
    private Button bReconnect;
    private Button bReceive;
    private Button bDisconnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvMessage = findViewById(R.id.tv_message);
        bConnectStatus = (Button) findViewById(R.id.b_connect_status);
        bReconnect = (Button) findViewById(R.id.b_reconnect);
        bReceive = (Button) findViewById(R.id.b_receive);
        bDisconnect = (Button) findViewById(R.id.b_disconnect);

        initData();
        bindListen();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private int getRandomInteger(int min, int max) {
        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }

    private void initData() {
        // 可以直接设置服务器的IP及端口，也可以设置部署Plex Server的服务器地址(建议这样做，因为Plex会做负载均衡，不要写死)
        /*PlexConfig.getInstance().setServerIp("117.50.198.225");
        PlexConfig.getInstance().setServerPort(9578);*/
        PlexConfig.getInstance().setServerAddress("https://plex.developer.icu/plex/v1/host");
        // 随机一个数字，实际使用的时候根据服务器校验规则处理
        PlexConfig.getInstance().setAuthData(String.valueOf(getRandomInteger(1, 10000)));
        PlexConfig.getInstance().setHeartbeatInterval(3000);

        PlexManager.init(this);
        PlexManager.getInstance().setOnMessageReceivedListener(new PlexCallbackInterface.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(PlexMessage message) {
                PlexLog.d("onMessageReceived, uri-> " + message.getUri());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvMessage.setText(message.toString());
                    }
                });
            }
        });

        PlexManager.getInstance().setOnConnectionStatusChangedListener(new PlexCallbackInterface.OnConnectionStatusChangedListener() {
            @Override
            public void onConnected() {
                PlexLog.d("Callback onConnected");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvMessage.setText("onConnected...");
                    }
                });
            }

            @Override
            public void onDisconnected() {
                PlexLog.d("Callback onDisconnected");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvMessage.setText("onDisconnected...");
                    }
                });
            }

            @Override
            public void onConnectionFailed(Exception e) {
                PlexLog.d("Callback onConnectionFailed");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvMessage.setText("onConnectionFailed, e-> " + e.getMessage());
                    }
                });
            }

            @Override
            public void onConnectAuthFailed() {
                PlexLog.d("Callback onConnectAuthFailed");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvMessage.setText("onConnectAuthFailed...");
                    }
                });
            }
        });

        PlexManager.getInstance().start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PlexManager.getInstance().stop();
    }

    private void bindListen() {
        bConnectStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvMessage.setText("当前连接状态：" + PlexManager.getInstance().connected());
            }
        });

        bReconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlexManager.getInstance().reconnect();
            }
        });

        bReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 调用服务器接口，模拟服务器推送，本地客户端接收消息
                HttpRequestTask.sendRequest();
            }
        });

        bDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlexManager.getInstance().stop();
            }
        });
    }
}