package com.dadatu.remotephone;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class RemoteAgentService extends Service {
    private static final String CHANNEL_ID = "remote_agent";
    private static final int NOTIFICATION_ID = 2001;
    private AgentHttpServer server;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification());
        ensureServerStarted();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ensureServerStarted();
        androidx.core.app.NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, buildNotification());
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private synchronized void ensureServerStarted() {
        if (server != null) return;
        server = new AgentHttpServer(this);
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Notification buildNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            ? PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            : PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, flags);
        String code = AppConfig.getOrCreateAccessCode(this);
        java.util.List<String> urls = AppConfig.getRemoteUrls();
        String firstUrl = urls.isEmpty() ? ("127.0.0.1:" + AppConfig.PORT) : urls.get(0).replace("/api/status", "");
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentTitle("RemotePhoneControl · 访问码 " + code)
            .setContentText("被控端运行中 · " + firstUrl)
            .setStyle(new NotificationCompat.BigTextStyle().bigText("访问码：" + code + "\n端口：" + AppConfig.PORT + "\n地址：" + firstUrl))
            .setContentIntent(pi)
            .setOngoing(true)
            .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationChannel channel = new NotificationChannel(
            CHANNEL_ID,
            "Remote Agent",
            NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) nm.createNotificationChannel(channel);
    }
}
