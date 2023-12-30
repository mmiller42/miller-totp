package com.millertotp;

import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.os.Bundle;
import android.util.Log;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint;
import com.facebook.react.defaults.DefaultReactActivityDelegate;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class MainActivity extends ReactActivity {
  private final int REQUEST_CODE_PERMISSION = 1;
  private final String[] permissions = {Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.POST_NOTIFICATIONS};

  /**
   * Returns the name of the main component registered from JavaScript. This is used to schedule
   * rendering of the component.
   */
  @Override
  @Nonnull
  protected String getMainComponentName() {
    return "MillerTOTP";
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (!shouldRequestPermissions()) {
      Log.d("mmmMainActivity", "onCreate: starting foreground service");
      startForeground();
    } else {
      ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSION);
    }
  }

  private boolean shouldRequestPermissions() {
    for (String permission : permissions) {
      if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
        Log.d("mmmMainActivity", "shouldRequestPermission: " + permission);
        return true;
      }
    }

    return false;
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode != REQUEST_CODE_PERMISSION) {
      return;
    }

    int grantCount = 0;
    final int neededCount = permissions.length;

    for (int result : grantResults) {
      if (result != PackageManager.PERMISSION_GRANTED) {
        Log.w("mmmMainActivity", "onRequestPermissionsResult: FAIL - permission not granted");
        return;
      }

        grantCount++;
    }

    if (grantCount >= neededCount) {
      Log.d("mmmMainActivity", "onRequestPermissionsResult: OK - got " + grantCount + "/" + neededCount + " permissions");
      startForeground();
    } else {
      Log.w("mmmMainActivity", "onRequestPermissionsResult: FAIL - got " + grantCount + "/" + neededCount + " permissions");
    }

      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  private void startForeground() {
    Intent intent = new Intent(this, ForegroundService.class);
    Log.d("mmmMainActivity", "startForeground: Sending intent: " + intent.toUri(0));

    getApplicationContext().startService(intent);
  }


  /**
   * Returns the instance of the {@link ReactActivityDelegate}. Here we use a util class {@link
   * DefaultReactActivityDelegate} which allows you to easily enable Fabric and Concurrent React
   * (aka React 18) with two boolean flags.
   */
  @Override
  protected ReactActivityDelegate createReactActivityDelegate() {
    return new DefaultReactActivityDelegate(
        this,
        getMainComponentName(),
        // If you opted-in for the New Architecture, we enable the Fabric Renderer.
        DefaultNewArchitectureEntryPoint.getFabricEnabled());
  }
}
