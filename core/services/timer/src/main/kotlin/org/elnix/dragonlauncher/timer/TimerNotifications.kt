package org.elnix.dragonlauncher.timer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import org.elnix.dragonlauncher.base.utils.DateUtils.formatDuration
import org.elnix.dragonlauncher.i18n.R

/**
 * Builds and posts every notification owned by [AppTimerService]:
 * channels, the ongoing timer notification and the periodic reminders.
 */
internal class TimerNotifications(
    private val ctx: Context
) {
    fun createChannels() {
        val nm = ctx.getSystemService(NotificationManager::class.java) ?: return

        nm.createNotificationChannel(
            NotificationChannel(
                AppTimerService.CHANNEL_TIMER,
                "App Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows remaining time for app usage limit"
                setShowBadge(false)
            }
        )

        nm.createNotificationChannel(
            NotificationChannel(
                AppTimerService.CHANNEL_REMINDER,
                "Usage Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Periodic reminders when you stay on an app too long"
            }
        )
    }

    fun buildTimerNotification(
        appName: String,
        timeLimitEnabled: Boolean,
        remainingMs: Long,
        startTimeMs: Long
    ): Notification {
        val stopIntent =
            Intent(ctx, AppTimerService::class.java).apply {
                action = AppTimerService.ACTION_STOP
            }
        val stopPI =
            PendingIntent.getService(
                ctx,
                0,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val text =
            if (timeLimitEnabled && remainingMs > 0) {
                val remainingMinutes = (remainingMs / 60_000).coerceAtLeast(1)
                ctx.getString(R.string.timer_notification_text, remainingMinutes.formatDuration(), appName)
            } else {
                val elapsedMs = System.currentTimeMillis() - startTimeMs
                val elapsedMinutes = (elapsedMs / 60_000).coerceAtLeast(1)
                ctx.getString(R.string.reminder_notification_text, appName, elapsedMinutes.formatDuration())
            }

        return NotificationCompat
            .Builder(ctx, AppTimerService.CHANNEL_TIMER)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(ctx.getString(R.string.timer_notification_title))
            .setContentText(text)
            .setOngoing(true)
            .setSilent(true)
            .addAction(R.drawable.notification, ctx.getString(R.string.time_limit_cancel), stopPI)
            .build()
    }

    fun updateTimerNotification(
        appName: String,
        timeLimitEnabled: Boolean,
        remainingMs: Long,
        startTimeMs: Long
    ) {
        val nm = ctx.getSystemService(NotificationManager::class.java) ?: return
        nm.notify(
            AppTimerService.NOTIF_ID_TIMER,
            buildTimerNotification(appName, timeLimitEnabled, remainingMs, startTimeMs)
        )
    }

    fun sendReminderNotification(appName: String, timeText: String) {
        val nm = ctx.getSystemService(NotificationManager::class.java) ?: return
        val notif =
            NotificationCompat
                .Builder(ctx, AppTimerService.CHANNEL_REMINDER)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(ctx.getString(R.string.reminder_notification_title, appName))
                .setContentText(ctx.getString(R.string.reminder_notification_text, appName, timeText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
        nm.notify(AppTimerService.NOTIF_ID_REMINDER, notif)
    }

    fun sendTestReminderNotification(appName: String, minutes: Int) {
        val nm = ctx.getSystemService(NotificationManager::class.java) ?: return
        nm.createNotificationChannel(
            NotificationChannel(
                AppTimerService.CHANNEL_REMINDER,
                "Usage Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Periodic reminders when you stay on an app too long"
            }
        )
        val timeText = "$minutes min"
        val notif =
            NotificationCompat
                .Builder(ctx, AppTimerService.CHANNEL_REMINDER)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(ctx.getString(R.string.reminder_notification_title, appName))
                .setContentText(ctx.getString(R.string.reminder_notification_text, appName, timeText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
        nm.notify(AppTimerService.NOTIF_ID_REMINDER, notif)
    }
}
