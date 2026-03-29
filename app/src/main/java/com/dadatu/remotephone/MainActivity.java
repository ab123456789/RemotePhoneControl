package com.dadatu.remotephone;

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
    private EditText editBaseUrl;
    private EditText editAccessCode;
    private EditText editRemoteText;
    private ImageView imageScreen;
    private SeekBar seekQuality;
    private boolean autoRefresh;
    private Bitmap lastBitmap;
    private final Runnable autoRefreshTask = new Runnable() {
        @Override
        public void run() {
            if (!autoRefresh) return;
            fetchScreen();
            handler.postDelayed(this, 2000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStartAgent = findViewById(R.id.btnStartAgent);
        Button btnFetchStatus = findViewById(R.id.btnFetchStatus);
        Button btnFetchScreen = findViewById(R.id.btnFetchScreen);
        Button btnKeyHome = findViewById(R.id.btnKeyHome);
        Button btnKeyBack = findViewById(R.id.btnKeyBack);
        Button btnKeyRecent = findViewById(R.id.btnKeyRecent);
        Button btnSendText = findViewById(R.id.btnSendText);
        Button btnAutoRefresh = findViewById(R.id.btnAutoRefresh);
        Button btnSwipeUp = findViewById(R.id.btnSwipeUp);

        textOutput = findViewById(R.id.textOutput);
        textQuality = findViewById(R.id.textQuality);
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
        });

        btnFetchStatus.setOnClickListener(v -> runTask(() -> {
            JSONObject status = ControllerRepository.fetchStatus(editBaseUrl.getText().toString());
            return status.toString(2);
        }));
        btnFetchScreen.setOnClickListener(v -> fetchScreen());
        btnKeyHome.setOnClickListener(v -> runTask(() -> ControllerRepository.key(baseUrl(), code(), "home").toString(2)));
        btnKeyBack.setOnClickListener(v -> runTask(() -> ControllerRepository.key(baseUrl(), code(), "back").toString(2)));
        btnKeyRecent.setOnClickListener(v -> runTask(() -> ControllerRepository.key(baseUrl(), code(), "recent").toString(2)));
        btnSendText.setOnClickListener(v -> runTask(() -> ControllerRepository.inputText(baseUrl(), code(), editRemoteText.getText().toString()).toString(2)));
        btnSwipeUp.setOnClickListener(v -> runTask(() -> ControllerRepository.swipe(baseUrl(), code(), 540, 1800, 540, 600, 220).toString(2)));
        btnAutoRefresh.setOnClickListener(v -> {
            autoRefresh = !autoRefresh;
            btnAutoRefresh.setText(autoRefresh ? "停止自动刷新" : "开启自动刷新");
            handler.removeCallbacks(autoRefreshTask);
            if (autoRefresh) handler.post(autoRefreshTask);
        });

        imageScreen.setOnTouchListener((v, event) -> {
            if (event.getAction() != MotionEvent.ACTION_UP) return true;
            int viewW = Math.max(v.getWidth(), 1);
            int viewH = Math.max(v.getHeight(), 1);
            int remoteW = 1080;
            int remoteH = 2400;
            if (lastBitmap != null && lastBitmap.getWidth() > 0 && lastBitmap.getHeight() > 0) {
                remoteW = lastBitmap.getWidth();
                remoteH = lastBitmap.getHeight();
            }
            int x = Math.max(0, Math.min(remoteW - 1, Math.round((event.getX() / viewW) * remoteW)));
            int y = Math.max(0, Math.min(remoteH - 1, Math.round((event.getY() / viewH) * remoteH)));
            runTask(() -> ControllerRepository.tap(baseUrl(), code(), x, y).toString(2));
            return true;
        });

        refreshLocalStatus();
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
        textOutput.setText("处理中...");
        executor.execute(() -> {
            try {
                String result = task.run();
                runOnUiThread(() -> textOutput.setText(result));
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
                    textOutput.setText("远程画面已刷新");
                });
            } catch (Exception e) {
                runOnUiThread(() -> textOutput.setText("ERROR: " + e));
            }
        });
    }

    private void fetchScreen() {
        runTaskWithBitmap(() -> ControllerRepository.fetchScreenshot(baseUrl(), code(), seekQuality.getProgress() + 25, 720));
    }

    private void refreshLocalStatus() {
        textOutput.setText(AgentRepository.buildStatusText(this));
        if (editBaseUrl.getText() == null || editBaseUrl.getText().toString().trim().isEmpty()) {
            editBaseUrl.setText("http://127.0.0.1:" + AppConfig.PORT);
        }
        if (editAccessCode.getText() == null || editAccessCode.getText().toString().trim().isEmpty()) {
            editAccessCode.setText(AppConfig.getOrCreateAccessCode(this));
        }
    }

    private String baseUrl() {
        return editBaseUrl.getText().toString();
    }

    private String code() {
        return editAccessCode.getText().toString();
    }
}
