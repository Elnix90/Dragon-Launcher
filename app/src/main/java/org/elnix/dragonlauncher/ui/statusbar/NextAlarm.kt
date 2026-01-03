package org.elnix.dragonlauncher.ui.statusbar

import android.app.AlarmManager
import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import org.elnix.dragonlauncher.R
import java.time.format.DateTimeFormatter


@Composable
fun StatusBarNextAlarm(textColor: Color) {
    val ctx = LocalContext.current
    var nextAlarm by remember { mutableStateOf<NextAlarmInfo?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            nextAlarm = getNextAlarm(ctx)
            delay(60_000L)
        }
    }

    nextAlarm?.let { alarm ->
        Text(
            text = alarm.formattedTime,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

data class NextAlarmInfo(
    val formattedTime: String,
    val label: String
)

private fun getNextAlarm(ctx: Context): NextAlarmInfo? {
    return try {
        val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextAlarm = alarmManager.nextAlarmClock?.triggerTime ?: return null

        val time = java.time.Instant.ofEpochMilli(nextAlarm)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalTime()

        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val formatted = time.format(formatter)

        NextAlarmInfo(
            formattedTime = formatted,
            label = ctx.getString(R.string.next_alarm_at, formatted)
        )

    } catch (e: Exception) {
        println("Alarm read failed: ${e.message}")
        null
    }
}
