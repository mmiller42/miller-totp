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
    private static final String ALIAS = "";
    private static long lastDecryptedAt = 0;

    private static KeychainData lastDecryptedData;
    private static TotpGenerator generator;
    private static TotpResult lastResult;
    private static final HashMap<Integer, Float> widgetFontSizes = new HashMap<Integer, Float>();


    public WidgetUpdateReceiver() {
        super();
        Log.d("mmmWidgetUpdateReceiver", "constructor");
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] widgetIds) {
        try {
            reloadCredentials();
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

    private void reloadCredentials() {
        final long now = System.currentTimeMillis();

        if (now - lastDecryptedAt <= 30000) {
            Log.d("mmmWidgetUpdateReceiver", "decryptCredentials: Last decrypted less than 30 seconds ago");
            return;
        }

        final ReactApplicationContext reactContext = ReactContextHelper.getContext();

        if (reactContext == null) {
            Log.d("mmmWidgetUpdateReceiver", "decryptCredentials: React Context is null");
            return;
        }

        try {
            KeychainData decryptedData = new KeychainData(reactContext, ALIAS);

            if (lastDecryptedData != null && decryptedData.equals(lastDecryptedData)) {
                Log.d("mmmWidgetUpdateReceiver", "decryptCredentials: Credentials have not changed");
            } else {
                Log.d("mmmWidgetUpdateReceiver", "decryptCredentials: Credentials have changed");
                lastDecryptedData = decryptedData;
                generator = new TotpGenerator(decryptedData.getSecret(), decryptedData.getPeriod(), decryptedData.getDigits());
                Log.d("mmmWidgetUpdateReceiver", "set generator");
                lastResult = null;
            }
        } catch (Exception e) {
            Log.e("mmmWidgetUpdateReceiver", "decryptCredentials: error");
            e.printStackTrace();
        } finally {
            lastDecryptedAt = now;
        }
    }

    private void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        String code = lastResult == null ? "--- ---" : lastResult.getCode();
        String secondsLeft = lastResult == null ? "  " : Long.toString(lastResult.getSecondsLeft());
        Log.d("mmmWidgetUpdateReceiver", "updateWidget: " + widgetId);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.totp_widget);
        views.setTextViewText(R.id.timeText, secondsLeft);
        views.setTextViewText(R.id.codeText, code);

        Float fontSize = widgetFontSizes.get(widgetId);

        if (fontSize != null) {
            views.setFloat(R.id.timeText, "setTextSize", fontSize);
            views.setFloat(R.id.codeText, "setTextSize", fontSize);
        }


        Intent launchIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_MUTABLE);
        views.setOnClickPendingIntent(R.id.widget, pendingIntent);

        manager.updateAppWidget(widgetId, views);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        int height = newOptions.getInt("appWidgetMaxHeight");
        int width = newOptions.getInt("appWidgetMaxWidth");
        Log.d("mmmWidgetUpdateReceiver", "onAppWidgetOptionsChanged - width = " + width +", height = " + height);

        final float maxVFontSize = (float) (0.9 * height - 2);
        final float maxHFontSize = (float) (0.1 * width);
        final float fontSize = Math.min(maxVFontSize, maxHFontSize);
        Log.d("mmmWidgetUpdateReceiver", "onAppWidgetOptionsChanged - fontSize = " + fontSize);

        widgetFontSizes.put(appWidgetId, fontSize);
    }
}
