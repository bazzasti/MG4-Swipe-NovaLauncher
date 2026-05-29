package com.tommasov.mg4swipenovalauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Starts SwipeService as a foreground service when the car boots up. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            try {
                context.startForegroundService(Intent(context, SwipeService::class.java))
                AppLogger.i("SwipeService started from boot")
            } catch (e: Exception) {
                AppLogger.e("Failed to start SwipeService from boot", e)
            }
        }
    }
}
