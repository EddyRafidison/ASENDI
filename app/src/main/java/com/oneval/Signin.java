package com.oneval;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.pm.PackageInfoCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.text.HtmlCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class Signin extends AppCompatActivity {
    private static final int REQUEST_STORAGE_PERMISSIONS = 123;
    private static final int REQUEST_FILE_PERMISSIONS = 456;
    private final String readPermission = android.Manifest.permission.READ_EXTERNAL_STORAGE;
    private final String writePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private final ActivityResultLauncher<Intent> getInstallPermResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {

            });
    private EditText account,
            pswd;
    private Button login;
    private TextView myapp;
    private String vers = "";
    private int dld = 0;
    private boolean isDownloading = false,
            doubleBackToExitPressedOnce = false;
    private final ActivityResultLauncher<Intent> getPermResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    downloadApp(ONEVAL.LATEST_APK);
                }
            });

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        Window w = getWindow();
        try {
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        } catch (Exception ignored) {
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            w.setDecorFitsSystemWindows(false);
            w.setNavigationBarColor(R.color.primary);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.TYPE_STATUS_BAR);
            w.setNavigationBarColor(R.color.primary);
        }
        setContentView(R.layout.signin);
        Locale langForTP = Locale.getDefault();
        String lang = langForTP.getLanguage();
        if (lang.startsWith("fr")) {
            ONEVAL.TPLANG = Locale.FRENCH;
        }
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        CheckNetwork network_ = new CheckNetwork(getApplicationContext());
        network_.registerNetworkCallback();
        account = findViewById(R.id.account_name);
        pswd = findViewById(R.id.account_pswd);
        login = findViewById(R.id.login);
        Button signup = findViewById(R.id.signup);
        myapp = findViewById(R.id.myapp);
        myapp.setMovementMethod(LinkMovementMethod.getInstance());
        myapp.setHighlightColor(Color.WHITE);
        ImageButton recover_pswd = findViewById(R.id.recover_pswd);
        OnBackPressedCallback onback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isDownloading) {
                    if (doubleBackToExitPressedOnce) {
                        getOnBackPressedDispatcher().onBackPressed();
                        return;
                    }
                    doubleBackToExitPressedOnce = true;
                    Toast.makeText(Signin.this,
                            getString(R.string.click_twice_exit),
                            Toast.LENGTH_SHORT).show();

                    new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackToExitPressedOnce = false,
                            2000);
                } else {
                    exitApp();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onback);
        if (Utils.isConnectionAvailable(getApplicationContext()) == false) {
            Utils.showNoConnectionAlert(getApplicationContext(), login);
        } else {
            PackageManager manager = getPackageManager();
            try {
                PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
                final int versionCode = (int) PackageInfoCompat.getLongVersionCode(info);
                Utils.connectToServer(Signin.this, ONEVAL.CHECKAPP, new String[]{}, new String[]{}, true, response -> {
                    try {
                        processApk(response, versionCode);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (PackageManager.NameNotFoundException e) {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.check_vers_failed),
                        Toast.LENGTH_SHORT).show();
            }

        }
        DrawableCompat.setTint(recover_pswd.getBackground(),
                Color.TRANSPARENT);
        DrawableCompat.setTint(login.getBackground(),
                Color.parseColor("#4AA1FF"));
        DrawableCompat.setTint(signup.getBackground(),
                Color.parseColor("#D32F2F"));
        login.setOnClickListener(this::onClick);
        signup.setOnClickListener(c -> {
            if (!isDownloading) {
                startActivity(new Intent(getApplicationContext(), Signup.class));
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.app_busy), Toast.LENGTH_SHORT).show();
            }
        });
        recover_pswd.setOnClickListener(c -> {
            String loginId = account.getText().toString();
            if (!loginId.isEmpty()) {
                showAccountRecovery();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.enter_Id_first), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onStart() {
        super.onStart();
        account.setText(Utils.getAccount(getApplicationContext()));
        if (account.length() == 0) {
            account.requestFocus();
        } else {
            pswd.requestFocus();
        }
    }

    private void exitApp() {
        finishAffinity();
    }

    private void processApk(final JSONObject response, final int versionCode) throws IOException {
        try {
            vers = response.getString("version");
            final String size = response.getString("size");
            if (!vers.isEmpty() && !size.isEmpty()) {
                final int v = Integer.parseInt(vers);
                final int fs = Math.round(Float.parseFloat(size));
                final String dv = Utils.getLastDldedApp(getApplicationContext());
                final int Dv = Integer.parseInt(dv);
                if (v > versionCode) {
                    File apkf = new File(getFilesDir(), "One-Val.apk");
                    if (apkf.exists()) {
                        if (v == Dv) {
                            final String strrr = getString(R.string.install);
                            SpannableStringBuilder ssb_ = new SpannableStringBuilder(strrr);
                            ssb_.setSpan(new Clickables(myapp, new String[]{
                                    strrr
                            }, 0, string -> {
                                if (string.equals(strrr)) {
                                    try {
                                        Utils.copyApkFromPrivateToPublic(getApplicationContext());
                                        final File appfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "One-Val.apk");
                                        if (appfile.exists()) {
                                            try {
                                                Uri apkUri;
                                                Intent intent;
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                    apkUri = FileProvider.getUriForFile(Signin.this, "com.oneval.fileprovider", appfile);
                                                    intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                                                    intent.setData(apkUri);
                                                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                                } else {
                                                    apkUri = Uri.fromFile(appfile);
                                                    intent = new Intent(Intent.ACTION_VIEW);
                                                    intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                }
                                                startActivity(intent);
                                            } catch (Exception e) {
                                                Utils.installUpdate(getApplicationContext());
                                            }
                                        } else {
                                            Toast.makeText(getApplicationContext(), getString(R.string.error_file), Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (Exception e) {
                                        Toast.makeText(getApplicationContext(), getString(R.string.error_file), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            },
                                    Color.GREEN), 0, ssb_.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            myapp.setText(ssb_, TextView.BufferType.SPANNABLE);
                        } else {
                            apkf.delete();
                            Utils.saveLastDldedApp(getApplicationContext(), "" + versionCode);
                            processApk(response, versionCode);
                        }
                    } else {
                        final String strr1 = getString(R.string.app_update_available);
                        final String strr2 = getString(R.string.download_vers) + " " + vers + " (~" + fs + "MB)";
                        SpannableStringBuilder ssb = new SpannableStringBuilder(strr1);
                        ssb.setSpan(new Clickables(myapp, new String[]{
                                strr1
                        }, 0, string -> {
                            if (string.equals(strr1)) {
                                SpannableStringBuilder ssb1 = new SpannableStringBuilder(strr2);
                                ssb1.setSpan(new Clickables(myapp, new String[]{
                                        strr2
                                }, 0, string1 -> {
                                    if (string1.equals(strr2)) {
                                        checkPermissions();
                                    }
                                },
                                        Color.GREEN), 0, ssb1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                myapp.setText(ssb1, TextView.BufferType.SPANNABLE);
                            }
                        },
                                Color.GREEN), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        myapp.setText(ssb, TextView.BufferType.SPANNABLE);

                    }
                } else if (v == versionCode) {
                    File apkF = new File(getFilesDir(), "One-Val.apk");
                    Utils.saveLastDldedApp(getApplicationContext(), "" + v);
                    if (apkF.exists()) {
                        apkF.delete();
                    }
                }

            }
        } catch (JSONException je) {
            Toast.makeText(getApplicationContext(), getString(R.string.data_error), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAccountRecovery() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme_Dialog));
        final View customLayout = getLayoutInflater().inflate(R.layout.recovery_dialog, null, false);
        builder.setView(customLayout);
        builder.setCancelable(false);
        final EditText edtSk = customLayout.findViewById(R.id.skDialog);
        edtSk.requestFocus();
        builder.setPositiveButton(HtmlCompat.fromHtml("<font color='yellow'>" + "Ok" + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY), (arg0, arg1) -> {
            final String sk = edtSk.getText().toString();
            final String acc = account.getText().toString();
            if (!sk.isEmpty()) {
                if (Utils.isConnectionAvailable(getApplicationContext()) == false) {
                    Utils.showNoConnectionAlert(getApplicationContext(), login);
                } else {
                    arg0.dismiss();
                    Utils.connectToServer(Signin.this, ONEVAL.REACC, new String[]{
                            "user", "sk"
                    }, new String[]{
                            acc, sk
                    }, true, response -> {
                        try {
                            String status = response.getString("auth");
                            if (status.equals("updated")) {
                                Utils.showMessage(getApplicationContext(), login, getString(R.string.successed_recovery), true);
                                Utils.saveCredentials(getApplicationContext(), acc, ONEVAL.PINREC);
                            } else if (status.equals("incorrect")) {
                                Utils.showMessage(getApplicationContext(), login, getString(R.string.error_sk), false);
                            } else {
                                Utils.showMessage(getApplicationContext(), login, getString(R.string.failed), false);
                            }
                        } catch (JSONException je) {
                            Toast.makeText(getApplicationContext(), getString(R.string.data_error), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.sk_required), Toast.LENGTH_SHORT).show();
            }

        });
        builder.setNeutralButton(HtmlCompat.fromHtml("<font color='#848482'>" + getString(R.string.cancel) + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY),
                (dialog, which) -> dialog.cancel());
        AlertDialog dialogRecovery = builder.create();
        dialogRecovery.show();
    }

    private void downloadApp(String url_) {
        isDownloading = true;
        myapp.setTextColor(Color.GREEN);
        final String strd = getString(R.string.downloading);
        myapp.setText(strd);

        new AsyncTaskV2<Void, Integer, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @SuppressLint("SuspiciousIndentation")
            @Override
            protected Void doInBackground(Void unused) {
                try {
                    InputStream input = null;
                    OutputStream output = null;
                    HttpURLConnection connection = null;
                    try {
                        URL url = new URL(url_);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.connect();
                        int fileLength = connection.getContentLength();
                        // download the file
                        input = connection.getInputStream();
                        output = new FileOutputStream(new File(getFilesDir(), "One-Val.apk"));
                        byte[] data = new byte[4096];
                        long total = 0;
                        int count;

                        while ((count = input.read(data)) != -1) {
                            total += count;
                            if (fileLength > 0) {
                                dld = (int) (total * 100 / fileLength);
                                publishProgress(dld);
                                output.write(data, 0, count);
                            }
                        }
                    } catch (Exception e) {
                        isDownloading = false;
                        myapp.setTextColor(Color.RED);
                        myapp.setText(getString(R.string.download_failed));
                    } finally {
                        try {
                            if (output != null) {
                                output.close();
                            }
                            if (input != null) {
                                input.close();
                            }
                        } catch (IOException ignored) {
                            isDownloading = false;
                            myapp.setTextColor(Color.RED);
                            myapp.setText(getString(R.string.download_failed));
                        }

                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
                } catch (Exception e) {
                    isDownloading = false;
                    myapp.setTextColor(Color.RED);
                    myapp.setText(getString(R.string.download_failed));
                }
                return null;
            }

            @SuppressLint({"SetTextI18n", "SuspiciousIndentation"})
            @Override
            protected void onProgressUpdate(Integer integer) {
                super.onProgressUpdate(integer);
                if (integer <= 99) {
                    myapp.setText(strd + "  " + integer + "%");
                } else {
                    if (isDownloading) {
                        isDownloading = false;
                        Utils.saveLastDldedApp(getApplicationContext(), vers);
                        final String strr = getString(R.string.install);
                        SpannableStringBuilder ssb = new SpannableStringBuilder(strr);
                        ssb.setSpan(new Clickables(myapp, new String[]{
                                strr
                        }, 0, string -> {
                            if (string.equals(strr)) {
                                try {
                                    Utils.copyApkFromPrivateToPublic(getApplicationContext());
                                    final File appfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "One-Val.apk");
                                    if (appfile.exists()) {
                                        try {
                                            Uri apkUri;
                                            Intent intent;
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                apkUri = FileProvider.getUriForFile(Signin.this, "com.oneval.fileprovider", appfile);
                                                intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                                                intent.setData(apkUri);
                                                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                            } else {
                                                apkUri = Uri.fromFile(appfile);
                                                intent = new Intent(Intent.ACTION_VIEW);
                                                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            }
                                            startActivity(intent);
                                        } catch (Exception e) {
                                            Utils.installUpdate(getApplicationContext());
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(), getString(R.string.error_file), Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.error_file), Toast.LENGTH_SHORT).show();
                                }
                            }
                        },
                                Color.GREEN), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        myapp.setText(ssb, TextView.BufferType.SPANNABLE);
                    }
                }
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
            }
        }.execute(null);

    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!getPackageManager().canRequestPackageInstalls()) {
                Intent inst = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse("package:com.oneval"));
                getInstallPermResult.launch(inst);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //As the device is Android 13 and above so I want the permission of accessing Audio, Images, Videos
            //You can ask permission according to your requirements what you want to access.
            String filesPermission = android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;
            //Check for permissions and request them if needed
            if (
                    ContextCompat.checkSelfPermission(this, filesPermission) == PackageManager.PERMISSION_GRANTED) {
                // You have the permissions, you can proceed with your media file operations.
                //Showing dialog when Show Dialog button is clicked.
                downloadApp(ONEVAL.LATEST_APK);
            } else {
                // You don't have the permissions. Request them.
                ActivityCompat.requestPermissions(this, new String[]{
                        filesPermission
                }, REQUEST_FILE_PERMISSIONS);
            }
        } else {
            //Android version is below 13 so we are asking normal read and write storage permissions
            //Check for permissions and request them if needed
            if (ContextCompat.checkSelfPermission(this, readPermission) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, writePermission) == PackageManager.PERMISSION_GRANTED) {
                // You have the permissions, you can proceed with your file operations.
                downloadApp(ONEVAL.LATEST_APK);
            } else {
                // You don't have the permissions. Request them.
                ActivityCompat.requestPermissions(this, new String[]{
                        readPermission, writePermission
                }, REQUEST_STORAGE_PERMISSIONS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions were granted. You can proceed with your file operations.
                //Showing dialog when Show Dialog button is clicked.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    //Android version is 11 and above so to access all types of files we have to give
                    //special permission so show user a dialog..
                    accessAllFilesPermissionDialog();
                } else {
                    //Android version is 10 and below so need of special permission...
                    downloadApp(ONEVAL.LATEST_APK);
                }
            } else {
                // Permissions were denied. Show a rationale dialog or inform the user about the importance of these permissions.
                showRationaleDialog();
            }
        }

        //This conditions only works on Android 13 and above versions
        if (requestCode == REQUEST_FILE_PERMISSIONS) {
            if (grantResults.length > 0 && areAllPermissionsGranted(grantResults)) {
                //Permissions were granted. You can proceed with your media file operations.
                //Showing dialog when Show Dialog button is clicked.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    //Android version is 11 and above so to access all types of files we have to give
                    //special permission so show user a dialog..
                    accessAllFilesPermissionDialog();
                }
            } else {
                // Permissions were denied. Show a rationale dialog or inform the user about the importance of these permissions.
                showRationaleDialog();
            }
        }
    }

    private boolean areAllPermissionsGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void showRationaleDialog() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, readPermission) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this, writePermission)) {
            // Show a rationale dialog explaining why the permissions are necessary.
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.requestPermTitle))
                    .setMessage(getString(R.string.requestPermText))
                    .setPositiveButton(HtmlCompat.fromHtml("<font color='yellow'>" + "Ok" + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY), (dialog, which) -> {
                        // Request permissions when the user clicks OK.
                        ActivityCompat.requestPermissions(this, new String[]{
                                readPermission, writePermission
                        }, REQUEST_STORAGE_PERMISSIONS);
                    })
                    .setNeutralButton(HtmlCompat.fromHtml("<font color='#848482'>" + getString(R.string.cancel) + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY), (dialog, which) -> {
                        dialog.dismiss();
                        // Handle the case where the user cancels the permission request.
                    })
                    .show();
        } else {
            // Request permissions directly if no rationale is needed.
            ActivityCompat.requestPermissions(this, new String[]{
                    readPermission, writePermission
            }, REQUEST_STORAGE_PERMISSIONS);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void accessAllFilesPermissionDialog() {
        new AlertDialog.Builder(Signin.this)
                .setTitle(getString(R.string.requestPermTitle))
                .setMessage(getString(R.string.requestPermText))
                .setPositiveButton(HtmlCompat.fromHtml("<font color='yellow'>" + "Ok" + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY), (dialog, which) -> {
                    // Request permissions when the user clicks OK.
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:com.oneval"));
                    getPermResult.launch(intent);
                })
                .setNeutralButton(HtmlCompat.fromHtml("<font color='#848482'>" + getString(R.string.cancel) + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY), (dialog, which) -> {
                    dialog.dismiss();
                    // Handle the case where the user cancels the permission request.
                })
                .show();
    }

    private void onClick(View c) {
        if (!isDownloading) {
            final Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            if (account.length() > 0 && pswd.length() > 0) {
                final String acc = account.getText().toString();
                final String psd = pswd.getText().toString();
                if (Utils.isConnectionAvailable(getApplicationContext()) == false) {
                    Utils.showNoConnectionAlert(getApplicationContext(), login);
                } else {
                    pswd.setText("");
                    final String Acc = Utils.getAccount(getApplicationContext());
                    final int status = Utils.OfflineStatusCode(getApplicationContext(), acc, psd);
                    if (status == 2) {
                        boolean pstat = Utils.getPswdChangedStatus(getApplicationContext());
                        if (pstat == false) {
                            intent.putExtra("act", acc);
                            intent.putExtra("psd", psd);
                            startActivity(intent);
                        } else {
                            showLoginConfirm(acc, psd, intent);
                        }
                    } else {
                        if (Acc.isEmpty()) {
                            //check if credentials correct
                            Utils.connectToServer(Signin.this, ONEVAL.SIGNIN, new String[]{
                                    "user", "pswd", "tkn", "recon", "sk"
                            }, new String[]{
                                    acc, psd, Utils.getTkn(getApplicationContext()), "0", "no"
                            }, true, response -> {
                                if (response != null) {
                                    try {
                                        String msg = response.getString("msg");
                                        String tkn = response.getString("ua");
                                        if (msg.equals("1") || msg.equals("2")) {
                                            Utils.saveCredentials(getApplicationContext(), acc, psd);
                                            intent.putExtra("act", acc);
                                            intent.putExtra("psd", psd);
                                            if (msg.equals("1")) {
                                                Utils.saveCategory(getApplicationContext(), "B");
                                            } else {
                                                Utils.saveCategory(getApplicationContext(), "C");
                                            }
                                            if (!tkn.isEmpty()) {
                                                Utils.saveTkn(getApplicationContext(), tkn);
                                                startActivity(intent);
                                            }
                                        } else if (msg.equals("incorrect auth")) {
                                            Utils.showMessage(getApplicationContext(), login, getString(R.string.error_pswd), false);
                                        } else {
                                            if (msg.contains("forbidden")) {
                                                showLoginConfirm(acc, psd, intent);
                                            } else {
                                                Utils.showMessage(getApplicationContext(), login, getString(R.string.account_null), false);
                                            }
                                        }
                                    } catch (JSONException je) {
                                        Toast.makeText(getApplicationContext(), getString(R.string.data_error), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            if (status == 0) {
                                Toast.makeText(getApplicationContext(), getString(R.string.notValidAccount),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), getString(R.string.error_pswd),
                                                Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }

                    }

                }
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.check_entries), Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.app_busy), Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoginConfirm(final String acct, final String psd, final Intent intent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme_Dialog));
        final View customLayout = getLayoutInflater().inflate(R.layout.entersk, null, false);
        builder.setView(customLayout);
        builder.setCancelable(false);
        final EditText edtSk = customLayout.findViewById(R.id.skEdt);
        edtSk.requestFocus();
        builder.setPositiveButton(HtmlCompat.fromHtml("<font color='yellow'>" + "Ok" + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY), (arg0, arg1) -> {
            final String sk = edtSk.getText().toString();
            if (!sk.isEmpty()) {
                if (Utils.isConnectionAvailable(getApplicationContext()) == false) {
                    Utils.showNoConnectionAlert(getApplicationContext(), login);
                } else {
                    arg0.dismiss();
                    Utils.connectToServer(Signin.this, ONEVAL.SIGNIN, new String[]{
                            "user", "pswd", "tkn", "recon", "sk"
                    }, new String[]{
                            acct, psd, Utils.getTkn(getApplicationContext()), "3", sk
                    }, true, response -> {
                        if (response != null) {
                            try {
                                String msg = response.getString("msg");
                                String tkn = response.getString("ua");
                                if (msg.equals("1") || msg.equals("2")) {
                                    if (msg.equals("1")) {
                                        Utils.saveCategory(getApplicationContext(), "B");
                                    } else {
                                        Utils.saveCategory(getApplicationContext(), "C");
                                    }
                                    Utils.saveCredentials(getApplicationContext(), acct, psd);
                                    Utils.setPswdChangedStatus(getApplicationContext(), false);
                                    intent.putExtra("act", acct);
                                    intent.putExtra("psd", psd);
                                    if (!tkn.isEmpty()) {
                                        Utils.saveTkn(getApplicationContext(), tkn);
                                        startActivity(intent);
                                    }
                                } else if (msg.contains("incorrect")) {
                                    if (msg.contains("secret word")) {
                                        Utils.showMessage(getApplicationContext(), login, getString(R.string.error_sk), false);
                                    } else {
                                        Utils.showMessage(getApplicationContext(), login, getString(R.string.error_pswd), false);
                                    }
                                } else {
                                    Utils.showMessage(getApplicationContext(), login, getString(R.string.account_null), false);
                                }
                            } catch (JSONException je) {
                                Toast.makeText(getApplicationContext(), getString(R.string.data_error), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.sk_required), Toast.LENGTH_SHORT).show();
            }

        });
        builder.setNeutralButton(HtmlCompat.fromHtml("<font color='#848482'>" + getString(R.string.cancel) + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY),
                (dialog, which) -> dialog.cancel());
        AlertDialog adialog = builder.create();
        adialog.show();
    }
}