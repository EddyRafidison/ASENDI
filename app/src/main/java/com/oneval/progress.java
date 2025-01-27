package com.oneval;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

public final class progress {

    private Dialog dialog;

    public Dialog show(Activity context) {
        LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") final View view = inflator.inflate(R.layout.dots_progress, null);
        dialog = new Dialog(context, R.style.WDialog);
        dialog.setContentView(view);
        dialog.setCancelable(false);
        dialog.show();

        return dialog;
    }

    private Dialog getDialog() {
        return dialog;
    }

    public void dismiss() {
        getDialog().dismiss();
    }
}