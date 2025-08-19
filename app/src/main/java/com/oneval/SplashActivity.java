package com.oneval;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SplashActivity extends AppCompatActivity {
  private Intent intent;
  private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
    splashScreen.setKeepOnScreenCondition(() -> true);

    if (Build.VERSION.SDK_INT >= 23) {
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
          != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(
            this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
      } else {
        gotoNext();
      }
    } else {
      gotoNext();
    }
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (requestCode == 100) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        gotoNext();
      } else {
        Toast.makeText(getApplicationContext(), getString(R.string.no_location), Toast.LENGTH_SHORT)
            .show();
        finish();
      }
    }
  }

  private void gotoNext() {
    scheduler.schedule(() -> {
      intent = new Intent(this, Signin.class);
      startActivity(intent);
      finish();

      runOnUiThread(()
                        -> {
                            // code here (empty)
                        });
    }, 3, TimeUnit.SECONDS);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (scheduler != null) {
      scheduler.shutdown();
    }
  }
}
