package com.millertotp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BroadcastBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            Log.w("mmmBroadcastBootReceiver", "onReceive: received null " + (intent == null ? "intent" : "intent action"));
            return;
        }

        Log.d("mmmBroadcastBootReceiver", "onReceive: received event: " + intent.getAction());

        switch (intent.getAction()) {
            case Intent.ACTION_BOOT_COMPLETED:
            case Intent.ACTION_USER_PRESENT:
                Intent serviceIntent = new Intent(context, ForegroundService.class);
                context.startForegroundService(serviceIntent);
                break;
        }
    }
}
