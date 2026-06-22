package org.elnix.dragonlauncher.timer

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.i18n.R
import io.github.elnix90.logging.OVERLAY_REMINDER_TAG
import io.github.elnix90.logging.logD
import io.github.elnix90.logging.logE
import io.github.elnix90.logging.logW
import org.elnix.dragonlauncher.settings.stores.map.WellbeingSettingsStore

/**
 * Service that displays a non-intrusive overlay popup at the top of the screen
 * using plain Android Views (no Compose) for maximum reliability.
 *
 * Uses WindowManager with FLAG_NOT_FOCUSABLE + FLAG_NOT_TOUCH_MODAL so
 * touches outside the popup pass through to the app below.
 */
class OverlayReminderService : Service() {

    companion object {
        const val EXTRA_APP_NAME = "extra_app_name"
        const val EXTRA_MODE = "extra_mode" // "reminder" or "time_warning"
        const val EXTRA_SESSION_TIME = "extra_session_time"
        const val EXTRA_TODAY_TIME = "extra_today_time"
        const val EXTRA_REMAINING_TIME = "extra_remaining_time"
        const val EXTRA_HAS_LIMIT = "extra_has_limit"

        private const val DISMISS_DELAY = 7000L

        fun show(
            ctx: Context,
            appName: String,
            sessionTime: String,
            todayTime: String,
            remainingTime: String,
            hasLimit: Boolean,
            mode: String = "reminder"
        ) {
            if (!Settings.canDrawOverlays(ctx)) {
                logW(OVERLAY_REMINDER_TAG) { "Cannot show overlay: permission not granted" }
                return
            }
            try {
                val intent = Intent(ctx, OverlayReminderService::class.java).apply {
                    putExtra(EXTRA_APP_NAME, appName)
                    putExtra(EXTRA_SESSION_TIME, sessionTime)
                    putExtra(EXTRA_TODAY_TIME, todayTime)
                    putExtra(EXTRA_REMAINING_TIME, remainingTime)
                    putExtra(EXTRA_HAS_LIMIT, hasLimit)
                    putExtra(EXTRA_MODE, mode)
                }
                ctx.startService(intent)
            } catch (e: Exception) {
                logE(OVERLAY_REMINDER_TAG, e) { "Failed to start overlay service" }
            }
        }
    }


    private val colorCardBg = "#E6192133".toColorInt()        // dark with slight transparency
    private val colorCardBorder = "#806C5CE7".toColorInt()    // purple border
    private val colorTextPrimary = "#FFFFFFFF".toColorInt()
    private val colorTextSecondary = "#B3FFFFFF".toColorInt()  // white 70%
    private val colorAccentTeal = "#FF00CEC9".toColorInt()
    private val colorDivider = "#33FFFFFF".toColorInt()        // white 20%

    // Time warning mode (dark + orange/red accent)
    private val colorWarningBg = "#E62D1B3D".toColorInt()
    private val colorWarningBorder = "#80FFA502".toColorInt()
    private val colorWarningAccent = "#FFFFA502".toColorInt()
    private val colorWarningText = "#CCFFA502".toColorInt()   // orange 80%

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private val handler = Handler(Looper.getMainLooper())
    private var pulseAnimator: ValueAnimator? = null
    private var autoDismissRunnable: Runnable? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            if (!Settings.canDrawOverlays(this)) {
                logW(OVERLAY_REMINDER_TAG) { "Overlay permission not granted" }
                stopSelf()
                return START_NOT_STICKY
            }

            removeOverlay()

            val appName = intent?.getStringExtra(EXTRA_APP_NAME) ?: "App"
            val mode = intent?.getStringExtra(EXTRA_MODE) ?: "reminder"
            val sessionTime = intent?.getStringExtra(EXTRA_SESSION_TIME) ?: ""
            val todayTime = intent?.getStringExtra(EXTRA_TODAY_TIME) ?: ""
            val remainingTime = intent?.getStringExtra(EXTRA_REMAINING_TIME) ?: ""
            val hasLimit = intent?.getBooleanExtra(EXTRA_HAS_LIMIT, false) ?: false

            logD(OVERLAY_REMINDER_TAG) { "onStartCommand: mode=$mode, app=$appName" }

