package one.x;

import android.text.method.PasswordTransformationMethod;
import android.view.View;
import androidx.annotation.NonNull;

public class StockHiding extends PasswordTransformationMethod {
  @Override
  public CharSequence getTransformation(CharSequence source, View view) {
    return new PasswordCharSequence(source);
  }

  private static class PasswordCharSequence implements CharSequence {
    private final CharSequence mSource;

    public PasswordCharSequence(CharSequence source) {
      mSource = source; // Store char sequence
    }

    public char charAt(int index) {
      return '•'; // This is the important part
    }

    public int length() {
      return mSource.length(); // Return default
    }

    @NonNull
    public CharSequence subSequence(int start, int end) {
      return mSource.subSequence(start, end); // Return default
    }
  }
}
