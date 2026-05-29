package com.tommasov.mg4swipenovalauncher

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * Accessibility service that performs GLOBAL_ACTION_BACK when triggered.
 * Uses a static callback — no broadcasts, no security risk.
 */
class AccService : AccessibilityService() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        AppLogger.i("AccService started")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onDestroy() {
        instance = null
        AppLogger.i("AccService destroyed")
        super.onDestroy()
    }

    companion object {
        @Volatile private var instance: AccService? = null

        /** Called by SwipeService to trigger the Back action */
        fun triggerBack() {
            val svc = instance
            if (svc != null) {
                try { svc.performGlobalAction(GLOBAL_ACTION_BACK) }
                catch (e: Exception) { AppLogger.e("Back action failed", e) }
            } else {
                AppLogger.w("AccService not running — cannot perform Back")
            }
        }
    }
}
