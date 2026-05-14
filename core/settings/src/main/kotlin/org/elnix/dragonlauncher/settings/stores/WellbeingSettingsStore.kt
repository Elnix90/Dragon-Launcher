package org.elnix.dragonlauncher.settings.stores

import org.elnix.dragonlauncher.enumsui.other.ReminderMode
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.MapSettingsStore
import org.elnix.dragonlauncher.settings.bases.Settings

/**
 * Settings store for the Digital Wellbeing feature.
 * Manages social media pause, guilt mode, and paused apps configuration.
 */
object WellbeingSettingsStore : MapSettingsStore() {

    override val name: String = "Wellbeing"

    override val dataStoreName = DataStoreName.WELLBEING

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

    /*  ─────────────  Main toggles  ─────────────  */

    /**
     * Whether the social media pause feature is enabled
     */
    val socialMediaPauseEnabled = Settings.boolean(
        key = "SOCIAL_MEDIA_PAUSE_ENABLED",
        dataStoreName = dataStoreName,
        default = false
    )

    /**
     * Whether to show guilt-inducing usage statistics
     */
    val guiltModeEnabled = Settings.boolean(
        key = "GUILT_MODE_ENABLED",
        dataStoreName = dataStoreName,
        default = false
    )

    /**
     * Whether to show detailed usage stats (time spent yesterday, etc.)
     */
    val showUsageStats = Settings.boolean(
        key = "SHOW_USAGE_STATS",
        dataStoreName = dataStoreName,
        default = true
    )

    /*  ─────────────  Configuration  ─────────────  */

    /**
     * Duration of the pause countdown in seconds (default 10s)
     */
    val pauseDurationSeconds = Settings.int(
        key = "PAUSE_DURATION_SECONDS",
        dataStoreName = dataStoreName,
        default = 10,
        allowedRange = 3..60
    )

    /*  ─────────────  Periodic Reminder  ─────────────  */

    /**
     * Whether the periodic reminder feature is enabled.
     * When active, the user gets reminded every X minutes that they are still on a paused app.
     */
    val reminderEnabled = Settings.boolean(
        key = "REMINDER_ENABLED",
        dataStoreName = dataStoreName,
        default = false
    )

    /**
     * How often to remind (in minutes). Default 5.
     */
    val reminderIntervalMinutes = Settings.int(
        key = "REMINDER_INTERVAL_MINUTES",
        dataStoreName = dataStoreName,
        default = 5,
        allowedRange = 1..30
    )

    /**
     * Reminder delivery mode: "notification" or "overlay"
     */
    val reminderMode = Settings.enum(
        key = "reminderMode",
        dataStoreName = dataStoreName,
        default = ReminderMode.Overlay,
        enumClass = ReminderMode::class.java
    )

    /**
     * Show session time in popup overlay (time since app opened)
     */
    val popupShowSessionTime = Settings.boolean(
        key = "POPUP_SHOW_SESSION_TIME",
        dataStoreName = dataStoreName,
        default = true
    )

    /**
     * Show today's total time in popup overlay
     */
    val popupShowTodayTime = Settings.boolean(
        key = "POPUP_SHOW_TODAY_TIME",
        dataStoreName = dataStoreName,
        default = true
    )

    /**
     * Show remaining time before limit in popup overlay (when return to launcher enabled)
     */
    val popupShowRemainingTime = Settings.boolean(
        key = "POPUP_SHOW_REMAINING_TIME",
        dataStoreName = dataStoreName,
        default = true
    )

    /*  ─────────────  Return to Launcher  ─────────────  */

    /**
     * Whether the auto-return-to-launcher feature is enabled.
     * User must set a time limit before opening a paused app; after the limit
     * they are brought back to Dragon Launcher.
     */
    val returnToLauncherEnabled = Settings.boolean(
        key = "RETURN_TO_LAUNCHER_ENABLED",
        dataStoreName = dataStoreName,
        default = false
    )


    val pausedApps = Settings.stringSet(
        key = "PAUSED_APPS_LIST",
        dataStoreName = dataStoreName,
        default = emptySet()
    )
}
