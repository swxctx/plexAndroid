package com.swxctx.plexdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.swxctx.plex.BackManager;

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
        String[] address = {"117.50.198.225:9578"};
        BackManager.init(this, address);
        BackManager.getInstance().connectToServer();
    }
}