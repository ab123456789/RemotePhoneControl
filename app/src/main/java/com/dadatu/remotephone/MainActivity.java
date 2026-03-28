package com.dadatu.remotephone;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStartAgent = findViewById(R.id.btnStartAgent);
        Button btnOpenController = findViewById(R.id.btnOpenController);
        TextView textOutput = findViewById(R.id.textOutput);

        btnStartAgent.setOnClickListener(v -> {
            Intent intent = new Intent(this, RemoteAgentService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            textOutput.setText("被控端服务已启动（骨架阶段）。后续这里会显示 IPv4 / IPv6 地址、访问码、画质档位和服务状态。\n\n当前目标：一个软件内同时实现被控端与控制端。");
        });

        btnOpenController.setOnClickListener(v -> textOutput.setText("控制端 UI 骨架下一步接入：设备连接、远程画面、手势操作、画质切换。"));
    }
}
