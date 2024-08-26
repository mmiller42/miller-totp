package com.millertotp;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt.PromptInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.oblador.keychain.KeychainModule;
import com.oblador.keychain.KeychainModuleBuilder;
import com.oblador.keychain.KeychainPackage;
import com.oblador.keychain.PrefsStorage;
import com.oblador.keychain.SecurityLevel;
import com.oblador.keychain.cipherStorage.CipherStorage;
import com.oblador.keychain.cipherStorage.CipherStorageKeystoreAesCbc;
import com.oblador.keychain.cipherStorage.CipherStorageKeystoreRsaEcb;
import com.oblador.keychain.decryptionHandler.DecryptionResultHandler;
import com.oblador.keychain.decryptionHandler.DecryptionResultHandlerProvider;
import com.oblador.keychain.exceptions.CryptoFailedException;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class WidgetUpdateReceiver extends AppWidgetProvider {
    private static TotpResult lastResult;
    private static final HashMap<Integer, Float> widgetFontSizes = new HashMap<>();


    public WidgetUpdateReceiver() {
        super();
        Log.d("mmmWidgetUpdateReceiver", "constructor");
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] widgetIds) {
        try {
            reloadResult();

            for (int widgetId : widgetIds) {
                updateWidget(context, manager, widgetId);
            }
        } catch (Exception e) {
            Log.e("mmmWidgetUpdateReceiver", "onUpdate error: " + e.toString());
            e.printStackTrace();
        }
    }

    private void reloadResult() {
        TotpGenerator generator = ForegroundService.getGenerator();

        if (generator == null) {
            Log.d("mmmWidgetUpdateReceiver", "getResult: no credentials loaded yet");
            return;
        }

        long current = generator.getPeriod();

        if (lastResult != null && lastResult.getPeriod() == current) {
            Log.d("mmmWidgetUpdateReceiver", "getResult: same period: " + current);
            return;
        }

        Log.d("mmmWidgetUpdateReceiver", "getResult: Generating new code for period: " + current);

        try {
            lastResult = new TotpResult(generator, generator.getPeriodMillis());
        } catch (Exception e) {
            Log.e("mmmWidgetUpdateReceiver", "getResult: exception " + e.toString());
            e.printStackTrace();
            lastResult = null;
        }
    }

    private void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        String code = lastResult == null ? "--- ---" : lastResult.getCode();
        String secondsLeft = lastResult == null ? "  " : Long.toString(lastResult.getSecondsLeft());

        Log.d("mmmWidgetUpdateReceiver", "updateWidget: " + widgetId);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.totp_widget);
        views.setTextViewText(R.id.timeText, secondsLeft);
        views.setTextViewText(R.id.codeText, code);

        float fontSize = ForegroundService.getFontSize();
        views.setFloat(R.id.timeText, "setTextSize", fontSize);
        views.setFloat(R.id.codeText, "setTextSize", fontSize);

        Intent launchIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_MUTABLE);
        views.setOnClickPendingIntent(R.id.widget, pendingIntent);

        manager.updateAppWidget(widgetId, views);
    }
}
