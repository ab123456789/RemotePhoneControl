package com.dadatu.remotephone;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView textOutput;
    private TextView textQuality;
    private TextView textWidthPreset;
    private TextView textRefreshPreset;
    private EditText editBaseUrl;
    private EditText editAccessCode;
    private EditText editRemoteText;
    private ImageView imageScreen;
    private SeekBar seekQuality;
    private boolean autoRefresh;
    private Bitmap lastBitmap;
    private int touchStartX;
    private int touchStartY;
    private long touchStartAt;
    private final Runnable autoRefreshTask = new Runnable() {
        @Override
        public void run() {
            if (!autoRefresh) return;
            fetchScreen();
            handler.postDelayed(this, currentRefreshMs());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStartAgent = findViewById(R.id.btnStartAgent);
        Button btnStopAgent = findViewById(R.id.btnStopAgent);
        Button btnCopyLocalAddress = findViewById(R.id.btnCopyLocalAddress);
        Button btnCopyLocalCode = findViewById(R.id.btnCopyLocalCode);
        Button btnCopyConnectionInfo = findViewById(R.id.btnCopyConnectionInfo);
        Button btnShareConnectionInfo = findViewById(R.id.btnShareConnectionInfo);
        Button btnQuickConnect = findViewById(R.id.btnQuickConnect);
        Button btnQuickWatch = findViewById(R.id.btnQuickWatch);
        Button btnStopWatch = findViewById(R.id.btnStopWatch);
        Button btnFetchStatus = findViewById(R.id.btnFetchStatus);
        Button btnFetchScreen = findViewById(R.id.btnFetchScreen);
        Button btnUseLocalAgent = findViewById(R.id.btnUseLocalAgent);
        Button btnImportConnectionInfo = findViewById(R.id.btnImportConnectionInfo);
        Button btnKeyHome = findViewById(R.id.btnKeyHome);
        Button btnKeyBack = findViewById(R.id.btnKeyBack);
        Button btnKeyRecent = findViewById(R.id.btnKeyRecent);
        Button btnKeyMenu = findViewById(R.id.btnKeyMenu);
        Button btnKeyUp = findViewById(R.id.btnKeyUp);
        Button btnKeyDown = findViewById(R.id.btnKeyDown);
        Button btnKeyLeft = findViewById(R.id.btnKeyLeft);
        Button btnKeyRight = findViewById(R.id.btnKeyRight);
        Button btnKeyEnter = findViewById(R.id.btnKeyEnter);
        Button btnKeyPower = findViewById(R.id.btnKeyPower);
        Button btnKeyVolumeUp = findViewById(R.id.btnKeyVolumeUp);
        Button btnKeyVolumeDown = findViewById(R.id.btnKeyVolumeDown);
        Button btnSendText = findViewById(R.id.btnSendText);
        Button btnPasteClipboard = findViewById(R.id.btnPasteClipboard);
        Button btnClearText = findViewById(R.id.btnClearText);
        Button btnAutoRefresh = findViewById(R.id.btnAutoRefresh);
        Button btnSwipeUp = findViewById(R.id.btnSwipeUp);
        Button btnSwipeLeft = findViewById(R.id.btnSwipeLeft);
        Button btnSwipeDown = findViewById(R.id.btnSwipeDown);
        Button btnSwipeRight = findViewById(R.id.btnSwipeRight);
        Button btnWidthLow = findViewById(R.id.btnWidthLow);
        Button btnWidthMedium = findViewById(R.id.btnWidthMedium);
        Button btnWidthHigh = findViewById(R.id.btnWidthHigh);
        Button btnRefresh1s = findViewById(R.id.btnRefresh1s);
        Button btnRefresh2s = findViewById(R.id.btnRefresh2s);
        Button btnRefresh4s = findViewById(R.id.btnRefresh4s);

        textOutput = findViewById(R.id.textOutput);
        textQuality = findViewById(R.id.textQuality);
        textWidthPreset = findViewById(R.id.textWidthPreset);
        textRefreshPreset = findViewById(R.id.textRefreshPreset);
        editBaseUrl = findViewById(R.id.editBaseUrl);
        editAccessCode = findViewById(R.id.editAccessCode);
        editRemoteText = findViewById(R.id.editRemoteText);
        imageScreen = findViewById(R.id.imageScreen);
        seekQuality = findViewById(R.id.seekQuality);

        seekQuality.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { textQuality.setText(String.valueOf(progress + 25)); }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        textQuality.setText(String.valueOf(seekQuality.getProgress() + 25));

        btnStartAgent.setOnClickListener(v -> {
            Intent intent = new Intent(this, RemoteAgentService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            refreshLocalStatus();
            updateAutoRefreshButtonText((Button) findViewById(R.id.btnAutoRefresh));
        });
        btnStopAgent.setOnClickListener(v -> {
            stopService(new Intent(this, RemoteAgentService.class));
            textOutput.setText("已请求停止被控端服务");
            refreshLocalStatus();
        });
        btnCopyLocalAddress.setOnClickListener(v -> copyLocalAddress());
        btnCopyLocalCode.setOnClickListener(v -> copyLocalCode());
        btnCopyConnectionInfo.setOnClickListener(v -> copyConnectionInfo());
        btnShareConnectionInfo.setOnClickListener(v -> shareConnectionInfo());

        btnQuickConnect.setOnClickListener(v -> quickConnect());
        btnQuickWatch.setOnClickListener(v -> quickWatch());
        btnStopWatch.setOnClickListener(v -> stopWatching());
        btnFetchStatus.setOnClickListener(v -> runTask(() -> refreshRemoteStatusText()));
        btnFetchScreen.setOnClickListener(v -> fetchScreen());
        btnUseLocalAgent.setOnClickListener(v -> useLocalAgentAddress());
        btnImportConnectionInfo.setOnClickListener(v -> importConnectionInfoFromClipboard());
        btnKeyHome.setOnClickListener(v -> sendKey("home"));
        btnKeyBack.setOnClickListener(v -> sendKey("back"));
        btnKeyRecent.setOnClickListener(v -> sendKey("recent"));
        btnKeyMenu.setOnClickListener(v -> sendKey("menu"));
        btnKeyUp.setOnClickListener(v -> sendKey("up"));
        btnKeyDown.setOnClickListener(v -> sendKey("down"));
        btnKeyLeft.setOnClickListener(v -> sendKey("left"));
        btnKeyRight.setOnClickListener(v -> sendKey("right"));
        btnKeyEnter.setOnClickListener(v -> sendKey("enter"));
        btnKeyPower.setOnClickListener(v -> sendKey("power"));
        btnKeyVolumeUp.setOnClickListener(v -> sendKey("volume_up"));
        btnKeyVolumeDown.setOnClickListener(v -> sendKey("volume_down"));
        btnSendText.setOnClickListener(v -> runTask(() -> {
            saveControllerPrefs();
            return ControllerRepository.inputText(baseUrl(), code(), editRemoteText.getText().toString()).toString(2);
        }, true));
        btnPasteClipboard.setOnClickListener(v -> pasteClipboardToRemoteText());
        btnClearText.setOnClickListener(v -> editRemoteText.setText(""));
        btnSwipeUp.setOnClickListener(v -> sendSwipe(540, 1800, 540, 600, 220));
        btnSwipeDown.setOnClickListener(v -> sendSwipe(540, 700, 540, 1900, 220));
        btnSwipeLeft.setOnClickListener(v -> sendSwipe(900, 1200, 180, 1200, 220));
        btnSwipeRight.setOnClickListener(v -> sendSwipe(180, 1200, 900, 1200, 220));
        btnWidthLow.setOnClickListener(v -> setWidthPreset(480));
        btnWidthMedium.setOnClickListener(v -> setWidthPreset(720));
        btnWidthHigh.setOnClickListener(v -> setWidthPreset(1080));
        btnRefresh1s.setOnClickListener(v -> setRefreshPreset(1000));
        btnRefresh2s.setOnClickListener(v -> setRefreshPreset(2000));
        btnRefresh4s.setOnClickListener(v -> setRefreshPreset(4000));
        btnAutoRefresh.setOnClickListener(v -> {
            autoRefresh = !autoRefresh;
            updateAutoRefreshButtonText(btnAutoRefresh);
            handler.removeCallbacks(autoRefreshTask);
            if (autoRefresh) handler.post(autoRefreshTask);
        });

        imageScreen.setOnTouchListener((v, event) -> {
            int viewW = Math.max(v.getWidth(), 1);
            int viewH = Math.max(v.getHeight(), 1);
            int remoteW = 1080;
            int remoteH = 2400;
            JSONObject lastStatus = ControllerSession.getLastStatus();
            if (lastStatus != null) {
                JSONObject display = lastStatus.optJSONObject("display");
                if (display != null) {
                    remoteW = display.optInt("width", remoteW);
                    remoteH = display.optInt("height", remoteH);
                }
            }
            int x = Math.max(0, Math.min(remoteW - 1, Math.round((event.getX() / viewW) * remoteW)));
            int y = Math.max(0, Math.min(remoteH - 1, Math.round((event.getY() / viewH) * remoteH)));
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                touchStartX = x;
                touchStartY = y;
                touchStartAt = System.currentTimeMillis();
                return true;
            }
            if (event.getAction() != MotionEvent.ACTION_UP) return true;
            int dx = Math.abs(x - touchStartX);
            int dy = Math.abs(y - touchStartY);
            long heldMs = System.currentTimeMillis() - touchStartAt;
            if (dx < 24 && dy < 24) {
                if (heldMs >= 420) {
                    runTask(() -> {
                        saveControllerPrefs();
                        return ControllerRepository.swipe(baseUrl(), code(), x, y, x + 1, y + 1, 650).toString(2);
                    }, true);
                } else {
                    runTask(() -> {
                        saveControllerPrefs();
                        return ControllerRepository.tap(baseUrl(), code(), x, y).toString(2);
                    }, true);
                }
            } else {
                int endX = x;
                int endY = y;
                runTask(() -> {
                    saveControllerPrefs();
                    return ControllerRepository.swipe(baseUrl(), code(), touchStartX, touchStartY, endX, endY, 220).toString(2);
                }, true);
            }
            return true;
        });

        refreshLocalStatus();
        handleSharedConnectionIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleSharedConnectionIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLocalStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        autoRefresh = false;
        handler.removeCallbacks(autoRefreshTask);
        executor.shutdownNow();
    }

    private interface TextTask { String run() throws Exception; }
    private interface BitmapTask { Bitmap run() throws Exception; }

    private void runTask(TextTask task) {
        runTask(task, false);
    }

    private void runTask(TextTask task, boolean refreshAfter) {
        textOutput.setText("处理中...");
        executor.execute(() -> {
            try {
                String result = task.run();
                runOnUiThread(() -> {
                    JSONObject status = ControllerSession.getLastStatus();
                    if (status != null && !result.startsWith("远程设备状态")) {
                        textOutput.setText(RemoteStatusFormatter.format(status) + "\n\n最近操作结果：\n" + result);
                    } else {
                        textOutput.setText(result);
                    }
                    if (refreshAfter) scheduleSingleRefresh();
                });
            } catch (Exception e) {
                runOnUiThread(() -> textOutput.setText("ERROR: " + e));
            }
        });
    }

    private void runTaskWithBitmap(BitmapTask task) {
        textOutput.setText("正在拉取远程画面...");
        executor.execute(() -> {
            try {
                Bitmap bitmap = task.run();
                lastBitmap = bitmap;
                runOnUiThread(() -> {
                    imageScreen.setImageBitmap(bitmap);
                    JSONObject status = ControllerSession.getLastStatus();
                    if (status != null) {
                        textOutput.setText(RemoteStatusFormatter.format(status) + "\n\n画面：已刷新");
                    } else {
                        textOutput.setText("远程画面已刷新");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> textOutput.setText("ERROR: " + e));
            }
        });
    }

    private void fetchScreen() {
        saveControllerPrefs();
        runTaskWithBitmap(() -> {
            ensureRemoteStatusLoaded();
            return ControllerRepository.fetchScreenshot(baseUrl(), code(), seekQuality.getProgress() + 25, currentWidth());
        });
    }

    private void quickConnect() {
        quickConnect(false);
    }

    private void quickWatch() {
        quickConnect(true);
    }

    private void stopWatching() {
        autoRefresh = false;
        handler.removeCallbacks(autoRefreshTask);
        updateAutoRefreshButtonText((Button) findViewById(R.id.btnAutoRefresh));
        textOutput.setText("已停止盯屏");
    }

    private void quickConnect(boolean enableAutoRefreshAfter) {
        saveControllerPrefs();
        textOutput.setText(enableAutoRefreshAfter ? "正在连接远程设备并开启盯屏..." : "正在连接远程设备...");
        executor.execute(() -> {
            try {
                JSONObject status = ControllerRepository.fetchStatus(baseUrl());
                ControllerSession.setLastStatus(status);
                Bitmap bitmap = ControllerRepository.fetchScreenshot(baseUrl(), code(), seekQuality.getProgress() + 25, currentWidth());
                lastBitmap = bitmap;
                runOnUiThread(() -> {
                    imageScreen.setImageBitmap(bitmap);
                    if (enableAutoRefreshAfter) {
                        autoRefresh = true;
                        updateAutoRefreshButtonText((Button) findViewById(R.id.btnAutoRefresh));
                        handler.removeCallbacks(autoRefreshTask);
                        handler.postDelayed(autoRefreshTask, currentRefreshMs());
                        textOutput.setText(RemoteStatusFormatter.format(status) + "\n\n画面：已连接并进入自动刷新");
                    } else {
                        updateAutoRefreshButtonText((Button) findViewById(R.id.btnAutoRefresh));
                        textOutput.setText(RemoteStatusFormatter.format(status) + "\n\n画面：已连接并刷新");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> textOutput.setText("ERROR: " + e));
            }
        });
    }

    private void scheduleSingleRefresh() {
        handler.removeCallbacks(autoRefreshTask);
        if (autoRefresh) {
            handler.postDelayed(autoRefreshTask, Math.min(600, currentRefreshMs()));
        } else {
            handler.postDelayed(this::fetchScreen, 350);
        }
    }

    private void sendKey(String key) {
        runTask(() -> {
            saveControllerPrefs();
            return ControllerRepository.key(baseUrl(), code(), key).toString(2);
        }, true);
    }

    private String refreshRemoteStatusText() throws Exception {
        saveControllerPrefs();
        JSONObject status = ControllerRepository.fetchStatus(baseUrl());
        ControllerSession.setLastStatus(status);
        return RemoteStatusFormatter.format(status);
    }

    private void ensureRemoteStatusLoaded() throws Exception {
        if (ControllerSession.getLastStatus() != null) return;
        JSONObject status = ControllerRepository.fetchStatus(baseUrl());
        ControllerSession.setLastStatus(status);
    }

    private void sendSwipe(int x1, int y1, int x2, int y2, int durationMs) {
        runTask(() -> {
            saveControllerPrefs();
            return ControllerRepository.swipe(baseUrl(), code(), x1, y1, x2, y2, durationMs).toString(2);
        }, true);
    }

    private void refreshLocalStatus() {
        textOutput.setText(AgentRepository.buildStatusText(this));
        String savedBaseUrl = ControllerPrefs.getBaseUrl(this);
        String savedCode = ControllerPrefs.getAccessCode(this);
        if (editBaseUrl.getText() == null || editBaseUrl.getText().toString().trim().isEmpty()) {
            editBaseUrl.setText(savedBaseUrl.isEmpty() ? "http://127.0.0.1:" + AppConfig.PORT : savedBaseUrl);
        }
        if (editAccessCode.getText() == null || editAccessCode.getText().toString().trim().isEmpty()) {
            editAccessCode.setText(savedCode.isEmpty() ? AppConfig.getOrCreateAccessCode(this) : savedCode);
        }
        updateWidthPresetText();
        updateRefreshPresetText();
        updateAutoRefreshButtonText((Button) findViewById(R.id.btnAutoRefresh));
    }

    private void saveControllerPrefs() {
        ControllerPrefs.save(this, baseUrl(), code(), currentWidth(), currentRefreshMs());
    }

    private void setWidthPreset(int width) {
        ControllerPrefs.save(this, baseUrl(), code(), width, currentRefreshMs());
        updateWidthPresetText();
        textOutput.setText("截图宽度已切到：" + width);
    }

    private void setRefreshPreset(int refreshMs) {
        ControllerPrefs.save(this, baseUrl(), code(), currentWidth(), refreshMs);
        updateRefreshPresetText();
        textOutput.setText("自动刷新间隔已切到：" + (refreshMs / 1000) + "秒");
    }

    private int currentWidth() {
        return ControllerPrefs.getWidth(this);
    }

    private int currentRefreshMs() {
        return ControllerPrefs.getRefreshMs(this);
    }

    private void updateWidthPresetText() {
        textWidthPreset.setText("当前宽度：" + currentWidth());
    }

    private void updateRefreshPresetText() {
        textRefreshPreset.setText("自动刷新：" + (currentRefreshMs() / 1000) + "秒");
    }

    private void updateAutoRefreshButtonText(Button button) {
        if (button == null) return;
        button.setText(autoRefresh ? "停止自动刷新" : "开启自动刷新");
    }

    private void pasteClipboardToRemoteText() {
        ClipboardManager cm = getSystemService(ClipboardManager.class);
        if (cm == null || !cm.hasPrimaryClip()) {
            textOutput.setText("本机剪贴板为空");
            return;
        }
        ClipData clip = cm.getPrimaryClip();
        if (clip == null || clip.getItemCount() == 0) {
            textOutput.setText("本机剪贴板为空");
            return;
        }
        CharSequence text = clip.getItemAt(0).coerceToText(this);
        editRemoteText.setText(text == null ? "" : text.toString());
        textOutput.setText("已把本机剪贴板内容带入远程输入框");
    }

    private void useLocalAgentAddress() {
        String selected = pickLocalAgentBaseUrl();
        editBaseUrl.setText(selected);
        editAccessCode.setText(AppConfig.getOrCreateAccessCode(this));
        saveControllerPrefs();
        textOutput.setText("已带入本机被控端地址和访问码");
    }

    private void copyLocalAddress() {
        ClipboardManager cm = getSystemService(ClipboardManager.class);
        if (cm == null) return;
        String selected = pickLocalAgentBaseUrl();
        cm.setPrimaryClip(ClipData.newPlainText("remote_agent_address", selected));
        textOutput.setText("已复制本机被控端地址到剪贴板");
    }

    private void copyLocalCode() {
        ClipboardManager cm = getSystemService(ClipboardManager.class);
        if (cm == null) return;
        String code = AppConfig.getOrCreateAccessCode(this);
        cm.setPrimaryClip(ClipData.newPlainText("remote_agent_code", code));
        textOutput.setText("已复制访问码到剪贴板");
    }

    private void copyConnectionInfo() {
        ClipboardManager cm = getSystemService(ClipboardManager.class);
        if (cm == null) return;
        cm.setPrimaryClip(ClipData.newPlainText("remote_agent_connection_info", buildConnectionInfoText()));
        textOutput.setText("已复制完整连接信息到剪贴板");
    }

    private void shareConnectionInfo() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, buildConnectionInfoText());
        startActivity(Intent.createChooser(intent, "分享连接信息"));
        textOutput.setText("已打开系统分享面板");
    }

    private String buildConnectionInfoText() {
        String base = pickLocalAgentBaseUrl();
        String code = AppConfig.getOrCreateAccessCode(this);
        return "RemotePhoneControl 连接信息\n地址：" + base + "\n访问码：" + code;
    }

    private void importConnectionInfoFromClipboard() {
        ClipboardManager cm = getSystemService(ClipboardManager.class);
        if (cm == null || !cm.hasPrimaryClip()) {
            textOutput.setText("剪贴板里没有连接信息");
            return;
        }
        ClipData clip = cm.getPrimaryClip();
        if (clip == null || clip.getItemCount() == 0) {
            textOutput.setText("剪贴板里没有连接信息");
            return;
        }
        String raw = String.valueOf(clip.getItemAt(0).coerceToText(this));
        if (applyConnectionInfoText(raw, "已从剪贴板导入连接信息")) return;
        textOutput.setText("剪贴板内容里没识别到地址和访问码");
    }

    private void handleSharedConnectionIntent(Intent intent) {
        if (intent == null || !Intent.ACTION_SEND.equals(intent.getAction())) return;
        String shared = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (shared == null || shared.trim().isEmpty()) return;
        applyConnectionInfoText(shared, "已从系统分享导入连接信息");
    }

    private boolean applyConnectionInfoText(String raw, String successMessage) {
        String[] lines = raw.split("\\r?\\n");
        String base = null;
        String code = null;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("地址：")) {
                base = trimmed.substring(3).trim();
            } else if (trimmed.startsWith("访问码：")) {
                code = trimmed.substring(4).trim();
            }
        }
        if (base == null || base.isEmpty() || code == null || code.isEmpty()) {
            return false;
        }
        editBaseUrl.setText(base);
        editAccessCode.setText(code);
        saveControllerPrefs();
        textOutput.setText(successMessage);
        return true;
    }

    private String pickLocalAgentBaseUrl() {
        java.util.List<String> urls = AppConfig.getRemoteUrls();
        String selected = "http://127.0.0.1:" + AppConfig.PORT;
        for (String url : urls) {
            if (url.startsWith("http://") && !url.contains("127.0.0.1")) {
                selected = url.replace("/api/status", "");
                break;
            }
        }
        if (selected.endsWith("/api/status")) {
            selected = selected.substring(0, selected.length() - "/api/status".length());
        }
        return selected;
    }

    private String baseUrl() {
        return editBaseUrl.getText().toString();
    }

    private String code() {
        return editAccessCode.getText().toString();
    }
}
