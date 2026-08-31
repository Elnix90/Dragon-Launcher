package org.elnix.dragonlauncher.ui.statusbar

import android.app.AlarmManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.elnix.dragonlauncher.base.model.serializables.StatusBar
import org.elnix.dragonlauncher.i18n.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun StatusBarNextAlarm(
    element: StatusBar.NextAlarm,
    // used only for preview in settings, so I don't use the element property
    forceShowIcon: Boolean = false
) {
    val ctx = LocalContext.current
    var nextAlarm by remember { mutableStateOf<NextAlarmInfo?>(null) }

    val formatter = element.formatter
    val dateFormat =
        remember(formatter) {
            try {
                DateTimeFormatter.ofPattern(formatter)
            } catch (_: Exception) {
                println("⚠️ Invalid time format '$formatter'")
                DateTimeFormatter.ofPattern("HH:mm")
            }
        }

    LaunchedEffect(Unit) {
        while (true) {
            nextAlarm = getNextAlarm(ctx, dateFormat)
            delay(60_000L.milliseconds)
        }
    }

    if (nextAlarm != null || forceShowIcon) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.alarm),
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            nextAlarm?.let { alarm ->
                Text(
                    text = alarm.formattedTime,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

data class NextAlarmInfo(
    val formattedTime: String,
    val label: String
)

private fun getNextAlarm(ctx: Context, formatter: DateTimeFormatter): NextAlarmInfo? {
    return try {
        val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextAlarm = alarmManager.nextAlarmClock?.triggerTime ?: return null

        val time =
            Instant
                .ofEpochMilli(nextAlarm)
                .atZone(ZoneId.systemDefault())
                .toLocalTime()

        val formatted = time.format(formatter)

        NextAlarmInfo(
            formattedTime = formatted,
            label = ctx.getString(R.string.next_alarm_at, formatted)
        )
    } catch (_: Exception) {
        println("Alarm read failed")
        null
    }
}
