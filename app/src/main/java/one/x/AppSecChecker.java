package one.x;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.text.HtmlCompat;
import com.scottyab.rootbeer.RootBeer;

public class AppSecChecker extends Service {
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    new Thread(() -> {
      RootBeer root = new RootBeer(getApplicationContext());
      boolean isRooted = root.isRooted();

      if (isRooted) {
        // Afficher le Toast sur le thread principal
        new Handler(Looper.getMainLooper()).post(() -> {
          Toast.makeText(getApplicationContext(), getString(R.string.not_secure), Toast.LENGTH_LONG)
              .show();
        });

        // Attendre un peu pour laisser le Toast s'afficher
        try {
          Thread.sleep(3000); // 3 secondes
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        if (!ONEX.BASE_URL.equals("http://127.0.0.1:5555")) {
          // Réinitialiser l'application
          ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.clearApplicationUserData(); // tue le processus
          }
        }
      } else {
        if (!isDeviceSecure()) {
          new Handler(Looper.getMainLooper()).post(() -> { showSecuritySettingsDialog(); });
        }
      }

      // Arrêter le service proprement
      new Handler(Looper.getMainLooper()).post(() -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
          stopForeground(Service.STOP_FOREGROUND_REMOVE);
        } else {
          stopForeground(true);
        }
        stopSelf();
      });
    }).start();

    startForeground(1, getSilentNotification());
    return START_NOT_STICKY;
  }

  private Notification getSilentNotification() {
    NotificationCompat.Builder builder =
        new NotificationCompat.Builder(this, "security_check_channel")
            .setContentTitle(getString(R.string.securi_check))
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel channel = new NotificationChannel(
          "security_check_channel", "Security Check", NotificationManager.IMPORTANCE_LOW);
      NotificationManager manager = getSystemService(NotificationManager.class);
      manager.createNotificationChannel(channel);
    }

    return builder.build();
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  public boolean isDeviceSecure() {
    KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      // API 23+ : détecte si un verrouillage sécurisé est activé
      return keyguardManager.isDeviceSecure();
    } else {
      // API 21–22 : fallback moins précis
      return keyguardManager.isKeyguardSecure();
    }
  }

  public void showSecuritySettingsDialog() {
    new AlertDialog.Builder(getApplicationContext())
        .setTitle(getString(R.string.set_device_security_title))
        .setMessage(getString(R.string.set_device_security))
        .setPositiveButton(HtmlCompat.fromHtml("<font color='yellow'>"
                                   + "Ok"
                                   + "</font>",
                               HtmlCompat.FROM_HTML_MODE_LEGACY),
            (dialog, which) -> {
              Intent securityIntent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
              startActivity(securityIntent);
            })
        .setNeutralButton(
            HtmlCompat.fromHtml("<font color='red'>" + getString(R.string.cancel) + "</font>",
                HtmlCompat.FROM_HTML_MODE_LEGACY),
            (dialog, which) -> { dialog.dismiss(); })
        .show();
  }
}