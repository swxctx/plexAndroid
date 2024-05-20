package com.swxctx.plexdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.swxctx.plex.PlexConfig;
import com.swxctx.plex.PlexManager;

public class MainActivity extends AppCompatActivity {
    private TextView tvMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvMessage = findViewById(R.id.tv_message);

        initData();
    }

    private void initData(){
        PlexConfig.getInstance().setServerIp("117.50.198.225");
        PlexConfig.getInstance().setServerPort(9578);
        PlexConfig.getInstance().setAuthData("1");

        PlexManager.init(this);
        PlexManager.getInstance().startService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PlexManager.getInstance().stopService();
    }
}