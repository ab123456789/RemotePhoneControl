package com.dadatu.remotephone;

import android.content.Context;

import com.topjohnwu.superuser.Shell;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class AgentControlManager {
    private final Context context;

    public AgentControlManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public JSONObject status() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("ok", true);
        obj.put("port", AppConfig.PORT);
        obj.put("accessCode", AppConfig.getOrCreateAccessCode(context));
        obj.put("remoteUrls", new JSONArray(AppConfig.getRemoteUrls()));
        Shell.Result idResult = RootShell.execSh("id");
        obj.put("rootOk", idResult.getCode() == 0 && RootShell.join(idResult.getOut()).contains("uid=0"));
        obj.put("idOutput", RootShell.join(idResult.getOut()));
        obj.put("serverTime", System.currentTimeMillis());
        return obj;
    }

    public JSONObject screenshot(int quality, int maxWidth) throws Exception {
        quality = clamp(quality, 25, 95);
        maxWidth = clamp(maxWidth, 360, 1440);
        String cmd = "sh -c " + RootShell.shellQuote(
            "rm -f " + AppConfig.SCREENSHOT_PATH + " " + AppConfig.SCREENSHOT_JPEG_PATH + " && " +
            "screencap -p " + AppConfig.SCREENSHOT_PATH + " && " +
            "if command -v magick >/dev/null 2>&1; then magick " + AppConfig.SCREENSHOT_PATH + " -resize " + maxWidth + "x -quality " + quality + " " + AppConfig.SCREENSHOT_JPEG_PATH + "; " +
            "elif command -v convert >/dev/null 2>&1; then convert " + AppConfig.SCREENSHOT_PATH + " -resize " + maxWidth + "x -quality " + quality + " " + AppConfig.SCREENSHOT_JPEG_PATH + "; " +
            "else cp " + AppConfig.SCREENSHOT_PATH + " " + AppConfig.SCREENSHOT_JPEG_PATH + "; fi && " +
            "base64 " + AppConfig.SCREENSHOT_JPEG_PATH
        );
        Shell.Result result = RootShell.execSh(cmd);
        JSONObject obj = new JSONObject();
        obj.put("ok", result.getCode() == 0);
        obj.put("quality", quality);
        obj.put("maxWidth", maxWidth);
        obj.put("stdout", RootShell.join(result.getOut()));
        obj.put("stderr", RootShell.join(result.getErr()));
        if (result.getCode() == 0) {
            obj.put("mimeType", "image/jpeg");
            obj.put("imageBase64", RootShell.join(result.getOut()).replace("\n", "").trim());
        }
        return obj;
    }

    public JSONObject tap(int x, int y) throws Exception {
        return runInput("input tap " + x + " " + y, "tap", x, y);
    }

    public JSONObject swipe(int x1, int y1, int x2, int y2, int durationMs) throws Exception {
        durationMs = clamp(durationMs, 50, 5000);
        return runInput("input swipe " + x1 + " " + y1 + " " + x2 + " " + y2 + " " + durationMs,
            "swipe", x1, y1, x2, y2, durationMs);
    }

    public JSONObject key(String keyName) throws Exception {
        int keyCode;
        switch ((keyName == null ? "" : keyName).toLowerCase()) {
            case "home": keyCode = 3; break;
            case "back": keyCode = 4; break;
            case "recent": keyCode = 187; break;
            case "power": keyCode = 26; break;
            case "volume_up": keyCode = 24; break;
            case "volume_down": keyCode = 25; break;
            case "enter": keyCode = 66; break;
            case "menu": keyCode = 82; break;
            case "up": keyCode = 19; break;
            case "down": keyCode = 20; break;
            case "left": keyCode = 21; break;
            case "right": keyCode = 22; break;
            default: keyCode = 0;
        }
        if (keyCode == 0) {
            JSONObject obj = new JSONObject();
            obj.put("ok", false);
            obj.put("error", "unsupported key");
            return obj;
        }
        return runInput("input keyevent " + keyCode, "key", keyName);
    }

    public JSONObject text(String text) throws Exception {
        if (text == null) text = "";
        String escaped = text.replace(" ", "%s");
        return runInput("input text " + RootShell.shellQuote(escaped), "text", text);
    }

    private JSONObject runInput(String command, String action, Object... args) throws Exception {
        Shell.Result result = RootShell.execSh(command);
        JSONObject obj = new JSONObject();
        obj.put("ok", result.getCode() == 0);
        obj.put("action", action);
        obj.put("command", command);
        JSONArray arr = new JSONArray();
        for (Object arg : args) arr.put(arg);
        obj.put("args", arr);
        obj.put("stdout", RootShell.join(result.getOut()));
        obj.put("stderr", RootShell.join(result.getErr()));
        return obj;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
