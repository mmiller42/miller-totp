package com.millertotp;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class BindWidgetsModule extends ReactContextBaseJavaModule {
    private static final String MODULE_NAME = "BindWidgetsModule";

    public BindWidgetsModule(ReactApplicationContext context) {
        super(context);
    }

    @Override
    @Nonnull
    public String getName() {
        return MODULE_NAME;
    }

    @ReactMethod
    public void getWidgetIds(final Promise promise) {
        try {
            Log.d("mmmBindWidgetsModule", "getWidgetIds");
            final Context context = getReactApplicationContext().getApplicationContext();
            final int[] widgetIds = AppWidgetManager.getInstance(context)
                    .getAppWidgetIds(new ComponentName(context, WidgetUpdateReceiver.class));

            final WritableArray widgetIdsArray = Arguments.createArray();
            for (int widgetId : widgetIds) {
                widgetIdsArray.pushInt(widgetId);
            }

            promise.resolve(widgetIdsArray);
        } catch (Exception e) {
            Log.e("mmmBindWidgetsModule", "getWidgetIds error:" + e.getMessage());
            e.printStackTrace();

            promise.reject("UNKNOWN", e);
        }
    }

    @ReactMethod
    public void syncSettings(ReadableMap settings, Promise promise) {
        try {
            Log.d("mmmBindWidgetsModule", "syncSettings");
            ForegroundService.updateSettings(new WidgetSettings(settings));
        } catch (Exception e) {
            Log.e("mmmBindWidgetsModule", "syncSettings: error: " + e.toString());
            e.printStackTrace();
        }

        promise.resolve(null);
    }
}
