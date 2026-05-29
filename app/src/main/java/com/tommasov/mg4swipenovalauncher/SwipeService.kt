package com.tommasov.mg4swipenovalauncher

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.WindowMetrics
import android.widget.Toast

@SuppressLint("ClickableViewAccessibility")
class SwipeService : Service() {

    private lateinit var prefs: PreferencesManager
    private lateinit var windowManager: WindowManager
    private var leftSwipeArea: View? = null
    private var rightSwipeArea: View? = null
    private var floatingButton: View? = null
    private var swipeThreshold = 100
    private var swipeVelocityThreshold = 100

    override fun onCreate() {
        super.onCreate()

        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        prefs = PreferencesManager.getInstance(this)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        createNotificationChannel()

        val openApp = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, openApp, PendingIntent.FLAG_IMMUTABLE)

        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.mipmap.ismart_launcher)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
        AppLogger.i("SwipeService started")

        swipeThreshold = prefs.swipeThreshold
        swipeVelocityThreshold = prefs.swipeVelocity

        try {
            setupSwipeZones()
        } catch (e: Exception) {
            AppLogger.e("Failed to init swipe zones — stopping service", e)
            stopSelf()
            return
        }

        try {
            setupBackButton()
        } catch (e: Exception) {
            AppLogger.e("Failed to init back button", e)
        }
    }

    // ── Gesture listener ─────────────────────────────────────────────────────

    private inner class SwipeUpListener(private val action: Runnable) :
        GestureDetector.SimpleOnGestureListener() {

        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            val startY = e1?.rawY ?: return false
            val diffY = e2.rawY - startY
            if (diffY < 0 && Math.abs(diffY) > swipeThreshold && Math.abs(velocityY) > swipeVelocityThreshold) {
                action.run()
                return true
            }
            return false
        }
    }

    // ── Swipe zones ──────────────────────────────────────────────────────────

    private fun setupSwipeZones() {
        leftSwipeArea = View(this)
        rightSwipeArea = View(this)

        val screenWidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.bounds.width()
        } else {
            @Suppress("DEPRECATION")
            val size = Point().also { windowManager.defaultDisplay.getSize(it) }
            size.x
        }

        val halfWidth = screenWidth / 2
        val zoneHeight = dpToPx(12)

        val leftParams = overlayParams(halfWidth, zoneHeight, Gravity.BOTTOM or Gravity.LEFT)
        val rightParams = overlayParams(halfWidth, zoneHeight, Gravity.BOTTOM or Gravity.RIGHT)

        val leftDetector = GestureDetector(this, SwipeUpListener { performBackAction() })
        val rightDetector = GestureDetector(this, SwipeUpListener { openLauncher() })

        try {
            windowManager.addView(leftSwipeArea, leftParams)
            windowManager.addView(rightSwipeArea, rightParams)
            AppLogger.i("Swipe zones added")
        } catch (e: Exception) {
            AppLogger.e("Failed to add swipe zones", e)
        }

        leftSwipeArea?.setOnTouchListener { _, event -> leftDetector.onTouchEvent(event) }
        rightSwipeArea?.setOnTouchListener { _, event -> rightDetector.onTouchEvent(event) }
    }

    // ── Floating back button ─────────────────────────────────────────────────

    private fun setupBackButton() {
        if (prefs.isBackButtonHidden) return

        floatingButton = LayoutInflater.from(this).inflate(R.layout.layout_floating_button, null)

        val params = overlayParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            Gravity.TOP or Gravity.LEFT
        ).apply {
            x = prefs.buttonX
            y = prefs.buttonY
        }

        try {
            windowManager.addView(floatingButton, params)
            AppLogger.i("Floating back button added")
        } catch (e: Exception) {
            AppLogger.e("Failed to add floating button", e)
        }

        val clickThreshold = dpToPx(5)
        var initialX = 0; var initialY = 0
        var initialTouchX = 0f; var initialTouchY = 0f

        floatingButton?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    floatingButton?.isPressed = true
                    initialX = params.x; initialY = params.y
                    initialTouchX = event.rawX; initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(floatingButton, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    floatingButton?.isPressed = false
                    if (Math.abs(event.rawX - initialTouchX) <= clickThreshold &&
                        Math.abs(event.rawY - initialTouchY) <= clickThreshold) {
                        floatingButton?.performClick()
                    } else {
                        prefs.saveButtonPosition(params.x, params.y)
                    }
                    true
                }
                MotionEvent.ACTION_CANCEL -> { floatingButton?.isPressed = false; true }
                else -> false
            }
        }
        floatingButton?.setOnClickListener { performBackAction() }
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    private fun performBackAction() = AccService.triggerBack()

    private fun openLauncher() {
        var packageName = prefs.selectedPackage ?: DEFAULT_LAUNCHER
        try {
            var intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent == null && packageName != DEFAULT_LAUNCHER) {
                AppLogger.w("Selected package gone: $packageName — trying default")
                packageName = DEFAULT_LAUNCHER
                intent = packageManager.getLaunchIntentForPackage(packageName)
            }
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(intent)
                AppLogger.i("Launched: $packageName")
            } else {
                AppLogger.w("No launchable app found")
                Toast.makeText(this, getString(R.string.toast_no_app), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            AppLogger.e("Failed to launch $packageName", e)
        }
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        AppLogger.i("Configuration changed — recreating swipe zones")
        try { leftSwipeArea?.let { windowManager.removeView(it) } } catch (_: Exception) {}
        try { rightSwipeArea?.let { windowManager.removeView(it) } } catch (_: Exception) {}
        leftSwipeArea = null; rightSwipeArea = null
        try { setupSwipeZones() } catch (e: Exception) { AppLogger.e("Failed to recreate zones", e) }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Settings.canDrawOverlays(this)) {
            AppLogger.w("Overlay permission revoked — stopping service")
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        AppLogger.i("SwipeService destroyed")
        super.onDestroy()
        try { leftSwipeArea?.let { windowManager.removeView(it) } } catch (_: Exception) {}
        try { rightSwipeArea?.let { windowManager.removeView(it) } } catch (_: Exception) {}
        try { floatingButton?.let { windowManager.removeView(it) } } catch (_: Exception) {}
        leftSwipeArea = null; rightSwipeArea = null; floatingButton = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun dpToPx(dp: Int) = (dp * resources.displayMetrics.density).toInt()

    private fun overlayParams(w: Int, h: Int, gravity: Int) = WindowManager.LayoutParams(
        w, h,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    ).apply { this.gravity = gravity }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, getString(R.string.notification_channel), NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "SwipeServiceChannel"
        const val DEFAULT_LAUNCHER = "com.teslacoilsw.launcher"
    }
}
