package org.elnix.dragonlauncher.settings.stores.map

import android.util.Log
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.objects.BooleanSettingObject.Companion.boolean
import org.elnix.dragonlauncher.settings.bases.objects.IntSettingObject.Companion.int
import org.elnix.dragonlauncher.settings.bases.objects.StringSettingObject.Companion.string
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

object DebugSettingsStore : MapSettingsStore(DataStoreName.DEBUG) {

    override val ALL: List<BaseSettingObject<*, *>> by lazy {
        listOf(
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
            this.disableExtensionSignatureCheck,
            this.snackBarLogLevel,
            this.filesLogLevel,
            this.showFps,
            this.filterTag,
            this.showKillLauncherActionInActionPicker,
        )
    }

    val debugEnabled by boolean(
        title = R.string.activate_debug_mode,
        description = R.string.activate_debug_mode_desc,
        default = false
    )

    val debugInfos by boolean(
        title = R.string.show_debug_infos,
        description = R.string.show_debug_infos_desc,
        default = false
    )

    val settingsDebugInfo by boolean(
        title = R.string.show_debug_infos_settings,
        description = R.string.show_debug_infos_settings_desc,
        default = false
    )

    val widgetsDebugInfo by boolean(
        title = R.string.show_debug_infos_widgets,
        description = R.string.show_debug_infos_widgets_desc,
        default = false
    )

    val workspacesDebugInfo by boolean(
        title = R.string.show_debug_infos_workspace,
        description = R.string.show_debug_infos_workspace_desc,
        default = false
    )

    val forceAppLanguageSelector by boolean(
        title = R.string.force_app_language_selector,
        description = R.string.force_app_language_selector_desc,
        default = false
    )

    val autoRaiseDragonOnSystemLauncher by boolean(
        title = R.string.auto_raise_dragon_on_system_launcher,
        description = R.string.auto_raise_dragon_on_system_launcher_desc,
        default = false
    )

    val systemLauncherPackageName by string(
        title = null,
        description = null,
        default = ""
    )

    val useAccessibilityInsteadOfContextToExpandActionPanel by boolean(
        title = R.string.use_accessibility_instead_of_context,
        description = R.string.use_accessibility_instead_of_context_desc,
        default = true
    )

    val enableLogging by boolean(
        title = R.string.enable_logging,
        description = R.string.enable_logging_desc,
        default = true
    )

    val disableExtensionSignatureCheck by boolean(
        title = R.string.disable_extension_signature_check,
        description = R.string.disable_extension_signature_check_desc,
        default = false
    )

    val snackBarLogLevel by int(
        title = R.string.snackbar_log_level,
        description = null,
        default = 7, // No logs
        allowedRange = 2..7
    )

    val filesLogLevel by int(
        title = R.string.files_log_level,
        description = null,
        default = Log.DEBUG,
        allowedRange = 2..7
    )

    val filterTag by string(
        title = R.string.filter_tag,
        description = null,
        default = ""
    )

    val showFps by boolean(
        title = R.string.show_fps,
        description = R.string.show_fps_desc,
        default = false
    )

    val showKillLauncherActionInActionPicker by boolean(
        title = R.string.show_kill_launcher_action,
        description = R.string.show_kill_launcher_action_desc,
        default = false
    )
}