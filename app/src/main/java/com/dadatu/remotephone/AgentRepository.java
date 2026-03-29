package com.dadatu.remotephone;

import android.content.Context;

import org.json.JSONObject;

public final class AgentRepository {
    private AgentRepository() {}

    public static String buildStatusText(Context context) {
        try {
            JSONObject status = new AgentControlManager(context).status();
            JSONObject display = status.optJSONObject("display");
            String displayText = display == null ? "未知" : (display.optInt("width", 0) + " x " + display.optInt("height", 0));
            return "被控端服务状态\n\n"
                + "访问码：" + status.optString("accessCode") + "\n\n"
                + "Root：" + (status.optBoolean("rootOk") ? "正常" : "异常") + "\n\n"
                + "屏幕：" + displayText + "\n\n"
                + "连接地址：\n"
                + joinUrls(status.optJSONArray("remoteUrls")) + "\n\n"
                + "id 输出：\n" + status.optString("idOutput");
        } catch (Exception e) {
            return "读取状态失败：" + e;
        }
    }

    private static String joinUrls(org.json.JSONArray arr) {
        if (arr == null || arr.length() == 0) return "无";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length(); i++) {
            if (i > 0) sb.append('\n');
            sb.append(arr.optString(i));
        }
        return sb.toString();
    }
}
