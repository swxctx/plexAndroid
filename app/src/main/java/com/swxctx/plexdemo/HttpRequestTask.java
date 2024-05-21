package com.swxctx.plexdemo;

import android.os.AsyncTask;
import android.util.Log;

import com.swxctx.plex.PlexConfig;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * @Author swxctx
 * @Date 2024-05-21
 * @Describe:
 */

public class HttpRequestTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = "HttpRequestTask";
    private static final String REQUEST_URL = "https://plex.developer.icu/plexApi/v1/send";
    private static final String BODY = "test";
    private static final String URI = "/logic/test";

    @Override
    protected String doInBackground(Void... voids) {
        try {
            URL url = new URL(REQUEST_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            // Create JSON body
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("uid", PlexConfig.getInstance().getAuthData());
            jsonBody.put("body", BODY);
            jsonBody.put("uri", URI);

            // Write the JSON data to the output stream
            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(jsonBody.toString());
            writer.flush();
            writer.close();
            os.close();

            // Connect and get the response
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
                return response.toString();
            } else {
                return "Error: " + responseCode;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in making HTTP request", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result != null) {
            Log.d(TAG, "Response from server: " + result);
        } else {
            Log.e(TAG, "Failed to get response from server");
        }
    }

    public static void sendRequest() {
        new HttpRequestTask().execute();
    }
}
