package org.elnix.dragonlauncher.settings.stores.map

import org.elnix.dragonlauncher.enumsui.toggle.LockMethod
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.objects.BooleanSettingObject.Companion.boolean
import org.elnix.dragonlauncher.settings.bases.objects.EnumSettingObject.Companion.enum
import org.elnix.dragonlauncher.settings.bases.objects.IntSettingObject.Companion.int
import org.elnix.dragonlauncher.settings.bases.objects.LongSettingObject.Companion.long
import org.elnix.dragonlauncher.settings.bases.objects.StringSettingObject.Companion.string
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

object PrivateSettingsStore : MapSettingsStore(DataStoreName.PRIVATE_SETTINGS) {

    val hasSeenWelcome by boolean(
        title = null,
        description = null,
        default = false
    )

    val hasInitialized by boolean(
        title = R.string.has_initialized,
        description = null,
        default = false
    )

    val showSetDefaultLauncherBanner by boolean(
        title = R.string.show_set_default_launcher_banner,
        description = R.string.show_set_default_launcher_banner_desc,
        default = true
    )

    val hideBetaVersionWarning by boolean(
        title = R.string.hide_beta_version_warning,
        description = R.string.hide_beta_version_warning_desc,
        default = false
    )

    val lastSeenVersionCodeWhatsNew by int(
        title = null,
        description = null,
        default = 0,
        allowedRange = 0..Int.MAX_VALUE
    )

    val lastSeenVersionCodeGoogleLockdownWarning by int(
        title = null,
        description = null,
        default = 0,
        allowedRange = 0..Int.MAX_VALUE
    )

    /** Hashed PIN for settings lock (SHA-256). Empty string means no PIN set. */
    val lockPinHash by string(
        title = null,
        description = null,
        default = ""
    )

    val lockMethod by enum(
        title = null,
        description = null,
        default = LockMethod.NONE,
        enumClass = LockMethod::class.java
    )

    val lastBackupTime by long(
        title = null,
        description = null,
        default = System.currentTimeMillis(),
        allowedRange = Long.MIN_VALUE..Long.MAX_VALUE
    )

    /**
     * Used to remember the page the user left when exiting the welcome screen, and going, for example to the default launcher selection
     */
    val welcomeScreenTempPage by int(
        title = null,
        description = null,
        default = 0,
        allowedRange = 0..6,
    )

    val lastCrashStackTrace by string(
        title = null,
        description = null,
        default = ""
    )

    val isInDragAroundMode by boolean(
        title = null,
        description = null,
        default = false
    )


    override val ALL: List<BaseSettingObject<*, *>> by lazy {
        listOf(
            this.hasSeenWelcome,
            this.hasInitialized,
            this.showSetDefaultLauncherBanner,
            this.hideBetaVersionWarning,
            this.lastSeenVersionCodeWhatsNew,
            this.lastSeenVersionCodeGoogleLockdownWarning,
            this.lockPinHash,
            this.lockMethod,
            this.lastBackupTime,
            this.welcomeScreenTempPage,
            this.lastCrashStackTrace,
            this.isInDragAroundMode
        )
    }
}