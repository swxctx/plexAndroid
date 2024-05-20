package com.swxctx.plex;

import android.content.Context;

/**
 * @Author hu_yang
 * @Date 2024-05-20
 * @Describe:
 */
public class PlexManager {
    private static PlexManager instance;
    private Context context;
    private String serverIp;
    private int serverPort;
    private PlexTCPService tcpService;

    private PlexManager(Context context) {
        this.context = context;
    }

    public static void init(Context context, String[] address) {
        if (instance == null) {
            instance = new PlexManager(context);
            instance.configureServerAddress(address);
        }
    }

    public static PlexManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Manager is not initialized, call init first.");
        }
        return instance;
    }

    private void configureServerAddress(String[] address) {
        // Assume address is in the form of "ip:port"
        this.serverIp = address[0].split(":")[0];
        this.serverPort = Integer.parseInt(address[0].split(":")[1]);
    }

    public void connectToServer() {
        if (tcpService == null) {
            tcpService = new PlexTCPService(); // Assuming TCPService handles its own thread management
        }
        tcpService.connect(serverIp, serverPort);
    }

    public void disconnectFromServer() {
        if (tcpService != null) {
            tcpService.disconnect();
        }
    }

    public void sendMessage(String message) throws IOException {
        if (tcpService != null) {
            tcpService.sendMessage(message);
        }
    }
}
