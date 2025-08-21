package one.x;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.elyeproj.loaderviewlibrary.LoaderTextView;

public class Terms extends Fragment {
  private LoaderTextView text;

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.textlayout, container, false);
    // Inflate the layout for this fragment
    text = layout.findViewById(R.id.textdata);
    // load data here
    Utils.requestTP(getActivity(),
        "terms"
            + "&l=" + ONEX.TPLANG,
        text);
    return layout;
  }
}
