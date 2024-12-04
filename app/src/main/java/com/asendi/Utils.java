package com.asendi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Base64;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.text.HtmlCompat;

import com.android.volley.toolbox.JsonObjectRequest;
import com.elyeproj.loaderviewlibrary.LoaderTextView;
import com.google.android.material.snackbar.BaseTransientBottomBar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;
import java.util.List;
import android.view.View;
import java.util.Map;
import java.util.Objects;

import org.json.JSONException;
import org.json.JSONObject;

public class Utils {
    public static Bitmap qr(String text) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        Bitmap bitmap = null;
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 600, 600);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(bitMatrix);
        } catch (WriterException ignored) {
        }
        return bitmap;
    }

    public static void showNoConnectionAlert(Context ctx, View v) {
        Snackbar sb = Snackbar.make(v, ctx.getString(R.string.no_connection), Snackbar.LENGTH_LONG);
        sb.setAction("Ok", v1 -> sb.dismiss());
        sb.show();
    }

    public static boolean isConnectionAvailable(Context context) {
        if(Objects.equals(ASENDI.BASE_URL, "http://127.0.0.1:5555")) { ASENDI.isNetworkConnected = true;}
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return ASENDI.isNetworkConnected;
        } else {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
               return ASENDI.isNetworkConnected = activeNetwork != null && activeNetwork.isConnected();
            }
        }
        return ASENDI.isNetworkConnected;
    }

    private static JSONObject post(String[] keys, String[] value) {
        JSONObject jo = new JSONObject();
        int kl = keys.length;
        try {
            for (int i = 0; i < kl; i++) {
                jo.put(keys[i], value[i]);
            }
        } catch (JSONException ignored) {}
        return jo;
    }

    public static void connectToServer(Activity ctx, String url, String[] keys, String[] values, final boolean showProgress,
        final ServerListener slistener) {
        try {
            progress prog = new progress();
            if (showProgress) {
                prog.show(ctx);
            }
            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, post(keys, values),
                    response -> {
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
                        }catch (Exception e) {
                            Toast.makeText(ctx, ctx.getString(R.string.connect_error), Toast.LENGTH_SHORT).show();
                        }

                    }) {

                @NonNull
                @Override
                public Map < String, String > getHeaders() {
                    // Build the headers
                    final Map < String, String > params = new HashMap<>();
                    params.put("Content-Type",
                        "application/json");
                    return params;
                }

            };
            Volley.newRequestQueue(ctx).add(jsonObjectRequest);
        }catch (Exception e) {
            Toast.makeText(ctx,
                ctx.getString(R.string.connect_error),
                Toast.LENGTH_SHORT).show();
        }
    }

    public static void requestTP (final Activity ctx,
        String item,
        final LoaderTextView lt) {
        try {
            RequestQueue queue = Volley.newRequestQueue(ctx);
            StringRequest sr = new StringRequest(Request.Method.GET,
                ASENDI.INFO + "?r=" + item,
                    response -> lt.setText(HtmlCompat.fromHtml(response, HtmlCompat.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE),
                    error -> {
                        lt.setText("( … )");
                        Toast.makeText(ctx, ctx.getString(R.string.connect_error), Toast.LENGTH_SHORT).show();
                    });
            queue.add(sr);
        }catch(Exception e) {
            Toast.makeText(ctx,
                ctx.getString(R.string.connect_error),
                Toast.LENGTH_SHORT).show();
        }
    }

    public static String imageToString(Bitmap bitmap) {
        // useful to upload image to server
        //converting image to base64 string
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,
            100,
            baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes,
            Base64.DEFAULT);
    }

    public static void hideSoftKeyboard(Activity ctx) {
        if (ctx.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) ctx
            .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(ctx.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public static void showCommonNotif(Activity ctx, List < CharSequence > list) {
        if (list.isEmpty()) {
            list.add(ctx.getString(R.string.empty_list)); // here show default alert in list if list is empty
        }
        ListPopupWindow listPopupWindow = new ListPopupWindow(ctx);
        listPopupWindow.setAdapter(new ArrayAdapter<>(ctx, R.layout.textview_item_two, R.id.textview_item, list));
        View v = ctx.findViewById(R.id.toolbar);
        listPopupWindow.setAnchorView(v);
        int w = v.getWidth();
        listPopupWindow.setWidth(w);
        listPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        listPopupWindow.setBackgroundDrawable(new ColorDrawable(ctx.getResources().getColor(R.color.primary)));
        listPopupWindow.setModal(true);
        listPopupWindow.setPromptPosition(ListPopupWindow.POSITION_PROMPT_BELOW);
        listPopupWindow.setOnItemClickListener(null);
        listPopupWindow.show();
    }

    private static String Decrypt(String key, String data) {
        String d;
        try {
            d = Objects.requireNonNull(DataCrypto.getDefault(key, "wa20" + key.substring(1, 4) + "ve24", new byte[16])).decryptOrNull(data);
        } catch (Exception e) {
            d = "";
        }
        return d;
    }

    private static String Encrypt(String key, String data) {
        String d;
        try {
            d = Objects.requireNonNull(DataCrypto.getDefault(key, "wa20" + key.substring(1, 4) + "ve24", new byte[16])).encryptOrNull(data);
        } catch (Exception e) {
            d = "";
        }
        return d;
    }

    public static void saveCredentials (Context ctx, String account, String pswd) {
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
        return sp.getString("acc_name", "");
    }

    public static void saveCategory (Context ctx, String cat) {
        SharedPreferences sp = ctx.getSharedPreferences("datavalues", Context.MODE_PRIVATE);
        SharedPreferences.Editor se = sp.edit();
        se.putString("cat", cat);
        se.apply();
    }

    public static String getCategory(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences("datavalues", Context.MODE_PRIVATE);
        return sp.getString("cat", "0");
    }

    public static void saveTkn (Context ctx, String tkn) {
        SharedPreferences sp = ctx.getSharedPreferences("datavalues", Context.MODE_PRIVATE);
        SharedPreferences.Editor se = sp.edit();
        se.putString("tkn", tkn);
        se.apply();
    }

    public static String getTkn(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences("datavalues", Context.MODE_PRIVATE);
        return sp.getString("tkn", "");
    }
    public static void saveLastDldedApp (Context ctx, String d) {
        SharedPreferences sp = ctx.getSharedPreferences("datavalues", Context.MODE_PRIVATE);
        SharedPreferences.Editor se = sp.edit();
        se.putString("ld", d);
        se.apply();
    }

    public static String getLastDldedApp(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences("datavalues", Context.MODE_PRIVATE);
        return sp.getString("ld", "0");
    }

    public static void clearAccFromApp(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences("datavalues", Context.MODE_PRIVATE);
        SharedPreferences.Editor se = sp.edit();
        se.clear().apply();
    }

    public static void setPswdChangedStatus(Context ctx, boolean status){
        SharedPreferences sp = ctx.getSharedPreferences("datavalues", Context.MODE_PRIVATE);
        SharedPreferences.Editor se = sp.edit();
        se.putBoolean("ps_stat", status);
        se.apply();
    }

    public static boolean getPswdChangedStatus(Context ctx){
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
    public static void showMessage (Context ctx, View v, String text, boolean isSuccessMessage) {
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
        } catch (Exception ignored) {}
        snackbar.show();
    }

    public static void copyApkFromPrivateToPublic(Context ctx){
		FileInputStream input;
		FileOutputStream output;
		try{
			input = new FileInputStream(new File(ctx.getFilesDir(), "Asendi.apk"));
			output = new FileOutputStream(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Asendi.apk"));
			byte[] data = new byte[4096];
			int count;
            while ((count = input.read(data)) != -1) {
				output.write(data, 0, count);
			}
            output.close();
            input.close();
		}catch (IOException ignored){
			
		}
	}
	public static void installUpdate(Context context){
	try {
		PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
		int sessionId = packageInstaller.createSession(new PackageInstaller
		.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL));
		PackageInstaller.Session session = packageInstaller.openSession(sessionId);
		
		long sizeBytes = 0;
		File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Asendi.apk");
		InputStream inputStream = new FileInputStream(f);
		OutputStream out;
		out = session.openWrite("asendi_session_install", 0, sizeBytes);

        byte[] buffer = new byte[65536];
		int c;
		while ((c = inputStream.read(buffer)) != -1) {
            out.write(buffer, 0, c);
		}
		session.fsync(out);
		inputStream.close();
		out.close();

        Intent intent = new Intent(context, Signin.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
		1337111117, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		session.commit(pendingIntent.getIntentSender());
		session.close();
	}catch (IOException ignored){}}
}