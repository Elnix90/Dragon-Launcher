package org.elnix.dragonlauncher.settings.stores.map

import android.util.Log
import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.boolean
import io.github.elnix90.core.objects.int
import io.github.elnix90.core.objects.string
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.i18n.R

@SettingsStore
object DebugSettingsStore : MapSettingsStore() {
	@SettingKey
	val debugEnabled =
		boolean(
			title = R.string.activate_debug_mode,
			description = R.string.activate_debug_mode_desc,
			icon = R.drawable.bug_report,
			default = false
		)

	@SettingKey
	val mainScreenDebugInfos =
		boolean(
			title = R.string.show_debug_infos,
			description = R.string.show_debug_infos_desc,
			icon = R.drawable.home,
			default = false
		)

	@SettingKey
	val nestDebugOverlay =
		boolean(
			title = R.string.nest_debug_overlay,
			description = R.string.nest_debug_overlay_desc,
			icon = R.drawable.nest_icon,
			default = false
		)

	@SettingKey
	val nestDebugInfo =
		boolean(
			title = R.string.nest_debug_info,
			description = R.string.nest_debug_info_desc,
			icon = R.drawable.nest_icon,
			default = false
		)

	@SettingKey
	val settingsDebugInfo =
		boolean(
			title = R.string.show_debug_infos_settings,
			description = R.string.show_debug_infos_settings_desc,
			icon = R.drawable.settings,
			default = false
		)

	@SettingKey
	val widgetsDebugInfo =
		boolean(
			title = R.string.show_debug_infos_widgets,
			description = R.string.show_debug_infos_widgets_desc,
			icon = R.drawable.widgets,
			default = false
		)

	@SettingKey
	val workspacesDebugInfo =
		boolean(
			title = R.string.show_debug_infos_workspace,
			description = R.string.show_debug_infos_workspace_desc,
			icon = R.drawable.workspaces,
			default = false
		)

	@SettingKey
	val forceAppLanguageSelector =
		boolean(
			title = R.string.force_app_language_selector,
			description = R.string.force_app_language_selector_desc,
			icon = R.drawable.web,
			default = false
		)

	@SettingKey
	val autoRaiseDragonOnSystemLauncher =
		boolean(
			title = R.string.auto_raise_dragon_on_system_launcher,
			description = R.string.auto_raise_dragon_on_system_launcher_desc,
			icon = R.drawable.ic_launcher_luck_white_transparent,
			default = false
		)

	@SettingKey
	val systemLauncherPackageName = string("")

	@SettingKey
	val useAccessibilityInsteadOfContextToExpandActionPanel =
		boolean(
			title = R.string.use_accessibility_instead_of_context,
			description = R.string.use_accessibility_instead_of_context_desc,
			icon = R.drawable.account_circle,
			default = true
		)

	@SettingKey
	val enableLogging =
		boolean(
			title = R.string.enable_logging,
			description = R.string.enable_logging_desc,
			icon = R.drawable.source_notes,
			default = true
		)

	@SettingKey
	val disableExtensionSignatureCheck =
		boolean(
			title = R.string.disable_extension_signature_check,
			description = R.string.disable_extension_signature_check_desc,
			icon = R.drawable.vpn_key,
			default = false
		)

	/**
	 * Whether to disable the warning in the settings, for example when you're not on stock Android (meaning you already escaped google's hell)
	 */
	@SettingKey
	val showGoogleLockDownWarning =
		boolean(
			title = R.string.show_google_lockdown_warning,
			description = R.string.show_google_lockdown_warning_desc,
			icon = R.drawable.visibility,
			default = true
		)

	@SettingKey
	val snackBarLogLevel =
		int(
			title = R.string.snackbar_log_level,
			description = R.string.snackbar_log_level_desc,
			icon = R.drawable.source_notes,
			default = 8, // No logs
			allowedRange = 2..8
		)

	@SettingKey
	val filesLogLevel =
		int(
			title = R.string.files_log_level,
			description = R.string.files_log_level_desc,
			icon = R.drawable.source_notes,
			default = Log.DEBUG,
			allowedRange = 2..8
		)

	@SettingKey
	val filterTag =
		string(
			title = R.string.filter_tag,
			description = R.string.pick_a_character,
			icon = R.drawable.source_notes,
			default = ""
		)

	@SettingKey
	val showFps =
		boolean(
			title = R.string.show_fps,
			description = R.string.show_fps_desc,
			icon = R.drawable._123,
			default = false
		)

	@SettingKey
	val showKillLauncherActionInActionPicker =
		boolean(
			title = R.string.show_kill_launcher_action,
			description = R.string.show_kill_launcher_action_desc,
			icon = R.drawable.ic_action_kill,
			default = false
		)

	@SettingKey
	val useAppEvenIfSignatureIsNotMatched =
		boolean(
			title = R.string.use_app_even_if_signature_does_not_match,
			description = R.string.use_app_even_if_signature_does_not_match_desc,
			icon = R.drawable.encrypted,
			default = false
		)
}
