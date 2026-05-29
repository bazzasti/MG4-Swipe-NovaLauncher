package com.tommasov.mg4swipenovalauncher;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

/**
 * Accessibility service that performs GLOBAL_ACTION_BACK when triggered.
 * Uses a static callback instead of broadcasts (no security risk, no deprecated APIs).
 */
public class AccService extends AccessibilityService {

    private static AccService instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        AppLogger.i("AccService started");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {}

    /** Called by SwipeService to trigger the Back action */
    public static void triggerBack() {
        if (instance != null) {
            try {
                instance.performGlobalAction(GLOBAL_ACTION_BACK);
            } catch (Exception e) {
                AppLogger.e("Back action failed", e);
            }
        } else {
            AppLogger.w("AccService not running — cannot perform Back");
        }
    }

    @Override
    public void onDestroy() {
        instance = null;
        AppLogger.i("AccService destroyed");
        super.onDestroy();
    }
}
