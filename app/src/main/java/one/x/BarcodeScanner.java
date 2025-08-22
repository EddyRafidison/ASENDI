package one.x;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.ViewfinderView;

/** Custom Scanner Activity extending from Activity to display a custom layout form scanner view. */
public class BarcodeScanner extends Activity implements DecoratedBarcodeView.TorchListener {
  private CaptureManager capture;
  private DecoratedBarcodeView barcodeScannerView;
  private ImageButton switchFlashlightButton;
  private ViewfinderView viewfinderView;
  private boolean flashON = false;

  @SuppressLint({"ResourceType", "MissingInflatedId"})
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.scanner_activity);

    barcodeScannerView = findViewById(R.id.zxing_barcode_scanner);
    barcodeScannerView.setTorchListener(this);

    switchFlashlightButton = findViewById(R.id.switch_flashlight);

    viewfinderView = findViewById(R.id.zxing_viewfinder_view);

    // if the device does not have flashlight in its camera,
    // then remove the switch flashlight button...
    if (!hasFlash()) {
      switchFlashlightButton.setVisibility(View.GONE);
    }

    capture = new CaptureManager(this, barcodeScannerView);
    capture.initializeFromIntent(getIntent(), savedInstanceState);
    capture.setShowMissingCameraPermissionDialog(false);
    capture.decode();

    changeMaskColor(null);
    changeLaserVisibility(false);
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
    // TODO: Implement this method
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

  /**
   * Check if the device's camera has a Flashlight.
   *
   * @return true if there is Flashlight, otherwise false.
   */
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
    switchFlashlightButton.setImageResource(R.drawable.flash_on);
  }

  @Override
  public void onTorchOff() {
    flashON = false;
    switchFlashlightButton.setImageResource(R.drawable.flash_off);
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    capture.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }
}
