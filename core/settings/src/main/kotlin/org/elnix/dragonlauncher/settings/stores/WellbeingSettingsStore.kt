package org.elnix.dragonlauncher.settings.stores

import org.elnix.dragonlauncher.enumsui.other.ReminderMode
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.boolean
import org.elnix.dragonlauncher.settings.bases.enum
import org.elnix.dragonlauncher.settings.bases.int
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import org.elnix.dragonlauncher.settings.bases.stringSet

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

    /*  ─────────────  Main toggles  ─────────────  */

    /**
     * Whether the social media pause feature is enabled
     */
    val socialMediaPauseEnabled = boolean(
        key = "SOCIAL_MEDIA_PAUSE_ENABLED",
        default = false
    )

    /**
     * Whether to show guilt-inducing usage statistics
     */
    val guiltModeEnabled = boolean(
        key = "GUILT_MODE_ENABLED",
        default = false
    )

    /**
     * Whether to show detailed usage stats (time spent yesterday, etc.)
     */
    val showUsageStats = boolean(
        key = "SHOW_USAGE_STATS",
        default = true
    )

    /*  ─────────────  Configuration  ─────────────  */

    /**
     * Duration of the pause countdown in seconds (default 10s)
     */
    val pauseDurationSeconds = int(
        key = "PAUSE_DURATION_SECONDS",
        default = 10,
        allowedRange = 3..60
    )

    /*  ─────────────  Periodic Reminder  ─────────────  */

    /**
     * Whether the periodic reminder feature is enabled.
     * When active, the user gets reminded every X minutes that they are still on a paused app.
     */
    val reminderEnabled = boolean(
        key = "REMINDER_ENABLED",
        default = false
    )

    /**
     * How often to remind (in minutes). Default 5.
     */
    val reminderIntervalMinutes = int(
        key = "REMINDER_INTERVAL_MINUTES",
        default = 5,
        allowedRange = 1..30
    )

    /**
     * Reminder delivery mode: "notification" or "overlay"
     */
    val reminderMode = enum(
        key = "reminderMode",
        default = ReminderMode.Overlay,
        enumClass = ReminderMode::class.java
    )

    /**
     * Show session time in popup overlay (time since app opened)
     */
    val popupShowSessionTime = boolean(
        key = "POPUP_SHOW_SESSION_TIME",
        default = true
    )

    /**
     * Show today's total time in popup overlay
     */
    val popupShowTodayTime = boolean(
        key = "POPUP_SHOW_TODAY_TIME",
        default = true
    )

    /**
     * Show remaining time before limit in popup overlay (when return to launcher enabled)
     */
    val popupShowRemainingTime = boolean(
        key = "POPUP_SHOW_REMAINING_TIME",
        default = true
    )

    /*  ─────────────  Return to Launcher  ─────────────  */

    /**
     * Whether the auto-return-to-launcher feature is enabled.
     * User must set a time limit before opening a paused app; after the limit
     * they are brought back to Dragon Launcher.
     */
    val returnToLauncherEnabled = boolean(
        key = "RETURN_TO_LAUNCHER_ENABLED",
        default = false
    )


    val pausedApps = stringSet(
        key = "PAUSED_APPS_LIST",
        default = emptySet()
    )
}
