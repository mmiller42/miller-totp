package com.millertotp;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import javax.annotation.Nullable;

public class ForegroundService extends Service {
    private static final String CHANNEL_ID = "totp_foreground_service";
    private final int NOTIFICATION_ID;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public ForegroundService() {
        super();
        NOTIFICATION_ID = (int) System.currentTimeMillis();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("mmmForegroundService", "onBind: " + intent.toUri(0));
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("mmmForegroundService", "onCreate: creating notification (channel_id = " + CHANNEL_ID + ", name = Foreground Service)");
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Foreground Service", NotificationManager.IMPORTANCE_HIGH);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    @Override
    public int onStartCommand(@Nullable Intent  intent, int flags, int startId) {
        Log.d("mmmForegroundService", "onStartCommand: creating notification (channel_id = " + CHANNEL_ID + ", notification_id = " + NOTIFICATION_ID + ")");
        updateWidget();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Miller TOTP")
                .setContentText("Foreground service is running")
                .build();

        startForeground(NOTIFICATION_ID, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("mmmForegroundService", "onDestroy");
    }

    private void updateWidget() {
        if (!isLocked()) {
            int[] widgetIds = AppWidgetManager.getInstance(getApplication())
                    .getAppWidgetIds(new ComponentName(getApplication(), WidgetUpdateReceiver.class));

            if (widgetIds.length == 0) {
                Log.d("mmmForegroundService", "updateWidget: No widgets to update");
            } else {
                Log.d("mmmForegroundService", "updateWidget: Sending intent to update widgets");
                Intent intent = new Intent(this, WidgetUpdateReceiver.class)
                        .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
                sendBroadcast(intent);
            }
        }

        handler.postDelayed(this::updateWidget, delayTime());
    }

    private long delayTime() {
        long delay = 1000 - (System.currentTimeMillis() % 1000);
        Log.d("mmmForegroundService", "delayTime: " + delay + "ms");
        return delay;
    }

    private boolean isLocked() {
        KeyguardManager keyguard = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
        boolean locked = keyguard.isKeyguardLocked();

        if (locked) {
            Log.d("mmmForegroundService", "isLocked: device is locked");
        }

        return locked;
    }
}
