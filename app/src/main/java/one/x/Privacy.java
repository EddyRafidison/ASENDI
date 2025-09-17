package one.x;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.elyeproj.loaderviewlibrary.LoaderTextView;

public class Privacy extends Fragment {
    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.textlayout, container, false);
        LoaderTextView text = layout.findViewById(R.id.textdata);
        Utils.requestTP(getActivity(),
                        "privacy"
                        + "&l=" + ONEX.TPLANG,
                        text);
        return layout;
    }
}
