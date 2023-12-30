package com.millertotp;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;

import com.facebook.react.bridge.ReactApplicationContext;
import com.oblador.keychain.PrefsStorage;
import com.oblador.keychain.SecurityLevel;
import com.oblador.keychain.cipherStorage.CipherStorage;
import com.oblador.keychain.cipherStorage.CipherStorageKeystoreAesCbc;
import com.oblador.keychain.cipherStorage.CipherStorageKeystoreRsaEcb;
import com.oblador.keychain.decryptionHandler.DecryptionResultHandler;
import com.oblador.keychain.decryptionHandler.DecryptionResultHandlerProvider;
import com.oblador.keychain.exceptions.CryptoFailedException;

import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;

public class KeychainData {
    private static final CipherStorage[] ciphers = { new CipherStorageKeystoreAesCbc(), new CipherStorageKeystoreRsaEcb() };

    private final String secret;
    private final int digits;
    private final int period;

    public KeychainData(final ReactApplicationContext reactContext, final String alias) throws KeychainDataNotFoundException, CryptoFailedException, JSONException {
        final CipherStorage.DecryptionResult data = decryptKeychainData(reactContext, alias);
        JSONObject json = new JSONObject(data.password);

        secret = json.getString("secret");
        digits = json.getInt("digits");
        period = json.getInt("period");

        Log.d("mmmKeychainData", "constructor: decrypted data for alias " + alias);
    }

    public String getSecret() {
        return secret;
    }

    public int getDigits() {
        return digits;
    }

    public int getPeriod() {
        return period;
    }

    @NonNull
    public String toString() {
        return "KeychainData{ secret: \"" + secret + "\", digits: " + digits + ", period: " + period + " }";
    }

    public boolean equals(KeychainData data) {
        return data.getDigits() == digits && data.getPeriod() == period && data.getSecret().equals(secret);
    }

    private CipherStorage.DecryptionResult decryptKeychainData(final ReactApplicationContext reactContext, final String alias) throws KeychainDataNotFoundException, CryptoFailedException {
        final PrefsStorage.ResultSet resultSet = getResultSet(reactContext, alias);

        if (resultSet == null) {
            throw new KeychainDataNotFoundException("Keychain data not found");
        }

        final CipherStorage cipher = getCipherStorage(resultSet);
        final BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Authenticate")
                .setNegativeButtonText("Cancel")
                .setConfirmationRequired(false)
                .build();

        final DecryptionResultHandler handler = DecryptionResultHandlerProvider.getHandler(reactContext, cipher, promptInfo);
        cipher.decrypt(handler, alias, resultSet.username, resultSet.password, SecurityLevel.ANY);
        CryptoFailedException.reThrowOnError(handler.getError());

        final CipherStorage.DecryptionResult result = handler.getResult();

        if (result == null) {
            throw new CryptoFailedException("handler.getResult() returned null");
        }

        return result;
    }

    private PrefsStorage.ResultSet getResultSet(final ReactApplicationContext reactContext, final String alias) {
        final PrefsStorage storage = new PrefsStorage(reactContext);
        return storage.getEncryptedEntry(alias);
    }

    private CipherStorage getCipherStorage(PrefsStorage.ResultSet resultSet) throws CryptoFailedException {
        for (CipherStorage cipher : ciphers) {
            if (cipher.getCipherStorageName().equals(resultSet.cipherStorageName)) {
                return cipher;
            }
        }

        throw new CryptoFailedException("Unable to resolve storage name: " + resultSet.cipherStorageName);
    }
}

