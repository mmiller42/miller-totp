package com.millertotp;

import com.facebook.react.bridge.NoSuchKeyException;
import com.facebook.react.bridge.ReadableMap;

public class WidgetSettings {
    public final String secret;
    public final int digits;
    public final int period;
    public final float fontSize;

    public WidgetSettings(final String secret, final int digits, final int period, final float fontSize) {
        this.secret = secret;
        this.digits = digits;
        this.period = period;
        this.fontSize = fontSize;
    }

    public WidgetSettings(final ReadableMap map) throws NoSuchKeyException {
        secret = map.getString("secret");
        period = map.getInt("period");
        digits = map.getInt("digits");
        fontSize = (float) map.getDouble("fontSize");
    }
}
