package org.elnix.dragonlauncher.settings.stores.map

import org.elnix.dragonlauncher.base.model.models.ReminderMode
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.objects.BooleanSettingObject.Companion.boolean
import org.elnix.dragonlauncher.settings.bases.objects.EnumSettingObject.Companion.enum
import org.elnix.dragonlauncher.settings.bases.objects.IntSettingObject.Companion.int
import org.elnix.dragonlauncher.settings.bases.objects.StringSetSettingObject.Companion.stringSet
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

/**
 * Settings store for the Digital Wellbeing feature.
 * Manages social media pause, guilt mode, and paused apps configuration.
 */
object WellbeingSettingsStore : MapSettingsStore(DataStoreName.WELLBEING) {

    override val ALL: List<BaseSettingObject<*, *>>
        get() = listOf(
            this.socialMediaPauseEnabled,
            this.guiltModeEnabled,
            this.pauseDurationSeconds,
            this.showUsageStats,
            this.reminderEnabled,
            this.reminderIntervalMinutes,
            this.reminderMode,
            this.popupShowSessionTime,
            this.popupShowTodayTime,
            this.popupShowRemainingTime,
            this.returnToLauncherEnabled,
            this.pausedApps
        )

    /**
     * Whether the social media pause feature is enabled
     */
    val socialMediaPauseEnabled by boolean(
        title = R.string.social_media_pause,
        description = R.string.social_media_pause_description,
        default = false
    )

    /**
     * Whether to show guilt-inducing usage statistics
     */
    val guiltModeEnabled by boolean(
        title = R.string.guilt_mode,
        description = R.string.guilt_mode_description,
        default = false
    )

    // TODO
    /**
     * Whether to show detailed usage stats (time spent yesterday, etc.)
     */
    val showUsageStats by boolean(
        title = null,
        description = null,
        default = true
    )

    /**
     * Duration of the pause countdown in seconds (default 10s)
     */
    val pauseDurationSeconds by int(
        title = R.string.pause_duration,
        description = R.string.pause_duration_description,
        default = 10,
        allowedRange = 3..60
    )

    /**
     * Whether the periodic reminder feature is enabled.
     * When active, the user gets reminded every X minutes that they are still on a paused app.
     */
    val reminderEnabled by boolean(
        title = R.string.reminder_mode_title,
        description = R.string.reminder_mode_description,
        default = false
    )

    /**
     * How often to remind (in minutes). Default 5.
     */
    val reminderIntervalMinutes by int(
        title = R.string.reminder_interval,
        description = R.string.reminder_interval_description,
        default = 5,
        allowedRange = 1..30
    )

    /**
     * Reminder delivery mode
     */
    val reminderMode by enum(
        title = R.string.mode,
        description = null,
        default = ReminderMode.Overlay,
        enumClass = ReminderMode::class.java
    )

    /**
     * Show session time in popup overlay (time since app opened)
     */
    val popupShowSessionTime by boolean(
        title = R.string.popup_show_session_time,
        description = R.string.popup_show_session_time_desc,
        default = true
    )

    /**
     * Show today's total time in popup overlay
     */
    val popupShowTodayTime by boolean(
        title = R.string.popup_show_today_time,
        description = R.string.popup_show_today_time_desc,
        default = true
    )

    /**
     * Show remaining time before limit in popup overlay (when return to launcher enabled)
     */
    val popupShowRemainingTime by boolean(
        title = R.string.popup_show_remaining_time,
        description = R.string.popup_show_remaining_time_desc,
        default = true
    )

    /**
     * Whether the auto-return-to-launcher feature is enabled.
     * User must set a time limit before opening a paused app; after the limit
     * they are brought back to Dragon Launcher.
     */
    val returnToLauncherEnabled by boolean(
        title = R.string.return_to_launcher_title,
        description = R.string.return_to_launcher_description,
        default = false
    )

    val pausedApps by stringSet(
        title = null,
        description = null,
        default = emptySet()
    )
}