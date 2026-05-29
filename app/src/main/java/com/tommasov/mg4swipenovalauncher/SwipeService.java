package com.tommasov.mg4swipenovalauncher;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
            Notification notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("MG4 Nova Launcher Swipe Service")
                    .setSmallIcon(R.mipmap.ismart_launcher)
                    .build();
            startForeground(1, notification);
            AppLogger.i("SwipeService started");

            try {
                swipe();
            } catch (Exception e) {
                AppLogger.e("Failed to init swipe zones", e);
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

    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    /** Reusable swipe-up gesture listener that triggers a callback on fling-up */
    private class SwipeUpListener extends SimpleOnGestureListener {
        private final Runnable action;
        SwipeUpListener(Runnable action) { this.action = action; }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffY = e2.getRawY() - e1.getRawY();
            if (diffY < 0 && Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                action.run();
                return true;
            }
            return false;
        }
    }

    private void performBackAction() {
        Intent intent = new Intent("com.tommasov.mg4swipenovalauncher.ACTION_BACK");
        sendBroadcast(intent);
    }

    static final String DEFAULT_LAUNCHER = "com.teslacoilsw.launcher";

    private void openLauncher() {
        String packageName = preferencesManager.getSelectedPackage();
        if (packageName == null) {
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

        leftSwipeArea.setOnTouchListener(new View.OnTouchListener() { @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return leftGestureDetector.onTouchEvent(event);
            }
        });

        rightSwipeArea.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return rightGestureDetector.onTouchEvent(event);
            }
        });
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
        params.x = 25;
        params.y = 5;

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
                        }

                        return true;

                    case MotionEvent.ACTION_CANCEL:
                        floatingButton.setPressed(false);
                        return true;
                }
                return false;
            }
        });
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performBackAction();
            }
        });
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
