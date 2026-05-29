package com.tommasov.mg4swipenovalauncher;

import android.util.Log;

/**
 * Centralised logging for MG4 Swipe.
 * All log calls go through here so they can be toggled or redirected.
 */
public final class AppLogger {
    private static final String TAG = "MG4Swipe";

    public static void i(String msg) { Log.i(TAG, msg); }
    public static void w(String msg) { Log.w(TAG, msg); }
    public static void e(String msg) { Log.e(TAG, msg); }
    public static void e(String msg, Throwable t) { Log.e(TAG, msg, t); }
}
