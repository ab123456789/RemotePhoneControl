package com.dadatu.remotephone;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView textOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStartAgent = findViewById(R.id.btnStartAgent);
        Button btnOpenController = findViewById(R.id.btnOpenController);
        textOutput = findViewById(R.id.textOutput);

        btnStartAgent.setOnClickListener(v -> {
            Intent intent = new Intent(this, RemoteAgentService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            refreshStatus();
        });

        btnOpenController.setOnClickListener(v -> textOutput.setText("控制端 UI 下一步接：设备连接、远程画面、点击/滑动/按键/文本输入、画质调节。当前已先落被控端 API。"));
        refreshStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
    }

    private void refreshStatus() {
        textOutput.setText(AgentRepository.buildStatusText(this));
    }
}
