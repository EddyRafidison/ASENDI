package com.oneval;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

public class Clickables extends ClickableSpan {
  String[] strs;
  int pos, color;
  TextView tv;
  ClickListener clickListener;

  public Clickables(TextView tv, String[] strs, int pos, ClickListener clickListener, int color) {
    this.strs = strs;
    this.pos = pos;
    this.tv = tv;
    this.color = color;
    this.clickListener = clickListener;
  }

  @Override
  public void onClick(View arg0) {
    clickListener.onValueClicked(strs[pos]);
    arg0.invalidate();
  }

  @Override
  public void updateDrawState(TextPaint arg0) {
    arg0.setUnderlineText(true);
    arg0.setColor(color);
    tv.invalidate();
  }
}
