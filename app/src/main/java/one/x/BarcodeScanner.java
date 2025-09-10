package one.x;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.ViewfinderView;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class BarcodeScanner extends Activity implements DecoratedBarcodeView.TorchListener {
  private CaptureManager capture;
  private DecoratedBarcodeView barcodeScannerView;
  private ImageView switchFlashlight;
  private View flashLayout;
  private ViewfinderView viewfinderView;
  private boolean flashON = false;

  @SuppressLint({"ResourceType", "MissingInflatedId"})
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.scanner_activity);

    ActionBar ab = getActionBar();
    SpannableString spanString = new SpannableString("QR Scanner");
    Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/RobotoRegular.ttf");
    spanString.setSpan(new ItemTypefaceSpan("", typeface), 0, spanString.length(),
        Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    ab.setTitle(spanString);

    barcodeScannerView = findViewById(R.id.zxing_barcode_scanner);
    barcodeScannerView.setTorchListener(this);

    switchFlashlight = findViewById(R.id.switch_flashlight);
    flashLayout = findViewById(R.id.flashlayout);

    viewfinderView = findViewById(R.id.zxing_viewfinder_view);

    if (!hasFlash()) {
      flashLayout.setVisibility(View.GONE);
    }

    capture = new CaptureManager(this, barcodeScannerView);
    capture.initializeFromIntent(getIntent(), savedInstanceState);
    capture.setShowMissingCameraPermissionDialog(false);
    capture.decode();

    changeMaskColor(null);
    changeLaserVisibility(false);
  }

  @Override
  protected void attachBaseContext(Context newBase) {
    super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
  }

  @Override
  protected void onResume() {
    super.onResume();
    capture.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    capture.onPause();
  }

  @Override
  @Deprecated
  public void onBackPressed() {
    super.onBackPressed();
    finish();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    capture.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    capture.onSaveInstanceState(outState);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
  }

  private boolean hasFlash() {
    return getApplicationContext().getPackageManager().hasSystemFeature(
        PackageManager.FEATURE_CAMERA_FLASH);
  }

  public void changeMaskColor(View view) {
    viewfinderView.setMaskColor(Color.parseColor("#761D486F"));
  }

  public void changeLaserVisibility(boolean visible) {
    viewfinderView.setLaserVisibility(visible);
  }

  public void switchFlash(View v) {
    if (hasFlash()) {
      if (!flashON) {
        barcodeScannerView.setTorchOn();
      } else {
        barcodeScannerView.setTorchOff();
      }
    }
  }

  @Override
  public void onTorchOn() {
    flashON = true;
    switchFlashlight.setImageResource(R.drawable.flash_on);
  }

  @Override
  public void onTorchOff() {
    flashON = false;
    switchFlashlight.setImageResource(R.drawable.flash_off);
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    capture.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }
}
