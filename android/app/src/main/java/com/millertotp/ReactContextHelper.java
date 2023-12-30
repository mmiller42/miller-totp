package com.millertotp;

import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;

import javax.annotation.Nullable;

public class ReactContextHelper {
    private static ReactApplicationContext mContext;

    public static void setContext(ReactApplicationContext context) {
        Log.d("mmmReactContextHelper", "Set ReactApplicationContext");
        mContext = context;
    }

    @Nullable
    public static ReactApplicationContext getContext() {
        return mContext;
    }
}
