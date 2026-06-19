package org.elnix.dragonlauncher.settings.stores.map

import io.github.elnix90.settings.SettingKey
import io.github.elnix90.settings.SettingStore
import org.elnix.dragonlauncher.enumsui.toggle.LockMethod
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.BooleanSettingObject.Companion.boolean
import org.elnix.dragonlauncher.settings.bases.objects.EnumSettingObject.Companion.enum
import org.elnix.dragonlauncher.settings.bases.objects.IntSettingObject.Companion.int
import org.elnix.dragonlauncher.settings.bases.objects.StringSettingObject.Companion.string
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

@SettingStore
object PrivateSettingsStore : MapSettingsStore(DataStoreName.Private) {

    @SettingKey
    val hasSeenWelcome = boolean(
        title = null,
        description = null,
        default = false
    )

    @SettingKey
    val hasInitialized = boolean(
        title = R.string.has_initialized,
        description = null,
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
        title = null,
        description = null,
        default = 0,
        allowedRange = 0..Int.MAX_VALUE
    )

    @SettingKey
    val lastSeenVersionCodeGoogleLockdownWarning = int(
        title = null,
        description = null,
        default = 0,
        allowedRange = 0..Int.MAX_VALUE
    )

    /** Hashed PIN for settings lock (SHA-256). Empty string means no PIN set. */
    @SettingKey
    val lockPinHash = string(
        title = null,
        description = null,
        default = ""
    )

    @SettingKey
    val lockMethod = enum(
        title = null,
        description = null,
        default = LockMethod.None
    )

    /**
     * Used to remember the page the user left when exiting the welcome screen, and going, for example to the default launcher selection
     */
    @SettingKey
    val welcomeScreenTempPage = int(
        title = null,
        description = null,
        default = 0,
        allowedRange = 0..6,
    )

    @SettingKey
    val lastCrashStackTrace = string(
        title = null,
        description = null,
        default = ""
    )

    @SettingKey
    val isInDragAroundMode = boolean(
        title = null,
        description = null,
        default = false
    )
}