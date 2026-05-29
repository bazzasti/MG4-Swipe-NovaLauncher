package com.tommasov.mg4swipenovalauncher

import android.content.Context
import android.content.SharedPreferences

/** Singleton SharedPreferences wrapper for all app settings. */
class PreferencesManager private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var selectedPackage: String?
        get() = prefs.getString(KEY_PACKAGE_NAME, null)
        set(v) = prefs.edit().putString(KEY_PACKAGE_NAME, v).apply()

    var backButtonVisibility: String
        get() = prefs.getString(KEY_BACK_BUTTON_VISIBILITY, "INVISIBLE") ?: "INVISIBLE"
        set(v) = prefs.edit().putString(KEY_BACK_BUTTON_VISIBILITY, v).apply()

    val isBackButtonHidden: Boolean get() = backButtonVisibility != "VISIBLE"

    // ── Floating button position ──

    var buttonX: Int
        get() = prefs.getInt("btn_x", 25)
        set(v) = prefs.edit().putInt("btn_x", v).apply()

    var buttonY: Int
        get() = prefs.getInt("btn_y", 5)
        set(v) = prefs.edit().putInt("btn_y", v).apply()

    fun saveButtonPosition(x: Int, y: Int) = prefs.edit().putInt("btn_x", x).putInt("btn_y", y).apply()

    // ── Swipe sensitivity ──

    var swipeThreshold: Int
        get() = prefs.getInt("swipe_threshold", 100)
        set(v) = prefs.edit().putInt("swipe_threshold", v).apply()

    var swipeVelocity: Int
        get() = prefs.getInt("swipe_velocity", 100)
        set(v) = prefs.edit().putInt("swipe_velocity", v).apply()

    companion object {
        private const val PREFS_NAME = "SwipeServicePrefs"
        private const val KEY_PACKAGE_NAME = "packageName"
        private const val KEY_BACK_BUTTON_VISIBILITY = "backButtonVisibility"

        @Volatile private var instance: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager =
            instance ?: synchronized(this) {
                instance ?: PreferencesManager(context).also { instance = it }
            }
    }
}
