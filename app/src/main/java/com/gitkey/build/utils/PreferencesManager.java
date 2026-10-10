package com.gitkey.build.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class PreferencesManager {
    private static final String PREF_NAME = "gitkey_user_settings";
    private static final String KEY_THEME = "app_theme_mode"; // 0 = System, 1 = Light, 2 = Dark
    private static final String KEY_CONFIRM_EXPORT = "pref_confirm_export";
    private static final String KEY_HIDE_SECRETS = "pref_hide_secrets";

    private final SharedPreferences prefs;

    public PreferencesManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public int getThemeMode() {
        return prefs.getInt(KEY_THEME, 0);
    }

    public void setThemeMode(int mode) {
        prefs.edit().putInt(KEY_THEME, mode).apply();
        applyTheme(mode);
    }

    public boolean isRequireExportConfirmation() {
        return prefs.getBoolean(KEY_CONFIRM_EXPORT, true);
    }

    public void setRequireExportConfirmation(boolean value) {
        prefs.edit().putBoolean(KEY_CONFIRM_EXPORT, value).apply();
    }

    public boolean isHideSecretsByDefault() {
        return prefs.getBoolean(KEY_HIDE_SECRETS, true);
    }

    public void setHideSecretsByDefault(boolean value) {
        prefs.edit().putBoolean(KEY_HIDE_SECRETS, value).apply();
    }

    public static void applyTheme(int mode) {
        switch (mode) {
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 2:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case 0:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}
