package com.tommasov.mg4swipenovalauncher;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Display;
import android.view.WindowMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class SwipeService extends Service {
    private PreferencesManager preferencesManager;
    private static final String CHANNEL_ID = "SwipeServiceChannel";
    private WindowManager windowManager;
    private View leftSwipeArea;
    private View rightSwipeArea;
    private View floatingButton;
    private GestureDetector leftGestureDetector;
    private GestureDetector rightGestureDetector;

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();

        if (Settings.canDrawOverlays(this)) {
            createNotificationChannel();
            Intent openApp = new Intent(this, MainActivity.class);
            openApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, openApp, PendingIntent.FLAG_IMMUTABLE);

            Notification notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("MG4 Swipe Active")
                    .setContentText("Tap to change settings")
                    .setSmallIcon(R.mipmap.ismart_launcher)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(1, notification);
            AppLogger.i("SwipeService started");

            swipeThreshold = preferencesManager.getSwipeThreshold();
            swipeVelocityThreshold = preferencesManager.getSwipeVelocity();

            try {
                swipe();
            } catch (Exception e) {
                AppLogger.e("Failed to init swipe zones — stopping service", e);
                stopSelf();
                return;
            }
            try {
                backButton();
            } catch (Exception e) {
                AppLogger.e("Failed to init back button", e);
            }

        } else {
            stopSelf();
        }
    }

    private int swipeThreshold = 100;
    private int swipeVelocityThreshold = 100;

    /** Reusable swipe-up gesture listener that triggers a callback on fling-up */
    private class SwipeUpListener extends SimpleOnGestureListener {
        private final Runnable action;
        SwipeUpListener(Runnable action) { this.action = action; }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffY = e2.getRawY() - e1.getRawY();
            if (diffY < 0 && Math.abs(diffY) > swipeThreshold && Math.abs(velocityY) > swipeVelocityThreshold) {
                action.run();
                return true;
            }
            return false;
        }
    }

    private void performBackAction() {
        Intent intent = new Intent("com.tommasov.mg4swipenovalauncher.ACTION_BACK");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    static final String DEFAULT_LAUNCHER = "com.teslacoilsw.launcher";

    private void openLauncher() {
        String packageName = preferencesManager.getSelectedPackage();
        if (packageName == null) {
            packageName = DEFAULT_LAUNCHER;
        }
        // Validate package still exists
        if (getPackageManager().getLaunchIntentForPackage(packageName) == null) {
            AppLogger.w("Selected package gone: " + packageName + " — falling back to default");
            packageName = DEFAULT_LAUNCHER;
        }

        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                AppLogger.i("Launched: " + packageName);
            } else {
                AppLogger.w("Package not found: " + packageName);
                Toast.makeText(this, "Package not found: " + packageName, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            AppLogger.e("Failed to launch " + packageName, e);
        }
    }

    private void swipe() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        leftSwipeArea = new View(this);
        rightSwipeArea = new View(this);

        int layoutFlags;
        layoutFlags = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

        int screenWidth;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics metrics = windowManager.getCurrentWindowMetrics();
            screenWidth = metrics.getBounds().width();
        } else {
            Display display = windowManager.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenWidth = size.x;
        }

        int halfScreenWidth = screenWidth / 2;

        WindowManager.LayoutParams leftParams = new WindowManager.LayoutParams(
                halfScreenWidth,
                dpToPx(12),
                layoutFlags,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        leftParams.gravity = Gravity.BOTTOM | Gravity.LEFT;

        WindowManager.LayoutParams rightParams = new WindowManager.LayoutParams(
                halfScreenWidth,
                dpToPx(12),
                layoutFlags,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        rightParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;

        try {
            windowManager.addView(leftSwipeArea, leftParams);
            windowManager.addView(rightSwipeArea, rightParams);
            AppLogger.i("Swipe zones added");
        } catch (Exception e) {
            AppLogger.e("Failed to add swipe zones", e);
        }

        leftGestureDetector = new GestureDetector(this, new SwipeUpListener(this::performBackAction));
        rightGestureDetector = new GestureDetector(this, new SwipeUpListener(this::openLauncher));

        leftSwipeArea.setOnTouchListener((v, event) -> leftGestureDetector.onTouchEvent(event));

        rightSwipeArea.setOnTouchListener((v, event) -> rightGestureDetector.onTouchEvent(event));
    }

    private void backButton() {
        preferencesManager = new PreferencesManager(this);

        String backButtonVisibility = preferencesManager.getBackButtonVisibility();

        if (backButtonVisibility.equals("INVISIBLE")) {
            return;
        }

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        floatingButton = LayoutInflater.from(this).inflate(R.layout.layout_floating_button, null);

        int layoutFlags;
        layoutFlags = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlags,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = preferencesManager.getButtonX();
        params.y = preferencesManager.getButtonY();

        try {
            windowManager.addView(floatingButton, params);
            AppLogger.i("Floating back button added");
        } catch (Exception e) {
            AppLogger.e("Failed to add floating button", e);
        }

        floatingButton.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private static final int CLICK_ACTION_THRESHOLD = 10;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        floatingButton.setPressed(true);
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        int deltaX = (int) (event.getRawX() - initialTouchX);
                        int deltaY = (int) (event.getRawY() - initialTouchY);
                        params.x = initialX + deltaX;
                        params.y = initialY + deltaY;
                        windowManager.updateViewLayout(floatingButton, params);
                        return true;

                    case MotionEvent.ACTION_UP:
                        floatingButton.setPressed(false);

                        if (Math.abs(event.getRawX() - initialTouchX) <= CLICK_ACTION_THRESHOLD &&
                                Math.abs(event.getRawY() - initialTouchY) <= CLICK_ACTION_THRESHOLD) {
                            floatingButton.performClick();
                        } else {
                            // Save new position after drag
                            preferencesManager.saveButtonPosition(params.x, params.y);
                        }

                        return true;

                    case MotionEvent.ACTION_CANCEL:
                        floatingButton.setPressed(false);
                        return true;
                }
                return false;
            }
        });
        floatingButton.setOnClickListener(v -> performBackAction());
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        AppLogger.i("Configuration changed — recreating swipe zones");
        try {
            if (leftSwipeArea != null) windowManager.removeView(leftSwipeArea);
            if (rightSwipeArea != null) windowManager.removeView(rightSwipeArea);
        } catch (Exception ignored) {}
        leftSwipeArea = null;
        rightSwipeArea = null;
        try { swipe(); } catch (Exception e) { AppLogger.e("Failed to recreate zones", e); }
    }

    @Override
    public void onDestroy() {
        AppLogger.i("SwipeService destroyed");
        super.onDestroy();
        try {
            if (leftSwipeArea != null) windowManager.removeView(leftSwipeArea);
        } catch (Exception ignored) {}
        try {
            if (rightSwipeArea != null) windowManager.removeView(rightSwipeArea);
        } catch (Exception ignored) {}
        try {
            if (floatingButton != null) windowManager.removeView(floatingButton);
        } catch (Exception ignored) {}
        leftSwipeArea = null;
        rightSwipeArea = null;
        floatingButton = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!Settings.canDrawOverlays(this)) {
            AppLogger.w("Overlay permission revoked — stopping service");
            stopSelf();
            return START_NOT_STICKY;
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Swipe Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}
