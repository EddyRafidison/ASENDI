package com.oneval;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import com.developer.filepicker.model.DialogConfigs;
import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;
import com.elyeproj.loaderviewlibrary.LoaderTextView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.wavetopsheet.TopSheetBehavior;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HomeContentFragment extends Fragment {
  private final List<HistoryItem> history = new ArrayList<>();
  private View topSheet;
  private View rel;
  private View rel_;
  private BottomSheetBehavior<View> behavior;
  private TopSheetBehavior<View> tsb;
  private ImageView topsheet_arrow;
  private ImageView topsheet_profile;
  private ImageView qr_dest;
  private ImageView bottomsheet_arrow;
  private ImageView bottomsheet_transfer;
  private boolean isTopsheetToOpen = false, isBottomSheetToOpen = false, isPbtoShow = false,
                  isBSopen = false;
  private TextView user_id, stat;
  private LoaderTextView stock;
  private EditText user_dest, value;
  private FloatingActionButton go;
  private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
      registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() == null) {
          Intent originalIntent = result.getOriginalIntent();
          if (originalIntent == null) {
          } else if (originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
            Toast
                .makeText(getActivity(), requireContext().getString(R.string.write_storage_missing),
                    Toast.LENGTH_LONG)
                .show();
          }
        } else {
          user_dest.setText(result.getContents());
          value.requestFocus();
        }
      });
  private static final int REQUEST_STORAGE_PERMISSIONS = 125;
  private static final int REQUEST_MEDIA_PERMISSIONS = 476;
  private final DialogProperties properties = new DialogProperties();
  private final String readPermission = android.Manifest.permission.READ_EXTERNAL_STORAGE;
  private final String writePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
  private FilePickerDialog filePickerDialog;
  private final ActivityResultLauncher<Intent> getPermResult = registerForActivityResult(
      new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<>() {
        @Override
        public void onActivityResult(ActivityResult result) {
          if (result.getResultCode() == Activity.RESULT_OK) {
            filePickerDialog.show();
          }
        }
      });
  private String user;
  private String pswd;
  private String user_;
  private String Fees = "?";
  private ListView historylist;
  private int days = 0, LastHistSize = 0, cursor = 0;
  private DecimalFormat df;
  private Bitmap dest_qr = null;

  @SuppressLint("ResourceAsColor")
  @Override
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.home_fragment, container, false);
    View bottomSheet = layout.findViewById(R.id.design_bottom_sheet);
    behavior = BottomSheetBehavior.from(bottomSheet);
    topSheet = layout.findViewById(R.id.top_sheet);
    tsb = TopSheetBehavior.from(topSheet);
    rel = layout.findViewById(R.id.rel);
    topsheet_arrow = layout.findViewById(R.id.topsheet_arrow);
    topsheet_profile = layout.findViewById(R.id.topsheet_profile);
    final ImageView qr = layout.findViewById(R.id.qr);
    Spinner filter_history = layout.findViewById(R.id.filter_history);
    user_id = layout.findViewById(R.id.user_id);
    qr_dest = layout.findViewById(R.id.dest_qr);
    rel_ = layout.findViewById(R.id.rel_);
    stat = layout.findViewById(R.id.stat);
    go = layout.findViewById(R.id.go);
    View v = requireActivity().findViewById(R.id.empty_data_layout);
    if (v != null) {
      v.setVisibility(View.INVISIBLE);
    }

    historylist = layout.findViewById(R.id.history_list);
    user = Utils.getAccount(requireContext());
    final Bitmap user_qr = Utils.qr(requireActivity(), user);
    qr.setImageBitmap(user_qr);
    qr_dest.setImageBitmap(user_qr);

    qr_dest.setOnLongClickListener(v6 -> {
      Vibrator vib = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);
      if (vib != null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vib.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
          vib.vibrate(50);
        }
      }
      String user_d = user_dest.getText().toString();
      if (user_d.length() > 3) {
        Utils.saveToDownloads(requireActivity(), Utils.resizeBitmap(dest_qr), user_d);
      } else {
        Toast
            .makeText(requireActivity(), requireActivity().getString(R.string.no_dest),
                Toast.LENGTH_SHORT)
            .show();
      }
      return true;
    });

    rel.setAlpha(0);
    rel_.setAlpha(0);
    DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance();
    decimalFormatSymbols.setDecimalSeparator('.');
    df = new DecimalFormat("0.00", decimalFormatSymbols);
    Bundle bundle = getArguments();
    assert bundle != null;
    pswd = bundle.getString("psd");

    List<String> strs = new ArrayList<>();
    strs.add(getString(R.string.today));
    strs.add(getString(R.string.last_3_days));
    strs.add(getString(R.string.last_week));
    strs.add(getString(R.string.last_month));
    ArrayAdapter<String> arr =
        new ArrayAdapter<>(requireContext(), R.layout.textview_spinner, strs);
    filter_history.setAdapter(arr);
    filter_history.setSelection(0);
    bottomsheet_arrow = layout.findViewById(R.id.bottomsheet_arrow);
    bottomsheet_transfer = layout.findViewById(R.id.bottomsheet_send);
    MaterialButton send = layout.findViewById(R.id.sendbutton);
    ImageButton scan_but = layout.findViewById(R.id.scan_button);

    stock = layout.findViewById(R.id.balance);
    Typeface tf = Typeface.createFromAsset(requireActivity().getAssets(), "fonts/SuperstarM54.ttf");
    stock.setTypeface(tf);

    user_dest = layout.findViewById(R.id.user_id_dest);
    user_dest.requestFocus();
    user_dest.setAllCaps(true);
    value = layout.findViewById(R.id.value);
    final ImageButton copy = layout.findViewById(R.id.copy);
    DrawableCompat.setTint(copy.getBackground(), Color.parseColor("#ABC1B6"));
    TextView group = layout.findViewById(R.id.user_group);
    user_id.setText(user);
    refresh();
    final String grp = Utils.getCategory(requireContext());
    String user_gr = null;
    if (grp.equals("C")) {
      user_gr = "Partner";
    } else if (grp.equals("B")) {
      user_gr = "Consumer";
    }
    group.setText(user_gr);

    qr.setOnLongClickListener(v5 -> {
      Vibrator vib = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);
      if (vib != null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vib.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
          vib.vibrate(50);
        }
      }
      Utils.saveToDownloads(requireActivity(), Utils.resizeBitmap(user_qr), user);
      return true;
    });

    send.setOnClickListener(v1 -> {
      String val = value.getText().toString();
      if (user_dest.length() > 2) {
        if (val.length() > 1) {
          if (val.startsWith(".")) {
            val = "0".concat(val);
          } else if (val.endsWith(".")) {
            val = val.concat("0");
          }
          if (Double.parseDouble(val) >= 100) {
            showPasswordConfirm();
          } else {
            Toast.makeText(getActivity(), getString(R.string.check_entries), Toast.LENGTH_SHORT)
                .show();
          }
        } else {
          Toast.makeText(getActivity(), getString(R.string.check_entries), Toast.LENGTH_SHORT)
              .show();
        }
      } else {
        Toast.makeText(getActivity(), getString(R.string.check_entries), Toast.LENGTH_SHORT).show();
      }
    });

    properties.selection_mode = DialogConfigs.SINGLE_MODE;
    properties.selection_type = DialogConfigs.FILE_SELECT;
    properties.root = new File(DialogConfigs.DEFAULT_DIR);
    properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
    properties.offset = new File(DialogConfigs.DEFAULT_DIR);
    properties.extensions = new String[] {"png", "jpg", "webp", "jpeg"};
    properties.show_hidden_files = false;
    filePickerDialog = new FilePickerDialog(requireActivity(), properties);
    filePickerDialog.setTitle(getString(R.string.select_file));
    filePickerDialog.setPositiveBtnName(
        HtmlCompat.fromHtml("<font color='green'>" + getString(R.string.selection) + "</font>",
            HtmlCompat.FROM_HTML_MODE_LEGACY));
    filePickerDialog.setCancelable(true);
    filePickerDialog.setNegativeBtnName(getString(R.string.cancel));
    filePickerDialog.setDialogSelectionListener(files -> {
      // files is the array of the paths of files selected by the Application User.
      String path = files[0];
      Bitmap qrc = BitmapFactory.decodeFile(path);
      user_dest.setText(Utils.getDestQRCode(requireContext(), qrc));
      if (user_dest.length() > 0) {
        value.requestFocus();
      } else {
        user_dest.requestFocus();
      }
    });

    filter_history.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
          case 0:
            // history today (default)
            if (Utils.isConnectionAvailable(requireContext()) == false) {
              Utils.showNoConnectionAlert(requireContext(), rel);

            } else {
              if (days == 0) {
                isPbtoShow = false;
              } else {
                days = 0;
                isPbtoShow = true;
              }
            }
            break;
          case 1:
            // history last 3 days
            if (Utils.isConnectionAvailable(requireContext()) == false) {
              Utils.showNoConnectionAlert(requireContext(), rel);

            } else {
              days = 3;
              isPbtoShow = true;
            }
            break;
          case 2:
            // history last 7 days
            if (Utils.isConnectionAvailable(requireContext()) == false) {
              Utils.showNoConnectionAlert(requireContext(), rel);

            } else {
              days = 7;
              isPbtoShow = true;
            }
            break;
          case 3:
            // history 30 days
            if (Utils.isConnectionAvailable(requireContext()) == false) {
              Utils.showNoConnectionAlert(requireContext(), rel);

            } else {
              days = 30;
              isPbtoShow = true;
            }
            break;
          default:
            return;
        }
        Utils.connectToServer(getActivity(), ONEVAL.TRANSREC,
            new String[] {"user", "pswd", "days", "tkn"},
            new String[] {user, pswd, String.valueOf(days), Utils.getTkn(requireContext())},
            isPbtoShow, response -> {
              history.clear();
              try {
                JSONArray jSONArray = response.getJSONArray("trans");
                for (int i = 0; i < jSONArray.length(); i++) {
                  JSONObject obj = jSONArray.getJSONObject(i);
                  String sen = obj.getString("sender");
                  String rec = obj.getString("receiver");
                  String type;
                  String amoun = obj.getString("amount");
                  String price = obj.getString("unit_price");
                  final double p = Double.parseDouble(price);
                  String refer = obj.getString("reference");
                  String deliver_date = obj.getString("deliver_date");
                  String deliver_time = obj.getString("deliver_time");
                  String transfees = df.format(Double.parseDouble(obj.getString("fees")));
                  String Amoun = df.format(Double.parseDouble(amoun) * p);

                  if (user.equals(sen)) {
                    user_ = rec;
                    type = "1";
                  } else {
                    user_ = sen;
                    type = "0";
                  }

                  history.add(new HistoryItem(getContext(), deliver_date + deliver_time, Amoun,
                      type, user_, refer, transfees));
                }
                Utils.connectToServer(getActivity(), ONEVAL.BALANCE,
                    new String[] {"user", "pswd", "tkn"},
                    new String[] {user, pswd, Utils.getTkn(requireContext())}, false, resp -> {
                      try {
                        String bal = resp.getString("msg");
                        if (!bal.equals("error")) {
                          Fees = resp.getString("fees");
                          stock.setText(df.format(Double.parseDouble(bal)));
                        } else {
                          Toast
                              .makeText(getContext(),
                                  requireActivity().getString(R.string.connect_error),
                                  Toast.LENGTH_SHORT)
                              .show();
                        }
                      } catch (JSONException e) {
                        Toast
                            .makeText(getContext(),
                                requireActivity().getString(R.string.data_error),
                                Toast.LENGTH_SHORT)
                            .show();
                      }
                    });

              } catch (JSONException je) {
                Toast
                    .makeText(getActivity(), requireActivity().getString(R.string.data_error),
                        Toast.LENGTH_SHORT)
                    .show();
              }
              ArrayAdapter<HistoryItem> la = new HistoryAdapter(getActivity(), history);
              historylist.setAdapter(la);
            });
      } // to close the onItemSelected

      public void onNothingSelected(AdapterView<?> parent) {}
    });
    user_dest.addTextChangedListener(new TextWatcher() {
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        final String s_ = String.valueOf(s);

        if (s.length() > 0) {
          stock.setText(stockMasker());
          stock.setTransformationMethod(new StockHiding());
          dest_qr = Utils.qr(requireActivity(), s_);
          if (dest_qr != null) {
            qr_dest.setImageBitmap(dest_qr);
          }
        } else {
          stock.setText(stockShower());
          stock.setTransformationMethod(null);
          qr_dest.setImageBitmap(Utils.qr(requireActivity(), user));
        }
        // TODO Auto-generated method stub
      }

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // TODO Auto-generated method stub
      }

      @Override
      public void afterTextChanged(Editable s) {
        // TODO Auto-generated method stub
      }
    });

    value.addTextChangedListener(new TextWatcher() {
      public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

      public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

      public void afterTextChanged(Editable arg0) {
        String str = value.getText().toString();
        if (str.isEmpty())
          return;
        String str2 = filterDecimal(str);
        if (!str2.equals(str)) {
          cursor = value.getSelectionEnd();
          value.setText(str2);
          try {
            value.setSelection(cursor);
          } catch (Exception e) {
            value.setSelection(cursor - 1);
          }
        }
      }
    });

    go.setOnClickListener(
        v4 -> { Toast.makeText(getActivity(), "go", Toast.LENGTH_SHORT).show(); });

    copy.setOnClickListener(v2 -> {
      ClipboardManager clipboard =
          (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
      ClipData clip = ClipData.newPlainText("oneval_user_id", user_id.getText().toString());
      clipboard.setPrimaryClip(clip);
      Toast.makeText(getActivity(), getString(R.string.copy), Toast.LENGTH_SHORT).show();
    });

    scan_but.setOnClickListener(v3 -> {
      ScanOptions options = new ScanOptions();
      options.setCaptureActivity(BarcodeScanner.class);
      options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
      options.setOrientationLocked(false);
      options.setBeepEnabled(false);
      barcodeLauncher.launch(options);
    });

    scan_but.setOnLongClickListener(v7 -> {
      Vibrator vib = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);
      if (vib != null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vib.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
          vib.vibrate(50);
        }
      }
      checkPermissions();
      return true;
    });

    tsb.setTopSheetCallback(new TopSheetBehavior.TopSheetCallback() {
      @Override
      public void onStateChanged(@NonNull View top_Sheet, int newState) {
        switch (newState) {
          case TopSheetBehavior.STATE_DRAGGING:
          case TopSheetBehavior.STATE_SETTLING:
          case TopSheetBehavior.STATE_HIDDEN:

            break;
          case TopSheetBehavior.STATE_EXPANDED:
            topsheet_arrow.setImageResource(R.drawable.arrow_up);
            topsheet_profile.setImageResource(R.drawable.history);

            break;
          case TopSheetBehavior.STATE_COLLAPSED:
            topsheet_arrow.setImageResource(R.drawable.arrow_down);
            topsheet_profile.setImageResource(R.drawable.profile);
            break;
        }
      }

      @Override
      public void onSlide(@NonNull View topSheet, float slideOffset) {
        rel.setAlpha(slideOffset * 2);
        if (slideOffset > 0) {
          if (slideOffset >= 0.5) {
            if (isBottomSheetToOpen) {
              isBottomSheetToOpen = false;
              behavior.setHideable(true);
              behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }

          } else {
            if (!isBottomSheetToOpen) {
              isBottomSheetToOpen = true;
              behavior.setHideable(false);
              behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
          }
        } else {
          isBottomSheetToOpen = false;
        }
      }
    });
    behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
      @Override
      public void onStateChanged(@NonNull View bottomSheet, int newState) {
        switch (newState) {
          case BottomSheetBehavior.STATE_DRAGGING:
          case BottomSheetBehavior.STATE_SETTLING:
          case BottomSheetBehavior.STATE_HIDDEN:
          case BottomSheetBehavior.STATE_HALF_EXPANDED:

            break;
          case BottomSheetBehavior.STATE_EXPANDED:
            bottomsheet_arrow.setImageResource(R.drawable.arrow_down);
            bottomsheet_transfer.setImageResource(R.drawable.history);
            if (user_dest.length() > 0) {
              stock.setText(stockMasker());
              stock.setTransformationMethod(new StockHiding());
            }
            isBSopen = true;
            break;
          case BottomSheetBehavior.STATE_COLLAPSED:
            bottomsheet_arrow.setImageResource(R.drawable.arrow_up);
            bottomsheet_transfer.setImageResource(R.drawable.transfer);
            if (stock.length() > 0) {
              stock.setText(stockShower());
              stock.setTransformationMethod(null);
            }
            isBSopen = false;
            Utils.hideSoftKeyboard(requireActivity());
            break;
        }
      }

      @Override
      public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        rel_.setAlpha(slideOffset * 2);
        if (slideOffset > 0) {
          if (slideOffset >= 0.5) {
            if (isTopsheetToOpen) {
              isTopsheetToOpen = false;
              tsb.setHideable(true);
              tsb.setState(TopSheetBehavior.STATE_HIDDEN);
            }

          } else {
            if (!isTopsheetToOpen) {
              isTopsheetToOpen = true;
              tsb.setHideable(false);
              tsb.setState(TopSheetBehavior.STATE_COLLAPSED);
            }
          }
        } else {
          isTopsheetToOpen = false;
        }
      }
    });
    // process data loading (else history) here
    return layout;
  }

  private String stockMasker() {
    String original = stock.getText().toString();
    StringBuilder filler = new StringBuilder();
    int originallength = original.length();
    if (originallength < 30) {
      int fillable = 30 - originallength;
      for (int i = 0; i < fillable; i++) {
        filler.append("%");
      }
    }
    return original + filler;
  }

  private String stockShower() {
    return stock.getText().toString().replaceAll("%", "");
  }

  private void showPasswordConfirm() {
    AlertDialog.Builder builder =
        new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AppTheme_Dialog));
    final View customLayout = getLayoutInflater().inflate(R.layout.pswd_dialog, null, false);
    builder.setView(customLayout);
    builder.setCancelable(false);
    final EditText edtpswd = customLayout.findViewById(R.id.pswdDialog);
    final TextView td = customLayout.findViewById(R.id.tdialog);
    String ftext = requireActivity().getString(R.string.fees_notice).replace("4", Fees) + " "
        + requireActivity().getString(R.string.enter_pswd);
    td.setText(ftext);
    edtpswd.requestFocus();
    builder.setPositiveButton(HtmlCompat.fromHtml("<font color='yellow'>"
                                      + "Ok"
                                      + "</font>",
                                  HtmlCompat.FROM_HTML_MODE_LEGACY),
        (arg0, arg1) -> {
          final String ps = edtpswd.getText().toString();
          final String des = user_dest.getText().toString();
          final String am = value.getText().toString();
          if (!ps.isEmpty()) {
            if (Utils.isConnectionAvailable(requireContext()) == false) {
              Utils.showNoConnectionAlert(requireContext(), rel);
            } else {
              arg0.dismiss();
              if (ps.equals(pswd)) {
                Utils.connectToServer(getActivity(), ONEVAL.TRANSFER,
                    new String[] {"sender", "pswd", "dest", "amount", "tkn"},
                    new String[] {user, pswd, des, am, Utils.getTkn(requireContext())}, true,
                    response -> {
                      try {
                        String status = response.getString("transf");
                        switch (status) {
                          case "sent":
                            reloadUpdate();
                            Utils.showMessage(getContext(), topSheet,
                                requireActivity().getString(R.string.successed_transfer), true);
                            break;
                          case "failed":
                            Utils.showMessage(getContext(), topSheet,
                                requireActivity().getString(R.string.failed_transfer), false);
                            break;
                          case "no dest":
                            Utils.showMessage(getContext(), topSheet,
                                requireActivity().getString(R.string.no_dest), false);
                            break;
                          case "no exp":
                            Utils.showMessage(getContext(), topSheet,
                                requireActivity().getString(R.string.no_exp), false);
                            break;
                          case "unsupported":
                            Utils.showMessage(getContext(), topSheet,
                                requireActivity().getString(R.string.not_allowed), false);
                            break;
                          case "not yet allowed":
                            Utils.showMessage(getContext(), topSheet,
                                requireActivity().getString(R.string.transfer_not_allowed), false);
                            break;
                          default:
                            Utils.showMessage(getContext(), topSheet,
                                requireActivity().getString(R.string.missing_balance), false);
                            break;
                        }
                      } catch (JSONException e) {
                        Toast
                            .makeText(getContext(),
                                requireActivity().getString(R.string.data_error),
                                Toast.LENGTH_SHORT)
                            .show();
                      }
                    });
              } else {
                Toast
                    .makeText(getContext(), requireActivity().getString(R.string.error_pswd),
                        Toast.LENGTH_SHORT)
                    .show();
              }
            }

          } else {
            Toast
                .makeText(getActivity(), requireActivity().getString(R.string.pswd_required),
                    Toast.LENGTH_SHORT)
                .show();
          }
        });
    builder.setNeutralButton(
        HtmlCompat.fromHtml("<font color='#848482'>" + getString(R.string.cancel) + "</font>",
            HtmlCompat.FROM_HTML_MODE_LEGACY),
        (dialog, which) -> dialog.cancel());
    AlertDialog dialogpswd = builder.create();
    dialogpswd.show();
  }

  private void refresh() {
    ONEVAL.TIMER2 = new Timer();
    final Handler handler = new Handler();
    TimerTask doAsynchronousTask = new TimerTask() {
      @Override
      public void run() {
        handler.post(() -> {
          try {
            reloadUpdate();
          } catch (Exception ignored) {
          }
        });
      }
    };
    ONEVAL.TIMER2.schedule(doAsynchronousTask, 15000, 15000);
  }

  private void reloadUpdate() {
    if (ONEVAL.ISCONNECTED) {
      Utils.connectToServer(getActivity(), ONEVAL.TRANSREC,
          new String[] {"user", "pswd", "days", "tkn"},
          new String[] {user, pswd, String.valueOf(days), Utils.getTkn(requireContext())}, false,
          response -> {
            history.clear();
            try {
              JSONArray jSONArray = response.getJSONArray("trans");
              for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject obj = jSONArray.getJSONObject(i);
                String sen = obj.getString("sender");
                String rec = obj.getString("receiver");
                String type;
                String amoun = obj.getString("amount");
                String price = obj.getString("unit_price");
                final double p = Double.parseDouble(price);
                String refer = obj.getString("reference");
                String deliver_date = obj.getString("deliver_date");
                String deliver_time = obj.getString("deliver_time");
                String transfees = df.format(Double.parseDouble(obj.getString("fees")));
                String Amoun = df.format(Double.parseDouble(amoun) * p);

                if (user.equals(sen)) {
                  user_ = rec;
                  type = "1";
                } else {
                  user_ = sen;
                  type = "0";
                }

                history.add(new HistoryItem(getContext(), deliver_date + deliver_time, Amoun, type,
                    user_, refer, transfees));
              }
              Utils.connectToServer(getActivity(), ONEVAL.BALANCE,
                  new String[] {"user", "pswd", "tkn"},
                  new String[] {user, pswd, Utils.getTkn(requireContext())}, false, resp -> {
                    try {
                      String bal = resp.getString("msg");
                      if (!bal.equals("error")) {
                        Fees = resp.getString("fees");
                        stock.setText(df.format(Double.parseDouble(bal)));
                        if (isBSopen) {
                          if (user_dest.length() > 0) {
                            stock.setText(stockMasker());
                            stock.setTransformationMethod(new StockHiding());
                          }
                        }
                      } else {
                        Toast
                            .makeText(getContext(),
                                requireActivity().getString(R.string.connect_error),
                                Toast.LENGTH_SHORT)
                            .show();
                      }
                    } catch (JSONException e) {
                      Toast
                          .makeText(getContext(), requireActivity().getString(R.string.data_error),
                              Toast.LENGTH_SHORT)
                          .show();
                    }
                  });
            } catch (JSONException je) {
              Toast
                  .makeText(getActivity(), requireActivity().getString(R.string.data_error),
                      Toast.LENGTH_SHORT)
                  .show();
            }
            final int hs = history.size();
            if (LastHistSize != hs) {
              LastHistSize = hs;
              ArrayAdapter<HistoryItem> la = new HistoryAdapter(getActivity(), history);
              historylist.setAdapter(la);
            }
          });
    }
  }

  public String filterDecimal(String str) {
    if (str.charAt(0) == '.')
      str = "0" + str;
    int max = str.length();
    StringBuilder rFinal = new StringBuilder();
    boolean after = false;
    int i = 0, up = 0, decimal = 0;
    char t;
    while (i < max) {
      t = str.charAt(i);
      if (t != '.' && !after) {
        up++;
        if (up > 7)
          return rFinal.toString();
      } else if (t == '.') {
        after = true;
      } else {
        decimal++;
        if (decimal > 2)
          return rFinal.toString();
      }
      rFinal.append(t);
      i++;
    }
    return rFinal.toString();
  }

  private void setStat(String text) {
    stat.setText(text);
    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(stat.getText());

    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (c == '|') {
        spannableStringBuilder.setSpan(
            new ForegroundColorSpan(Color.WHITE), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
    }

    String[] symbols = {"#S", "#C", "#Val/C"};
    int syms = symbols.length;
    for (int i = 0; i < syms; i++) {
      String symbol = symbols[i];
      int indexStart = text.indexOf(symbol);
      int length = symbol.length();
      int indexEnd = text.lastIndexOf(symbol) + length;

      spannableStringBuilder.setSpan(new Clickables(stat, symbols, i, string -> {
        String statString = null;
        if (string.equals("#S")) {
          statString = getString(R.string.stat_session);
        } else if (string.equals("#C")) {
          statString = getString(R.string.stat_clicks);
        } else {
          statString = getString(R.string.stat_vpc);
        }
        Toast.makeText(getContext(), statString, Toast.LENGTH_SHORT).show();
      }, Color.MAGENTA), indexStart, indexEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    stat.setText(spannableStringBuilder, TextView.BufferType.SPANNABLE);
  }

  private void checkPermissions() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      // As the device is Android 13 and above so I want the permission of accessing Audio, Images,
      // Videos
      // You can ask permission according to your requirements what you want to access.
      String imagesPermission = android.Manifest.permission.READ_MEDIA_IMAGES;
      // Check for permissions and request them if needed
      if (ContextCompat.checkSelfPermission(requireActivity(), imagesPermission)
          == PackageManager.PERMISSION_GRANTED) {
        // You have the permissions, you can proceed with your media file operations.
        // Showing dialog when Show Dialog button is clicked.
        filePickerDialog.show();
      } else {
        // You don't have the permissions. Request them.
        ActivityCompat.requestPermissions(
            requireActivity(), new String[] {imagesPermission}, REQUEST_MEDIA_PERMISSIONS);
      }
    } else {
      // Android version is below 13 so we are asking normal read and write storage permissions
      // Check for permissions and request them if needed
      if (ContextCompat.checkSelfPermission(requireActivity(), readPermission)
              == PackageManager.PERMISSION_GRANTED
          && ContextCompat.checkSelfPermission(requireActivity(), writePermission)
              == PackageManager.PERMISSION_GRANTED) {
        // You have the permissions, you can proceed with your file operations.
        // Show the file picker dialog when needed
        filePickerDialog.show();
      } else {
        // You don't have the permissions. Request them.
        ActivityCompat.requestPermissions(requireActivity(),
            new String[] {readPermission, writePermission}, REQUEST_STORAGE_PERMISSIONS);
      }
    }
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == REQUEST_STORAGE_PERMISSIONS) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        // Permissions were granted. You can proceed with your file operations.
        // Showing dialog when Show Dialog button is clicked.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          // Android version is 11 and above so to access all types of files we have to give
          // special permission so show user a dialog..
          accessAllFilesPermissionDialog();
        } else {
          // Android version is 10 and below so need of special permission...
          filePickerDialog.show();
        }
      } else {
        // Permissions were denied. Show a rationale dialog or inform the user about the importance
        // of these permissions.
        showRationaleDialog();
      }
    }

    // This conditions only works on Android 13 and above versions
    if (requestCode == REQUEST_MEDIA_PERMISSIONS) {
      if (grantResults.length > 0 && areAllPermissionsGranted(grantResults)) {
        // Permissions were granted. You can proceed with your media file operations.
        // Showing dialog when Show Dialog button is clicked.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          // Android version is 11 and above so to access all types of files we have to give
          // special permission so show user a dialog..
          accessAllFilesPermissionDialog();
        }
      } else {
        // Permissions were denied. Show a rationale dialog or inform the user about the importance
        // of these permissions.
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
    if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), readPermission)
        || ActivityCompat.shouldShowRequestPermissionRationale(
            requireActivity(), writePermission)) {
      // Show a rationale dialog explaining why the permissions are necessary.
      new AlertDialog.Builder(requireActivity())
          .setTitle(getString(R.string.requestPermTitle))
          .setMessage(getString(R.string.requestPermText))
          .setPositiveButton(HtmlCompat.fromHtml("<font color='yellow'>"
                                     + "Ok"
                                     + "</font>",
                                 HtmlCompat.FROM_HTML_MODE_LEGACY),
              (dialog, which) -> {
                // Request permissions when the user clicks OK.
                ActivityCompat.requestPermissions(requireActivity(),
                    new String[] {readPermission, writePermission}, REQUEST_STORAGE_PERMISSIONS);
              })
          .setNeutralButton(
              HtmlCompat.fromHtml("<font color='red'>" + getString(R.string.cancel) + "</font>",
                  HtmlCompat.FROM_HTML_MODE_LEGACY),
              (dialog, which) -> {
                dialog.dismiss();
                // Handle the case where the user cancels the permission request.
              })
          .show();
    } else {
      // Request permissions directly if no rationale is needed.
      ActivityCompat.requestPermissions(requireActivity(),
          new String[] {readPermission, writePermission}, REQUEST_STORAGE_PERMISSIONS);
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.R)
  private void accessAllFilesPermissionDialog() {
    new AlertDialog.Builder(requireActivity())
        .setTitle(getString(R.string.requestPermTitle))
        .setMessage(getString(R.string.requestPermText))
        .setPositiveButton(HtmlCompat.fromHtml("<font color='yellow'>"
                                   + "Ok"
                                   + "</font>",
                               HtmlCompat.FROM_HTML_MODE_LEGACY),
            (dialog, which) -> {
              // Request permissions when the user clicks OK.
              Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                  Uri.parse("package:com.oneval"));
              getPermResult.launch(intent);
            })
        .setNeutralButton(
            HtmlCompat.fromHtml("<font color='red'>" + getString(R.string.cancel) + "</font>",
                HtmlCompat.FROM_HTML_MODE_LEGACY),
            (dialog, which) -> {
              dialog.dismiss();
              // Handle the case where the user cancels the permission request.
            })
        .show();
  }
  @Override
  public void onStart() {
    super.onStart();
    // TODO: Implement this method
    // #S number of active session, #C Number of clicks in session
    // #Val/C Average value (OV) per click
    setStat("#S 0  |  #C 0  |  #Val/C 0");
    stat.setMovementMethod(LinkMovementMethod.getInstance());
    stat.setHighlightColor(Color.YELLOW);
  }
}