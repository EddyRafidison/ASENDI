package one.x;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.elyeproj.loaderviewlibrary.LoaderTextView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import io.github.inflationx.calligraphy3.CalligraphyUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;

public class Utils {
  public static Bitmap qr(Activity activity, String text) {
    Bitmap logo = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_launcher_qr);
    final int size = 250;
    try {
      Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
      hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
      hints.put(EncodeHintType.MARGIN, 1);
      hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

      BitMatrix matrix =
          new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, size, size, hints);

      Bitmap qrBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
      Canvas canvas = new Canvas(qrBitmap);

      for (int x = 0; x < size; x++) {
        for (int y = 0; y < size; y++) {
          qrBitmap.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
        }
      }

      if (logo != null) {
        int overlaySize = size / 5;
        Bitmap scaledLogo = Bitmap.createScaledBitmap(logo, overlaySize, overlaySize, true);

        int left = (size - overlaySize) / 2;
        int top = (size - overlaySize) / 2;

        canvas.drawBitmap(scaledLogo, left, top, null);
      }

      return qrBitmap;

    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public static Bitmap resizeBitmap(Bitmap original) {
    return Bitmap.createScaledBitmap(original, 600, 600, true);
  }

  public static String getDestQRCode(Context ctx, Bitmap bitmap) {
    String qrString = "";
    int width = bitmap.getWidth();
    int height = bitmap.getHeight();
    int[] pixels = new int[width * height];
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

    RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
    BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
    try {
      Result result = new MultiFormatReader().decode(binaryBitmap);
      qrString = result.getText();
    } catch (Exception e) {
      Toast.makeText(ctx, ctx.getString(R.string.invalid_img), Toast.LENGTH_SHORT).show();
    }
    return qrString;
  }

