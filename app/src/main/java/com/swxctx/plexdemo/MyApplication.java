package com.swxctx.plexdemo;

import android.app.Application;

import com.swxctx.plex.PlexManager;

/**
 * @Author hu_yang
 * @Date 2024-05-21
 * @Describe:
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // 注册生命周期回调
        AppLifecycleHandler appLifecycleHandler = new AppLifecycleHandler(new AppLifecycleHandler.AppLifecycleListener() {
            @Override
            public void onAppBackgrounded() {
                // 应用进入后台
                PlexManager.getInstance().onAppBackgrounded();
            }
            @Override
            public void onAppForegrounded() {
                // 应用进入前台
                PlexManager.getInstance().onAppForegrounded();
            }
        });
        registerActivityLifecycleCallbacks(appLifecycleHandler);
    }
}
