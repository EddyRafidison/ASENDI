package one.x;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import one.x.helper.LocaleHelper;

public class App extends Application {
  @Override
  public void onCreate() {
    super.onCreate();

    ViewPump.init(ViewPump.builder()
            .addInterceptor(new CalligraphyInterceptor(new CalligraphyConfig.Builder()
                    .setDefaultFontPath("fonts/ralewayRegular.ttf")
                    .setFontAttrId(R.attr.fontPath)
                    .build()))
            .build());
  }
}