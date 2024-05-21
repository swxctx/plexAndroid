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

    private void initData() {
        PlexConfig.getInstance().setServerIp("117.50.198.225");
        PlexConfig.getInstance().setServerPort(9578);
        PlexConfig.getInstance().setAuthData("1");
        PlexConfig.getInstance().setHeartbeatInterval(10000);

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