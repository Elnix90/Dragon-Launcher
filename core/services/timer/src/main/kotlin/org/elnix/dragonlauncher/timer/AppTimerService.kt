package org.elnix.dragonlauncher.timer

import android.Manifest
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.models.ReminderMode
import org.elnix.dragonlauncher.base.utils.DateUtils.formatDuration
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.permissions.PermissionGroup
import org.elnix.dragonlauncher.permissions.PermissionsManager
import java.util.Calendar

/**
 * Foreground service that:
 * 1. Tracks how long the user has been on a paused app.
 * 2. Optionally sends periodic reminder notifications (every X minutes).
 * 3. Optionally triggers an overlay popup via [OverlayReminderService].
 * 4. Optionally returns the user to Dragon Launcher when the time limit is reached.
 */
@AndroidEntryPoint
public class AppTimerService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Inject
    public lateinit var permissionManager: PermissionsManager

    public companion object {
        public const val CHANNEL_TIMER: String = "dragon_timer_channel"
        public const val CHANNEL_REMINDER: String = "dragon_reminder_channel"
        public const val NOTIF_ID_TIMER: Int = 9001
        public const val NOTIF_ID_REMINDER: Int = 9002

        public const val EXTRA_PACKAGE_NAME: String = "extra_package_name"

        // Reminder mode
        public const val EXTRA_REMINDER_ENABLED: String = "extra_reminder_enabled"
        public const val EXTRA_REMINDER_INTERVAL_MINUTES: String = "extra_reminder_interval_min"
        public const val EXTRA_REMINDER_MODE: String = "extra_reminder_mode" // "notification" | "overlay"

        // Return-to-launcher mode
        public const val EXTRA_TIME_LIMIT_ENABLED: String = "extra_time_limit_enabled"
        public const val EXTRA_TIME_LIMIT_MINUTES: String = "extra_time_limit_min"

        public const val ACTION_STOP: String = "org.elnix.dragonlauncher.STOP_TIMER"
        public const val SHOW_LAUNCHER: String = "com.elnix.dragonlauncher.SHOW_LAUNCHER"
        public const val EXTRA_APP_NAME: String = "extra_app_name"

        public fun start(
            ctx: Context,
            application: Application,
            reminderEnabled: Boolean,
            reminderIntervalMinutes: Int,
            reminderMode: String,
            timeLimitMinutes: Int?
        ) {
            val intent =
                Intent(ctx, AppTimerService::class.java).apply {
                    putExtra(EXTRA_PACKAGE_NAME, application.packageName)
                    putExtra(EXTRA_APP_NAME, application.label)
                    putExtra(EXTRA_REMINDER_ENABLED, reminderEnabled)
                    putExtra(EXTRA_REMINDER_INTERVAL_MINUTES, reminderIntervalMinutes)
                    putExtra(EXTRA_REMINDER_MODE, reminderMode)
                    putExtra(EXTRA_TIME_LIMIT_ENABLED, timeLimitMinutes != null)
                    putExtra(EXTRA_TIME_LIMIT_MINUTES, timeLimitMinutes)
                }
            ctx.startForegroundService(intent)
        }

        public fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, AppTimerService::class.java))
        }

        /**
         * Helper used by debug UI to send a one-off reminder notification for testing.
         */
        public fun sendTestReminderNotification(ctx: Context, appName: String = "Dragon Launcher", minutes: Int = 5) {
            val nm = ctx.getSystemService(NotificationManager::class.java) ?: return
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_REMINDER,
                    "Usage Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Periodic reminders when you stay on an app too long"
                }
            )
            val timeText = "$minutes min"
            val notif =
                NotificationCompat
                    .Builder(ctx, CHANNEL_REMINDER)
                    .setSmallIcon(R.drawable.notification)
                    .setContentTitle(ctx.getString(R.string.reminder_notification_title, appName))
                    .setContentText(ctx.getString(R.string.reminder_notification_text, appName, timeText))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()
            nm.notify(NOTIF_ID_REMINDER, notif)
        }
    }

    private var trackedPackage: String = ""
    private var appName: String = ""
    private var reminderEnabled = false
    private var reminderIntervalMs = 5 * 60 * 1000L
    private var reminderMode = ReminderMode.Overlay
    private var timeLimitEnabled = false
    private var timeLimitMs = 0L
    private var startTimeMs = 0L
    private var timerThread: Thread? = null
    private var fiveMinWarningShown = false

    /**
     * Returns today's total foreground time in minutes for [packageName],
     * or -1 if usage stats permission is not granted.
     */
    @RequiresPermission(Manifest.permission.PACKAGE_USAGE_STATS)
    private fun getTodayUsageMinutes(packageName: String): Long {
        var todayUsage = 0L
        serviceScope.launch {
            todayUsage =
                permissionManager
                    .hasPermissionBlocking(PermissionGroup.UsageStat)
                    .let { hasPermission ->
                        if (!hasPermission) {
                            -1L
                        } else {
                            try {
                                val usm = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
                                val cal =
                                    Calendar.getInstance().apply {
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                val todayStart = cal.timeInMillis
                                val now = System.currentTimeMillis()
                                val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, todayStart, now)
                                stats
                                    .filter { it.packageName == packageName }
                                    .sumOf { it.totalTimeInForeground } / 60_000
                            } catch (_: Exception) {
                                -1L
                            }
                        }
                    }
        }
        return todayUsage
    }

    /**
     * Returns the package name of the app currently in the foreground,
     * or null if it cannot be determined.
     * Uses multiple methods for better reliability.
     */
    @RequiresPermission(Manifest.permission.PACKAGE_USAGE_STATS)
    private fun getCurrentForegroundPackage(): String? {
        var result: String? = null

        serviceScope.launch {
            result =
                permissionManager.hasPermissionBlocking(PermissionGroup.UsageStat).let { hasPermission ->
                    if (!hasPermission) return@let null

                    try {
                        val usm = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
                        val now = System.currentTimeMillis()

                        // Method 1: Query recent events (most reliable for foreground detection)
                        val events = usm.queryEvents(now - 5000, now)
                        var lastPackage: String? = null
                        val event = UsageEvents.Event()
                        while (events.hasNextEvent()) {
                            events.getNextEvent(event)
                            @Suppress("DEPRECATION")
                            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                                lastPackage = event.packageName
                            }
                        }

                        // If we found a recent foreground event, trust it
                        if (lastPackage != null) {
                            return@let lastPackage
                        }

                        // Method 2: Fallback - check which app was used most recently
                        // If another app has been used in the last 10 seconds, the tracked app is NOT foreground
                        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, now - 10000, now)
                        if (stats.isNotEmpty()) {
                            val mostRecentApp = stats.maxByOrNull { it.lastTimeUsed }
                            if (mostRecentApp != null && mostRecentApp.packageName != trackedPackage && mostRecentApp.lastTimeUsed > (now - 10000)) {
                                // Another app is more recently used -> tracked app is not foreground
                                mostRecentApp.packageName
                            }
                        }

                        // If no other app was recently used, assume tracked app is still foreground
                        trackedPackage
                    } catch (_: Exception) {
                        null
                    }
                }
        }
        return result
    }

    private fun createTimerThread(startId: Int) =
        object : Thread("AppTimerThread") {
            @RequiresPermission(Manifest.permission.PACKAGE_USAGE_STATS)
            @Suppress("AssignedValueIsNeverRead")
            override fun run() {
                try {
                    var elapsed: Long
                    var nextReminderAt = if (reminderEnabled) reminderIntervalMs else Long.MAX_VALUE
                    var lastForegroundCheckMs = System.currentTimeMillis()
                    var notForegroundCount = 0
                    val maxNotForeground = 5
                    var isAppActive = true // Track if we're still on the tracked app

                    @Suppress("KotlinConstantConditions")
                    while (!isInterrupted && isAppActive) {
                        sleep(1000)
                        elapsed = System.currentTimeMillis() - startTimeMs

                        // Check if user is still on the tracked app (every 3 seconds)
                        val nowMs = System.currentTimeMillis()
                        if (nowMs - lastForegroundCheckMs >= 3000) {
                            lastForegroundCheckMs = nowMs
                            val fg = getCurrentForegroundPackage()

                            // If fg is null (no permission), give benefit of the doubt one more time
                            // But if we haven't gotten permission by now, something is wrong
                            if (fg == null) {
                                notForegroundCount++
                                if (notForegroundCount >= maxNotForeground) {
                                    // Can't detect foreground app - stop after grace period
                                    isAppActive = false
                                    break
                                }
                            } else if (fg != trackedPackage) {
                                // User has switched to a different app
                                notForegroundCount++
                                if (notForegroundCount >= maxNotForeground) {
                                    // User has left the app for 15+ seconds -> stop service
                                    isAppActive = false
                                    break
                                }
                            } else {
                                // Still on tracked app, reset counter
                                notForegroundCount = 0
                            }
                        }

                        // Update foreground notification every 30s
                        if (timeLimitEnabled && elapsed % 30_000 < 1000) {
                            val remaining = (timeLimitMs - elapsed).coerceAtLeast(0)
                            updateTimerNotification(remaining)
                        }

                        // 5-minute warning overlay (only once, only if total limit > 5 min)
                        if (timeLimitEnabled && !fiveMinWarningShown) {
                            val remainingMs = timeLimitMs - elapsed
                            if (remainingMs in 1..300_000 && timeLimitMs > 300_000) {
                                fiveMinWarningShown = true
                                val remainingMinutes = (remainingMs / 60_000).coerceAtLeast(1)
                                val remainingText = remainingMinutes.formatDuration()
                                val sessionMinutes = (elapsed / 60_000).coerceAtLeast(1)
                                val sessionText = sessionMinutes.formatDuration()
                                val todayText = buildTodayText()

                                OverlayReminderService.show(
                                    this@AppTimerService,
                                    appName,
                                    sessionText,
                                    todayText,
                                    remainingText,
                                    true,
                                    "time_warning"
                                )
                            }
                        }

                        // Periodic reminder
                        if (isAppActive && reminderEnabled && elapsed >= nextReminderAt) {
                            sendReminder(elapsed)
                            nextReminderAt += reminderIntervalMs
                        }

                        // Time limit reached
                        if (timeLimitEnabled && elapsed >= timeLimitMs) {
                            returnToLauncher()
                            break
                        }
                    }
                } catch (_: InterruptedException) {
                    // Service stopped
                } finally {
                    // When loop exits (app switched or time limit), stop the service
                    // Use stopSelfResult so only the latest start can stop the service
                    stopSelfResult(startId)
                }
            }
        }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Stop previous timer if running
        timerThread?.interrupt()
        timerThread = null
        fiveMinWarningShown = false

        trackedPackage = intent?.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""
        appName = intent?.getStringExtra(EXTRA_APP_NAME) ?: trackedPackage
        reminderEnabled = intent?.getBooleanExtra(EXTRA_REMINDER_ENABLED, false) ?: false
        val intervalMin = intent?.getIntExtra(EXTRA_REMINDER_INTERVAL_MINUTES, 5) ?: 5
        reminderIntervalMs = intervalMin * 60 * 1000L
        reminderMode = ReminderMode.valueOf(intent?.getStringExtra(EXTRA_REMINDER_MODE) ?: "overlay")
        timeLimitEnabled = intent?.getBooleanExtra(EXTRA_TIME_LIMIT_ENABLED, false) ?: false
        val limitMin = intent?.getIntExtra(EXTRA_TIME_LIMIT_MINUTES, 0) ?: 0
        timeLimitMs = limitMin * 60 * 1000L
        startTimeMs = System.currentTimeMillis()

        val notification =
            buildTimerNotification(
                if (timeLimitEnabled) timeLimitMs else 0L
            )

        ServiceCompat.startForeground(
            this,
            NOTIF_ID_TIMER,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
        )

        timerThread = createTimerThread(startId).also { it.start() }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        timerThread?.interrupt()
        timerThread = null
        val nm = getSystemService(NotificationManager::class.java)
        nm?.cancel(NOTIF_ID_TIMER)
        nm?.cancel(NOTIF_ID_REMINDER)
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannels() {
        val nm = getSystemService(NotificationManager::class.java) ?: return

        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_TIMER,
                "App Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows remaining time for app usage limit"
                setShowBadge(false)
            }
        )

        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_REMINDER,
                "Usage Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Periodic reminders when you stay on an app too long"
            }
        )
    }

    private fun buildTimerNotification(remainingMs: Long): Notification {
        val stopIntent =
            Intent(this, AppTimerService::class.java).apply {
                action = ACTION_STOP
            }
        val stopPI =
            PendingIntent.getService(
                this,
                0,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val text =
            if (timeLimitEnabled && remainingMs > 0) {
                val remainingMinutes = (remainingMs / 60_000).coerceAtLeast(1)
                getString(R.string.timer_notification_text, remainingMinutes.formatDuration(), appName)
            } else {
                val elapsedMs = System.currentTimeMillis() - startTimeMs
                val elapsedMinutes = (elapsedMs / 60_000).coerceAtLeast(1)
                getString(R.string.reminder_notification_text, appName, elapsedMinutes.formatDuration())
            }

        return NotificationCompat
            .Builder(this, CHANNEL_TIMER)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(getString(R.string.timer_notification_title))
            .setContentText(text)
            .setOngoing(true)
            .setSilent(true)
            .addAction(R.drawable.notification, getString(R.string.time_limit_cancel), stopPI)
            .build()
    }

    private fun updateTimerNotification(remainingMs: Long) {
        val nm = getSystemService(NotificationManager::class.java) ?: return
        nm.notify(NOTIF_ID_TIMER, buildTimerNotification(remainingMs))
    }

    /**
     * Build the "today total" text from UsageStatsManager.
     * Returns empty string if permission is missing.
     */
    @RequiresPermission(Manifest.permission.PACKAGE_USAGE_STATS)
    private fun buildTodayText(): String =
        getTodayUsageMinutes(trackedPackage)
            .takeIf { it >= 0 }
            ?.formatDuration()
            ?: ""

    @RequiresPermission(Manifest.permission.PACKAGE_USAGE_STATS)
    private fun sendReminder(elapsedMs: Long) {
        val elapsedMinutes = (elapsedMs / 60_000).coerceAtLeast(1)
        val timeText = elapsedMinutes.formatDuration()
        val todayText = buildTodayText()

        when (reminderMode) {
            ReminderMode.Notification -> {
                val nm = getSystemService(NotificationManager::class.java) ?: return
                val notif =
                    NotificationCompat
                        .Builder(this, CHANNEL_REMINDER)
                        .setSmallIcon(R.drawable.notification)
                        .setContentTitle(getString(R.string.reminder_notification_title, appName))
                        .setContentText(getString(R.string.reminder_notification_text, appName, timeText))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .build()
                nm.notify(NOTIF_ID_REMINDER, notif)
            }

            ReminderMode.Overlay -> {
                val remainingText =
                    if (timeLimitEnabled) {
                        val remaining = (timeLimitMs - elapsedMs).coerceAtLeast(0)
                        val remainingMinutes = (remaining / 60_000).coerceAtLeast(1)
                        remainingMinutes.formatDuration()
                    } else {
                        ""
                    }

                OverlayReminderService.show(
                    this,
                    appName,
                    timeText,
                    todayText,
                    remainingText,
                    timeLimitEnabled,
                    "reminder"
                )
            }
        }
    }

    /**
     * Kill the app the user has been too long in, and then return to launcher
     */
    private fun returnToLauncher() {
//        // Show the time limit exceeded screen
//        val intent = Intent(this, TimeLimitExceededActivity::class.java).apply {
//            .putExtra(TimeLimitExceededActivity.EXTRA_APP_NAME, appName)
//            Intent.setFlags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//        }
//        startActivity(intent)

        val broadIntent =
            Intent(SHOW_LAUNCHER).apply {
                putExtra(EXTRA_APP_NAME, appName)
                setPackage(this.`package`)
            }
        sendBroadcast(broadIntent)

        val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        am.killBackgroundProcesses(trackedPackage)

        // Launch home intent to return to launcher
        val homeIntent =
            Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        startActivity(homeIntent)

        stopSelf()
    }
}
