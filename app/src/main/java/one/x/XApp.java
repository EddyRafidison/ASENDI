package one.x;

import android.app.Application;
import android.content.Context;
import one.x.helper.LocaleHelper;

public class XApp extends App {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "" + ONEX.TPLANG));
    }
}