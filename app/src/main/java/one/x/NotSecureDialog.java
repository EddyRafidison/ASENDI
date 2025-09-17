package one.x;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.text.HtmlCompat;

public class NotSecureDialog extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(getString(R.string.set_device_security_title));
        builder.setMessage(getString(R.string.set_device_security));
        builder.setPositiveButton(
            HtmlCompat.fromHtml("<font color='yellow'>" + getString(R.string.settings) + "</font>",
                                HtmlCompat.FROM_HTML_MODE_LEGACY),
        (dialog, which) -> {
            dialog.dismiss();
            finish();
            Intent securityIntent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
            startActivity(securityIntent);
        });
        builder.setNeutralButton(
            HtmlCompat.fromHtml("<font color='red'>" + getString(R.string.cancel) + "</font>",
                                HtmlCompat.FROM_HTML_MODE_LEGACY),
        (dialog, which) -> {
            dialog.dismiss();
            finish();
        });
        AlertDialog nsdialog = builder.create();
        nsdialog.show();
    }
}
