package com.dadatu.remotephone;

import org.json.JSONObject;

public final class RemoteStatusFormatter {
    private RemoteStatusFormatter() {}

    public static String format(JSONObject status) {
        if (status == null) return "无远程状态";
        JSONObject display = status.optJSONObject("display");
        String displayText = display == null ? "未知" : (display.optInt("width", 0) + " x " + display.optInt("height", 0));
        return "远程设备状态\n\n"
            + "Root：" + (status.optBoolean("rootOk") ? "正常" : "异常") + "\n"
            + "屏幕：" + displayText + "\n"
            + "端口：" + status.optInt("port", 0) + "\n"
            + "时间：" + status.optLong("serverTime", 0L) + "\n\n"
            + "连接地址：\n" + status.optJSONArray("remoteUrls");
    }
}
