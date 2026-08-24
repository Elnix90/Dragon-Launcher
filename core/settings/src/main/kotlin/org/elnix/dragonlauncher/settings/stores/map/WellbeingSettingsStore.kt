package org.elnix.dragonlauncher.settings.stores.map


import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.BooleanSettingObject
import io.github.elnix90.core.objects.EnumSettingObject
import io.github.elnix90.core.objects.IntSettingObject
import io.github.elnix90.core.objects.StringSetSettingObject
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
public object WellbeingSettingsStore : MapSettingsStore() {

    /**
     * Whether the social media pause feature is enabled
     */
    @SettingKey
    public val socialMediaPauseEnabled: BooleanSettingObject = boolean(
        title = R.string.social_media_pause,
        description = R.string.social_media_pause_description,
        icon = R.drawable.self_improvement,
        default = false
    )

    /**
     * Whether to show guilt-inducing usage statistics
     */
    @SettingKey
    public val guiltModeEnabled: BooleanSettingObject = boolean(
        title = R.string.guilt_mode,
        description = R.string.guilt_mode_description,
        icon = R.drawable.timer,
        default = false
    )

    // TODO
    /**
     * Whether to show detailed usage stats (time spent yesterday, etc.)
     */
    @SettingKey
    public val showUsageStats: BooleanSettingObject = boolean(true)

    /**
     * Duration of the pause countdown in seconds (default 10s)
     */
    @SettingKey
    public val pauseDurationSeconds: IntSettingObject = int(
        title = R.string.pause_duration,
        description = R.string.pause_duration_description,
        icon = R.drawable.pause,
        default = 10,
        allowedRange = 3..60
    )

    /**
     * Whether the periodic reminder feature is enabled.
     * When active, the user gets reminded every X minutes that they are still on a paused app.
     */
    @SettingKey
    public val reminderEnabled: BooleanSettingObject = boolean(
        title = R.string.reminder_mode_title,
        description = R.string.reminder_mode_description,
        icon = R.drawable.timer,
        default = false
    )

    /**
     * How often to remind (in minutes). Default 5.
     */
    @SettingKey
    public val reminderIntervalMinutes: IntSettingObject = int(
        title = R.string.reminder_interval,
        description = R.string.reminder_interval_description,
        icon = R.drawable.timer,
        default = 5,
        allowedRange = 1..30
    )

    /**
     * Reminder delivery mode
     */
    @SettingKey
    public val reminderMode: EnumSettingObject<ReminderMode> = enum(
        title = R.string.mode,
        icon = R.drawable.visibility,
        default = ReminderMode.Overlay
    )

    /**
     * Show session time in popup overlay (time since app opened)
     */
    @SettingKey
    public val popupShowSessionTime: BooleanSettingObject = boolean(
        title = R.string.popup_show_session_time,
        description = R.string.popup_show_session_time_desc,
        icon = R.drawable.timer,
        default = true
    )

    /**
     * Show today's total time in popup overlay
     */
    @SettingKey
    public val popupShowTodayTime: BooleanSettingObject = boolean(
        title = R.string.popup_show_today_time,
        description = R.string.popup_show_today_time_desc,
        icon = R.drawable.timer,
        default = true
    )

    /**
     * Show remaining time before limit in popup overlay (when return to launcher enabled)
     */
    @SettingKey
    public val popupShowRemainingTime: BooleanSettingObject = boolean(
        title = R.string.popup_show_remaining_time,
        description = R.string.popup_show_remaining_time_desc,
        icon = R.drawable.timer,
        default = true
    )

    /**
     * Whether the auto-return-to-launcher feature is enabled.
     * User must set a time limit before opening a paused app; after the limit
     * they are brought back to Dragon Launcher.
     */
    @SettingKey
    public val returnToLauncherEnabled: BooleanSettingObject = boolean(
        title = R.string.return_to_launcher_title,
        description = R.string.return_to_launcher_description,
        icon = R.drawable.account_tree,
        default = false
    )

    @SettingKey
    public val pausedApps: StringSetSettingObject = stringSet(emptySet())
}