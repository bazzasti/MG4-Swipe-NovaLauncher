package com.tommasov.mg4swipenovalauncher

import android.util.Log

/** Centralised logging for MG4 Swipe. All calls go through here. */
object AppLogger {
    private const val TAG = "MG4Swipe"

    fun i(msg: String) = Log.i(TAG, msg)
    fun w(msg: String) = Log.w(TAG, msg)
    fun e(msg: String) = Log.e(TAG, msg)
    fun e(msg: String, t: Throwable) = Log.e(TAG, msg, t)
}
