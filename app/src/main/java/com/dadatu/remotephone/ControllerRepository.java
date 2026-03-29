package com.dadatu.remotephone;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import org.json.JSONObject;

public final class ControllerRepository {
    private ControllerRepository() {}

    public static JSONObject fetchStatus(String baseUrl) throws Exception {
        return ControllerHttpClient.getJson(normalize(baseUrl) + "/api/status", null);
    }

    public static Bitmap fetchScreenshot(String baseUrl, String accessCode, int quality, int maxWidth) throws Exception {
        JSONObject body = new JSONObject();
        body.put("quality", quality);
        body.put("maxWidth", maxWidth);
        JSONObject res = ControllerHttpClient.postJson(normalize(baseUrl) + "/api/screenshot", accessCode, body);
        String b64 = res.optString("imageBase64", "");
        byte[] bytes = Base64.decode(b64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static JSONObject tap(String baseUrl, String accessCode, int x, int y) throws Exception {
        JSONObject body = new JSONObject();
        body.put("x", x);
        body.put("y", y);
        return ControllerHttpClient.postJson(normalize(baseUrl) + "/api/tap", accessCode, body);
    }

    public static JSONObject key(String baseUrl, String accessCode, String key) throws Exception {
        JSONObject body = new JSONObject();
        body.put("key", key);
        return ControllerHttpClient.postJson(normalize(baseUrl) + "/api/key", accessCode, body);
    }

    public static JSONObject inputText(String baseUrl, String accessCode, String text) throws Exception {
        JSONObject body = new JSONObject();
        body.put("text", text);
        return ControllerHttpClient.postJson(normalize(baseUrl) + "/api/text", accessCode, body);
    }

    private static String normalize(String baseUrl) {
        String s = baseUrl == null ? "" : baseUrl.trim();
        while (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        return s;
    }
}
