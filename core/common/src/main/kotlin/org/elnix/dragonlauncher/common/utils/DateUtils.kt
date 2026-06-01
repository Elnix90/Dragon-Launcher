package org.elnix.dragonlauncher.common.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.AlarmClock
import android.provider.CalendarContract
import androidx.compose.runtime.Composable
import org.elnix.dragonlauncher.base.Constants.PackageNameLists.knownClockPackages
import org.elnix.dragonlauncher.logging.TAG
import org.elnix.dragonlauncher.logging.logD
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object DateUtils {
    fun Context.openAlarmApp2(): Boolean {
        val pm = this.packageManager

        // 1. Official alarm UI
        val alarmIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (alarmIntent.resolveActivity(pm) != null) {
            startActivity(alarmIntent)
            return true
        }

        // 2. Clock apps that declare alarm/clock actions
        val alarmLikeIntents = listOf(
            Intent(AlarmClock.ACTION_SET_ALARM),
            Intent("android.intent.action.SHOW_ALARMS"),
            Intent("android.intent.action.SHOW_ALARM")
        )

        val candidates = alarmLikeIntents
            .flatMap { base ->
                pm.queryIntentActivities(
                    base,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
            }
            .distinctBy { it.activityInfo.packageName to it.activityInfo.name }

        if (candidates.isNotEmpty()) {
            val best = candidates.first()
            startActivity(
                Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                    this.component = ComponentName(
                        best.activityInfo.packageName,
                        best.activityInfo.name
                    )
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
            return true
        }

        // 3. Launcher activities, filtered by known clock packages or name
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val launcherActivities = pm.queryIntentActivities(
            launcherIntent,
            PackageManager.MATCH_DEFAULT_ONLY
        )

        val fallback = launcherActivities.firstOrNull {
            val pkg = it.activityInfo.packageName
            pkg in knownClockPackages ||
                    pkg
                        .contains("clock", ignoreCase = true) || it.loadLabel(pm).toString()
                .contains("clock", ignoreCase = true)
        }

        if (fallback != null) {
            startActivity(
                Intent(Intent.ACTION_MAIN).apply {
                    this.component = ComponentName(
                        fallback.activityInfo.packageName,
                        fallback.activityInfo.name
                    )
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
            return true
        }

        return false
    }

    fun Context.openAlarmApp() {
        val pm = this.packageManager

        // Try official alarm actions in priority order
        listOf(
            AlarmClock.ACTION_SHOW_ALARMS,
            AlarmClock.ACTION_SET_ALARM,
            AlarmClock.ACTION_SET_TIMER
        ).forEach { action ->
            val intent = Intent(action).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(pm) != null) {
                startActivity(intent)
                return
            }
        }

        // Fallback, use the other function
        if (!this.openAlarmApp2()) return

        // No alarm-capable app found
        logD(TAG) { "No alarm app found" }
    }


    fun Context.openCalendar() {
        try {
            val calendarUri = CalendarContract.CONTENT_URI
                .buildUpon()
                .appendPath("time")
                .build()
            startActivity(Intent(Intent.ACTION_VIEW, calendarUri))
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val intent = Intent(Intent.ACTION_MAIN).setClassName(
                    this,
                    "org.elnix.dragonlauncher.MainActivity"
                )
                intent.addCategory(Intent.CATEGORY_APP_CALENDAR)
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    /**
     * Takes a timestamp [Long] and return the formatted date in `MMM dd, yyyy HH:mm:ss` format
     * It uis used by the logs tab and the backup tab to format file dates in a readable output
     *
     * @return [String] the formatted [this] timestamp
     */
    fun Long.formatDateTime(): String {
        return SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
            .format(Date(this))
    }

    /**
     * Today - returns the today's date, formatted in the given format
     *
     * @param format the date format
     * @return [String] today's date
     */
    fun today(format: String = "MMM dd, yyyy"): String =
        SimpleDateFormat(format, Locale.getDefault()).format(Date())


    /**
     * Format duration
     * Takes a timestamp and format it into a duration in hours, minutes and seconds
     * Depending on the duration, the minutes and hours may or may not be displayed (e.g. if under 60 min, no hours)
     *
     * @return [String] the formatted duration
     */
    fun Long.formatDuration(): String {
        return when {
            this >= 60 -> {
                val hours = this / 60
                val mins = this % 60
                if (mins > 0) "${hours}h ${mins}m" else "${hours}h"
            }

            else -> "$this min"
        }
    }

    @Composable
    fun isValidTimeFormat(formatter: String): Boolean = try {
        val timeFormatter = DateTimeFormatter.ofPattern(formatter)
        val now = LocalTime.now()
        now.format(timeFormatter)
        true
    } catch (e: Exception) {
        println("❌ Time format validation failed: '$formatter' -> ${e.message}")
        false
    }

    @Composable
    fun isValidDateFormat(formatter: String): Boolean = try {
        val dateFormatter = DateTimeFormatter.ofPattern(formatter)
        val today = LocalDate.now()
        today.format(dateFormatter)
        true
    } catch (e: Exception) {
        println("❌ Date format validation failed: '$formatter' -> ${e.message}")
        false
    }

//fun Long.timeAgo(): String {
//    val seconds = (System.currentTimeMillis() - this) / 1000
//    return when {
//        seconds < 60 -> "${seconds}s ago"
//        seconds < 3600 -> "${seconds / 60}m ago"
//        seconds < 86400 -> "${seconds / 3600}h ago"
//        seconds < 2592000 -> "${seconds / 86400}d ago"
//        else -> "${seconds / 2592000}mo ago"
//    }
//}

}