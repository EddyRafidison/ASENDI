package one.x;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SplashActivity extends AppCompatActivity {
  private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private final ActivityResultLauncher<String> notifPermissionLauncher =
      registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
          gotoNext();
        } else {
          Toast
              .makeText(
                  getApplicationContext(), getString(R.string.no_location), Toast.LENGTH_SHORT)
              .show();
          finish();
        }
      });
  private final ActivityResultLauncher<String> locPermissionLauncher =
      registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
              notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
          } else {
            gotoNext();
          }
        } else {
          Toast
              .makeText(
                  getApplicationContext(), getString(R.string.no_location), Toast.LENGTH_SHORT)
              .show();
          finish();
        }
      });

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
    splashScreen.setKeepOnScreenCondition(() -> true);

    if (Build.VERSION.SDK_INT >= 23) {
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
          != PackageManager.PERMISSION_GRANTED) {
        locPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
      } else {
        gotoNext();
      }
    } else {
      gotoNext();
    }
  }

  private void gotoNext() {
  final String loc = Utils.getCountryCode(getApplicationContext());
    if (loc.isEmpty()) {
      Toast.makeText(getApplicationContext(), getString(R.string.no_location), Toast.LENGTH_SHORT)
          .show();
      finish();
    }
    scheduler.schedule(() -> {
      // Lancer l'activité Signin
      Intent signinIntent = new Intent(SplashActivity.this, Signin.class);
      signinIntent.putExtra("loc", loc);
      startActivity(signinIntent);
      finish();

      // Démarrer le service AppSecChecker
      Intent serviceIntent = new Intent(SplashActivity.this, AppSecChecker.class);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(serviceIntent);
      } else {
        startService(serviceIntent);
      }
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
