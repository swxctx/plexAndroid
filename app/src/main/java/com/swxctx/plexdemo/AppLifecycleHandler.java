package com.swxctx.plexdemo;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * @Author swxctx
 * @Date 2024-05-21
 * @Describe:
 */
public class AppLifecycleHandler implements Application.ActivityLifecycleCallbacks {
    private int numStarted = 0;
    private AppLifecycleListener listener;

    public AppLifecycleHandler(AppLifecycleListener listener) {
        this.listener = listener;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (numStarted == 0) {
            // 应用进入前台
            listener.onAppForegrounded();
        }
        numStarted++;
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
        numStarted--;
        if (numStarted == 0) {
            // 应用进入后台
            listener.onAppBackgrounded();
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    public interface AppLifecycleListener {
        void onAppBackgrounded();

        void onAppForegrounded();
    }
}
