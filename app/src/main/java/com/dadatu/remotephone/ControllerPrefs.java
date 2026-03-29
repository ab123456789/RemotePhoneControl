package com.dadatu.remotephone;

import android.content.Context;
import android.content.SharedPreferences;

public final class ControllerPrefs {
    private ControllerPrefs() {}

    private static final String PREFS = "controller_prefs";
    private static final String KEY_BASE_URL = "base_url";
    private static final String KEY_ACCESS_CODE = "access_code_input";
    private static final String KEY_WIDTH = "screen_width";
    private static final String KEY_REFRESH_MS = "refresh_ms";

    public static void save(Context context, String baseUrl, String accessCode, int width, int refreshMs) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit()
            .putString(KEY_BASE_URL, baseUrl == null ? "" : baseUrl.trim())
            .putString(KEY_ACCESS_CODE, accessCode == null ? "" : accessCode.trim())
            .putInt(KEY_WIDTH, width)
            .putInt(KEY_REFRESH_MS, refreshMs)
            .apply();
    }

    public static String getBaseUrl(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_BASE_URL, "");
    }

    public static String getAccessCode(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_ACCESS_CODE, "");
    }

    public static int getWidth(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_WIDTH, 720);
    }

    public static int getRefreshMs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_REFRESH_MS, 2000);
    }
}
