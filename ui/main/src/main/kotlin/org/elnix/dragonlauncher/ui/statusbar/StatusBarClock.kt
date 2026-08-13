package org.elnix.dragonlauncher.ui.statusbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.github.elnix90.logging.STATUS_BAR_TAG
import io.github.elnix90.logging.logE
import kotlinx.coroutines.delay
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.StatusBar
import org.elnix.dragonlauncher.base.utils.DateUtils.openAlarmApp
import org.elnix.dragonlauncher.base.utils.DateUtils.openCalendar
import org.elnix.dragonlauncher.ui.base.modifiers.conditional
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun StatusBarDate(
    element: StatusBar.Date,
    onAction: ((Action) -> Unit)? = null
) {
    val ctx = LocalContext.current
    val formatterPattern = element.formatter

    val dateFormat = remember(formatterPattern) {
        try {
            DateTimeFormatter.ofPattern(formatterPattern)
        } catch (e: Exception) {
            logE(STATUS_BAR_TAG, e) { "Invalid date format '$formatterPattern'" }
            DateTimeFormatter.ofPattern("MMM dd")
        }
    }

    var date by remember { mutableStateOf(LocalDate.now()) }

    // Update only at midnight or when the component is first composed
    LaunchedEffect(Unit) {
        while (true) {
            val now = LocalDate.now()
            if (date != now) {
                date = now
            }
            // Wait until the next day starts
            val nextDay = now.plusDays(1).atStartOfDay()
            val delayMillis = java.time.Duration.between(java.time.LocalDateTime.now(), nextDay).toMillis()
            delay(delayMillis.coerceAtLeast(60_000L).milliseconds) // Check at least every minute to be safe
        }
    }

    val dateText by remember(date, dateFormat) {
        derivedStateOf {
            try {
                date.format(dateFormat)
            } catch (e: Exception) {
                logE(STATUS_BAR_TAG, e) { "Date formatting failed" }
                date.format(DateTimeFormatter.ofPattern("MMM dd"))
            }
        }
    }

    Row {
        Text(
            text = dateText,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.conditional(onAction) { onAction ->
                clickable {
                    element.action?.let { onAction(it) } ?: ctx.openCalendar()
                }
            }
        )
    }
}


@Composable
fun StatusBarTime(
    element: StatusBar.Time,
    onAction: ((Action) -> Unit)? = null
) {
    val ctx = LocalContext.current

    val action = element.action
    val formatter = element.formatter

    val timeFormat = remember(formatter) {
        try {
            DateTimeFormatter.ofPattern(formatter)
        } catch (e: Exception) {
            logE(STATUS_BAR_TAG, e) { "Invalid time format '$formatter'" }
            DateTimeFormatter.ofPattern("HH:mm")
        }
    }

    var time by remember { mutableStateOf(LocalTime.now()) }

    // Update every second if formatter contains 'ss', else every 30 seconds
    val updateInterval = remember(formatter) {
        if ("ss" in formatter) 1_000L else 30_000L
    }

    LaunchedEffect(updateInterval) {
        while (true) {
            time = LocalTime.now()
            delay(updateInterval.milliseconds)
        }
    }

    val timeText by remember(time, timeFormat) {
        derivedStateOf {
            try {
                time.format(timeFormat)
            } catch (e: Exception) {
                logE(STATUS_BAR_TAG, e) { "Time formatting failed" }
                time.format(DateTimeFormatter.ofPattern("HH:mm"))
            }
        }
    }

    Row {
        Text(
            text = timeText,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.conditional(onAction) { onAction ->
                clickable {
                    action?.let { onAction(it) } ?: ctx.openAlarmApp()
                }
            }
        )
    }
}
