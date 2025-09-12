package one.x;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.pm.PackageInfoCompat;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SelfUpdater {
  private final Context context;
  private final File apkFile;
  private int currentVersion;

  public SelfUpdater(Context context, File apkFile, int currentVersion) {
    this.context = context;
    this.apkFile = apkFile;
    this.currentVersion = currentVersion;
  }

  public void updateIfNeeded() {
    try {
      removeIfOldApp(context, apkFile, currentVersion);
      if (apkFile.exists()) {
        installApk();
      }
    } catch (Exception e) {
      Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
    }
  }

  private void installApk() throws IOException {
    Utils.restoreText(context, context.getString(R.string.installing), "#0BCCD8");
    ParcelFileDescriptor apkDescriptor =
        ParcelFileDescriptor.open(apkFile, ParcelFileDescriptor.MODE_READ_ONLY);

    PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
    PackageInstaller.SessionParams params =
        new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
    int sessionId = packageInstaller.createSession(params);
    PackageInstaller.Session session = packageInstaller.openSession(sessionId);

    try (OutputStream out = session.openWrite("onex_update", 0, -1);
        InputStream in = new FileInputStream(apkDescriptor.getFileDescriptor())) {
      byte[] buffer = new byte[65536];
      int c;
      while ((c = in.read(buffer)) != -1) {
        out.write(buffer, 0, c);
      }
      session.fsync(out);
    }

    Intent intent = new Intent(context, UpdateReceiver.class);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, sessionId, intent,
        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    session.commit(pendingIntent.getIntentSender());
  }

  public static void restartApp(Context context) {
    Intent restartIntent =
        context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, restartIntent,
        PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

    AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, pendingIntent);

    System.exit(0);
  }

  public static boolean removeIfOldApp(Context context, File file, int versionCode) {
    try {
      PackageManager pm = context.getPackageManager();
      PackageInfo newApkInfo;

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        newApkInfo =
            pm.getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.PackageInfoFlags.of(0));
      } else {
        newApkInfo = pm.getPackageArchiveInfo(file.getAbsolutePath(), 0);
      }

      if (newApkInfo == null) {
        Toast.makeText(context, context.getString(R.string.check_vers_failed), Toast.LENGTH_SHORT)
            .show();
        return false;
      } else {
        int newVersion = (int) PackageInfoCompat.getLongVersionCode(newApkInfo);
        if (newVersion <= versionCode) {
          file.delete();
          return true;
        }
      }

    } catch (Exception ignored) {
    }
    return false;
  }
}