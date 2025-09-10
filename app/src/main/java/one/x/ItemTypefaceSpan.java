package one.x;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.TypefaceSpan;

public class ItemTypefaceSpan extends TypefaceSpan {
  private final Typeface newType;

  public ItemTypefaceSpan(String family, Typeface type) {
    super(family);
    newType = type;
  }

  @Override
  public void updateDrawState(TextPaint ds) {
    applyCustomTypeFace(ds, newType);
  }

  @Override
  public void updateMeasureState(TextPaint paint) {
    applyCustomTypeFace(paint, newType);
  }

  private static void applyCustomTypeFace(Paint paint, Typeface tf) {
    Typeface old = paint.getTypeface();
    int oldStyle = old != null ? old.getStyle() : 0;

    Typeface styled = Typeface.create(tf, oldStyle);
    paint.setTypeface(styled);

    paint.setFlags(paint.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
  }
}