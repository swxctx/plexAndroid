package com.swxctx.plex;

/**
 * @Author swxctx
 * @Date 2024-05-20
 * @Describe:
 */
public class PlexConfig {
    private static PlexConfig instance;
    private String serverIp;
    private int serverPort;
    private ReconnectStrategy reconnectStrategy;

    private PlexConfig() {
        // 默认值
        this.serverIp = "default_ip";
        this.serverPort = 1234;
        this.reconnectStrategy = ReconnectStrategy.ALWAYS;
    }

    public static PlexConfig getInstance() {
        if (instance == null) {
            instance = new PlexConfig();
        }
        return instance;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String ip) {
        this.serverIp = ip;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int port) {
        this.serverPort = port;
    }

    public ReconnectStrategy getReconnectStrategy() {
        return reconnectStrategy;
    }

    public void setReconnectStrategy(ReconnectStrategy strategy) {
        this.reconnectStrategy = strategy;
    }

    public enum ReconnectStrategy {
        ALWAYS,
        NEVER,
        ON_FAILURE
    }
}