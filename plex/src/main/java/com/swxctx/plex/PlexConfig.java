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
    private String authData;

    private int heartbeatInterval;

    private int connectTimeout;
    private int reconnectInterval;

    private ReconnectStrategy reconnectStrategy;

    private PlexConfig() {
        // 默认值
        this.serverIp = "default_ip";
        this.serverPort = 1234;
        this.authData = "default_auth";
        this.heartbeatInterval = 60000;
        this.connectTimeout = 5000;
        this.reconnectInterval = 5000;
        this.reconnectStrategy = ReconnectStrategy.ALWAYS;
    }

    public static PlexConfig getInstance() {
        if (instance == null) {
            synchronized (PlexConfig.class) {
                if (instance == null) {
                    instance = new PlexConfig();
                }
            }
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

    public String getAuthData() {
        return authData;
    }

    public void setAuthData(String authData) {
        this.authData = authData;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReconnectInterval() {
        return reconnectInterval;
    }

    public void setReconnectInterval(int reconnectInterval) {
        this.reconnectInterval = reconnectInterval;
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