  public static void saveToDownloads(Activity context, Bitmap bitmap, String fileName) {
    OutputStream out = null;
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName + ".png");
        values.put(MediaStore.Downloads.MIME_TYPE, "image/png");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        Uri uri =
            context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
          out = context.getContentResolver().openOutputStream(uri);
        } else {
          Toast.makeText(context, context.getString(R.string.error_saving_image), Toast.LENGTH_LONG)
              .show();
        }
      } else {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(dir, fileName + ".png");
        out = new FileOutputStream(file);
      }
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
      if (out != null) {
        out.close();
        Toast.makeText(context, context.getString(R.string.saved), Toast.LENGTH_LONG).show();
      }
    } catch (Exception e) {
      Toast.makeText(context, context.getString(R.string.error_saving_image), Toast.LENGTH_LONG)
          .show();
    }
  }

  public static void showNoConnectionAlert(Context ctx, View v) {
    Snackbar sb = Snackbar.make(v, ctx.getString(R.string.no_connection), Snackbar.LENGTH_LONG);
    sb.setAction("Ok", v1 -> sb.dismiss());
    sb.show();
  }

  public static boolean isConnectionAvailable(Context context) {
    if (Objects.equals(ONEX.BASE_URL, "http://127.0.0.1:5555")) { //  To allow test only
      ONEX.isNetworkConnected = true;
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      return ONEX.isNetworkConnected;
    } else {
      ConnectivityManager connectivityManager =
          (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
      if (connectivityManager != null) {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return ONEX.isNetworkConnected = activeNetwork != null && activeNetwork.isConnected();
      }
    }
    return ONEX.isNetworkConnected;
  }

  private static JSONObject post(String[] keys, String[] value) {
    JSONObject jo = new JSONObject();
    int kl = keys.length;
    try {
      for (int i = 0; i < kl; i++) {
        jo.put(keys[i], value[i]);
      }
    } catch (JSONException ignored) {
    }
    return jo;
  }

  public static void connectToServer(Activity ctx, String url, String[] keys, String[] values,
      final boolean showProgress, final ServerListener slistener) {
    try {
      progress prog = new progress();
      if (showProgress) {
        prog.show(ctx);
      }
      final JsonObjectRequest jsonObjectRequest =
          new JsonObjectRequest(Request.Method.POST, url, post(keys, values),
              response
              -> {
                if (showProgress) {
                  prog.dismiss();
                }
                slistener.OnDataLoaded(response);
              },
              error -> {
                if (showProgress) {
                  prog.dismiss();
                }
                try {
                  slistener.OnDataLoaded(null);
                } catch (Exception ignored) {
                }
              }) {
            @NonNull
            @Override
            public Map<String, String> getHeaders() {
              // Build the headers
              final Map<String, String> params = new HashMap<>();
              params.put("Content-Type", "application/json");
              return params;
            }
          };
      Volley.newRequestQueue(ctx).add(jsonObjectRequest);
    } catch (Exception ignored) {
    }
  }

  public static void requestTP(final Activity ctx, String item, final LoaderTextView lt) {
    try {
      RequestQueue queue = Volley.newRequestQueue(ctx);
      StringRequest sr = new StringRequest(Request.Method.GET, ONEX.INFO + "?r=" + item,
          response
          -> lt.setText(HtmlCompat.fromHtml(response, HtmlCompat.FROM_HTML_MODE_LEGACY),
              TextView.BufferType.SPANNABLE),
          error -> { lt.setText("( ? )"); });
      queue.add(sr);
    } catch (Exception ignored) {
    }
  }

  public static String imageToString(Bitmap bitmap) {
    // useful to upload image to server
    // converting image to base64 string
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
    byte[] imageBytes = baos.toByteArray();
    return Base64.encodeToString(imageBytes, Base64.DEFAULT);
  }

  public static void hideSoftKeyboard(Activity ctx) {
    if (ctx.getCurrentFocus() != null) {
      InputMethodManager inputMethodManager =
          (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
      inputMethodManager.hideSoftInputFromWindow(ctx.getCurrentFocus().getWindowToken(), 0);
    }
  }

  public static void showCommonNotif(Activity ctx, List<CharSequence> list) {
    if (list.isEmpty()) {
      list.add(
          ctx.getString(R.string.empty_list)); // here show default alert in list if list is empty
    }
    // Supports also full direct link clickable eg: http://www.google.com with or without using href
    // tag.
    ListPopupWindow listPopupWindow = new ListPopupWindow(ctx);
    listPopupWindow.setAdapter(
        new ArrayAdapter<>(ctx, R.layout.textview_item_two, R.id.textview_item, list));
    View v = ctx.findViewById(R.id.toolbar);
    listPopupWindow.setAnchorView(v);
    int w = v.getWidth();
    listPopupWindow.setWidth(w);
    listPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
    listPopupWindow.setBackgroundDrawable(
        new ColorDrawable(ContextCompat.getColor(ctx, R.color.primary)));
    listPopupWindow.setModal(true);
    listPopupWindow.setPromptPosition(ListPopupWindow.POSITION_PROMPT_BELOW);
    listPopupWindow.setOnItemClickListener(null);
    listPopupWindow.show();
  }

  private static String Decrypt(String key, String data) {
    String d;
    try {
      d = Objects
              .requireNonNull(
                  DataCrypto.getDefault(key, "wa20" + key.substring(1, 4) + "ve24", new byte[16]))
              .decryptOrNull(data);
    } catch (Exception e) {
      d = "";
    }
    return d;
  }

  private static String Encrypt(String key, String data) {
    String d;
    try {
      d = Objects
              .requireNonNull(
                  DataCrypto.getDefault(key, "wa20" + key.substring(1, 4) + "ve24", new byte[16]))
              .encryptOrNull(data);
    } catch (Exception e) {
      d = "";
    }
    return d;
  }

  public static void saveCredentials(Context ctx, String account, String pswd) {
    SharedPreferences sp = ctx.getSharedPreferences("datavalues", Context.MODE_PRIVATE);
    SharedPreferences.Editor se = sp.edit();
    se.putString("acc_name", account);
    se.putString("psd", Encrypt(pswd, pswd));
    se.apply();
  }

  private static String getEncPswd(Context ctx) {
    SharedPreferences sp = ctx.getSharedPreferences("datavalues", Context.MODE_PRIVATE);
    return sp.getString("psd", "");
  }

  public static String getAccount(Context ctx) {
    SharedPreferences sp = ctx.getSharedPreferences("datavalues", Context.MODE_PRIVATE);
    return sp.getString("acc_name", "A-A00Z");
  }

  public static void saveCategory(Context ctx, String cat) {
    SharedPreferences sp = ctx.getSharedPreferences("datavalues", Context.MODE_PRIVATE);
    SharedPreferences.Editor se = sp.edit();
    se.putString("cat", cat);
    se.apply();
  }

  public static String getCategory(Context ctx) {
    SharedPreferences sp = ctx.getSharedPreferences("datavalues", Context.MODE_PRIVATE);
    return sp.getString("cat", "---");
  }

  public static void saveTkn(Context ctx, String tkn) {
    SharedPreferences sp = ctx.getSharedPreferences("datavalues", Context.MODE_PRIVATE);
    SharedPreferences.Editor se = sp.edit();
    se.putString("tkn", tkn);
    se.apply();
  }

  public static void saveLastNotifs(Context ctx, String notif) {
    SharedPreferences sp = ctx.getSharedPreferences("datavalues", Context.MODE_PRIVATE);
    SharedPreferences.Editor se = sp.edit();
    List<String> lns = getLastNotifs(ctx);
    lns.add(notif);
    Gson gson = new Gson();
    String json = gson.toJson(lns);
    se.putString("lns", json);
    se.apply();
  }

  public static List<String> getLastNotifs(Context ctx) {
    SharedPreferences sp = ctx.getSharedPreferences("datavalues", Context.MODE_PRIVATE);
    Gson gson = new Gson();
    String jsonString = sp.getString("lns", null);
    Type type = new TypeToken<List<String>>() {}.getType();
    return gson.fromJson(jsonString, type);
  }

  public static void saveLastNotifTime(Context ctx, String notif) {
    SharedPreferences sp = ctx.getSharedPreferences("datavalues", Context.MODE_PRIVATE);
    SharedPreferences.Editor se = sp.edit();
    se.putString("lnd", notif);
    se.apply();
  }

  public static String getLastNotifTime(Context ctx) {
    SharedPreferences sp = ctx.getSharedPreferences("datavalues", Context.MODE_PRIVATE);
    return sp.getString("lnd", "000000");
  }

  public static String getTkn(Context ctx) {
    SharedPreferences sp = ctx.getSharedPreferences("datavalues", Context.MODE_PRIVATE);
    return sp.getString("tkn", "");
  }

  public static void clearAccFromApp(Context ctx) {
    SharedPreferences sp = ctx.getSharedPreferences("datavalues", Context.MODE_PRIVATE);
    SharedPreferences.Editor se = sp.edit();
    se.clear().apply();
  }

  public static void setPswdChangedStatus(Context ctx, boolean status) {
    SharedPreferences sp = ctx.getSharedPreferences("datavalues", Context.MODE_PRIVATE);
    SharedPreferences.Editor se = sp.edit();
    se.putBoolean("ps_stat", status);
    se.apply();
  }

  public static boolean getPswdChangedStatus(Context ctx) {
    SharedPreferences sp = ctx.getSharedPreferences("datavalues", Context.MODE_PRIVATE);
    return sp.getBoolean("ps_stat", false);
  }

  public static int OfflineStatusCode(Context ctx, String account, String pswd) {
    int status = 0;
    if (account.equals(getAccount(ctx))) {
      if (pswd.equals(Decrypt(pswd, getEncPswd(ctx)))) {
        status = 2;
      } else {
        status = 1;
      }
    }
    return status;
  }

  @SuppressLint("UseCompatLoadingForDrawables")
  public static void showMessage(Context ctx, View v, String text, boolean isSuccessMessage) {
    final Snackbar snackbar = Snackbar.make(v, text, Snackbar.LENGTH_INDEFINITE);
    View view = snackbar.getView();
    final ViewGroup.LayoutParams params = view.getLayoutParams();
    if (params instanceof CoordinatorLayout.LayoutParams) {
      ((CoordinatorLayout.LayoutParams) params).gravity = Gravity.TOP;
    } else {
      ((FrameLayout.LayoutParams) params).gravity = Gravity.TOP;
    }
    view.setLayoutParams(params);
    if (!isSuccessMessage) {
      view.setBackground(ctx.getDrawable(R.drawable.red_snack));
    } else {
      view.setBackground(ctx.getDrawable(R.drawable.green_snack));
    }
    TextView snackbarText = view.findViewById(R.id.snackbar_text);
    snackbarText.setTextColor(Color.WHITE);
    snackbarText.setMaxLines(4);
    snackbarText.setGravity(Gravity.CENTER);
    snackbar.setAction("Ok", v1 -> snackbar.dismiss());
    snackbar.setActionTextColor(Color.YELLOW);
    snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);
    try {
      new CountDownTimer(15000, 15000) {
        @Override
        public void onTick(long l) {}

        @Override
        public void onFinish() {
          snackbar.dismiss();
        }
      }.start();
    } catch (Exception ignored) {
    }
    snackbar.show();
  }

  public static void installUpdate(
      Context context, int currentVersion, ActivityResultLauncher<Intent> launcher) {
    File f = new File(context.getExternalFilesDir(null), "OneX.apk");
    if (f.exists()) {
      try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          if (!context.getPackageManager().canRequestPackageInstalls()) {
            Intent install = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                                 .setData(Uri.parse("package:one.x"));
            launcher.launch(install);
          }
        }
        SelfUpdater updater = new SelfUpdater(context, f, currentVersion);
        updater.updateIfNeeded();
      } catch (Exception ex) {
        Toast
            .makeText(
                context, getActivity(context).getString(R.string.error_file), Toast.LENGTH_SHORT)
            .show();
      }
    }
  }
  public static String getCountryCode(Context context) {
    String countryCode = null;

    TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    if (tm != null) {
      countryCode = tm.getSimCountryIso();
      if (countryCode == null || countryCode.isEmpty()) {
        countryCode = tm.getNetworkCountryIso();
      }
    }

    if (countryCode == null || countryCode.isEmpty()) {
      LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
      if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
          == PackageManager.PERMISSION_GRANTED) {
        Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
          try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            if (Build.VERSION.SDK_INT <= 32) {
              List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
              if (!addresses.isEmpty()) {
                countryCode = addresses.get(0).getCountryCode();
              }
            } else {
              geocoder.getFromLocation(lat, lon, 1, new Geocoder.GeocodeListener() {
                @Override
                public void onGeocode(List<Address> addresses) {
                  if (addresses != null && !addresses.isEmpty()) {
                    String country = addresses.get(0).getCountryName();
                  }
                }
              });
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }

    return countryCode != null ? countryCode.toUpperCase() : "";
  }

  public static List<Integer> getIndexes(String text, char start, char end) {
    List<Integer> indexes = new ArrayList<Integer>();
    int tlength = text.length();
    for (int i = 0; i < tlength; i++) {
      if (text.charAt(i) == start) {
        indexes.add(i);
      }
      if (text.charAt(i) == end) {
        indexes.add(i);
      }
    }
    return indexes;
  }

  public static void restoreText(Context context, String text, String color) {
    Activity activity = getActivity(context);
    if (activity != null) {
      TextView myapp = activity.findViewById(R.id.myapp);
      if (myapp != null) {
        myapp.setTextColor(Color.parseColor(color));
        myapp.setText(text);
      }
    }
  }

  public static Activity getActivity(Context context) {
    if (context == null)
      return null;
    if (context instanceof Activity)
      return (Activity) context;
    if (context instanceof ContextWrapper)
      return getActivity(((ContextWrapper) context).getBaseContext());
    return null;
  }
}