            showOverlay(appName, sessionTime, todayTime, remainingTime, hasLimit, mode)
        } catch (e: Exception) {
            logE(OVERLAY_REMINDER_TAG, e) { "Error in onStartCommand" }
            stopSelf()
        }
        return START_NOT_STICKY
    }


    private fun showOverlay(
        appName: String,
        sessionTime: String,
        todayTime: String,
        remainingTime: String,
        hasLimit: Boolean,
        mode: String
    ) {
        // Launch coroutine to read preferences asynchronously
        serviceScope.launch {
            try {
                // Read user preferences (which stats to show)
                val showSession = WellbeingSettingsStore.popupShowSessionTime.get(this@OverlayReminderService)
                val showToday = WellbeingSettingsStore.popupShowTodayTime.get(this@OverlayReminderService)
                val showRemaining =
                    WellbeingSettingsStore.popupShowRemainingTime.get(this@OverlayReminderService)

                val isWarning = mode == "time_warning"

                windowManager = applicationContext.getSystemService(WINDOW_SERVICE) as WindowManager

                // Build the view hierarchy
                val container = buildOverlayView(
                    appName, sessionTime, todayTime, remainingTime,
                    hasLimit, showSession, showToday, showRemaining, isWarning
                )

                // WindowManager layout params — the key to non-blocking overlay
                val layoutParams = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    // FLAG_NOT_FOCUSABLE: overlay never gets input focus
                    // FLAG_NOT_TOUCH_MODAL: touches outside overlay go to app below
                    // FLAG_LAYOUT_IN_SCREEN: allow overlay in status bar area
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                }

                overlayView = container
                windowManager?.addView(container, layoutParams)
                logD(OVERLAY_REMINDER_TAG) { "Overlay view added successfully" }

                // Entry animation
                animateIn(container)

                // If time warning mode, add pulsing animation
                if (isWarning) {
                    startPulseAnimation(container)
                }

                // Auto-dismiss
                autoDismissRunnable = Runnable {
                    try {
                        if (overlayView === container) {
                            removeOverlay()
                            stopSelf()
                        }
                    } catch (e: Exception) {
                        logE(OVERLAY_REMINDER_TAG, e) { "Error in auto-dismiss" }
                    }
                }
                handler.postDelayed(autoDismissRunnable!!, DISMISS_DELAY)

            } catch (e: Exception) {
                logE(OVERLAY_REMINDER_TAG, e) { "Error in showOverlay" }
                stopSelf()
            }
        }
    }

    private fun buildOverlayView(
        appName: String,
        sessionTime: String,
        todayTime: String,
        remainingTime: String,
        hasLimit: Boolean,
        showSession: Boolean,
        showToday: Boolean,
        showRemaining: Boolean,
        isWarning: Boolean
    ): FrameLayout {
        val ctx = applicationContext
        val density = ctx.resources.displayMetrics.density

        fun dp(value: Int): Int = (value * density + 0.5f).toInt()

        // Outer wrapper with padding (acts as the "padding from screen edge")
        val wrapper = FrameLayout(ctx).apply {
            val statusBarHeight = getStatusBarHeight()
            setPadding(dp(12), statusBarHeight + dp(8), dp(12), dp(8))
        }

        // Card background
        val cardBg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(24).toFloat()
            setColor(if (isWarning) colorWarningBg else colorCardBg)
            setStroke(dp(1), if (isWarning) colorWarningBorder else colorCardBorder)
        }

        // Main card layout
        val card = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            background = cardBg
            setPadding(dp(20), dp(16), dp(20), dp(16))
            elevation = dp(12).toFloat()
        }

        // Header row
        val headerRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        // Emoji icon
        val emojiView = TextView(ctx).apply {
            text = if (isWarning) "\u231B" else "\uD83D\uDC09"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            setPadding(0, 0, dp(12), 0)
        }

        // Title column
        val titleColumn = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val titleText = TextView(ctx).apply {
            text = appName
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(colorTextPrimary)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            maxLines = 1
        }

        val subtitleText = TextView(ctx).apply {
            text = if (isWarning)
                getString(R.string.time_warning_subtitle)
            else
                getString(R.string.reminder_overlay_subtext)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            setTextColor(if (isWarning) colorWarningText else colorTextSecondary)
            maxLines = 1
        }

        titleColumn.addView(titleText)
        titleColumn.addView(subtitleText)

        // Close button
        val closeBtn = ImageView(ctx).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setColorFilter("#99FFFFFF".toColorInt())
            val size = dp(32)
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                gravity = Gravity.CENTER_VERTICAL
            }
            setPadding(dp(4), dp(4), dp(4), dp(4))
            isClickable = true
            isFocusable = true
            setOnClickListener {
                removeOverlay()
                stopSelf()
            }
        }

        headerRow.addView(emojiView)
        headerRow.addView(titleColumn)
        headerRow.addView(closeBtn)
        card.addView(headerRow)

        // Time statistics row
        data class TimeItem(val label: String, val value: String)

        val items = mutableListOf<TimeItem>()

        if (showSession && sessionTime.isNotEmpty()) {
            items.add(TimeItem(getString(R.string.popup_session_label), sessionTime))
        }
        if (showToday && todayTime.isNotEmpty()) {
            items.add(TimeItem(getString(R.string.popup_today_label), todayTime))
        }
        if (showRemaining && hasLimit) {
            items.add(
                TimeItem(
                    getString(R.string.popup_remaining_label),
                    remainingTime.ifEmpty { getString(R.string.popup_no_limit) }
                )
            )
        }

        if (items.isNotEmpty()) {
            // Spacer
            card.addView(View(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(12)
                )
            })

            val statsRow = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

            items.forEachIndexed { index, item ->
                // Time info column
                val infoCol = LinearLayout(ctx).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER_HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                    )
                }

                val valueText = TextView(ctx).apply {
                    text = item.value
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                    setTextColor(if (isWarning) colorWarningAccent else colorTextPrimary)
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    gravity = Gravity.CENTER
                    maxLines = 1
                }

                val labelText = TextView(ctx).apply {
                    text = item.label
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                    setTextColor(if (isWarning) colorWarningText else colorTextSecondary)
                    gravity = Gravity.CENTER
                    maxLines = 1
                    setPadding(0, dp(2), 0, 0)
                }

                infoCol.addView(valueText)
                infoCol.addView(labelText)
                statsRow.addView(infoCol)

                // Divider between items
                if (index < items.size - 1) {
                    val divider = View(ctx).apply {
                        setBackgroundColor(colorDivider)
                        layoutParams = LinearLayout.LayoutParams(dp(1), dp(36)).apply {
                            setMargins(dp(4), dp(2), dp(4), dp(2))
                            gravity = Gravity.CENTER_VERTICAL
                        }
                    }
                    statsRow.addView(divider)
                }
            }

            card.addView(statsRow)
        }

        // Progress bar (auto-dismiss indicator) 
        card.addView(View(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(6)
            )
        })

        val progressTrack = FrameLayout(ctx).apply {
            val trackBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(2).toFloat()
                setColor("#1AFFFFFF".toColorInt())
            }
            background = trackBg
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(3)
            ).apply {
                setMargins(dp(12), 0, dp(12), 0)
            }
        }

        val progressFill = View(ctx).apply {
            val fillBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(2).toFloat()
                setColor(if (isWarning) colorWarningAccent else colorAccentTeal)
            }
            background = fillBg
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            pivotX = 0f
        }

        progressTrack.addView(progressFill)
        card.addView(progressTrack)

        // Animate progress bar from 100% to 0%
        handler.post {
            ObjectAnimator.ofFloat(progressFill, "scaleX", 1f, 0f).apply {
                duration = DISMISS_DELAY
                interpolator = LinearInterpolator()
                start()
            }
        }

        wrapper.addView(card)
        return wrapper
    }

    private fun animateIn(view: View) {
        view.translationY = -200f
        view.alpha = 0f

        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "translationY", -200f, 0f).apply {
                    duration = 500
                    interpolator = OvershootInterpolator(0.8f)
                },
                ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
                    duration = 350
                }
            )
            start()
        }
    }

