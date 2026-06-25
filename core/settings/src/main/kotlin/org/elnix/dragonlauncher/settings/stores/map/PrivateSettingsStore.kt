package org.elnix.dragonlauncher.settings.stores.map

import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.BooleanSettingObject
import io.github.elnix90.core.objects.EnumSettingObject
import io.github.elnix90.core.objects.IntSettingObject
import io.github.elnix90.core.objects.StringSettingObject
import io.github.elnix90.core.objects.boolean
import io.github.elnix90.core.objects.enum
import io.github.elnix90.core.objects.int
import io.github.elnix90.core.objects.string
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.enumsui.toggle.LockMethod
import org.elnix.dragonlauncher.i18n.R

@SettingsStore
public object PrivateSettingsStore : MapSettingsStore(backupable = false) {

    @SettingKey
    public val hasSeenWelcome: BooleanSettingObject = boolean(false)

    @SettingKey
    public val hasInitialized: BooleanSettingObject = boolean(
        title = R.string.has_initialized,
        default = false
    )

    @SettingKey
    public val showSetDefaultLauncherBanner: BooleanSettingObject = boolean(
        title = R.string.show_set_default_launcher_banner,
        description = R.string.show_set_default_launcher_banner_desc,
        default = true
    )

    @SettingKey
    public val hideBetaVersionWarning: BooleanSettingObject = boolean(
        title = R.string.hide_beta_version_warning,
        description = R.string.hide_beta_version_warning_desc,
        default = false
    )

    @SettingKey
    public val lastSeenVersionCodeWhatsNew: IntSettingObject = int(
        default = 0,
        allowedRange = 0..Int.MAX_VALUE
    )

    @SettingKey
    public val lastSeenVersionCodeGoogleLockdownWarning: IntSettingObject = int(
        default = 0,
        allowedRange = 0..Int.MAX_VALUE
    )

    /** Hashed PIN for settings lock (SHA-256). Empty string means no PIN set. */
    @SettingKey
    public val lockPinHash: StringSettingObject = string("")

    @SettingKey
    public val lockMethod: EnumSettingObject<LockMethod> = enum(LockMethod.None)

    /**
     * Used to remember the page the user left when exiting the welcome screen, and going, for example to the default launcher selection
     */
    @SettingKey
    public val welcomeScreenTempPage: IntSettingObject = int(
        default = 0,
        allowedRange = 0..6,
    )

    @SettingKey
    public val lastCrashStackTrace: StringSettingObject = string("")

    @SettingKey
    public val isInDragAroundMode: BooleanSettingObject = boolean(false)
}