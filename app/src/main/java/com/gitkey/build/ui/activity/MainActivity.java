package com.gitkey.build.ui.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.gitkey.build.R;
import com.gitkey.build.ui.fragment.HomeFragment;
import com.gitkey.build.ui.fragment.KeysFragment;
import com.gitkey.build.ui.fragment.SettingsFragment;
import com.gitkey.build.ui.fragment.ToolsFragment;
import com.gitkey.build.utils.PreferencesManager;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private int activeTab = 0; // 0: Home, 1: Keys, 2: Tools, 3: Settings

    private View tabHome, tabKeys, tabTools, tabSettings;
    private View pillTabHome, pillTabKeys, pillTabTools, pillTabSettings;
    private ImageView ivTabHome, ivTabKeys, ivTabTools, ivTabSettings;
    private TextView tvTabHome, tvTabKeys, tvTabTools, tvTabSettings;
    private ExtendedFloatingActionButton fabCreateKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PreferencesManager prefs = new PreferencesManager(this);
        PreferencesManager.applyTheme(prefs.getThemeMode());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();

        if (savedInstanceState == null) {
            selectTab(0);
        }
    }

    private void initViews() {
        tabHome = findViewById(R.id.tabHome);
        tabKeys = findViewById(R.id.tabKeys);
        tabTools = findViewById(R.id.tabTools);
        tabSettings = findViewById(R.id.tabSettings);

        pillTabHome = findViewById(R.id.pillTabHome);
        pillTabKeys = findViewById(R.id.pillTabKeys);
        pillTabTools = findViewById(R.id.pillTabTools);
        pillTabSettings = findViewById(R.id.pillTabSettings);

        ivTabHome = findViewById(R.id.ivTabHome);
        ivTabKeys = findViewById(R.id.ivTabKeys);
        ivTabTools = findViewById(R.id.ivTabTools);
        ivTabSettings = findViewById(R.id.ivTabSettings);

        tvTabHome = findViewById(R.id.tvTabHome);
        tvTabKeys = findViewById(R.id.tvTabKeys);
        tvTabTools = findViewById(R.id.tvTabTools);
        tvTabSettings = findViewById(R.id.tvTabSettings);

        fabCreateKey = findViewById(R.id.fabCreateKey);
    }

    private void setupListeners() {
        tabHome.setOnClickListener(v -> selectTab(0));
        tabKeys.setOnClickListener(v -> selectTab(1));
        tabTools.setOnClickListener(v -> selectTab(2));
        tabSettings.setOnClickListener(v -> selectTab(3));

        fabCreateKey.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateKeyActivity.class);
            startActivity(intent);
        });
    }

    public void selectTab(int index) {
        activeTab = index;
        Fragment fragment;
        switch (index) {
            case 1:
                fragment = new KeysFragment();
                fabCreateKey.show();
                break;
            case 2:
                fragment = new ToolsFragment();
                fabCreateKey.hide();
                break;
            case 3:
                fragment = new SettingsFragment();
                fabCreateKey.hide();
                break;
            case 0:
            default:
                fragment = new HomeFragment();
                fabCreateKey.show();
                break;
        }

        updateTabUi();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        ft.replace(R.id.fragmentContainer, fragment);
        ft.commit();
    }

    private void updateTabUi() {
        TypedValue primaryVal = new TypedValue();
        TypedValue mutedVal = new TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, primaryVal, true);
        getTheme().resolveAttribute(R.attr.colorTextMuted, mutedVal, true);

        int colorPrimary = primaryVal.data;
        int colorMuted = mutedVal.data;

        setTabState(pillTabHome, ivTabHome, tvTabHome, activeTab == 0, colorPrimary, colorMuted);
        setTabState(pillTabKeys, ivTabKeys, tvTabKeys, activeTab == 1, colorPrimary, colorMuted);
        setTabState(pillTabTools, ivTabTools, tvTabTools, activeTab == 2, colorPrimary, colorMuted);
        setTabState(pillTabSettings, ivTabSettings, tvTabSettings, activeTab == 3, colorPrimary, colorMuted);
    }

    private void setTabState(View pill, ImageView iv, TextView tv, boolean isSelected, int activeColor, int inactiveColor) {
        int color = isSelected ? activeColor : inactiveColor;
        iv.setImageTintList(ColorStateList.valueOf(color));
        tv.setTextColor(color);
        tv.setTypeface(null, isSelected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        if (pill != null) {
            if (isSelected) {
                pill.setBackgroundResource(R.drawable.bg_nav_pill_active);
                pill.setScaleX(0.85f);
                pill.setScaleY(0.85f);
                pill.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(220)
                        .setInterpolator(new android.view.animation.OvershootInterpolator(2.0f))
                        .start();
            } else {
                pill.setBackgroundResource(0);
                pill.setScaleX(1.0f);
                pill.setScaleY(1.0f);
            }
        }
    }
}
