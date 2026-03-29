package com.dadatu.remotephone;

import android.content.Context;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AgentHttpServer {
    private final Context context;
    private final AgentControlManager manager;
    private volatile boolean running;
    private ServerSocket serverSocket;
    private ExecutorService pool;
    private Thread acceptThread;

    public AgentHttpServer(Context context) {
        this.context = context.getApplicationContext();
        this.manager = new AgentControlManager(context);
    }

    public synchronized void start() throws Exception {
        if (running) return;
        running = true;
        pool = Executors.newCachedThreadPool();
        serverSocket = new ServerSocket(AppConfig.PORT, 50);
        serverSocket.setReuseAddress(true);
        acceptThread = new Thread(this::acceptLoop, "rpc-agent-http");
        acceptThread.start();
    }

    public synchronized void stop() {
        running = false;
        try { if (serverSocket != null) serverSocket.close(); } catch (Exception ignored) {}
        if (pool != null) pool.shutdownNow();
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                pool.execute(() -> handle(socket));
            } catch (Exception e) {
                if (!running) return;
            }
        }
    }

    private void handle(Socket socket) {
        try (Socket s = socket) {
            InputStream in = s.getInputStream();
            OutputStream out = s.getOutputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.isEmpty()) {
                writeJson(out, 400, error("bad_request"));
                return;
            }
            String[] parts = requestLine.split(" ");
            String method = parts[0].trim().toUpperCase(Locale.ROOT);
            String path = parts.length > 1 ? parts[1].trim() : "/";
            int contentLength = 0;
            String accessCode = "";
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                String lower = line.toLowerCase(Locale.ROOT);
                if (lower.startsWith("content-length:")) {
                    contentLength = Integer.parseInt(line.substring(line.indexOf(':') + 1).trim());
                } else if (lower.startsWith("x-access-code:")) {
                    accessCode = line.substring(line.indexOf(':') + 1).trim();
                }
            }

            if ("GET".equals(method) && "/api/status".equals(path)) {
                writeJson(out, 200, manager.status());
                return;
            }

            if (!AppConfig.isCodeValid(context, accessCode)) {
                writeJson(out, 401, error("invalid_access_code"));
                return;
            }

            String body = readBody(reader, contentLength);
            JSONObject req = new JSONObject(body.isEmpty() ? "{}" : body);

            if ("POST".equals(method) && "/api/screenshot".equals(path)) {
                writeJson(out, 200, manager.screenshot(req.optInt("quality", 60), req.optInt("maxWidth", 720)));
                return;
            }
            if ("POST".equals(method) && "/api/tap".equals(path)) {
                writeJson(out, 200, manager.tap(req.optInt("x", 0), req.optInt("y", 0)));
                return;
            }
            if ("POST".equals(method) && "/api/swipe".equals(path)) {
                writeJson(out, 200, manager.swipe(req.optInt("x1", 0), req.optInt("y1", 0), req.optInt("x2", 0), req.optInt("y2", 0), req.optInt("durationMs", 220)));
                return;
            }
            if ("POST".equals(method) && "/api/key".equals(path)) {
                writeJson(out, 200, manager.key(req.optString("key", "")));
                return;
            }
            if ("POST".equals(method) && "/api/text".equals(path)) {
                writeJson(out, 200, manager.text(req.optString("text", "")));
                return;
            }

            writeJson(out, 404, error("not_found"));
        } catch (Throwable t) {
            try {
                OutputStream out = socket.getOutputStream();
                JSONObject obj = new JSONObject();
                obj.put("ok", false);
                obj.put("error", t.toString());
                writeJson(out, 500, obj);
            } catch (Exception ignored) {}
        }
    }

    private String readBody(BufferedReader reader, int contentLength) throws Exception {
        char[] chars = new char[Math.max(contentLength, 0)];
        int read = 0;
        while (read < chars.length) {
            int n = reader.read(chars, read, chars.length - read);
            if (n < 0) break;
            read += n;
        }
        return new String(chars, 0, read);
    }

    private JSONObject error(String msg) throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("ok", false);
        obj.put("error", msg);
        return obj;
    }

    private void writeJson(OutputStream out, int code, JSONObject obj) throws Exception {
        byte[] body = obj.toString().getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream header = new ByteArrayOutputStream();
        header.write(("HTTP/1.1 " + code + " OK\r\n").getBytes(StandardCharsets.UTF_8));
        header.write("Content-Type: application/json; charset=utf-8\r\n".getBytes(StandardCharsets.UTF_8));
        header.write(("Content-Length: " + body.length + "\r\n").getBytes(StandardCharsets.UTF_8));
        header.write("Connection: close\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        out.write(header.toByteArray());
        out.write(body);
        out.flush();
    }
}
