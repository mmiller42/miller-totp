package com.millertotp;

import android.util.Log;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import javax.annotation.Nonnull;

public class SyncReactContextModule extends ReactContextBaseJavaModule {
    private static final String MODULE_NAME = "SyncReactContextModule";

    public SyncReactContextModule(ReactApplicationContext reactContext) {
        super(reactContext);
        Log.d("mmmSyncReactContextModule", "constructor: setting context singleton");
        ReactContextHelper.setContext(reactContext);
    }

    @Override
    @Nonnull
    public String getName() {
        return MODULE_NAME;
    }

    @ReactMethod
    public void emitKeychainDataChange() {

    }
}
