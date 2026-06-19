package org.elnix.dragonlauncher.settings.stores.map

import android.util.Log
import io.github.elnix90.settings.SettingKey
import io.github.elnix90.settings.SettingStore
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.BooleanSettingObject.Companion.boolean
import org.elnix.dragonlauncher.settings.bases.objects.IntSettingObject.Companion.int
import org.elnix.dragonlauncher.settings.bases.objects.StringSettingObject.Companion.string
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

@SettingStore
object DebugSettingsStore : MapSettingsStore(DataStoreName.Debug) {

    @SettingKey
    val debugEnabled = boolean(
        title = R.string.activate_debug_mode,
        description = R.string.activate_debug_mode_desc,
        default = false
    )

    @SettingKey
    val mainScreenDebugInfos = boolean(
        title = R.string.show_debug_infos,
        description = R.string.show_debug_infos_desc,
        default = false
    )

    @SettingKey
    val settingsDebugInfo = boolean(
        title = R.string.show_debug_infos_settings,
        description = R.string.show_debug_infos_settings_desc,
        default = false
    )

    @SettingKey
    val widgetsDebugInfo = boolean(
        title = R.string.show_debug_infos_widgets,
        description = R.string.show_debug_infos_widgets_desc,
        default = false
    )

    @SettingKey
    val workspacesDebugInfo = boolean(
        title = R.string.show_debug_infos_workspace,
        description = R.string.show_debug_infos_workspace_desc,
        default = false
    )

    @SettingKey
    val forceAppLanguageSelector = boolean(
        title = R.string.force_app_language_selector,
        description = R.string.force_app_language_selector_desc,
        default = false
    )

    @SettingKey
    val autoRaiseDragonOnSystemLauncher = boolean(
        title = R.string.auto_raise_dragon_on_system_launcher,
        description = R.string.auto_raise_dragon_on_system_launcher_desc,
        default = false
    )

    @SettingKey
    val systemLauncherPackageName = string(
        title = null,
        description = null,
        default = ""
    )

    @SettingKey
    val useAccessibilityInsteadOfContextToExpandActionPanel = boolean(
        title = R.string.use_accessibility_instead_of_context,
        description = R.string.use_accessibility_instead_of_context_desc,
        default = true
    )

    @SettingKey
    val enableLogging = boolean(
        title = R.string.enable_logging,
        description = R.string.enable_logging_desc,
        default = true
    )

    @SettingKey
    val disableExtensionSignatureCheck = boolean(
        title = R.string.disable_extension_signature_check,
        description = R.string.disable_extension_signature_check_desc,
        default = false
    )

    @SettingKey
    val snackBarLogLevel = int(
        title = R.string.snackbar_log_level,
        description = null,
        default = 7, // No logs
        allowedRange = 2..7
    )

    @SettingKey
    val filesLogLevel = int(
        title = R.string.files_log_level,
        description = null,
        default = Log.DEBUG,
        allowedRange = 2..7
    )

    @SettingKey
    val filterTag = string(
        title = R.string.filter_tag,
        description = null,
        default = ""
    )

    @SettingKey
    val showFps = boolean(
        title = R.string.show_fps,
        description = R.string.show_fps_desc,
        default = false
    )

    @SettingKey
    val showKillLauncherActionInActionPicker = boolean(
        title = R.string.show_kill_launcher_action,
        description = R.string.show_kill_launcher_action_desc,
        default = false
    )
}