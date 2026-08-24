package org.elnix.dragonlauncher.settings.stores.map

import android.util.Log
import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.BooleanSettingObject
import io.github.elnix90.core.objects.IntSettingObject
import io.github.elnix90.core.objects.StringSettingObject
import io.github.elnix90.core.objects.boolean
import io.github.elnix90.core.objects.int
import io.github.elnix90.core.objects.string
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.i18n.R

@SettingsStore
public object DebugSettingsStore : MapSettingsStore() {

    @SettingKey
    public val debugEnabled: BooleanSettingObject = boolean(
        title = R.string.activate_debug_mode,
        description = R.string.activate_debug_mode_desc,
        icon = R.drawable.bug_report,
        default = false
    )

    @SettingKey
    public val mainScreenDebugInfos: BooleanSettingObject = boolean(
        title = R.string.show_debug_infos,
        description = R.string.show_debug_infos_desc,
        icon = R.drawable.home,
        default = false
    )

    @SettingKey
    public val nestDebugOverlay: BooleanSettingObject = boolean(
        title = R.string.nest_debug_overlay,
        description = R.string.nest_debug_overlay_desc,
        icon = R.drawable.nest_icon,
        default = false
    )

    @SettingKey
    public val nestDebugInfo: BooleanSettingObject = boolean(
        title = R.string.nest_debug_info,
        description = R.string.nest_debug_info_desc,
        icon = R.drawable.nest_icon,
        default = false
    )

    @SettingKey
    public val settingsDebugInfo: BooleanSettingObject = boolean(
        title = R.string.show_debug_infos_settings,
        description = R.string.show_debug_infos_settings_desc,
        icon = R.drawable.settings,
        default = false
    )

    @SettingKey
    public val widgetsDebugInfo: BooleanSettingObject = boolean(
        title = R.string.show_debug_infos_widgets,
        description = R.string.show_debug_infos_widgets_desc,
        icon = R.drawable.widgets,
        default = false
    )

    @SettingKey
    public val workspacesDebugInfo: BooleanSettingObject = boolean(
        title = R.string.show_debug_infos_workspace,
        description = R.string.show_debug_infos_workspace_desc,
        icon = R.drawable.workspaces,
        default = false
    )

    @SettingKey
    public val forceAppLanguageSelector: BooleanSettingObject = boolean(
        title = R.string.force_app_language_selector,
        description = R.string.force_app_language_selector_desc,
        icon = R.drawable.web,
        default = false
    )

    @SettingKey
    public val autoRaiseDragonOnSystemLauncher: BooleanSettingObject = boolean(
        title = R.string.auto_raise_dragon_on_system_launcher,
        description = R.string.auto_raise_dragon_on_system_launcher_desc,
        icon = R.drawable.ic_launcher_luck_white_transparent,
        default = false
    )

    @SettingKey
    public val systemLauncherPackageName: StringSettingObject = string("")

    @SettingKey
    public val useAccessibilityInsteadOfContextToExpandActionPanel: BooleanSettingObject = boolean(
        title = R.string.use_accessibility_instead_of_context,
        description = R.string.use_accessibility_instead_of_context_desc,
        icon = R.drawable.account_circle,
        default = true
    )

    @SettingKey
    public val enableLogging: BooleanSettingObject = boolean(
        title = R.string.enable_logging,
        description = R.string.enable_logging_desc,
        icon = R.drawable.source_notes,
        default = true
    )

    @SettingKey
    public val disableExtensionSignatureCheck: BooleanSettingObject = boolean(
        title = R.string.disable_extension_signature_check,
        description = R.string.disable_extension_signature_check_desc,
        icon = R.drawable.vpn_key,
        default = false
    )

    @SettingKey
    public val snackBarLogLevel: IntSettingObject = int(
        title = R.string.snackbar_log_level,
        description = R.string.snackbar_log_level_desc,
        icon = R.drawable.source_notes,
        default = 8, // No logs
        allowedRange = 2..8
    )

    @SettingKey
    public val filesLogLevel: IntSettingObject = int(
        title = R.string.files_log_level,
        description = R.string.files_log_level_desc,
        icon = R.drawable.source_notes,
        default = Log.DEBUG,
        allowedRange = 2..8
    )

    @SettingKey
    public val filterTag: StringSettingObject = string(
        title = R.string.filter_tag,
        description = R.string.pick_a_character,
        icon = R.drawable.source_notes,
        default = ""
    )

    @SettingKey
    public val showFps: BooleanSettingObject = boolean(
        title = R.string.show_fps,
        description = R.string.show_fps_desc,
        icon = R.drawable._123,
        default = false
    )

    @SettingKey
    public val showKillLauncherActionInActionPicker: BooleanSettingObject = boolean(
        title = R.string.show_kill_launcher_action,
        description = R.string.show_kill_launcher_action_desc,
        icon = R.drawable.ic_action_kill,
        default = false
    )
}