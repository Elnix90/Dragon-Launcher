package org.elnix.dragonlauncher.timer

import android.Manifest
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.annotation.RequiresPermission
import org.elnix.dragonlauncher.base.utils.DateUtils.formatDuration
import org.elnix.dragonlauncher.permissions.PermissionGroup
import org.elnix.dragonlauncher.permissions.PermissionsManager
import java.util.Calendar

/**
 * Synchronous reads on [UsageStatsManager] for the app timer.
 *
 * All functions run on the timer coroutine, so blocking calls are fine.
 * They must stay synchronous: callers need the result immediately, an async
 * wrapper would always return the default value before the query finishes.
 */
internal class UsageStatsReader(
	private val ctx: Context,
	private val permissionManager: PermissionsManager
) {
	/**
	 * Today's total foreground time in minutes for [packageName],
	 * or -1 if usage stats permission is not granted.
	 */
	@RequiresPermission(Manifest.permission.PACKAGE_USAGE_STATS)
	fun getTodayUsageMinutes(packageName: String): Long {
		// hasPermissionBlocking is suspend, so the synchronous
		// checkPermissionOnce is used here instead.
		if (!permissionManager.checkPermissionOnce(PermissionGroup.UsageStat)) {
			return -1L
		}
		return try {
			val usm = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
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

	/**
	 * The "today total" text for overlays.
	 * Returns an empty string if permission is missing.
	 */
	@RequiresPermission(Manifest.permission.PACKAGE_USAGE_STATS)
	fun todayText(packageName: String): String =
		getTodayUsageMinutes(packageName)
			.takeIf { it >= 0 }
			?.formatDuration()
			?: ""

	/**
	 * The package name of the app currently in the foreground,
	 * or null if it cannot be determined.
	 * Uses multiple methods for better reliability.
	 */
	@RequiresPermission(Manifest.permission.PACKAGE_USAGE_STATS)
	fun getCurrentForegroundPackage(trackedPackage: String): String? {
		// hasPermissionBlocking is suspend, so the synchronous
		// checkPermissionOnce is used here instead.
		if (!permissionManager.checkPermissionOnce(PermissionGroup.UsageStat)) {
			return null
		}

		return try {
			val usm = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
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
				return lastPackage
			}

			// Method 2: Fallback - check which app was used most recently
			// If another app has been used in the last 10 seconds, the tracked app is NOT foreground
			val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, now - 10000, now)
			if (stats.isNotEmpty()) {
				val mostRecentApp = stats.maxByOrNull { it.lastTimeUsed }
				if (mostRecentApp != null && mostRecentApp.packageName != trackedPackage && mostRecentApp.lastTimeUsed > (now - 10000)) {
					// Another app is more recently used -> tracked app is not foreground
					return mostRecentApp.packageName
				}
			}

			// If no other app was recently used, assume tracked app is still foreground
			trackedPackage
		} catch (_: Exception) {
			null
		}
	}
}
