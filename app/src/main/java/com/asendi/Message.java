package com.asendi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import org.json.JSONException;

public class Message extends Fragment {
    private EditText subject,
            text;
    private MaterialButton sendMsg;
    private String pswd,
            user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.message, container, false);
        // Inflate the layout for this fragment
        text = layout.findViewById(R.id.message);
        subject = layout.findViewById(R.id.subject);
        sendMsg = layout.findViewById(R.id.sendmsg);
        Bundle bundle = getArguments();
        assert bundle != null;
        pswd = bundle.getString("psd");
        user = bundle.getString("act");
        sendMsg.setOnClickListener(v -> {
            final String t = text.getText().toString();
            final String s = subject.getText().toString();
            if (!t.isEmpty() && !s.isEmpty()) {
                if (Utils.isConnectionAvailable(requireContext()) == false) {
                    Utils.showNoConnectionAlert(requireContext(), sendMsg);
                } else {
                    Utils.connectToServer(getActivity(), ASENDI.CONTACT, new String[]{
                            "user", "pswd", "subj", "msg", "tkn"
                    }, new String[]{
                            user, pswd, s, t, Utils.getTkn(requireContext())
                    }, true, response -> {
                        try {
                            String status = response.getString("status");
                            if (status.equals("sent")) {
                                Utils.showMessage(getContext(), sendMsg, requireActivity().getString(R.string.successed_msg), true);
                                text.setText("");
                                subject.setText("");
                            } else {
                                Utils.showMessage(getContext(), sendMsg, requireActivity().getString(R.string.failed_msg), false);
                            }
                        } catch (JSONException je) {
                            Toast.makeText(getContext(), requireActivity().getString(R.string.data_error), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Toast.makeText(getActivity(), requireActivity().getString(R.string.check_entries), Toast.LENGTH_SHORT).show();
            }
        });
        return layout;
    }
}