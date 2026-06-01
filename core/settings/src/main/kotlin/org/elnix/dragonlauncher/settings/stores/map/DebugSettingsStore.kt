package org.elnix.dragonlauncher.settings.stores.map

import android.util.Log
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.boolean
import org.elnix.dragonlauncher.settings.bases.int
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import org.elnix.dragonlauncher.settings.bases.string

object DebugSettingsStore : MapSettingsStore(DataStoreName.DEBUG) {

    override val ALL: List<BaseSettingObject<*, *>>
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

    val debugEnabled = boolean(
        key = "debugEnabled",
        default = false
    )

    val debugInfos = boolean(
        key = "debugInfos",
        default = false
    )

    val settingsDebugInfo = boolean(
        key = "settingsDebugInfo",
        default = false
    )

    val widgetsDebugInfo = boolean(
        key = "widgetsDebugInfo",
        default = false
    )

    val workspacesDebugInfo = boolean(
        key = "workspacesDebugInfo",
        default = false
    )

    val forceAppLanguageSelector = boolean(
        key = "forceAppLanguageSelector",
        default = false
    )

    val autoRaiseDragonOnSystemLauncher = boolean(
        key = "autoRaiseDragonOnSystemLauncher",
        default = false
    )

    val systemLauncherPackageName = string(
        key = "systemLauncherPackageName",
        default = ""
    )

    val useAccessibilityInsteadOfContextToExpandActionPanel = boolean(
        key = "useAccessibilityInsteadOfContextToExpandActionPanel",
        default = true
    )

    val enableLogging = boolean(
        key = "enableLogging",
        default = true
    )

    val privateSpaceDebugInfo = boolean(
        key = "privateSpaceDebugInfo",
        default = false
    )

    val disableExtensionSignatureCheck = boolean(
        key = "disableExtensionSignatureCheck",
        default = false
    )

    val snackBarLogLevel = int(
        key = "snackBarLogLevel",
        default = 7, // No logs
        allowedRange = 2..7
    )

    val filesLogLevel = int(
        key = "filesLogLevel",
        default = Log.DEBUG,
        allowedRange = 2..7
    )

    val filterTag = string(
        key = "filterTag",
        default = ""
    )

    val showFps = boolean(
        key = "showFps",
        default = false
    )

    val showKillLauncherActionInActionPicker = boolean(
        key = "showKillLauncherActionInActionPicker",
        default = false
    )

    val showDebugViewModel = boolean(
        key = "showDebugViewModel",
        default = false
    )
}