package com.oneval;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DrawerLayout.DrawerListener, ServerListener {

    private final List<CharSequence> notif_list = new ArrayList<>();
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private BadgeDrawable notif_badge;
    private String titlefrag,
            user,
            pswd;
    private boolean isYesClicked = false,
            isOnCreate = false,
            doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        overridePendingTransition(R.anim.nav_enter, R.anim.nav_exit);
        if (!isOnCreate) {
            isOnCreate = true;
        }
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.settings_menu);
        notif_badge = BadgeDrawable.create(this);
        notif_badge.setBackgroundColor(Color.BLUE);
        notif_badge.setBadgeTextColor(Color.WHITE);
        // load data from server here
        Intent intent = getIntent();
        if (intent != null) {
            user = intent.getStringExtra("act");
            pswd = intent.getStringExtra("psd");
            refreshFeed();
        }
        OnBackPressedCallback onback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    if (doubleBackToExitPressedOnce) {
                        try {
                            ONEVAL.TIMER.cancel();
                            ONEVAL.TIMER.purge();
                        } catch (Exception ignored) {
                        }
                        try {
                            ONEVAL.TIMER2.cancel();
                            ONEVAL.TIMER2.purge();
                        } catch (Exception ignored) {
                        }
                        exitApp();
                        return;
                    }
                    doubleBackToExitPressedOnce = true;
                    Toast.makeText(HomeActivity.this,
                            getString(R.string.click_twice_exit),
                            Toast.LENGTH_SHORT).show();

                    new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackToExitPressedOnce = false,
                            2000);
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onback);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                switch (id) {
                    case R.id.refresh:
                        refreshData();
                        break;
                    case R.id.notif:
                        Utils.showCommonNotif(HomeActivity.this, notif_list);
                        break;
                    default:
                        return false;
                }
                return false;
            }
        });
        setupDrawer();
        CheckNetwork network_ = new CheckNetwork(getApplicationContext());
        network_.registerNetworkCallback();
        Intent getCredentials = getIntent();
        ONEVAL.ID = getCredentials.getStringExtra("act");
        ONEVAL.PSWD = getCredentials.getStringExtra("psd");
        String[] vuln_pswd = new String[]{
                "0123",
                "01234",
                "012345",
                "0123456",
                "012345678",
                "0123456789",
                "1234",
                "12345",
                "123456",
                "1234567",
                "12345678",
                "123456789",
                "1234567890",
                "0000",
                "00000",
                "000000",
                "0000000",
                "00000000",
                "000000000",
                "0000000000", "1111", "2222", "3333",
                "4444", "5555", "6666", "7777", "8888", "9999"
        };
        for (String p : vuln_pswd) {
            assert ONEVAL.PSWD != null;
            if (ONEVAL.PSWD.equals(p)) {
                Utils.showMessage(getApplicationContext(), toolbar, getString(R.string.weak_security_pswd), false);
            }
        }

    }

    private void refreshData() {
        try {
            Utils.connectToServer(HomeActivity.this, ONEVAL.FEED, new String[]{
                    "user", "pswd"
            }, new String[]{
                    user, pswd
            }, true, HomeActivity.this);
            //restart Fragment here
            if (titlefrag.equals(getString(R.string.home))) {
                showHomeFragment(titlefrag);
            } else {
                if (titlefrag.equals(getString(R.string.modpassword))) {
                    showPswdFragment(titlefrag);
                }
                if (titlefrag.equals(getString(R.string.modsecretkey))) {
                    showSKFragment(titlefrag);
                }
                if (titlefrag.equals(getString(R.string.terms))) {
                    showTermsFragment(titlefrag);

                }
                if (titlefrag.equals(getString(R.string.privacy))) {
                    showPrivacyFragment(titlefrag);
                }
                if (titlefrag.equals(getString(R.string.delete))) {
                    showDeleteActFragment(titlefrag);
                }
                if (titlefrag.equals(getString(R.string.mail_us))) {
                    showMsgFragment(titlefrag);
                }
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), getString(R.string.errorFragment), Toast.LENGTH_LONG).show();
        }
    }

    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    private void setBadgeCount(int c) {
        if (c == 0) {
            notif_badge.setVisible(false);
        } else {
            notif_badge.setVisible(true);
            notif_badge.setNumber(c);
        }
        BadgeUtils.attachBadgeDrawable(notif_badge, toolbar, R.id.notif);
    }

    private void setupDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        drawerLayout.addDrawerListener(this);

        setupNavigationView();
    }

    private void setupNavigationView() {
        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        setDefaultMenuItem();
        setupHeader();
    }

    private void setDefaultMenuItem() {
        MenuItem menuItem = navigationView.getMenu().getItem(0);
        onNavigationItemSelected(menuItem);
        menuItem.setChecked(true);
    }

    private void setupHeader() {
        View header = navigationView.getHeaderView(0);
        header.findViewById(R.id.user_code).setOnClickListener(
                view -> Toast.makeText(HomeActivity.this, getString(R.string.app_name), Toast.LENGTH_SHORT).show());
    }

    private void exitApp() {
        finishAffinity();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        final String titleA = "" + menuItem.getTitle();
        switch (menuItem.getItemId()) {
            case R.id.dashboard:
                showHomeFragment(titleA);
                break;
            case R.id.modpassword:
                showPswdFragment(titleA);
                try {
                    ONEVAL.TIMER2.cancel();
                } catch (Exception ignored) {
                }
                break;
            case R.id.modkeysecret:
                showSKFragment(titleA);
                try {
                    ONEVAL.TIMER2.cancel();
                } catch (Exception ignored) {
                }
                break;
            case R.id.terms:
                try {
                    ONEVAL.TIMER2.cancel();
                } catch (Exception ignored) {
                }
                showTermsFragment(titleA);
                break;
            case R.id.privacy:
                showPrivacyFragment(titleA);
                try {
                    ONEVAL.TIMER2.cancel();
                } catch (Exception ignored) {
                }
                break;
            case R.id.mail_us:
                showMsgFragment(titleA);
                try {
                    ONEVAL.TIMER2.cancel();
                } catch (Exception ignored) {
                }
                break;
            case R.id.delete:
                showDeleteActFragment(titleA);
                try {
                    ONEVAL.TIMER2.cancel();
                } catch (Exception ignored) {
                }
                break;
            case R.id.off:
                quitDevice();
                break;
            default:
                return true;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showHomeFragment(String title) {
        titlefrag = title;
        if (!isOnCreate) {
            if (Utils.isConnectionAvailable(getApplicationContext()) == false) {
                Utils.showNoConnectionAlert(getApplicationContext(), drawerLayout);
            } else {
                //refresh checks for internet
                Fragment fragment = new HomeContentFragment();
                Bundle mBundle = new Bundle();
                mBundle.putString(
                        "act", user);
                mBundle.putString("psd", pswd);
                fragment.setArguments(mBundle);
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.nav_enter, R.anim.nav_exit)
                        .replace(R.id.home_content, fragment).commit();
            }
        } else {
            Fragment fragment = new HomeContentFragment();
            Bundle mBundle = new Bundle();
            mBundle.putString(
                    "act", user);
            mBundle.putString("psd", pswd);
            fragment.setArguments(mBundle);
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.nav_enter, R.anim.nav_exit)
                    .replace(R.id.home_content, fragment).commit();
            isOnCreate = false;
        }
        setTitle(titlefrag);
        toolbar.setTitle(title());
    }

    private void showPrivacyFragment(String title) {
        titlefrag = title;
        if (Utils.isConnectionAvailable(getApplicationContext()) == false) {
            Utils.showNoConnectionAlert(getApplicationContext(), drawerLayout);
        } else {
            Fragment fragment = new Privacy();
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.nav_enter, R.anim.nav_exit)
                    .replace(R.id.home_content, fragment).commit();
            setTitle(titlefrag);
            toolbar.setTitle(title());
        }
    }

    private void showTermsFragment(String title) {
        titlefrag = title;
        if (Utils.isConnectionAvailable(getApplicationContext()) == false) {
            Utils.showNoConnectionAlert(getApplicationContext(), drawerLayout);
        } else {
            Fragment fragment = new Terms();
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.nav_enter, R.anim.nav_exit)
                    .replace(R.id.home_content, fragment).commit();
            setTitle(titlefrag);
            toolbar.setTitle(title());
        }
    }

    private void showPswdFragment(String title) {
        titlefrag = title;
        Fragment fragment = new ChangePswd();
        Bundle mBundle = new Bundle();
        mBundle.putString(
                "act", user);
        mBundle.putString("psd", pswd);
        fragment.setArguments(mBundle);
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.nav_enter, R.anim.nav_exit)
                .replace(R.id.home_content, fragment).commit();
        setTitle(titlefrag);
        toolbar.setTitle(title());
    }

    private void showSKFragment(String title) {
        titlefrag = title;
        Fragment fragment = new ChangeSK();
        Bundle mBundle = new Bundle();
        mBundle.putString(
                "act", user);
        mBundle.putString("psd", pswd);
        fragment.setArguments(mBundle);
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.nav_enter, R.anim.nav_exit)
                .replace(R.id.home_content, fragment).commit();
        setTitle(titlefrag);
        toolbar.setTitle(title());
    }

    private void showMsgFragment(String title) {
        titlefrag = title;
        Fragment fragment = new Message();
        Bundle mBundle = new Bundle();
        mBundle.putString(
                "act", user);
        mBundle.putString("psd", pswd);
        fragment.setArguments(mBundle);
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.nav_enter, R.anim.nav_exit)
                .replace(R.id.home_content, fragment).commit();
        setTitle(titlefrag);
        toolbar.setTitle(title());
    }

    private void showDeleteActFragment(String title) {
        titlefrag = title;
        Fragment fragment = new DeleteAct();
        Bundle mBundle = new Bundle();
        mBundle.putString(
                "act", user);
        mBundle.putString("psd", pswd);
        fragment.setArguments(mBundle);
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.nav_enter, R.anim.nav_exit)
                .replace(R.id.home_content, fragment).commit();
        setTitle(titlefrag);
        toolbar.setTitle(title());
    }

    private Spannable title() {
        Spannable ss = new SpannableStringBuilder(titlefrag);
        ss.setSpan(new ForegroundColorSpan(Color.WHITE), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    @Override
    public void onDrawerSlide(@NonNull View view, float v) {
    }

    @Override
    public void onDrawerOpened(@NonNull View view) {
    }

    @Override
    public void onDrawerClosed(@NonNull View view) {
    }

    @Override
    public void onDrawerStateChanged(int i) {
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private void quitDevice() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // Setting Alert Dialog Title
        alertDialogBuilder.setTitle(getString(R.string.off));
        // Icon Of Alert Dialog
        alertDialogBuilder.setIcon(R.mipmap.oneval_logo);
        // Setting Alert Dialog Message
        alertDialogBuilder.setMessage(getString(R.string.off_msg));
        alertDialogBuilder.setCancelable(true);

        alertDialogBuilder.setPositiveButton(HtmlCompat.fromHtml("<font color='yellow'>" + getString(R.string.yes) + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY), (arg0, arg1) -> {
            isYesClicked = true;
            arg0.cancel();
        });
        alertDialogBuilder.setNeutralButton(HtmlCompat.fromHtml("<font color='#848482'>" + getString(R.string.cancel) + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY), (dialog, which) -> {
            isYesClicked = false;
            dialog.cancel();
        });
        alertDialogBuilder.setOnCancelListener(di -> {
            if (isYesClicked) {
                try {
                    ONEVAL.TIMER.cancel();
                    ONEVAL.TIMER.purge();
                } catch (Exception ignored) {
                }
                try {
                    ONEVAL.TIMER2.cancel();
                    ONEVAL.TIMER2.purge();
                } catch (Exception ignored) {
                }
                Utils.clearAccFromApp(getApplicationContext());
                finish();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    @Override
    public void OnDataLoaded(JSONObject response) {
        notif_list.clear();
        try {
            JSONArray jSONArray = response.getJSONArray("feed");
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject obj = jSONArray.getJSONObject(i);
                String cont = obj.getString("content");
                String dtime = obj.getString("dtime");
                String ptime = dtime.substring(0, 2) + ":" + dtime.substring(2, 4) + "." + dtime.substring(4, 6);
                String content = cont + "<br><i>(" + getString(R.string.bc_time) + " " + ptime + ")</i>";
                notif_list.add(HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY));
            }
        } catch (JSONException je) {
            Toast.makeText(getApplicationContext(), getString(R.string.data_error), Toast.LENGTH_SHORT).show();
        }
        int notif_size = notif_list.size();
        setBadgeCount(notif_size);
    }

    private void refreshFeed() {
        ONEVAL.TIMER = new Timer();
        final Handler handler = new Handler();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> {
                    try {
                        if (Utils.isConnectionAvailable(getApplicationContext()) == false) {
                            Utils.showNoConnectionAlert(getApplicationContext(), drawerLayout);
                            ONEVAL.ISCONNECTED = false;
                        } else {
                            ONEVAL.ISCONNECTED = true;
                            Utils.connectToServer(HomeActivity.this, ONEVAL.FEED, new String[]{
                                    "user", "pswd", "tkn"
                            }, new String[]{
                                    user, pswd, Utils.getTkn(getApplicationContext())
                            }, false, HomeActivity.this);
                        }
                    } catch (Exception ignored) {
                    }
                });
            }
        };
        ONEVAL.TIMER.schedule(doAsynchronousTask,
                0,
                30000);
    }

}