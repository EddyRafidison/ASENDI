package com.oneval;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.elyeproj.loaderviewlibrary.LoaderTextView;

public class Privacy extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.textlayout, container, false);
        // Inflate the layout for this fragment
        LoaderTextView text = layout.findViewById(R.id.textdata);
        //load data here
        Utils.requestTP(getActivity(), "privacy" + "&l=" + ONEVAL.TPLANG, text);
        return layout;
    }
}