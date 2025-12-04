package com.vishnu.expensetracker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {
    
    private static final String THEME_PREFS = "theme_prefs";
    private static final String THEME_KEY = "selected_theme";
    
    // Theme constants
    public static final int THEME_LIGHT = AppCompatDelegate.MODE_NIGHT_NO;
    public static final int THEME_DARK = AppCompatDelegate.MODE_NIGHT_YES;
    public static final int THEME_SYSTEM = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    
    private static ThemeManager instance;
    private SharedPreferences preferences;
    
    private ThemeManager(Context context) {
        preferences = context.getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE);
    }
    
    public static synchronized ThemeManager getInstance(Context context) {
        if (instance == null) {
            instance = new ThemeManager(context.getApplicationContext());
        }
        return instance;
    }
    
    public void setTheme(int theme) {
        preferences.edit().putInt(THEME_KEY, theme).apply();
        AppCompatDelegate.setDefaultNightMode(theme);
    }
    
    public int getCurrentTheme() {
        return preferences.getInt(THEME_KEY, THEME_SYSTEM);
    }
    
    public void applyTheme() {
        AppCompatDelegate.setDefaultNightMode(getCurrentTheme());
    }
    
    public String getThemeName() {
        int theme = getCurrentTheme();
        switch (theme) {
            case THEME_LIGHT:
                return "Light Mode";
            case THEME_DARK:
                return "Dark Mode";
            case THEME_SYSTEM:
                return "System Default";
            default:
                return "System Default";
        }
    }
    
    public boolean isDarkMode() {
        return getCurrentTheme() == THEME_DARK;
    }
    
    public boolean isLightMode() {
        return getCurrentTheme() == THEME_LIGHT;
    }
    
    public boolean isSystemMode() {
        return getCurrentTheme() == THEME_SYSTEM;
    }
}