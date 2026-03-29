package com.dadatu.remotephone;

import org.json.JSONObject;

public final class ControllerSession {
    private ControllerSession() {}

    private static volatile JSONObject lastStatus;

    public static void setLastStatus(JSONObject status) {
        lastStatus = status;
    }

    public static JSONObject getLastStatus() {
        return lastStatus;
    }
}
