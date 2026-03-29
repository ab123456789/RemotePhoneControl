package com.dadatu.remotephone;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private TextView textOutput;
    private EditText editBaseUrl;
    private EditText editAccessCode;
    private EditText editRemoteText;
    private ImageView imageScreen;

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

        textOutput = findViewById(R.id.textOutput);
        editBaseUrl = findViewById(R.id.editBaseUrl);
        editAccessCode = findViewById(R.id.editAccessCode);
        editRemoteText = findViewById(R.id.editRemoteText);
        imageScreen = findViewById(R.id.imageScreen);

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

        btnFetchScreen.setOnClickListener(v -> runTaskWithBitmap(() ->
            ControllerRepository.fetchScreenshot(
                editBaseUrl.getText().toString(),
                editAccessCode.getText().toString(),
                60,
                720
            )
        ));

        btnKeyHome.setOnClickListener(v -> runTask(() ->
            ControllerRepository.key(editBaseUrl.getText().toString(), editAccessCode.getText().toString(), "home").toString(2)
        ));
        btnKeyBack.setOnClickListener(v -> runTask(() ->
            ControllerRepository.key(editBaseUrl.getText().toString(), editAccessCode.getText().toString(), "back").toString(2)
        ));
        btnKeyRecent.setOnClickListener(v -> runTask(() ->
            ControllerRepository.key(editBaseUrl.getText().toString(), editAccessCode.getText().toString(), "recent").toString(2)
        ));
        btnSendText.setOnClickListener(v -> runTask(() ->
            ControllerRepository.inputText(
                editBaseUrl.getText().toString(),
                editAccessCode.getText().toString(),
                editRemoteText.getText().toString()
            ).toString(2)
        ));

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
        executor.shutdownNow();
    }

    private interface TextTask {
        String run() throws Exception;
    }

    private interface BitmapTask {
        Bitmap run() throws Exception;
    }

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
                runOnUiThread(() -> {
                    imageScreen.setImageBitmap(bitmap);
                    textOutput.setText("远程画面已刷新");
                });
            } catch (Exception e) {
                runOnUiThread(() -> textOutput.setText("ERROR: " + e));
            }
        });
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
}
