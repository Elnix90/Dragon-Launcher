package org.elnix.dragonlauncher.settings.stores.map

import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.boolean
import io.github.elnix90.core.objects.enum
import io.github.elnix90.core.objects.int
import io.github.elnix90.core.objects.string
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.enumsui.toggle.LockMethod
import org.elnix.dragonlauncher.i18n.R

@SettingsStore
object PrivateSettingsStore : MapSettingsStore(backupable = false) {

    @SettingKey
    val hasSeenWelcome = boolean(false)

    @SettingKey
    val hasInitialized = boolean(
        title = R.string.has_initialized,
        default = false
    )

    @SettingKey
    val showSetDefaultLauncherBanner = boolean(
        title = R.string.show_set_default_launcher_banner,
        description = R.string.show_set_default_launcher_banner_desc,
        default = true
    )

    @SettingKey
    val hideBetaVersionWarning = boolean(
        title = R.string.hide_beta_version_warning,
        description = R.string.hide_beta_version_warning_desc,
        default = false
    )

    @SettingKey
    val lastSeenVersionCodeWhatsNew = int(
        default = 0,
        allowedRange = 0..Int.MAX_VALUE
    )

    @SettingKey
    val lastSeenVersionCodeGoogleLockdownWarning = int(
        default = 0,
        allowedRange = 0..Int.MAX_VALUE
    )

    /** Hashed PIN for settings lock (SHA-256). Empty string means no PIN set. */
    @SettingKey
    val lockPinHash = string("")

    @SettingKey
    val lockMethod = enum(LockMethod.None)

    /**
     * Used to remember the page the user left when exiting the welcome screen, and going, for example to the default launcher selection
     */
    @SettingKey
    val welcomeScreenTempPage = int(
        default = 0,
        allowedRange = 0..6,
    )

    @SettingKey
    val lastCrashStackTrace = string("")

    @SettingKey
    val isInDragAroundMode = boolean(false)
}