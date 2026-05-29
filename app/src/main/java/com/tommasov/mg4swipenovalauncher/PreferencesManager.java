package com.tommasov.mg4swipenovalauncher;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {
    private static final String PREFS_NAME = "SwipeServicePrefs";
    private static final String KEY_PACKAGE_NAME = "packageName";
    private static final String KEY_BACK_BUTTON_VISIBILITY = "backButtonVisibility";

    private static PreferencesManager instance;
    private final SharedPreferences sharedPreferences;

    private PreferencesManager(Context context) {
        this.sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized PreferencesManager getInstance(Context context) {
        if (instance == null) instance = new PreferencesManager(context);
        return instance;
    }

    public void saveSelectedPackage(String packageName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PACKAGE_NAME, packageName);
        editor.apply();
    }

    public String getSelectedPackage() {
        return sharedPreferences.getString(KEY_PACKAGE_NAME, null);
    }

    public void saveBackButtonVisibility(String visibility) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_BACK_BUTTON_VISIBILITY, visibility);
        editor.apply();
    }

    public String getBackButtonVisibility() {
        return sharedPreferences.getString(KEY_BACK_BUTTON_VISIBILITY, "INVISIBLE");
    }

    /** Convenience: true if the floating back button is hidden */
    public boolean isBackButtonHidden() {
        return !"VISIBLE".equals(getBackButtonVisibility());
    }

    // ── Floating button position ──

    public void saveButtonPosition(int x, int y) {
        sharedPreferences.edit().putInt("btn_x", x).putInt("btn_y", y).apply();
    }

    public int getButtonX() { return sharedPreferences.getInt("btn_x", 25); }

    // ── Swipe sensitivity ──

    public int getSwipeThreshold() { return sharedPreferences.getInt("swipe_threshold", 100); }
    public int getSwipeVelocity() { return sharedPreferences.getInt("swipe_velocity", 100); }

    public void saveSwipeSensitivity(int threshold, int velocity) {
        sharedPreferences.edit().putInt("swipe_threshold", threshold).putInt("swipe_velocity", velocity).apply();
    }
    public int getButtonY() { return sharedPreferences.getInt("btn_y", 5); }
}
