package com.dadatu.remotephone;

import android.content.Context;
import android.content.SharedPreferences;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

public final class AppConfig {
    private AppConfig() {}

    public static final String PREFS = "remote_phone_control";
    public static final String PREF_ACCESS_CODE = "access_code";
    public static final int PORT = 19876;
    public static final String SCREENSHOT_PATH = "/data/local/tmp/rpc_screen.png";
    public static final String SCREENSHOT_JPEG_PATH = "/data/local/tmp/rpc_screen.jpg";

    public static String getOrCreateAccessCode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String existing = prefs.getString(PREF_ACCESS_CODE, "");
        if (existing != null && !existing.isEmpty()) return existing;
        SecureRandom random = new SecureRandom();
        String code = String.format(Locale.US, "%06d", random.nextInt(1_000_000));
        prefs.edit().putString(PREF_ACCESS_CODE, code).apply();
        return code;
    }

    public static boolean isCodeValid(Context context, String code) {
        return getOrCreateAccessCode(context).equals(code == null ? "" : code.trim());
    }

    public static List<String> getRemoteUrls() {
        List<String> urls = new ArrayList<>();
        urls.add("http://127.0.0.1:" + PORT + "/api/status");
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces == null) return urls;
            for (NetworkInterface networkInterface : Collections.list(interfaces)) {
                try {
                    if (!networkInterface.isUp() || networkInterface.isLoopback()) continue;
                } catch (Exception ignored) {
                    continue;
                }
                for (InetAddress address : Collections.list(networkInterface.getInetAddresses())) {
                    if (address.isLoopbackAddress() || address.isLinkLocalAddress()) continue;
                    if (address instanceof Inet4Address) {
                        urls.add("http://" + address.getHostAddress() + ":" + PORT + "/api/status");
                    } else if (address instanceof Inet6Address) {
                        String host = address.getHostAddress();
                        int percent = host.indexOf('%');
                        if (percent >= 0) host = host.substring(0, percent);
                        urls.add("http://[" + host + "]:" + PORT + "/api/status");
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return urls;
    }
}
