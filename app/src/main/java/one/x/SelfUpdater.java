package one.x;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.core.content.pm.PackageInfoCompat;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SelfUpdater {
    private final Activity activity;
    private final File apkFile;
    private int currentVersion;
    private static final String PACKAGE_INSTALLED_ACTION = "one.x.SESSION_API_PACKAGE_INSTALLED";

    public SelfUpdater(Activity activity, File apkFile, int currentVersion) {
        this.activity = activity;
        this.apkFile = apkFile;
        this.currentVersion = currentVersion;
    }

    public void updateIfNeeded() {
        try {
            removeIfOldApp(activity, apkFile, currentVersion);
            if (apkFile.exists()) {
                PackageInstaller.Session session = null;
                try {
                    PackageInstaller packageInstaller = activity.getPackageManager().getPackageInstaller();
                    PackageInstaller.SessionParams params =
                        new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
                    int sessionId = packageInstaller.createSession(params);
                    session = packageInstaller.openSession(sessionId);
                    installApk(apkFile, session);
                    Context context = activity;
                    Intent intent = new Intent(context, Signin.class);
                    intent.putExtra("loc", ONEX.COUNTRY);
                    intent.putExtra("curr", ONEX.CURRENCY);
                    intent.putExtra("lang", ONEX.CURRENT_LANG);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setAction(PACKAGE_INSTALLED_ACTION);
                    int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        flags |= PendingIntent.FLAG_MUTABLE;
                    }
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, flags);
                    IntentSender statusReceiver = pendingIntent.getIntentSender();
                    session.commit(statusReceiver);
                } catch (IOException e) {
                    throw new RuntimeException("Couldn't install package", e);
                } catch (RuntimeException e) {
                    if (session != null) {
                        session.abandon();
                    }
                    throw e;
                }
            }
        } catch (Exception e) {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void installApk(File apkFile, PackageInstaller.Session ps) {
        Utils.restoreText(activity, activity.getString(R.string.installing), "#1CFFB5");
        try {
            addApkToInstallSession("OneX.apk", ps);
        } catch (Exception e) {
            Utils.restoreText(activity, activity.getString(R.string.slog), "#FDFDFD");
            Toast.makeText(activity, activity.getString(R.string.error_file), Toast.LENGTH_SHORT).show();
        }
    }

    public void addApkToInstallSession(String fileName, PackageInstaller.Session session)
    throws IOException {
        File apkFile = new File(activity.getExternalFilesDir(null), fileName);

        if (!apkFile.exists()) {
            throw new FileNotFoundException("Fichier APK introuvable : " + apkFile.getAbsolutePath());
        }

        try (InputStream is = new FileInputStream(apkFile);
                    OutputStream packageInSession = session.openWrite("one.x", 0, apkFile.length())) {
            byte[] buffer = new byte[16384];
            int n;
            while ((n = is.read(buffer)) >= 0) {
                packageInSession.write(buffer, 0, n);
            }

            session.fsync(packageInSession);
        }
    }

    public static boolean removeIfOldApp(Activity activity, File file, int versionCode) {
        try {
            PackageManager pm = activity.getPackageManager();
            PackageInfo newApkInfo;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                newApkInfo =
                    pm.getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.PackageInfoFlags.of(0));
            } else {
                newApkInfo = pm.getPackageArchiveInfo(file.getAbsolutePath(), 0);
            }

            if (newApkInfo == null) {
                Toast.makeText(activity, activity.getString(R.string.check_vers_failed), Toast.LENGTH_SHORT)
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