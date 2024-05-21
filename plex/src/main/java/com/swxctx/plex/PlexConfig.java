package com.swxctx.plex;

/**
 * @Author swxctx
 * @Date 2024-05-20
 * @Describe:
 */
public class PlexConfig {
    private static PlexConfig instance;
    // fetch ip plex server address
    private String serverAddress;
    // tcp ip
    private String serverIp;
    // tcp port
    private int serverPort;
    // plex server auth data
    private String authData;
    // heartbeat interval(ms)
    private int heartbeatInterval;
    // tcp connect timeout(ms)
    private int connectTimeout;
    // reconnect interval(ms) for tcp disconnect
    private int reconnectInterval;

    private ReconnectStrategy reconnectStrategy;

    private PlexConfig() {
        // default
        this.serverIp = "";
        this.serverPort = 0;
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

    public void clearServerData() {
        if (this.serverAddress.length() == 0) {
            return;
        }
        this.serverIp = "";
        this.serverPort = 0;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
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
