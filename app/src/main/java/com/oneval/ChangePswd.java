package com.oneval;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import org.json.JSONException;

public class ChangePswd extends Fragment {
  private EditText curpswd, newpswd1, newpswd2;
  private MaterialButton change;
  private String user, pswd;

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.password, container, false);
    // Inflate the layout for this fragment
    curpswd = layout.findViewById(R.id.current_pswd);
    newpswd1 = layout.findViewById(R.id.new_pswd1);
    newpswd2 = layout.findViewById(R.id.new_pswd2);
    change = layout.findViewById(R.id.update_pswd);
    curpswd.requestFocus();
    Bundle bundle = getArguments();
    assert bundle != null;
    pswd = bundle.getString("psd");
    user = bundle.getString("act");
    change.setOnClickListener(
        v -> {
          final String e1 = curpswd.getText().toString();
          final String e2 = newpswd1.getText().toString();
          final String e3 = newpswd2.getText().toString();
          if (!e1.isEmpty() && !e2.isEmpty() && !e3.isEmpty()) {
            if (e2.equals(e3)) {
              if (e2.length() > 3) {
                if (Utils.isConnectionAvailable(requireContext()) == false) {
                  Utils.showNoConnectionAlert(requireContext(), change);
                } else {
                  if (e1.equals(pswd)) {
                    Utils.connectToServer(
                        getActivity(),
                        ONEVAL.MPC,
                        new String[] {"user", "pswd1", "pswd2", "tkn"},
                        new String[] {user, e1, e2, Utils.getTkn(requireContext())},
                        true,
                        response -> {
                          try {
                            String auth = response.getString("auth");
                            if (auth.equals("updated")) {
                              Utils.showMessage(
                                  getContext(),
                                  change,
                                  requireActivity().getString(R.string.successed_update),
                                  true);
                              curpswd.setText("");
                              newpswd1.setText("");
                              newpswd2.setText("");
                              Utils.clearAccFromApp(requireContext());
                              Utils.saveCredentials(requireContext(), user, e2);
                              Utils.setPswdChangedStatus(requireContext(), true);
                              try {
                                try {
                                  ONEVAL.TIMER.cancel();
                                  ONEVAL.TIMER.purge();
                                } catch (Exception ignored) {
                                }
                                try {
                                  ONEVAL.TIMER2.cancel();
                                  ONEVAL.TIMER2.purge();
                                } catch (Exception ignored) {
                                }
                                new CountDownTimer(3000, 3000) {
                                  @Override
                                  public void onTick(long l) {}

                                  @Override
                                  public void onFinish() {
                                    requireActivity().finish();
                                  }
                                }.start();
                              } catch (Exception ignored) {
                              }
                            } else if (auth.equals("incorrect")) {
                              Utils.showMessage(
                                  getContext(),
                                  change,
                                  requireActivity().getString(R.string.error_pswd),
                                  false);
                            } else {
                              Utils.showMessage(
                                  getContext(),
                                  change,
                                  requireActivity().getString(R.string.failed_update),
                                  false);
                            }
                          } catch (JSONException je) {
                            Toast.makeText(
                                    getContext(),
                                    requireActivity().getString(R.string.data_error),
                                    Toast.LENGTH_SHORT)
                                .show();
                          }
                        });
                  } else {
                    Toast.makeText(
                            getContext(),
                            requireActivity().getString(R.string.error_pswd),
                            Toast.LENGTH_SHORT)
                        .show();
                  }
                }
              } else {
                Toast.makeText(
                        getActivity(),
                        requireActivity().getString(R.string.tooShortPswd),
                        Toast.LENGTH_SHORT)
                    .show();
              }
            } else {
              Toast.makeText(
                      getActivity(),
                      requireActivity().getString(R.string.not_matched_pswd),
                      Toast.LENGTH_SHORT)
                  .show();
            }
          } else {
            Toast.makeText(
                    getActivity(),
                    requireActivity().getString(R.string.check_entries),
                    Toast.LENGTH_SHORT)
                .show();
          }
        });
    return layout;
  }
}
