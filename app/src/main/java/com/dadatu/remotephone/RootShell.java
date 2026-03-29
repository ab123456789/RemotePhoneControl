package com.dadatu.remotephone;

import com.topjohnwu.superuser.Shell;

import java.util.List;

public final class RootShell {
    private RootShell() {}

    public static Shell.Result execSh(String shellCommand) {
        return Shell.cmd("su -M -c " + shellQuote(shellCommand)).exec();
    }

    public static String join(List<String> lines) {
        if (lines == null || lines.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String line : lines) sb.append(line).append('\n');
        return sb.toString();
    }

    public static String shellQuote(String s) {
        if (s == null || s.isEmpty()) return "''";
        return "'" + s.replace("'", "'\"'\"'") + "'";
    }
}
