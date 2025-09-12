package one.x;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.widget.Toast;
import java.io.File;

public class UpdateReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
    String message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);

    if (status == PackageInstaller.STATUS_SUCCESS) {
      File f = new File(context.getExternalFilesDir(null), "OneX.apk");
      Utils.restoreText(context, context.getString(R.string.slog), "#FDFDFD");
      f.delete();
      SelfUpdater.restartApp(context);
    } else {
      Utils.restoreText(context, context.getString(R.string.slog), "#FDFDFD");
    }
  }
}
