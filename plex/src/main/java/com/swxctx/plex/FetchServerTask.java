package com.swxctx.plex;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @Author swxctx
 * @Date 2024-05-21
 * @Describe:
 */
public class FetchServerTask {
    public interface FetchServerCallback {
        void onFetchSuccess();

        void onFetchFailure(Exception e);
    }

    private final FetchServerCallback callback;
    private final ExecutorService executorService;

    public FetchServerTask(FetchServerCallback callback) {
        this.callback = callback;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void execute() {
        if (PlexConfig.getInstance().getServerIp().length() > 0 && PlexConfig.getInstance().getServerPort() > 0) {
            callback.onFetchSuccess();
            return;
        }
        /**
         * METHOD: GET
         * response
         * {
         *     "host": "117.50.198.225:9578"
         * }
         */
        Future<String> future = executorService.submit(new FetchTask(PlexConfig.getInstance().getServerAddress()));
        executorService.submit(() -> {
            try {
                String result = future.get();
                if (result != null) {
                    JSONObject jsonObject = new JSONObject(result);
                    String host = jsonObject.getString("host");

                    String serverIp = PlexUtil.getServerIp(host);
                    int serverPort = PlexUtil.getServerPort(host);
                    if (serverIp.length() == 0 || serverPort <= 0) {
                        callback.onFetchFailure(new Exception("Invalid host format"));
                        return;
                    }

                    PlexConfig.getInstance().setServerIp(serverIp);
                    PlexConfig.getInstance().setServerPort(serverPort);
                    callback.onFetchSuccess();
                } else {
                    callback.onFetchFailure(new Exception("Failed to fetch server info"));
                }
            } catch (Exception e) {
                PlexLog.e("Error fetching server info-> " + e.getMessage());
                callback.onFetchFailure(e);
            }
        });
    }

    private static class FetchTask implements Callable<String> {
        private final String url;

        FetchTask(String url) {
            this.url = url;
        }

        @Override
        public String call() throws Exception {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(this.url);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(10000);
                urlConnection.setReadTimeout(15000);
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    return response.toString();
                } else {
                    return null;
                }
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }
    }
}
