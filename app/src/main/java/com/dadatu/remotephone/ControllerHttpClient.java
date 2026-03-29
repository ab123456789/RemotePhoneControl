package com.dadatu.remotephone;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class ControllerHttpClient {
    private ControllerHttpClient() {}

    public static JSONObject getJson(String url, String accessCode) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(4000);
        conn.setReadTimeout(12000);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        if (accessCode != null && !accessCode.isEmpty()) {
            conn.setRequestProperty("X-Access-Code", accessCode);
        }
        return new JSONObject(read(conn));
    }

    public static JSONObject postJson(String url, String accessCode, JSONObject body) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(4000);
        conn.setReadTimeout(20000);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("Accept", "application/json");
        if (accessCode != null && !accessCode.isEmpty()) {
            conn.setRequestProperty("X-Access-Code", accessCode);
        }
        conn.setDoOutput(true);
        byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
        conn.setFixedLengthStreamingMode(payload.length);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload);
        }
        return new JSONObject(read(conn));
    }

    private static String read(HttpURLConnection conn) throws Exception {
        int code = conn.getResponseCode();
        InputStream stream = code >= 200 && code < 400 ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }
}
