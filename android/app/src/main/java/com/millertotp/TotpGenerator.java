package com.millertotp;

import android.util.Log;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

public class TotpGenerator {
    private static final String BASE32_CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private final TimeBasedOneTimePasswordGenerator totp;
    private final SecretKey secret;
    private final long period;
    private final int length;

    public TotpGenerator(String secret, long period, int length) {
        Log.d("mmmTotpGenerator", "constructor: secret = " + secret + ", period = " + period + ", length = " + length);
        this.secret = createSecretKey(secret);
        this.period = period;
        this.length = length;
        totp = new TimeBasedOneTimePasswordGenerator(Duration.ofSeconds(period), length);
    }

    private byte[] decodeBase32(String string) {
        string = string.trim().toUpperCase();
        byte[] bytes = new byte[(int) (string.length() * 5 / 8)];
        int outIdx = 0;
        int buffer = 0;
        int bufferLen = 0;

        for (char c: string.toCharArray()) {
            int value = BASE32_CHARSET.indexOf(c);

            if (value == -1) {
                throw new IllegalArgumentException("Invalid character: " + c);
            }

            buffer = (buffer << 5) | value;
            bufferLen += 5;
            if (bufferLen >= 8) {
                bytes[outIdx++] = (byte) ((buffer >> (bufferLen - 8)) & 0xFF);
                bufferLen -= 8;
            }
        }

        return bytes;
    }

    private SecretKey createSecretKey(String secret) {
        return new SecretKeySpec(decodeBase32(secret), "AES");
    }

    public String generate() {
        return generate(System.currentTimeMillis());
    }

    public String generate(long time) {
        try {
            return totp.generateOneTimePasswordString(secret, Instant.ofEpochMilli(time));
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public long getPeriod() {
        return getPeriod(System.currentTimeMillis());
    }

    public long getPeriod(long time) {
        return ((time / 1000) / period) * period;
    }

    public long getPeriodMillis() {
        return getPeriod() * 1000;
    }

    public long getPeriodMillis(long time) {
        return getPeriod(time) * 1000;
    }

    public long getSecondsLeft() {
        return getSecondsLeft(System.currentTimeMillis());
    }

    public long getSecondsLeft(long time) {
        return period - ((time / 1000) - getPeriod(time));
    }

    public int getLength() {
        return length;
    }
}
