package one.x;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import org.json.JSONException;

public class DeleteAct extends Fragment {
    private EditText pswd;
    private MaterialButton delete;
    private String user, pswd_;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.delete_acct, container, false);
        pswd = layout.findViewById(R.id.pswd_);
        delete = layout.findViewById(R.id.dlt_btn);
        Bundle bundle = getArguments();
        assert bundle != null;
        pswd_ = bundle.getString("psd");
        user = bundle.getString("act");
        delete.setOnClickListener(v -> {
            final String pswdt = pswd.getText().toString();
            if (!pswdt.isEmpty()) {
                if (Utils.isConnectionAvailable(requireContext()) == false) {
                    Utils.showNoConnectionAlert(requireContext(), delete);
                } else {
                    if (pswdt.equals(pswd_)) {
                        Utils.connectToServer(getActivity(), ONEX.DLTACC, new String[] {"user", "pswd", "tkn"},
                        new String[] {user, pswdt, Utils.getTkn(requireContext())}, true, response -> {
                            try {
                                String auth = response.getString("auth");
                                if (auth.equals("deleted")) {
                                    Toast
                                    .makeText(getContext(), requireActivity().getString(R.string.Acc_deleted),
                                              Toast.LENGTH_SHORT)
                                    .show();
                                    try {
                                        ONEX.TIMER.cancel();
                                        ONEX.TIMER.purge();
                                    } catch (Exception ignored) {
                                    }
                                    try {
                                        ONEX.TIMER2.cancel();
                                        ONEX.TIMER2.purge();
                                    } catch (Exception ignored) {
                                    }
                                    Utils.clearAccFromApp(requireContext());
                                    requireActivity().finish();
                                } else if (auth.equals("incorrect")) {
                                    Utils.showMessage(getContext(), delete,
                                                      requireActivity().getString(R.string.error_pswd), false);
                                } else if (auth.contains("balance")) {
                                    Utils.showMessage(getContext(), delete,
                                                      requireActivity().getString(R.string.err_bal_dlt), false);
                                } else {
                                    if (auth.contains("forbidden")) {
                                        Toast
                                        .makeText(requireActivity(),
                                                  requireActivity().getString(R.string.login_again),
                                                  Toast.LENGTH_SHORT)
                                        .show();
                                    } else {
                                        Utils.showMessage(getContext(), delete,
                                                          requireActivity().getString(R.string.failed), false);
                                    }
                                }
                            } catch (JSONException je) {
                                Toast
                                .makeText(getContext(), requireActivity().getString(R.string.data_error),
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
        return layout;
    }
}
