package com.swxctx.plex;

import android.content.Context;

/**
 * @Author swxctx
 * @Date 2024-05-20
 * @Describe:
 */
public class PlexUtil {
    public static boolean isNetworkAvailable(Context context) {
        // 实现检查网络状态的代码
        return true;
    }
    public static String getServerIp(String address){
        String[] serverData = splitAddress(address);
        if (serverData.length < 1) {
            return "";
        }
        return serverData[0];
    }

    public static int getServerPort(String address){
        String[] serverData = splitAddress(address);
        if (serverData.length < 2) {
            return 0;
        }
        return Integer.parseInt(serverData[1]);
    }

    private static String[] splitAddress(String address){
        return address.split(":");
    }
}
