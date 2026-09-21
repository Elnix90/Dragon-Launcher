package org.elnix.dragonlauncher.timer

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.models.ReminderMode
import org.elnix.dragonlauncher.base.utils.DateUtils.formatDuration
import org.elnix.dragonlauncher.permissions.PermissionsManager
import kotlin.time.Duration.Companion.milliseconds

/**
 * Foreground service that:
 * 1. Tracks how long the user has been on a paused app.
 * 2. Optionally sends periodic reminder notifications (every X minutes).
 * 3. Optionally triggers an overlay popup via [OverlayReminderService].
 * 4. Optionally returns the user to Dragon Launcher when the time limit is reached.
 *
 * Usage-stats reads live in [UsageStatsReader], notifications in
 * [TimerNotifications]; this service only orchestrates the tracking loop.
 */
@AndroidEntryPoint
public class AppTimerService : Service() {
	private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

	@Inject
	public lateinit var permissionManager: PermissionsManager

	private val usageStats: UsageStatsReader by lazy { UsageStatsReader(this, permissionManager) }
	private val notifications: TimerNotifications by lazy { TimerNotifications(this) }

	public companion object {
		public const val CHANNEL_TIMER: String = "dragon_timer_channel"
		public const val CHANNEL_REMINDER: String = "dragon_reminder_channel"
		public const val NOTIF_ID_TIMER: Int = 9001
		public const val NOTIF_ID_REMINDER: Int = 9002

		public const val EXTRA_PACKAGE_NAME: String = "extra_package_name"

		// Reminder mode
		public const val EXTRA_REMINDER_ENABLED: String = "extra_reminder_enabled"
		public const val EXTRA_REMINDER_INTERVAL_MINUTES: String = "extra_reminder_interval_min"
		public const val EXTRA_REMINDER_MODE: String = "extra_reminder_mode" // "Notification" | "Overlay"

		// Return-to-launcher mode
		public const val EXTRA_TIME_LIMIT_ENABLED: String = "extra_time_limit_enabled"
		public const val EXTRA_TIME_LIMIT_MINUTES: String = "extra_time_limit_min"

		public const val ACTION_STOP: String = "org.elnix.dragonlauncher.STOP_TIMER"
		public const val SHOW_LAUNCHER: String = "org.elnix.dragonlauncher.SHOW_LAUNCHER"
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
			TimerNotifications(ctx).sendTestReminderNotification(appName, minutes)
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
	private var timerJob: Job? = null
	private var fiveMinWarningShown = false

	private fun createTimerJob(startId: Int) =
		serviceScope.launch {
			var nextReminderAt = if (reminderEnabled) reminderIntervalMs else Long.MAX_VALUE
			var lastForegroundCheckMs = System.currentTimeMillis()
			var notForegroundCount = 0
			val maxNotForeground = 5
			var isAppActive = true // Track if we're still on the tracked app

			while (isActive && isAppActive) {
				delay(1000.milliseconds)
				val elapsed = System.currentTimeMillis() - startTimeMs

				// Check if user is still on the tracked app (every 3 seconds)
				val nowMs = System.currentTimeMillis()
				if (nowMs - lastForegroundCheckMs >= 3000) {
					lastForegroundCheckMs = nowMs
					val fg = usageStats.getCurrentForegroundPackage(trackedPackage)

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
					notifications.updateTimerNotification(appName, timeLimitEnabled, remaining, startTimeMs)
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
						val todayText = usageStats.todayText(trackedPackage)

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
			// When loop exits (app switched or time limit), stop the service
			// Use stopSelfResult so only the latest start can stop the service
			stopSelfResult(startId)
		}

	override fun onCreate() {
		super.onCreate()
		notifications.createChannels()
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		if (intent?.action == ACTION_STOP) {
			stopSelf()
			return START_NOT_STICKY
		}

		// Stop previous timer if running
		timerJob?.cancel()
		timerJob = null
		fiveMinWarningShown = false

		trackedPackage = intent?.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""
		appName = intent?.getStringExtra(EXTRA_APP_NAME) ?: trackedPackage
		reminderEnabled = intent?.getBooleanExtra(EXTRA_REMINDER_ENABLED, false) ?: false
		val intervalMin = intent?.getIntExtra(EXTRA_REMINDER_INTERVAL_MINUTES, 5) ?: 5
		reminderIntervalMs = intervalMin * 60 * 1000L
		reminderMode =
			intent?.getStringExtra(EXTRA_REMINDER_MODE)?.let {
				runCatching { ReminderMode.valueOf(it) }.getOrNull()
			} ?: ReminderMode.Overlay
		timeLimitEnabled = intent?.getBooleanExtra(EXTRA_TIME_LIMIT_ENABLED, false) ?: false
		val limitMin = intent?.getIntExtra(EXTRA_TIME_LIMIT_MINUTES, 0) ?: 0
		timeLimitMs = limitMin * 60 * 1000L
		startTimeMs = System.currentTimeMillis()

		ServiceCompat.startForeground(
			this,
			NOTIF_ID_TIMER,
			notifications.buildTimerNotification(
				appName,
				timeLimitEnabled,
				if (timeLimitEnabled) timeLimitMs else 0L,
				startTimeMs
			),
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
				ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
			} else {
				0
			}
		)

		timerJob = createTimerJob(startId)
		return START_NOT_STICKY
	}

	override fun onDestroy() {
		timerJob?.cancel()
		timerJob = null
		val nm = getSystemService(NotificationManager::class.java)
		nm?.cancel(NOTIF_ID_TIMER)
		nm?.cancel(NOTIF_ID_REMINDER)
		serviceScope.cancel()
		super.onDestroy()
	}

	override fun onBind(intent: Intent?): IBinder? = null

	private fun sendReminder(elapsedMs: Long) {
		val elapsedMinutes = (elapsedMs / 60_000).coerceAtLeast(1)
		val timeText = elapsedMinutes.formatDuration()
		val todayText = usageStats.todayText(trackedPackage)

		when (reminderMode) {
			ReminderMode.Notification -> {
				notifications.sendReminderNotification(appName, timeText)
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
	 * Bring the user back to Dragon Launcher once the time limit expires.
	 */
	private fun returnToLauncher() {
		val broadIntent =
			Intent(SHOW_LAUNCHER).apply {
				putExtra(EXTRA_APP_NAME, appName)
				setPackage(this@AppTimerService.packageName)
			}
		sendBroadcast(broadIntent)

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
