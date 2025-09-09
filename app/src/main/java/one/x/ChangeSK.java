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

public class ChangeSK extends Fragment {
  private EditText pswd, newSk;
  private MaterialButton change;
  private String user, pswd_;

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.secretkey, container, false);
    // Inflate the layout for this fragment
    pswd = layout.findViewById(R.id.pswd);
    newSk = layout.findViewById(R.id.new_sk);
    change = layout.findViewById(R.id.update_sk);
    newSk.requestFocus();
    Bundle bundle = getArguments();
    assert bundle != null;
    user = bundle.getString("act");
    pswd_ = bundle.getString("psd");
    change.setOnClickListener(v -> {
      final String e1 = pswd.getText().toString();
      final String e2 = newSk.getText().toString();
      if (!e1.isEmpty() && !e2.isEmpty()) {
        if (Utils.isConnectionAvailable(requireContext()) == false) {
          Utils.showNoConnectionAlert(requireContext(), change);

        } else {
          if (e1.equals(pswd_)) {
            Utils.connectToServer(getActivity(), ONEX.MSK,
                new String[] {"user", "pswd", "sk", "tkn"},
                new String[] {user, e1, e2, Utils.getTkn(requireContext())}, true, response -> {
                  try {
                    String auth = response.getString("auth");
                    if (auth.equals("updated")) {
                      Utils.showMessage(getContext(), change,
                          requireActivity().getString(R.string.successed_update), true);
                      pswd.setText("");
                      newSk.setText("");
                    } else if (auth.equals("incorrect")) {
                      Utils.showMessage(getContext(), change,
                          requireActivity().getString(R.string.error_pswd), false);
                    } else {
                      if (auth.contains("forbidden")) {
                        Toast
                            .makeText(requireActivity(),
                                requireActivity().getString(R.string.login_again),
                                Toast.LENGTH_SHORT)
                            .show();
                      } else {
                        Utils.showMessage(getContext(), change,
                            requireActivity().getString(R.string.failed_update), false);
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
            .makeText(getActivity(), requireActivity().getString(R.string.check_entries),
                Toast.LENGTH_SHORT)
            .show();
      }
    });
    return layout;
  }
}
