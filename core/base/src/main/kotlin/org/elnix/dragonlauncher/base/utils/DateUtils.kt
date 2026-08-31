package org.elnix.dragonlauncher.base.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.AlarmClock
import android.provider.CalendarContract
import io.github.elnix90.logging.logD
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.toLocalDateTime
import org.elnix.dragonlauncher.TAG
import org.elnix.dragonlauncher.base.Constants.PackageNameLists.knownClockPackages
import org.elnix.dragonlauncher.base.model.models.DateTimeFormats
import org.elnix.dragonlauncher.base.utils.DateUtils.defaultDateTimeFormatter
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.time.Clock
import kotlin.time.Instant

public object DateUtils {
    public fun Context.openAlarmApp2(): Boolean {
        val pm = this.packageManager

        // 1. Official alarm UI
        val alarmIntent =
            Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        if (alarmIntent.resolveActivity(pm) != null) {
            startActivity(alarmIntent)
            return true
        }

        // 2. Clock apps that declare alarm/clock actions
        val alarmLikeIntents =
            listOf(
                Intent(AlarmClock.ACTION_SET_ALARM),
                Intent("android.intent.action.SHOW_ALARMS"),
                Intent("android.intent.action.SHOW_ALARM")
            )

        val candidates =
            alarmLikeIntents
                .flatMap { base ->
                    pm.queryIntentActivities(
                        base,
                        PackageManager.MATCH_DEFAULT_ONLY
                    )
                }.distinctBy { it.activityInfo.packageName to it.activityInfo.name }

        if (candidates.isNotEmpty()) {
            val best = candidates.first()
            startActivity(
                Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                    this.component =
                        ComponentName(
                            best.activityInfo.packageName,
                            best.activityInfo.name
                        )
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
            return true
        }

        // 3. Launcher activities, filtered by known clock packages or name
        val launcherIntent =
            Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
        val launcherActivities =
            pm.queryIntentActivities(
                launcherIntent,
                PackageManager.MATCH_DEFAULT_ONLY
            )

        val fallback =
            launcherActivities.firstOrNull {
                val pkg = it.activityInfo.packageName
                pkg in knownClockPackages ||
                    pkg
                        .contains("clock", ignoreCase = true) ||
                    it
                        .loadLabel(pm)
                        .toString()
                        .contains("clock", ignoreCase = true)
            }

        if (fallback != null) {
            startActivity(
                Intent(Intent.ACTION_MAIN).apply {
                    this.component =
                        ComponentName(
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

    public fun Context.openAlarmApp() {
        val pm = this.packageManager

        // Try official alarm actions in priority order
        listOf(
            AlarmClock.ACTION_SHOW_ALARMS,
            AlarmClock.ACTION_SET_ALARM,
            AlarmClock.ACTION_SET_TIMER
        ).forEach { action ->
            val intent =
                Intent(action).apply {
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

    public fun Context.openCalendar() {
        try {
            val calendarUri =
                CalendarContract.CONTENT_URI
                    .buildUpon()
                    .appendPath("time")
                    .build()
            startActivity(Intent(Intent.ACTION_VIEW, calendarUri))
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val intent =
                    Intent(Intent.ACTION_MAIN).setClassName(
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
     * Default date time formatter
     * Outputs `MMM dd, yyyy HH:mm:ss`
     */
    private val defaultDateTimeFormatter =
        LocalDateTime.Format {
            monthName(MonthNames.ENGLISH_ABBREVIATED)
            chars(" ")
            day()
            chars(", ")
            year()
            chars(" ")
            hour()
            chars(":")
            minute()
            chars(":")
            second()
        }

    /**
     * Format a timestamp (milliseconds) to a readable datetime string.
     * Used by logs and backup tabs to format file dates.
     *
     * @return [String] formatted as [defaultDateTimeFormatter]
     */
    public fun Long.formatDateTime(format: DateTimeFormat<LocalDateTime> = defaultDateTimeFormatter): String {
        val instant = Instant.fromEpochMilliseconds(this)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return localDateTime.format(format)
    }

    /**
     * Format the current moment as a datetime string.
     *
     * @param format the datetime format to apply
     * @return [String] the current datetime formatted according to the specified format
     */
    public fun nowFormattedDateTime(format: DateTimeFormat<LocalDateTime> = defaultDateTimeFormatter): String {
        val instant = Clock.System.now()
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return localDateTime.format(format)
    }

    /**
     * Format the current time as a time string.
     *
     * @param format the time format to apply (default: 24-hour with seconds)
     * @return [String] the current time formatted according to the specified format
     */
    public fun nowFormattedTime(
        format: DateTimeFormat<kotlinx.datetime.LocalTime> = DateTimeFormats.time24HourSeconds
    ): String {
        val instant = Clock.System.now()
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return localDateTime.time.format(format)
    }

    /**
     * Format the current date as a date string.
     *
     * @param format the date format to apply (default: European format)
     * @return [String] the current date formatted according to the specified format
     */
    public fun nowFormattedDate(format: DateTimeFormat<kotlinx.datetime.LocalDate> = DateTimeFormats.dateEu): String {
        val instant = Clock.System.now()
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return localDateTime.date.format(format)
    }

    /**
     * Format duration
     * Takes a timestamp and format it into a duration in hours, minutes and seconds
     * Depending on the duration, the minutes and hours may or may not be displayed (e.g. if under 60 min, no hours)
     *
     * @return [String] the formatted duration
     */
    public fun Long.formatDuration(): String =
        when {
            this >= 60 -> {
                val hours = this / 60
                val mins = this % 60
                if (mins > 0) "${hours}h ${mins}m" else "${hours}h"
            }

            else -> "$this min"
        }

    public fun isValidTimeFormat(formatter: String): Boolean =
        try {
            val timeFormatter = DateTimeFormatter.ofPattern(formatter)
            val now = LocalTime.now()
            now.format(timeFormatter)
            true
        } catch (e: Exception) {
            println("❌ Time format validation failed: '$formatter' -> ${e.message}")
            false
        }

    public fun isValidDateFormat(formatter: String): Boolean =
        try {
            val dateFormatter = DateTimeFormatter.ofPattern(formatter)
            val today = LocalDate.now()
            today.format(dateFormatter)
            true
        } catch (e: Exception) {
            println("❌ Date format validation failed: '$formatter' -> ${e.message}")
            false
        }

// fun Long.timeAgo(): String {
//    val seconds = (System.currentTimeMillis() - this) / 1000
//    return when {
//        seconds < 60 -> "${seconds}s ago"
//        seconds < 3600 -> "${seconds / 60}m ago"
//        seconds < 86400 -> "${seconds / 3600}h ago"
//        seconds < 2592000 -> "${seconds / 86400}d ago"
//        else -> "${seconds / 2592000}mo ago"
//    }
// }
}
