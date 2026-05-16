package org.elnix.dragonlauncher.settings.stores

import android.util.Log
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.MapSettingsStore
import org.elnix.dragonlauncher.settings.bases.Settings

object DebugSettingsStore : MapSettingsStore() {
    override val name: String = "Debug"
    override val dataStoreName = DataStoreName.DEBUG

    val debugEnabled = Settings.boolean(
        key = "debugEnabled",
        dataStoreName = dataStoreName,
        default = false
    )

    val debugInfos = Settings.boolean(
        key = "debugInfos",
        dataStoreName = dataStoreName,
        default = false
    )

    val settingsDebugInfo = Settings.boolean(
        key = "settingsDebugInfo",
        dataStoreName = dataStoreName,
        default = false
    )

    val widgetsDebugInfo = Settings.boolean(
        key = "widgetsDebugInfo",
        dataStoreName = dataStoreName,
        default = false
    )

    val workspacesDebugInfo = Settings.boolean(
        key = "workspacesDebugInfo",
        dataStoreName = dataStoreName,
        default = false
    )

    val forceAppLanguageSelector = Settings.boolean(
        key = "forceAppLanguageSelector",
        dataStoreName = dataStoreName,
        default = false
    )

    val autoRaiseDragonOnSystemLauncher = Settings.boolean(
        key = "autoRaiseDragonOnSystemLauncher",
        dataStoreName = dataStoreName,
        default = false
    )

    val systemLauncherPackageName = Settings.string(
        key = "systemLauncherPackageName",
        dataStoreName = dataStoreName,
        default = ""
    )

    val useAccessibilityInsteadOfContextToExpandActionPanel = Settings.boolean(
        key = "useAccessibilityInsteadOfContextToExpandActionPanel",
        dataStoreName = dataStoreName,
        default = true
    )

    val enableLogging = Settings.boolean(
        key = "enableLogging",
        dataStoreName = dataStoreName,
        default = true
    )

    val privateSpaceDebugInfo = Settings.boolean(
        key = "privateSpaceDebugInfo",
        dataStoreName = dataStoreName,
        default = false
    )

    val disableExtensionSignatureCheck = Settings.boolean(
        key = "disableExtensionSignatureCheck",
        dataStoreName = dataStoreName,
        default = false
    )

    val snackBarLogLevel = Settings.int(
        key = "snackBarLogLevel",
        dataStoreName = dataStoreName,
        default = 7, // No logs
        allowedRange = 2..7
    )

    val filesLogLevel = Settings.int(
        key = "filesLogLevel",
        dataStoreName = dataStoreName,
        default = Log.DEBUG,
        allowedRange = 2..7
    )

    val filterTag = Settings.string(
        key = "filterTag",
        dataStoreName = dataStoreName,
        default = ""
    )

    val showFps = Settings.boolean(
        key = "showFps",
        dataStoreName = dataStoreName,
        default = false
    )

    val showKillLauncherActionInActionPicker = Settings.boolean(
        key = "showKillLauncherActionInActionPicker",
        dataStoreName = dataStoreName,
        default = false
    )

    val showDebugViewModel = Settings.boolean(
        key = "showDebugViewModel",
        dataStoreName = dataStoreName,
        default = false
    )

    override val ALL: List<BaseSettingObject<*,*>>
        get() = listOf(
            this.debugEnabled,
            this.debugInfos,
            this.settingsDebugInfo,
            this.widgetsDebugInfo,
            this.workspacesDebugInfo,
            this.forceAppLanguageSelector,
            this.autoRaiseDragonOnSystemLauncher,
            this.systemLauncherPackageName,
            this.useAccessibilityInsteadOfContextToExpandActionPanel,
            this.enableLogging,
            this.privateSpaceDebugInfo,
            this.disableExtensionSignatureCheck,
            this.snackBarLogLevel,
            this.filesLogLevel,
            this.showFps,
            this.filterTag,
            this.showKillLauncherActionInActionPicker,
            this.showDebugViewModel
        )
    }
