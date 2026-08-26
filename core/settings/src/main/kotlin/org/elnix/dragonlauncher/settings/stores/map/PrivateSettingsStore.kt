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
import org.elnix.dragonlauncher.base.model.enumsui.toggle.LockMethod
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
        icon = R.drawable.question_mark,
        default = true
    )

    @SettingKey
    public val showReselectBackupBanner: BooleanSettingObject = boolean(
        title = R.string.show_reselect_backup_banner,
        description = R.string.show_set_default_launcher_banner_desc,
        icon = R.drawable.question_mark,
        default = true
    )

    @SettingKey
    public val hideBetaVersionWarning: BooleanSettingObject = boolean(
        title = R.string.hide_beta_version_warning,
        description = R.string.hide_beta_version_warning_desc,
        icon = R.drawable.warning,
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

    /**
     *  Hashed code for settings lock (SHA-256).
     *  This can contain either the Pattern hashed or the PIN hashed.
     *  They are both handled as string, containing the digits in the LtR direction:
     */
    @SettingKey
    public val settingsHash: StringSettingObject = string("")

    /**
     *  Hashed code for launching actions (SHA-256).
     *  This can contain either the Pattern hashed or the PIN hashed.
     *  They are both handled as string, containing the digits in the LtR direction:
     */
    @SettingKey
    public val actionsHash: StringSettingObject = string("")


    @SettingKey
    public val lockMethod: EnumSettingObject<LockMethod> = enum(LockMethod.None)

    /**
     * The lock method for the actions
     */
    @SettingKey
    public val actionsLockMethod: EnumSettingObject<LockMethod> = enum(LockMethod.None)


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