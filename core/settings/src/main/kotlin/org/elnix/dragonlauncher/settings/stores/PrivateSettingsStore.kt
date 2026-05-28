package org.elnix.dragonlauncher.settings.stores

import org.elnix.dragonlauncher.enumsui.toggle.LockMethod
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.boolean
import org.elnix.dragonlauncher.settings.bases.enum
import org.elnix.dragonlauncher.settings.bases.int
import org.elnix.dragonlauncher.settings.bases.long
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import org.elnix.dragonlauncher.settings.bases.string

object PrivateSettingsStore : MapSettingsStore(DataStoreName.PRIVATE_SETTINGS) {

    val hasSeenWelcome = boolean(
        key = "hasSeenWelcome",
        default = false
    )

    val hasInitialized = boolean(
        key = "hasInitialized",
        default = false
    )

    val showSetDefaultLauncherBanner = boolean(
        key = "showSetDefaultLauncherBanner",
        default = true
    )

    val hideBetaVersionWarning = boolean(
        key = "hideBetaVersionWarning",
        default = false
    )


    val lastSeenVersionCodeWhatsNew = int(
        key = "lastSeenVersionCode",
        default = 0,
        allowedRange = 0..Int.MAX_VALUE
    )

    val lastSeenVersionCodeGoogleLockdownWarning = int(
        key = "lastSeenVersionCodeGoogleLockdownWarning",
        default = 0,
        allowedRange = 0..Int.MAX_VALUE
    )

    /** Hashed PIN for settings lock (SHA-256). Empty string means no PIN set. */
    val lockPinHash = string(
        key = "lockPinHash",
        default = ""
    )

    val lockMethod = enum(
        key = "lockMethod",
        default = LockMethod.NONE,
        enumClass = LockMethod::class.java
    )

    val samsungPreferSecureFolder = boolean(
        key = "samsung_prefer_secure_folder",
        default = false
    )

    val lastBackupTime = long(
        key = "lastBackupTime",
        default = System.currentTimeMillis(),
        allowedRange = Long.MIN_VALUE..Long.MAX_VALUE
    )

    /**
     * Used to remember the page the user left when exiting the welcome screen, and going, for example to the default launcher selection
     */
    val welcomeScreenTempPage = int(
        key = "welcomeScreenTempPage",
        default = 0,
        allowedRange = 0..6,
    )

    val lastCrashStackTrace = string(
        key = "lastCrashStackTrace",
        default = ""
    )

    override val ALL: List<BaseSettingObject<*,*>> = listOf(
        this.hasSeenWelcome,
        this.hasInitialized,
        this.showSetDefaultLauncherBanner,
        this.hideBetaVersionWarning,
        this.lastSeenVersionCodeWhatsNew,
        this.lastSeenVersionCodeGoogleLockdownWarning,
        this.lockPinHash,
        this.lockMethod,
        this.samsungPreferSecureFolder,
        this.lastBackupTime,
        this.welcomeScreenTempPage,
        this.lastCrashStackTrace
    )
}
