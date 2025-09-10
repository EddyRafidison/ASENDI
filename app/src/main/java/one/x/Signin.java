package one.x;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
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
import androidx.appcompat.widget.ListPopupWindow;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.pm.PackageInfoCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.text.HtmlCompat;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import one.x.helper.LocaleHelper;
import org.json.JSONException;
import org.json.JSONObject;

public class Signin extends AppCompatActivity {
  private final ActivityResultLauncher<Intent> getInstallPermResult =
      registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
          result
          -> {

          });
  private EditText account, pswd;
  private Button login;
  private TextView myapp, CurrentLang;
  private String vers = "", currentLan = "";
  private int dld = 0;
  private boolean isDownloading = false, doubleBackToExitPressedOnce = false;

  @SuppressLint("ResourceAsColor")
  @Override
  protected void onCreate(Bundle arg0) {
    super.onCreate(arg0);
    Window w = getWindow();
    try {
      w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
          WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
    } catch (Exception ignored) {
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      w.setDecorFitsSystemWindows(false);
      w.setNavigationBarColor(R.color.primary);
    } else {
      getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
          WindowManager.LayoutParams.TYPE_STATUS_BAR);
      w.setNavigationBarColor(R.color.primary);
    }
    setContentView(R.layout.signin);

    Intent getData = getIntent();
    if (getData != null) {
      ONEX.COUNTRY = getData.getStringExtra("loc");
      ONEX.CURRENCY = getData.getStringExtra("curr");
      currentLan = getData.getStringExtra("lang");
    } else {
      finish();
    }

    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    StrictMode.setThreadPolicy(policy);
    CheckNetwork network_ = new CheckNetwork(getApplicationContext());
    network_.registerNetworkCallback();
    account = findViewById(R.id.account_name);
    CurrentLang = findViewById(R.id.Lang);
    pswd = findViewById(R.id.account_pswd);
    login = findViewById(R.id.login);
    Button signup = findViewById(R.id.signup);
    myapp = findViewById(R.id.myapp);
    myapp.setMovementMethod(LinkMovementMethod.getInstance());
    myapp.setHighlightColor(Color.WHITE);
    ImageButton recover_pswd = findViewById(R.id.recover_pswd);

    CurrentLang.setText(("" + ONEX.TPLANG).toUpperCase());

    OnBackPressedCallback onback = new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        if (isDownloading) {
          if (doubleBackToExitPressedOnce) {
            getOnBackPressedDispatcher().onBackPressed();
            return;
          }
          doubleBackToExitPressedOnce = true;
          Toast.makeText(Signin.this, getString(R.string.click_twice_exit), Toast.LENGTH_SHORT)
              .show();

          new Handler(Looper.getMainLooper())
              .postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
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
        Utils.connectToServer(
            Signin.this, ONEX.CHECKAPP, new String[] {}, new String[] {}, true, response -> {
              try {
                processApk(response, versionCode);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
      } catch (PackageManager.NameNotFoundException e) {
        Toast
            .makeText(
                getApplicationContext(), getString(R.string.check_vers_failed), Toast.LENGTH_SHORT)
            .show();
      }
    }
    DrawableCompat.setTint(recover_pswd.getBackground(), Color.TRANSPARENT);
    DrawableCompat.setTint(login.getBackground(), Color.parseColor("#4AA1FF"));
    DrawableCompat.setTint(signup.getBackground(), Color.parseColor("#D32F2F"));
    login.setOnClickListener(this::onClick);
    signup.setOnClickListener(c -> {
      if (!isDownloading) {
        ActivityOptions option =
            ActivityOptions.makeCustomAnimation(Signin.this, R.anim.nav_enter, R.anim.nav_exit);
        startActivity(new Intent(getApplicationContext(), Signup.class), option.toBundle());
      } else {
        Toast.makeText(getApplicationContext(), getString(R.string.app_busy), Toast.LENGTH_SHORT)
            .show();
      }
    });
    recover_pswd.setOnClickListener(c -> {
      String loginId = account.getText().toString();
      if (!loginId.isEmpty()) {
        showAccountRecovery();
      } else {
        Toast
            .makeText(
                getApplicationContext(), getString(R.string.enter_Id_first), Toast.LENGTH_SHORT)
            .show();
      }
    });
  }

  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(CommonTools.getInstance(base));
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
          File apkf = new File(getFilesDir(), "OneX.apk");
          if (apkf.exists()) {
            if (v == Dv) {
              final String strrr = getString(R.string.install);
              SpannableStringBuilder ssb_ = new SpannableStringBuilder(strrr);
              ssb_.setSpan(new Clickables(myapp, new String[] {strrr}, 0, string -> {
                if (string.equals(strrr)) {
                  try {
                    Utils.copyApkToExternal(getApplicationContext());
                    final File appfile =
                        new File(getExternalFilesDir(null) + File.separator + "OneX.apk");
                    if (appfile.exists()) {
                      try {
                        Uri apkUri;
                        Intent intent;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                          apkUri = FileProvider.getUriForFile(
                              Signin.this, "one.x.fileprovider", appfile);
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
                      Toast
                          .makeText(getApplicationContext(), getString(R.string.error_file),
                              Toast.LENGTH_SHORT)
                          .show();
                    }
                  } catch (Exception e) {
                    Toast
                        .makeText(getApplicationContext(), getString(R.string.error_file),
                            Toast.LENGTH_SHORT)
                        .show();
                  }
                }
              }, Color.GREEN), 0, ssb_.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
              myapp.setText(ssb_, TextView.BufferType.SPANNABLE);
            } else {
              apkf.delete();
              Utils.saveLastDldedApp(getApplicationContext(), "" + versionCode);
              processApk(response, versionCode);
            }
          } else {
            final String strr1 = getString(R.string.app_update_available);
            final String strr2 =
                getString(R.string.download_vers) + " " + vers + " (~" + fs + "MB)";
            SpannableStringBuilder ssb = new SpannableStringBuilder(strr1);
            ssb.setSpan(new Clickables(myapp, new String[] {strr1}, 0, string -> {
              if (string.equals(strr1)) {
                SpannableStringBuilder ssb1 = new SpannableStringBuilder(strr2);
                ssb1.setSpan(new Clickables(myapp, new String[] {strr2}, 0, string1 -> {
                  if (string1.equals(strr2)) {
                    checkPermissions();
                  }
                }, Color.GREEN), 0, ssb1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                myapp.setText(ssb1, TextView.BufferType.SPANNABLE);
              }
            }, Color.GREEN), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            myapp.setText(ssb, TextView.BufferType.SPANNABLE);
          }
        } else if (v == versionCode) {
          File apkF = new File(getFilesDir(), "OneX.apk");
          Utils.saveLastDldedApp(getApplicationContext(), "" + v);
          if (apkF.exists()) {
            apkF.delete();
          }
        }
      }
    } catch (JSONException je) {
      Toast.makeText(getApplicationContext(), getString(R.string.data_error), Toast.LENGTH_SHORT)
          .show();
    }
  }

  private void showAccountRecovery() {
    AlertDialog.Builder builder =
        new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme_Dialog));
    final View customLayout = getLayoutInflater().inflate(R.layout.recovery_dialog, null, false);
    builder.setView(customLayout);
    builder.setCancelable(false);
    final EditText edtSk = customLayout.findViewById(R.id.skDialog);
    edtSk.requestFocus();
    builder.setPositiveButton(HtmlCompat.fromHtml("<font color='yellow'>"
                                      + "Ok"
                                      + "</font>",
                                  HtmlCompat.FROM_HTML_MODE_LEGACY),
        (arg0, arg1) -> {
          final String sk = edtSk.getText().toString();
          final String acc = account.getText().toString();
          if (!sk.isEmpty()) {
            if (Utils.isConnectionAvailable(getApplicationContext()) == false) {
              Utils.showNoConnectionAlert(getApplicationContext(), login);
            } else {
              arg0.dismiss();
              Utils.connectToServer(Signin.this, ONEX.REACC, new String[] {"user", "sk"},
                  new String[] {acc, sk}, true, response -> {
                    try {
                      String status = response.getString("auth");
                      if (status.equals("updated")) {
                        Utils.showMessage(getApplicationContext(), login,
                            getString(R.string.successed_recovery), true);
                        Utils.saveCredentials(getApplicationContext(), acc, ONEX.PINREC);
                      } else if (status.equals("incorrect")) {
                        Utils.showMessage(
                            getApplicationContext(), login, getString(R.string.error_sk), false);
                      } else {
                        Utils.showMessage(
                            getApplicationContext(), login, getString(R.string.failed), false);
                      }
                    } catch (JSONException je) {
                      Toast
                          .makeText(getApplicationContext(), getString(R.string.data_error),
                              Toast.LENGTH_SHORT)
                          .show();
                    }
                  });
            }

          } else {
            Toast
                .makeText(
                    getApplicationContext(), getString(R.string.sk_required), Toast.LENGTH_SHORT)
                .show();
          }
        });
    builder.setNeutralButton(
        HtmlCompat.fromHtml("<font color='#848482'>" + getString(R.string.cancel) + "</font>",
            HtmlCompat.FROM_HTML_MODE_LEGACY),
        (dialog, which) -> dialog.cancel());
    AlertDialog dialogRecovery = builder.create();
    dialogRecovery.show();
  }

  public void chooseLang(View v) {
    showLangList(v);
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
            output = new FileOutputStream(new File(getFilesDir(), "OneX.apk"));
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
            ssb.setSpan(new Clickables(myapp, new String[] {strr}, 0, string -> {
              if (string.equals(strr)) {
                try {
                  Utils.copyApkToExternal(getApplicationContext());
                  final File appfile =
                      new File(getExternalFilesDir(null) + File.separator + "OneX.apk");
                  if (appfile.exists()) {
                    try {
                      Uri apkUri;
                      Intent intent;
                      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        apkUri =
                            FileProvider.getUriForFile(Signin.this, "one.x.fileprovider", appfile);
                        intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                        intent.setData(apkUri);
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                      } else {
                        apkUri = Uri.fromFile(appfile);
                        intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                      }
                      startActivity(intent);
                    } catch (Exception e) {
                      Utils.installUpdate(getApplicationContext());
                    }
                  } else {
                    Toast
                        .makeText(getApplicationContext(), getString(R.string.error_file),
                            Toast.LENGTH_SHORT)
                        .show();
                  }
                } catch (Exception e) {
                  Toast
                      .makeText(getApplicationContext(), getString(R.string.error_file),
                          Toast.LENGTH_SHORT)
                      .show();
                }
              }
            }, Color.GREEN), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
        Intent inst = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                          .setData(Uri.parse("package:one.x"));
        getInstallPermResult.launch(inst);
      }
    }
    downloadApp(ONEX.LATEST_APK);
  }

  private void onClick(View c) {
    final Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
    final ActivityOptions option =
        ActivityOptions.makeCustomAnimation(Signin.this, R.anim.nav_enter, R.anim.nav_exit);
    final String acc = account.getText().toString();
    final String psd = pswd.getText().toString();
    if (!isDownloading) {
      if (account.length() > 0 && pswd.length() > 0) {
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
              startActivity(intent, option.toBundle());
            } else {
              showLoginConfirm(acc, psd, intent);
            }
          } else {
            if (Acc.equals("A-A00Z")) {
              // check if credentials correct
              Utils.connectToServer(Signin.this, ONEX.SIGNIN,
                  new String[] {"user", "pswd", "tkn", "recon", "sk"},
                  new String[] {acc, psd, Utils.getTkn(getApplicationContext()), "0", "no"}, true,
                  response -> {
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
                            startActivity(intent, option.toBundle());
                          }
                        } else if (msg.equals("incorrect auth")) {
                          Utils.showMessage(getApplicationContext(), login,
                              getString(R.string.error_pswd), false);
                        } else {
                          if (msg.contains("forbidden")) {
                            showLoginConfirm(acc, psd, intent);
                          } else {
                            Utils.showMessage(getApplicationContext(), login,
                                getString(R.string.account_null), false);
                          }
                        }
                      } catch (JSONException je) {
                        Toast
                            .makeText(getApplicationContext(), getString(R.string.data_error),
                                Toast.LENGTH_SHORT)
                            .show();
                      }
                    } else {
                      Toast
                          .makeText(getApplicationContext(), getString(R.string.connect_error),
                              Toast.LENGTH_SHORT)
                          .show();
                    }
                  });
            } else {
              if (status == 0) {
                Toast
                    .makeText(getApplicationContext(), getString(R.string.notValidAccount),
                        Toast.LENGTH_SHORT)
                    .show();
              } else {
                Toast
                    .makeText(
                        getApplicationContext(), getString(R.string.error_pswd), Toast.LENGTH_SHORT)
                    .show();
              }
            }
          }
        }
      } else {
        Toast
            .makeText(
                getApplicationContext(), getString(R.string.check_entries), Toast.LENGTH_SHORT)
            .show();
      }

    } else {
      Toast.makeText(getApplicationContext(), getString(R.string.app_busy), Toast.LENGTH_SHORT)
          .show();
    }
  }

  private void showLoginConfirm(final String acct, final String psd, final Intent intent) {
    AlertDialog.Builder builder =
        new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme_Dialog));
    final View customLayout = getLayoutInflater().inflate(R.layout.entersk, null, false);
    builder.setView(customLayout);
    builder.setCancelable(false);
    final EditText edtSk = customLayout.findViewById(R.id.skEdt);
    edtSk.requestFocus();
    builder.setPositiveButton(HtmlCompat.fromHtml("<font color='yellow'>"
                                      + "Ok"
                                      + "</font>",
                                  HtmlCompat.FROM_HTML_MODE_LEGACY),
        (arg0, arg1) -> {
          final ActivityOptions option =
              ActivityOptions.makeCustomAnimation(Signin.this, R.anim.nav_enter, R.anim.nav_exit);
          final String sk = edtSk.getText().toString();
          if (!sk.isEmpty()) {
            if (Utils.isConnectionAvailable(getApplicationContext()) == false) {
              Utils.showNoConnectionAlert(getApplicationContext(), login);
            } else {
              arg0.dismiss();
              Utils.connectToServer(Signin.this, ONEX.SIGNIN,
                  new String[] {"user", "pswd", "tkn", "recon", "sk"},
                  new String[] {acct, psd, Utils.getTkn(getApplicationContext()), "3", sk}, true,
                  response -> {
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
                            startActivity(intent, option.toBundle());
                          }
                        } else if (msg.contains("incorrect")) {
                          if (msg.contains("secret key")) {
                            Utils.showMessage(getApplicationContext(), login,
                                getString(R.string.error_sk), false);
                          } else {
                            Utils.showMessage(getApplicationContext(), login,
                                getString(R.string.error_pswd), false);
                          }
                        } else {
                          if (msg.contains("forbidden")) {
                            showAccountRecovery();
                          } else {
                            Utils.showMessage(getApplicationContext(), login,
                                getString(R.string.account_null), false);
                          }
                        }
                      } catch (JSONException je) {
                        Toast
                            .makeText(getApplicationContext(), getString(R.string.data_error),
                                Toast.LENGTH_SHORT)
                            .show();
                      }
                    }
                  });
            }

          } else {
            Toast
                .makeText(
                    getApplicationContext(), getString(R.string.sk_required), Toast.LENGTH_SHORT)
                .show();
          }
        });
    builder.setNeutralButton(
        HtmlCompat.fromHtml("<font color='#848482'>" + getString(R.string.cancel) + "</font>",
            HtmlCompat.FROM_HTML_MODE_LEGACY),
        (dialog, which) -> dialog.cancel());
    AlertDialog adialog = builder.create();
    adialog.show();
  }

  private void showLangList(View anchorv) {
    ArrayList<String> listLang = new ArrayList<>();
    listLang.add("EN");
    listLang.add("FR");
    listLang.add("IT");
    listLang.add("PT");
    listLang.add("ES");
    listLang.add("NL");
    listLang.add("DE");
    listLang.add("SW");
    Collections.sort(listLang);
    String clang = CurrentLang.getText().toString();
    listLang.remove(clang);
    ContextThemeWrapper theme = new ContextThemeWrapper(this, android.R.style.Theme_Material_Light);
    ListPopupWindow listlang = new ListPopupWindow(theme);
    listlang.setAdapter(new ArrayAdapter<String>(this, R.layout.popup_textview_lang, listLang));
    listlang.setAnchorView(anchorv);
    int wid = anchorv.getWidth();
    listlang.setWidth(wid - ((int) wid / 8));
    listlang.setHeight(ListPopupWindow.WRAP_CONTENT);
    listlang.setOnItemClickListener((parent, view, position, id) -> {
      String l = listLang.get(position);
      listlang.dismiss();
      if (!isDownloading) {
        setLocale(l.toLowerCase());
      } else {
        Toast.makeText(getApplicationContext(), getString(R.string.app_busy), Toast.LENGTH_SHORT)
            .show();
      }
    });
    listlang.show();
  }

  public void setLocale(String localeName) {
    if (!localeName.equals(currentLan)) {
      Context context = LocaleHelper.setLocale(this, localeName);
      Resources res = context.getResources();
      DisplayMetrics dm = res.getDisplayMetrics();
      Configuration conf = res.getConfiguration();
      conf.locale = new Locale(localeName);
      res.updateConfiguration(conf, dm);
      Intent refresh = new Intent(this, Signin.class);
      refresh.putExtra("lang", localeName);
      refresh.putExtra("loc", ONEX.COUNTRY);
      refresh.putExtra("curr", ONEX.CURRENCY);
      startActivity(refresh);
    }
  }
}
