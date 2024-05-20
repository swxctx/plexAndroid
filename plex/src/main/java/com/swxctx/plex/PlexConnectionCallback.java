package com.swxctx.plex;

/**
 * @Author swxctx
 * @Date 2024-05-20
 * @Describe:
 */
public interface PlexConnectionCallback {
    void onConnectionSuccess();

    void onConnectionFailed(Exception e);
}
