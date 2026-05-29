package com.tommasov.mg4swipenovalauncher;

import android.os.Build;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.accessibility.AccessibilityEvent;

public class AccService extends AccessibilityService {

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.tommasov.mg4swipenovalauncher.ACTION_BACK");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(backReceiver, filter, RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(backReceiver, filter);
        }
    }

    private final BroadcastReceiver backReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            performBackAction();
        }
    };

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }

    public void performBackAction() {
        try { performGlobalAction(GLOBAL_ACTION_BACK); } catch (Exception ignored) {}
    }

    @Override
    public void onDestroy() {
        try { unregisterReceiver(backReceiver); } catch (Exception ignored) {}
        super.onDestroy();
    }
}
