package one.x;

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
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import androidx.appcompat.widget.TooltipCompat;
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
import com.topsheet.TopSheetBehavior;
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
  private ImageView topsheet_arrow, pm_src;
  private ImageView topsheet_profile;
  private ImageView qr_dest;
  private ImageView bottomsheet_arrow;
  private ImageView bottomsheet_transfer;
  private boolean isTopsheetToOpen = false, isBottomSheetToOpen = false, isPbtoShow = false,
                  isBSopen = false, isNextLoad = false;
  private TextView user_id, stat;
  private LoaderTextView stock;
  private ProgressBar addPb;
  private EditText user_dest, value, addressPay;
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
  private String Fees = "?", Fees2 = "?", op_index, laddr;
  private ListView historylist;
  private int days = 0, LastHistSize = 0, cursor = 0, codeClick = 0;
  private DecimalFormat df;
  private Bitmap user_qr = null, dest_qr = null;
  private Vibrator vib = null;
  private View select_pm, confirm_pm;
  private int selected_pm = 0;

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
    select_pm = layout.findViewById(R.id.select_pm);
    confirm_pm = layout.findViewById(R.id.yes_pm);
    pm_src = layout.findViewById(R.id.img_pm);
    addressPay = layout.findViewById(R.id.address_pay);
    View v = requireActivity().findViewById(R.id.empty_data_layout);
    if (v != null) {
      v.setVisibility(View.INVISIBLE);
    }
    addressPay.setSelectAllOnFocus(true);
    String statString = requireActivity().getString(R.string.stat_session) + " | "
        + requireActivity().getString(R.string.stat_clicks) + " | "
        + requireActivity().getString(R.string.stat_vpc);
    TooltipCompat.setTooltipText(stat, statString);
    select_pm.setOnClickListener(v9 -> { showPmList(v9); });
    confirm_pm.setOnClickListener(v10 -> {
      if (addressPay.length() > 7) {
        if (selected_pm == 0) {
          showMMTopup(addressPay.getText().toString());
        } else {
          if (Utils.isConnectionAvailable(requireContext()) == false) {
            Utils.showNoConnectionAlert(requireContext(), rel);
          } else {
            Utils.connectToServer(getActivity(), ONEX.TBUY,
                new String[] {"user", "pswd", "user_addr", "pay_index", "tkn"},
                new String[] {user, pswd, addressPay.getText().toString(), "" + selected_pm,
                    Utils.getTkn(requireContext())},
                true, response -> {
                  try {
                    String msg = response.getString("onaddr");
                    if (!msg.isEmpty()) {
                      showOneXAddress(msg);
                    } else {
                      Toast
                          .makeText(getContext(), requireActivity().getString(R.string.no_addr),
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
          }
        }
      } else {
        Toast
            .makeText(getActivity(), requireActivity().getString(R.string.check_entry),
                Toast.LENGTH_SHORT)
            .show();
      }
    });

    historylist = layout.findViewById(R.id.history_list);
    user = Utils.getAccount(requireContext());
    user_qr = Utils.qr(requireActivity(), user);
    qr.setImageBitmap(user_qr);
    qr_dest.setImageBitmap(user_qr);

    vib = (Vibrator) requireActivity().getSystemService(Vibrator.class);

    qr_dest.setOnLongClickListener(v6 -> {
      if (vib != null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vib.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
          vib.vibrate(50);
        }
      }
      codeClick = 2;
      checkPermissions();
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
    DrawableCompat.setTint(copy.getBackground(), Color.parseColor("#214469"));
    TextView group = layout.findViewById(R.id.user_group);
    user_id.setText(user);

    final String grp = Utils.getCategory(requireContext());
    String user_gr = null;
    if (grp.equals("C")) {
      user_gr = "Standard";
    } else if (grp.equals("B")) {
      user_gr = "Pro";
    } else {
      user_gr = "Standard";
    }
    group.setText(user_gr);

    qr.setOnLongClickListener(v5 -> {
      if (vib != null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vib.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
          vib.vibrate(50);
        }
      }
      codeClick = 1;
      checkPermissions();
      return true;
    });
    go.setOnClickListener(v_ -> { showLPEActivity(); });
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
      }

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

    copy.setOnClickListener(v2 -> {
      ClipboardManager clipboard =
          (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
      ClipData clip = ClipData.newPlainText("onex_user_id", user_id.getText().toString());
      clipboard.setPrimaryClip(clip);
      Toast.makeText(getActivity(), getString(R.string.copied), Toast.LENGTH_SHORT).show();
    });

    DrawableCompat.setTint(scan_but.getBackground(), Color.parseColor("#214469"));

    scan_but.setOnClickListener(v3 -> {
      ScanOptions options = new ScanOptions();
      options.setCaptureActivity(BarcodeScanner.class);
      options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
      options.setOrientationLocked(false);
      options.setBeepEnabled(false);
      barcodeLauncher.launch(options);
    });

    scan_but.setOnLongClickListener(v7 -> {
      if (vib != null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vib.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
          vib.vibrate(50);
        }
      }
      codeClick = 3;
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
            getLastPAddress();

            break;
          case TopSheetBehavior.STATE_COLLAPSED:
            topsheet_arrow.setImageResource(R.drawable.arrow_down);
            topsheet_profile.setImageResource(R.drawable.topup);
            removeAddressProgressBar();
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
              if (ps.equals(pswd)) {
                arg0.dismiss();
                Utils.connectToServer(getActivity(), ONEX.TRANSFER,
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
                          case "forbidden":
                            Toast
                                .makeText(requireActivity(),
                                    requireActivity().getString(R.string.login_again),
                                    Toast.LENGTH_SHORT)
                                .show();
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

  private void showMMTopup(String mobile) {
    AlertDialog.Builder builder =
        new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AppTheme_Dialog2));
    final View customLayout = getLayoutInflater().inflate(R.layout.mm_buy, null, false);
    builder.setView(customLayout);
    builder.setCancelable(false);
    final EditText am = customLayout.findViewById(R.id.mm_val);
    am.setHint(ONEX.CURRENCY);
    int resId = getResources().getIdentifier(
        ONEX.COUNTRY.toLowerCase() + "_flag", "drawable", requireActivity().getPackageName());
    ImageView flag = customLayout.findViewById(R.id.cflag);
    flag.setImageResource(resId);
    am.requestFocus();
    builder.setPositiveButton(HtmlCompat.fromHtml("<font color='yellow'>"
                                      + requireActivity().getString(R.string.next) + "</font>",
                                  HtmlCompat.FROM_HTML_MODE_LEGACY),
        (arg0, arg1) -> {
          final String val = am.getText().toString();
          if (!val.isEmpty() && !val.startsWith("0") && !val.startsWith(".")
              && !val.startsWith(",")) {
            if (Utils.isConnectionAvailable(requireContext()) == false) {
              Utils.showNoConnectionAlert(requireContext(), rel);
            } else {
              arg0.dismiss();
              Utils.connectToServer(getActivity(), ONEX.MBUY,
                  new String[] {"user", "pswd", "mobile", "pay_index", "amount", "tkn"},
                  new String[] {
                      user, pswd, mobile, "" + selected_pm, val, Utils.getTkn(requireContext())},
                  true, response -> {
                    try {
                      String status = response.getString("transf");
                      switch (status) {
                        case "ok":
                          reloadUpdate();
                          Utils.showMessage(getContext(), topSheet,
                              requireActivity().getString(R.string.successed_transfer), true);
                          break;
                        case "bad":
                          Utils.showMessage(getContext(), topSheet,
                              requireActivity().getString(R.string.failed_transfer), false);
                          break;
                        default:
                          return;
                      }

                    } catch (JSONException e) {
                      Toast
                          .makeText(getContext(), requireActivity().getString(R.string.data_error),
                              Toast.LENGTH_SHORT)
                          .show();
                    }
                  });
            }

          } else {
            Toast
                .makeText(getActivity(), requireActivity().getString(R.string.check_entry),
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

  private void showOneXAddress(String address) {
    AlertDialog.Builder builder =
        new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AppTheme_Dialog2));
    final View customLayout = getLayoutInflater().inflate(R.layout.usdt_buy, null, false);
    builder.setView(customLayout);
    builder.setCancelable(false);
    final EditText pk = customLayout.findViewById(R.id.pk);
    pk.setText(address);
    builder.setPositiveButton(HtmlCompat.fromHtml("<font color='yellow'>"
                                      + requireActivity().getString(R.string.copy) + "</font>",
                                  HtmlCompat.FROM_HTML_MODE_LEGACY),
        (arg0, arg1) -> {
          ClipboardManager clipboard =
              (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
          ClipData clip = ClipData.newPlainText("onex_ad", address);
          clipboard.setPrimaryClip(clip);
          Toast.makeText(getActivity(), getString(R.string.copied), Toast.LENGTH_SHORT).show();
          arg0.dismiss();
        });
    builder.setNeutralButton(
        HtmlCompat.fromHtml("<font color='#848482'>" + getString(R.string.cancel) + "</font>",
            HtmlCompat.FROM_HTML_MODE_LEGACY),
        (dialog, which) -> dialog.cancel());
    AlertDialog dialogpswd = builder.create();
    dialogpswd.show();
  }

  private void showLPEActivity() {
    AlertDialog.Builder builder =
        new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AppTheme_Dialog2));
    final View customLayout = getLayoutInflater().inflate(R.layout.lpe_activity, null, false);
    builder.setView(customLayout);
    builder.setCancelable(false);
    final EditText pswdval = customLayout.findViewById(R.id.pswdD);
    final TextView tx = customLayout.findViewById(R.id.lpe_text);

    final String more = requireActivity().getString(R.string.know_more);
    final String t = String.valueOf(HtmlCompat.fromHtml(
        requireActivity().getString(R.string.buy_campaign) + "<br><br>(" + more + ")",
        HtmlCompat.FROM_HTML_MODE_LEGACY));
    String[] strs = new String[] {more};
    SpannableStringBuilder ss = new SpannableStringBuilder(t);
    for (int i = 0; i < strs.length; i++) {
      String s = strs[i];
      int start = t.indexOf(s);
      int end = t.lastIndexOf(s) + s.length();
      ss.setSpan(new Clickables(tx, strs, i, string -> {
        if (string.equals(strs[0])) {
          tx.setText(
              t.replace(more, requireActivity().getString(R.string.lpe_text).replace("10", Fees2)));
        }
      }, Color.MAGENTA), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    tx.setText(ss, TextView.BufferType.SPANNABLE);
    tx.setMovementMethod(LinkMovementMethod.getInstance());
    tx.setHighlightColor(Color.YELLOW);

    builder.setPositiveButton(HtmlCompat.fromHtml("<font color='yellow'>"
                                      + requireActivity().getString(R.string.next) + "</font>",
                                  HtmlCompat.FROM_HTML_MODE_LEGACY),
        (arg0, arg1) -> {
          if (pswdval.getText().toString().equals(pswd)) {
            if (Utils.isConnectionAvailable(requireContext()) == false) {
              Utils.showNoConnectionAlert(requireContext(), rel);
            } else {
              arg0.dismiss();
              Utils.connectToServer(getActivity(), ONEX.BUY_CAMPAIGN,
                  new String[] {"user", "pswd", "tkn"},
                  new String[] {user, pswd, Utils.getTkn(requireContext())}, true, response -> {
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
                        case "occupied":
                          Utils.showMessage(getContext(), topSheet,
                              requireActivity().getString(R.string.campaign_still_running), false);
                          break;
                        case "forbidden":
                          Toast
                              .makeText(requireActivity(),
                                  requireActivity().getString(R.string.login_again),
                                  Toast.LENGTH_SHORT)
                              .show();
                          break;
                        default:
                          Utils.showMessage(getContext(), topSheet,
                              requireActivity().getString(R.string.missing_balance), false);
                          break;
                      }

                    } catch (JSONException e) {
                      Toast
                          .makeText(getContext(), requireActivity().getString(R.string.data_error),
                              Toast.LENGTH_SHORT)
                          .show();
                    }
                  });
            }
          } else {
            if (pswdval.length() == 0) {
              Toast
                  .makeText(getActivity(), requireActivity().getString(R.string.check_entry),
                      Toast.LENGTH_SHORT)
                  .show();
            } else {
              Toast
                  .makeText(getContext(), requireActivity().getString(R.string.error_pswd),
                      Toast.LENGTH_SHORT)
                  .show();
            }
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
    ONEX.TIMER2 = new Timer();
    final Handler handler = new Handler(Looper.getMainLooper());
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
    if (ONEX.TIMER2 != null) {
      ONEX.TIMER2.cancel();
      ONEX.TIMER2 = null;
    }
    ONEX.TIMER2 = new Timer();
    ONEX.TIMER2.schedule(doAsynchronousTask, 3000);
  }

  private void reloadUpdate() {
    if (ONEX.ISCONNECTED) {
      if (!isNextLoad) {
        isPbtoShow = true;
        isNextLoad = true;
      } else {
        isPbtoShow = false;
      }
      Utils.connectToServer(getActivity(), ONEX.TRANSREC,
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

                history.add(new HistoryItem(getContext(), deliver_date + deliver_time, Amoun, type,
                    user_, refer, transfees));
              }
              final int hs = history.size();
              if (LastHistSize != hs) {
                LastHistSize = hs;
                ArrayAdapter<HistoryItem> la = new HistoryAdapter(getActivity(), history);
                historylist.setAdapter(la);
              }
              Utils.connectToServer(getActivity(), ONEX.BALANCE,
                  new String[] {"user", "pswd", "tkn"},
                  new String[] {user, pswd, Utils.getTkn(requireContext())}, false, resp -> {
                    try {
                      String bal = resp.getString("msg");
                      if (!bal.equals("error")) {
                        Fees = resp.getString("fees");
                        Fees2 = resp.getString("fees2");
                        stock.setText(df.format(Double.parseDouble(bal)));
                        if (isBSopen) {
                          if (user_dest.length() > 0) {
                            stock.setText(stockMasker());
                            stock.setTransformationMethod(new StockHiding());
                          }
                        }
                      }
                      // Check min stat
                      Utils.connectToServer(getActivity(), ONEX.GET_STAT,
                          new String[] {"user", "pswd", "tkn"},
                          new String[] {user, pswd, Utils.getTkn(requireContext())}, false,
                          resp1 -> {
                            try {
                              String dataStat = resp1.getString("stat");
                              // Data display here
                              setStat(dataStat);
                            } catch (JSONException e) {
                              Toast
                                  .makeText(getContext(),
                                      requireActivity().getString(R.string.data_error),
                                      Toast.LENGTH_SHORT)
                                  .show();
                            }
                          });
                      refresh();
                    } catch (JSONException e) {
                      Toast
                          .makeText(getContext(), requireActivity().getString(R.string.data_error),
                              Toast.LENGTH_SHORT)
                          .show();
                    }
                    refresh();
                  });
            } catch (JSONException je) {
              Toast
                  .makeText(getActivity(), requireActivity().getString(R.string.data_error),
                      Toast.LENGTH_SHORT)
                  .show();
            }
            refresh();
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
        if (up > 8)
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

  private synchronized void setStat(String text) {
    if (!text.isEmpty()) {
      stat.setText(HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY));
    } else {
      String dflt = "0";
      String S = "<font color='#FFDA1A'>#S</font>&nbsp;<font color='#0CFE54'>" + dflt
          + "</font>&nbsp;"
          + "<font color='#FDFDFD'>|</font>&nbsp;";
      String C = "<font color='#FFDA1A'>#C</font>&nbsp;<font color='#0CFE54'>" + dflt
          + "</font>&nbsp;"
          + "<font color='#FDFDFD'>|</font>&nbsp;";
      String ValC =
          "<font color='#FFDA1A'>#Val/C</font>&nbsp;<font color='#0CFE54'>" + dflt + "</font>";
      String deflt = S + C + ValC;
      stat.setText(HtmlCompat.fromHtml(deflt, HtmlCompat.FROM_HTML_MODE_LEGACY));
    }
  }

  private void checkPermissions() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (codeClick == 3) {
        // As the device is Android 13 and above so I want the permission of accessing Audio,
        // Images, Videos You can ask permission according to your requirements what you want to
        // access.
        String imagesPermission = android.Manifest.permission.READ_MEDIA_IMAGES;
        // Check for permissions and request them if needed
        if (ContextCompat.checkSelfPermission(requireActivity(), imagesPermission)
            == PackageManager.PERMISSION_GRANTED) {
          // You have the permissions, you can proceed with your media file operations.
          // Showing dialog when Show Dialog button is clicked.
          switchAction();
        } else {
          // You don't have the permissions. Request them.
          imagesPermissionLauncher.launch(imagesPermission);
        }
      } else {
        // No permission needed for Android 13
        switchAction();
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
        switchAction();
      } else {
        // You don't have the permissions. Request them.
        storagePermissionsLauncher.launch(new String[] {readPermission, writePermission});
      }
    }
  }

  private void switchAction() {
    switch (codeClick) {
      case 1:
        Utils.saveToDownloads(requireActivity(), Utils.resizeBitmap(user_qr), user);
        break;
      case 2:
        String user_d = user_dest.getText().toString();
        if (user_d.length() > 3) {
          Utils.saveToDownloads(requireActivity(), Utils.resizeBitmap(dest_qr), user_d);
        } else {
          Toast
              .makeText(requireActivity(), requireActivity().getString(R.string.check_dest),
                  Toast.LENGTH_SHORT)
              .show();
        }
        break;
      case 3:
        filePickerDialog.show();
        break;
      default:
        return;
    }
  }

  private final ActivityResultLauncher<String[]> storagePermissionsLauncher =
      registerForActivityResult(
          new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            boolean allGranted = result.containsValue(true) && !result.containsValue(false);
            if (allGranted) {
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                accessAllFilesPermissionDialog();
              } else {
                switchAction();
              }
            } else {
              showRationaleDialog();
            }
          });

  private final ActivityResultLauncher<String> imagesPermissionLauncher =
      registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          accessAllFilesPermissionDialog();
        } else {
          showRationaleDialog();
        }
      });

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
                checkPermissions();
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
      // Request permissions
      checkPermissions();
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
                  Uri.parse("package:one.x"));
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
    setStat("");
    refresh();
  }

  private void showPmList(View anchorv) {
    ArrayList<String> listPm = new ArrayList<>();
    listPm.add("AirtelMoney");
    listPm.add("Tether (USDT)");
    final View txtv = requireActivity().findViewById(R.id.phone_input_limit);
    ContextThemeWrapper theme =
        new ContextThemeWrapper(requireContext(), android.R.style.Theme_Material_Light);
    ListPopupWindow listPmD = new ListPopupWindow(theme);

    listPmD.setAdapter(
        new ArrayAdapter<String>(requireActivity(), R.layout.popup_textview, listPm) {
          @Override
          public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView text = view.findViewById(R.id.textItem);

            if (position == 0) {
              text.setTextColor(Color.parseColor("#E11E11"));
            } else {
              text.setTextColor(Color.parseColor("#FF51AF95"));
            }

            return view;
          };
        });
    listPmD.setAnchorView(anchorv);
    listPmD.setWidth(200);
    listPmD.setVerticalOffset(anchorv.getHeight() / (-2));
    listPmD.setHeight(ListPopupWindow.WRAP_CONTENT);
    listPmD.setOnItemClickListener((parent, view, position, id) -> {
      selected_pm = position;
      addressPay.setText("");
      if (!op_index.isEmpty()) {
        if (selected_pm == 0) {
          select_pm.setBackground(requireActivity().getDrawable(R.drawable.round_bg_red_padded));
          pm_src.setImageResource(R.drawable.airtel_logo);
          addressPay.setHint(R.string.mm_pm_hint);
          int visibility = txtv.getVisibility();
          if (visibility == 4) {
            txtv.setVisibility(0);
          }
          addressPay.setInputType(InputType.TYPE_CLASS_PHONE);
        } else {
          select_pm.setBackground(requireActivity().getDrawable(R.drawable.round_bg_green_padded));
          pm_src.setImageResource(R.drawable.tether_logo);
          addressPay.setHint(R.string.pk_pm_hint);
          int visibility = txtv.getVisibility();
          if (visibility == 0) {
            txtv.setVisibility(4);
          }
          addressPay.setInputType(InputType.TYPE_CLASS_TEXT);
        }
        if (("" + selected_pm).equals(op_index)) {
          addressPay.setText(laddr);
        }
        addressPay.requestFocus();
        InputMethodManager imm =
            (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(addressPay, InputMethodManager.SHOW_IMPLICIT);
      }
      listPmD.dismiss();
    });
    listPmD.show();
  }

  private void getLastPAddress() {
    showAddressProgressBar();
    addressPay.setInputType(InputType.TYPE_NULL);
    addressPay.setText("");
    op_index = "";
    laddr = "";
    if (Utils.isConnectionAvailable(requireContext()) == false) {
      Utils.showNoConnectionAlert(requireContext(), rel);
    } else {
      Utils.connectToServer(getActivity(), ONEX.LADDR, new String[] {"user", "pswd", "tkn"},
          new String[] {user, pswd, Utils.getTkn(requireContext())}, false, response -> {
            try {
              String info = response.getString("iaddress");
              if (!info.isEmpty()) {
                op_index =
                    info.substring(0, 1); // same reference as listed in available payments methods
                laddr = info.substring(1, info.length());
                if (op_index.equals("" + selected_pm)) {
                  if (op_index.equals("0")) {
                    addressPay.setText(laddr);
                    addressPay.setInputType(InputType.TYPE_CLASS_PHONE);
                  } else {
                    addressPay.setInputType(InputType.TYPE_CLASS_TEXT);
                  }
                } else {
                  if (selected_pm == 0) {
                    addressPay.setInputType(InputType.TYPE_CLASS_PHONE);
                  } else {
                    addressPay.setInputType(InputType.TYPE_CLASS_TEXT);
                  }
                }
                addressPay.requestFocus();
                removeAddressProgressBar();
              }

            } catch (JSONException e) {
              Toast
                  .makeText(getContext(), requireActivity().getString(R.string.data_error),
                      Toast.LENGTH_SHORT)
                  .show();
              removeAddressProgressBar();
            }
          });
    }
  }
  private void showAddressProgressBar() {
    RelativeLayout mlayout = requireActivity().findViewById(R.id.cp_layout);
    if (addPb == null) {
      addPb = new ProgressBar(requireActivity());
      addPb.setId(View.generateViewId());
      RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
          RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
      params.addRule(RelativeLayout.CENTER_IN_PARENT);
      mlayout.addView(addPb, params);
    }
  }
  private void removeAddressProgressBar() {
    RelativeLayout mlayout = requireActivity().findViewById(R.id.cp_layout);
    if (addPb != null)
      mlayout.removeViewAt(1);
    addPb = null;
  }
}
