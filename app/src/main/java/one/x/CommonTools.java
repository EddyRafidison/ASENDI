package one.x;
import android.content.Context;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import one.x.helper.LocaleHelper;

public class CommonTools {
    public static Context getInstance(Context context) {
        return setTools(context);
    }
    public static Context setFont(Context ctx) {
        return ViewPumpContextWrapper.wrap(ctx);
    }
    public static Context setLang(Context ctx) {
        return LocaleHelper.onAttach(ctx);
    }
    public static Context setTools(Context ctx) {
        setLang(ctx);
        return setFont(ctx);
    }
}
