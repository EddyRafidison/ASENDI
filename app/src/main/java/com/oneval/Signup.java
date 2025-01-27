package com.oneval;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.text.HtmlCompat;

import com.developer.filepicker.model.DialogConfigs;
import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;

import org.json.JSONException;

import java.io.File;

public class Signup extends AppCompatActivity {
    private static final int REQUEST_STORAGE_PERMISSIONS = 123;
    private static final int REQUEST_MEDIA_PERMISSIONS = 456;
    private final DialogProperties properties = new DialogProperties();
    private final String readPermission = android.Manifest.permission.READ_EXTERNAL_STORAGE;
    private final String writePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private FilePickerDialog filePickerDialog;
    private final ActivityResultLauncher<Intent> getPermResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        filePickerDialog.show();
                    }
                }
            });
    private EditText fullname,
            birthdate,
            address,
            cin,
            email,
            pswd1,
            pswd2,
            sk;
    private ImageButton cin1,
            cin2;
    private CheckBox check;
    private Button signup;
    private int cin_code = 0;
    private String cin_bytes_1 = "",
            cin_bytes_2 = "";

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
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.TYPE_STATUS_BAR);
        }
        w.setNavigationBarColor(getResources().getColor(R.color.primary));
        setContentView(R.layout.signup);
        overridePendingTransition(R.anim.nav_enter, R.anim.nav_exit);
        CheckNetwork network_ = new CheckNetwork(getApplicationContext());
        network_.registerNetworkCallback();
        fullname = findViewById(R.id.fullname);
        birthdate = findViewById(R.id.birthday);
        address = findViewById(R.id.address);
        cin = findViewById(R.id.cin);
        email = findViewById(R.id.mailAddress);
        pswd1 = findViewById(R.id.new_Pswd);
        pswd2 = findViewById(R.id.new_Pswd2);
        sk = findViewById(R.id.new_Sk);
        cin1 = findViewById(R.id.cin_1);
        cin2 = findViewById(R.id.cin2);
        check = findViewById(R.id.checkbox);
        signup = findViewById(R.id.Signup);
        TextView textCheck = findViewById(R.id.textCheck);
        DrawableCompat.setTint(signup.getBackground(), Color.parseColor("#D32F2F"));
        fullname.requestFocus();
        fullname.setAllCaps(true);

        String[] strs = new String[]{
                getString(R.string.terms).toLowerCase(),
                getString(R.string.privacy).toLowerCase()};
        final String t = getString(R.string.notice_terms_policy);
        final String tlc = t.toLowerCase();
        SpannableStringBuilder ss = new SpannableStringBuilder(t);
        for (int i = 0; i < strs.length; i++) {
            String s = strs[i];
            int start = tlc.indexOf(s);
            int end = tlc.lastIndexOf(s) + s.length();
            ss.setSpan(new Clickables(textCheck, strs, i, string -> {
                Intent browserIntent;
                if (string.equals(strs[0])) {
                    //show terms of use
                    browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ONEVAL.INFO + "?r=terms" + "&l=" + ONEVAL.TPLANG));
                } else {
                    //show policy
                    browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ONEVAL.INFO + "?r=privacy" + "&l=" + ONEVAL.TPLANG));
                }
                startActivity(browserIntent);
            },
                    Color.MAGENTA), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        textCheck.setText(ss, TextView.BufferType.SPANNABLE);
        textCheck.setMovementMethod(LinkMovementMethod.getInstance());
        textCheck.setHighlightColor(Color.YELLOW);
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
        //If you want to view files with specific type of extensions the pass string array to properties.extensions
        properties.extensions = new String[]{
                "png", "jpg", "webp", "jpeg"
        };
        properties.show_hidden_files = false;
        filePickerDialog = new FilePickerDialog(this, properties);
        filePickerDialog.setTitle(getString(R.string.select_file));
        filePickerDialog.setPositiveBtnName(HtmlCompat.fromHtml("<font color='green'>" + getString(R.string.selection) + "</font>",
                HtmlCompat.FROM_HTML_MODE_LEGACY));
        filePickerDialog.setCancelable(true);
        filePickerDialog.setNegativeBtnName(getString(R.string.cancel));
        filePickerDialog.setDialogSelectionListener(files -> {
            //files is the array of the paths of files selected by the Application User.
            if (cin_code == 1) {
                String cin_path_1 = files[0];
                Bitmap cin1b = BitmapFactory.decodeFile(cin_path_1);
                cin_bytes_1 = Utils.imageToString(cin1b);
                cin1.setImageBitmap(cin1b);

            } else {
                String cin_path_2 = files[0];
                Bitmap cin2b = BitmapFactory.decodeFile(cin_path_2);
                cin_bytes_2 = Utils.imageToString(cin2b);
                cin2.setImageBitmap(cin2b);

            }
        });
        signup.setOnClickListener(v -> {
            String fulln = fullname.getText().toString();
            String birth = birthdate.getText().toString();
            String addrs = address.getText().toString();
            String Cin = cin.getText().toString();
            String mailAd = email.getText().toString();
            String psd1 = pswd1.getText().toString();
            String psd2 = pswd2.getText().toString();
            String secretkey = sk.getText().toString();
            if (!fulln.isEmpty()) {
                if (!birth.isEmpty()) {
                    if (!addrs.isEmpty()) {
                        if (!Cin.isEmpty()) {
                            if (!mailAd.isEmpty()) {
                                if (!cin_bytes_1.isEmpty()) {
                                    if (!cin_bytes_2.isEmpty()) {
                                        if (!psd1.isEmpty()) {
                                            if (!psd2.isEmpty()) {
                                                if (!secretkey.isEmpty()) {
                                                    if (psd1.equals(psd2)) {
                                                        if (psd1.length() >= 4) {
                                                            if (!check.isChecked()) {
                                                                Toast.makeText(getApplicationContext(), getString(R.string.check_termsPolicy), Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                if (Utils.isConnectionAvailable(getApplicationContext()) == false) {
                                                                    Utils.showNoConnectionAlert(getApplicationContext(), signup);
                                                                } else {
                                                                    //process signup
                                                                    Utils.connectToServer(Signup.this, ONEVAL.SIGNUP, new String[]{
                                                                                    "email", "birth", "addr", "name", "cin", "pswd",
                                                                                    "sk", "cinimg1", "cinimg2"
                                                                            },
                                                                            new String[]{
                                                                                    mailAd, birth, addrs, fulln, Cin, psd1, secretkey, cin_bytes_1, cin_bytes_2
                                                                            }, true, response -> {
                                                                                if (response != null) {
                                                                                    try {
                                                                                        String msg = response.getString("msg");
                                                                                        if (msg.equals("ok")) {
                                                                                            signup.setClickable(false);
                                                                                            Utils.showMessage(getApplicationContext(), signup, getString(R.string.check_signup_mail), true);
                                                                                        } else if (msg.equals("retry")) {
                                                                                            Toast.makeText(getApplicationContext(), getString(R.string.retry_signup), Toast.LENGTH_SHORT).show();
                                                                                        } else if (msg.contains("unsupported")) {
                                                                                            Toast.makeText(getApplicationContext(), getString(R.string.unsupported_country), Toast.LENGTH_SHORT).show();
                                                                                        } else if (msg.contains("what")) {
                                                                                            Toast.makeText(getApplicationContext(), getString(R.string.country_null), Toast.LENGTH_SHORT).show();
                                                                                        } else {
                                                                                            Toast.makeText(getApplicationContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    } catch (
                                                                                            JSONException je) {
                                                                                        Toast.makeText(getApplicationContext(), getString(R.string.data_error), Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        } else {
                                                            Toast.makeText(getApplicationContext(), getString(R.string.tooShortPswd), Toast.LENGTH_SHORT).show();
                                                        }
                                                    } else {
                                                        Toast.makeText(getApplicationContext(), getString(R.string.not_matched_pswd), Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    Toast.makeText(getApplicationContext(), getString(R.string.check_entries), Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(getApplicationContext(), getString(R.string.check_entries), Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(getApplicationContext(), getString(R.string.check_entries), Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(), getString(R.string.check_entries), Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), getString(R.string.check_entries), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), getString(R.string.check_entries), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.check_entries), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.check_entries), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.check_entries), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.check_entries), Toast.LENGTH_SHORT).show();
            }

        });
        cin1.setOnClickListener(v -> {
            cin_code = 1;
            checkPermissions();
        });
        cin2.setOnClickListener(v -> {
            cin_code = 2;
            checkPermissions();
        });
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //As the device is Android 13 and above so I want the permission of accessing Audio, Images, Videos
            //You can ask permission according to your requirements what you want to access.
            String imagesPermission = android.Manifest.permission.READ_MEDIA_IMAGES;
            //Check for permissions and request them if needed
            if (
                    ContextCompat.checkSelfPermission(this, imagesPermission) == PackageManager.PERMISSION_GRANTED) {
                // You have the permissions, you can proceed with your media file operations.
                //Showing dialog when Show Dialog button is clicked.
                filePickerDialog.show();
            } else {
                // You don't have the permissions. Request them.
                ActivityCompat.requestPermissions(this, new String[]{
                        imagesPermission
                }, REQUEST_MEDIA_PERMISSIONS);
            }
        } else {
            //Android version is below 13 so we are asking normal read and write storage permissions
            // Check for permissions and request them if needed
            if (ContextCompat.checkSelfPermission(this, readPermission) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, writePermission) == PackageManager.PERMISSION_GRANTED) {
                // You have the permissions, you can proceed with your file operations.
                // Show the file picker dialog when needed
                filePickerDialog.show();
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
                    filePickerDialog.show();
                }
            } else {
                // Permissions were denied. Show a rationale dialog or inform the user about the importance of these permissions.
                showRationaleDialog();
            }
        }

        //This conditions only works on Android 13 and above versions
        if (requestCode == REQUEST_MEDIA_PERMISSIONS) {
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
                    .setNeutralButton(HtmlCompat.fromHtml("<font color='red'>" + getString(R.string.cancel) + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY), (dialog, which) -> {
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
        new AlertDialog.Builder(Signup.this)
                .setTitle(getString(R.string.requestPermTitle))
                .setMessage(getString(R.string.requestPermText))
                .setPositiveButton(HtmlCompat.fromHtml("<font color='yellow'>" + "Ok" + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY), (dialog, which) -> {
                    // Request permissions when the user clicks OK.
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:com.oneval"));
                    getPermResult.launch(intent);
                })
                .setNeutralButton(HtmlCompat.fromHtml("<font color='red'>" + getString(R.string.cancel) + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY), (dialog, which) -> {
                    dialog.dismiss();
                    // Handle the case where the user cancels the permission request.
                })
                .show();
    }

}