//    private fun animateOut(view: View, onEnd: () -> Unit) {
//        AnimatorSet().apply {
//            playTogether(
//                ObjectAnimator.ofFloat(view, "translationY", 0f, -200f).apply {
//                    duration = 400
//                    interpolator = AccelerateDecelerateInterpolator()
//                },
//                ObjectAnimator.ofFloat(view, "alpha", 1f, 0f).apply {
//                    duration = 300
//                }
//            )
//            addListener(object : android.animation.AnimatorListenerAdapter() {
//                override fun onAnimationEnd(animation: android.animation.Animator) {
//                    onEnd()
//                }
//            })
//            start()
//        }
//    }

    private fun startPulseAnimation(view: View) {
        pulseAnimator = ValueAnimator.ofFloat(1f, 1.02f).apply {
            duration = 800
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { anim ->
                val scale = anim.animatedValue as Float
                view.scaleX = scale
                view.scaleY = scale
            }
            start()
        }
    }

    private fun removeOverlay() {
        autoDismissRunnable?.let { handler.removeCallbacks(it) }
        autoDismissRunnable = null
        pulseAnimator?.cancel()
        pulseAnimator = null
        try {
            overlayView?.let { view ->
                if (view.isAttachedToWindow) {
                    windowManager?.removeViewImmediate(view)
                    logD(OVERLAY_REMINDER_TAG) { "Overlay removed" }
                }
            }
        } catch (e: Exception) {
            logE(OVERLAY_REMINDER_TAG, e) { "Error removing overlay" }
        } finally {
            overlayView = null
            windowManager = null
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        removeOverlay()
        serviceScope.cancel()
        super.onDestroy()
    }

    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }
}