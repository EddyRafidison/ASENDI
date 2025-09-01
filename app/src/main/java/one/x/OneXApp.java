package one.x;
import android.text.InputFilter;
import android.text.Spanned;
import java.util.regex.Pattern;

public class OneXApp extends App {
    
    private static OneXApp instance;
    private static InputFilter latinInputFilter;
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        latinInputFilter = createLatinInputFilter();
    }
    
    public static OneXApp getInstance() {
        return instance;
    }
    
    public static InputFilter getLatinInputFilter() {
        return latinInputFilter;
    }
    
    private static InputFilter createLatinInputFilter() {
        return new InputFilter() {
            private final Pattern latinPattern = Pattern.compile(
                "^[\\p{L}0-9\\s\\p{Punct}ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõöøùúûüýþÿ]*$"
            );

            @Override
            public CharSequence filter(CharSequence source, int start, int end, 
                                       Spanned dest, int dstart, int dend) {
                String input = source.subSequence(start, end).toString();
                return latinPattern.matcher(input).matches() ? null : "";
            }
        };
    }
}