package com.swxctx.plex;

import android.util.Log;

/**
 * @Author hu_yang
 * @Date 2024-05-20
 * @Describe:
 */
public class PlexLog {
    private static String TAG = "PLEX";

    public static void d(String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, msg);
        }
    }

    public static void w(String msg) {
        Log.d(TAG, msg);
    }

    public static void e(String msg) {
        Log.e(TAG, msg);
    }
}
