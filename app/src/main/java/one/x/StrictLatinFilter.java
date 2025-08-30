package one.x;

import android.text.InputFilter;
import android.text.Spanned;

public class StrictLatinFilter implements InputFilter {
  @Override
  public CharSequence filter(
      CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
    for (int i = start; i < end; i++) {
      char c = source.charAt(i);
      String s = Character.toString(c);

      // Autorise lettres latines (y compris accentuées), espaces et ponctuation simple
      if (!s.matches("[\\p{IsLatin}\\s\\-\\.\\'\\,\\?]")) {
        return ""; // Rejette le caractère
      }
    }
    return null; // Accepte l'entrée
  }
}