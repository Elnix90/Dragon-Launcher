package org.elnix.dragonlauncher.settings.stores.map


import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.boolean
import io.github.elnix90.core.objects.enum
import io.github.elnix90.core.objects.int
import io.github.elnix90.core.objects.stringSet
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.base.model.models.ReminderMode
import org.elnix.dragonlauncher.i18n.R

/**
 * Settings store for the Digital Wellbeing feature.
 * Manages social media pause, guilt mode, and paused apps configuration.
 */
@SettingsStore
object WellbeingSettingsStore : MapSettingsStore() {

    /**
     * Whether the social media pause feature is enabled
     */
    @SettingKey
    val socialMediaPauseEnabled = boolean(
        title = R.string.social_media_pause,
        description = R.string.social_media_pause_description,
        default = false
    )

    /**
     * Whether to show guilt-inducing usage statistics
     */
    @SettingKey
    val guiltModeEnabled = boolean(
        title = R.string.guilt_mode,
        description = R.string.guilt_mode_description,
        default = false
    )

    // TODO
    /**
     * Whether to show detailed usage stats (time spent yesterday, etc.)
     */
    @SettingKey
    val showUsageStats = boolean(true)

    /**
     * Duration of the pause countdown in seconds (default 10s)
     */
    @SettingKey
    val pauseDurationSeconds = int(
        title = R.string.pause_duration,
        description = R.string.pause_duration_description,
        default = 10,
        allowedRange = 3..60
    )

    /**
     * Whether the periodic reminder feature is enabled.
     * When active, the user gets reminded every X minutes that they are still on a paused app.
     */
    @SettingKey
    val reminderEnabled = boolean(
        title = R.string.reminder_mode_title,
        description = R.string.reminder_mode_description,
        default = false
    )

    /**
     * How often to remind (in minutes). Default 5.
     */
    @SettingKey
    val reminderIntervalMinutes = int(
        title = R.string.reminder_interval,
        description = R.string.reminder_interval_description,
        default = 5,
        allowedRange = 1..30
    )

    /**
     * Reminder delivery mode
     */
    @SettingKey
    val reminderMode = enum(
        title = R.string.mode,
        default = ReminderMode.Overlay
    )

    /**
     * Show session time in popup overlay (time since app opened)
     */
    @SettingKey
    val popupShowSessionTime = boolean(
        title = R.string.popup_show_session_time,
        description = R.string.popup_show_session_time_desc,
        default = true
    )

    /**
     * Show today's total time in popup overlay
     */
    @SettingKey
    val popupShowTodayTime = boolean(
        title = R.string.popup_show_today_time,
        description = R.string.popup_show_today_time_desc,
        default = true
    )

    /**
     * Show remaining time before limit in popup overlay (when return to launcher enabled)
     */
    @SettingKey
    val popupShowRemainingTime = boolean(
        title = R.string.popup_show_remaining_time,
        description = R.string.popup_show_remaining_time_desc,
        default = true
    )

    /**
     * Whether the auto-return-to-launcher feature is enabled.
     * User must set a time limit before opening a paused app; after the limit
     * they are brought back to Dragon Launcher.
     */
    @SettingKey
    val returnToLauncherEnabled = boolean(
        title = R.string.return_to_launcher_title,
        description = R.string.return_to_launcher_description,
        default = false
    )

    @SettingKey
    val pausedApps = stringSet(emptySet())
}