package org.elnix.dragonlauncher.settings.stores.map

import androidx.compose.ui.unit.dp
import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.boolean
import io.github.elnix90.core.objects.dp
import io.github.elnix90.core.objects.int
import io.github.elnix90.core.objects.string
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.specialObjects.action

@SettingsStore
object BehaviorSettingsStore : MapSettingsStore() {
	@SettingKey
	val backAction =
		action(
			title = R.string.back_action,
			description = R.string.back_action_desc,
			icon = R.drawable.back,
			default = Action.None
		)

	@SettingKey
	val doubleClickAction =
		action(
			title = R.string.double_click_action,
			description = R.string.double_click_action_desc,
			icon = R.drawable.animation,
			default = Action.OpenAppDrawer()
		)

	@SettingKey
	val homeAction =
		action(
			title = R.string.home_action,
			description = R.string.home_action_desc,
			icon = R.drawable.home,
			default = Action.OpenDragonLauncherSettings()
		)

	@SettingKey
	val keepScreenOn =
		boolean(
			title = R.string.keep_screen_on,
			description = R.string.keep_screen_on_desc,
			icon = R.drawable.visibility,
			default = false
		)

	@SettingKey
	val leftPadding =
		int(
			default = 60,
			title = R.string.left_padding,
			description = R.string.left_padding_desc,
			allowedRange = 0..300
		)

	@SettingKey
	val rightPadding =
		int(
			default = 60,
			title = R.string.right_padding,
			description = R.string.right_padding_desc,
			allowedRange = 0..300
		)

	@SettingKey
	val topPadding =
		int(
			default = 80,
			title = R.string.top_padding,
			description = R.string.top_padding_desc,
			allowedRange = 0..300
		)

	@SettingKey
	val bottomPadding =
		int(
			default = 100,
			title = R.string.bottom_padding,
			description = R.string.bottom_padding_desc,
			allowedRange = 0..300
		)

	@SettingKey
	val disableHapticFeedbackGlobally =
		boolean(
			title = R.string.disable_haptic_globally,
			description = R.string.disable_haptic_globally_desc,
			icon = R.drawable.haptic,
			default = false
		)

	@SettingKey
	val superWarningMode =
		boolean(
			title = R.string.super_warning_mode,
			description = R.string.super_warning_mode_desc,
			icon = R.drawable.warning,
			default = false
		)

	@SettingKey
	val vibrateOnError =
		boolean(
			title = R.string.vibrate_on_error,
			description = R.string.vibrate_on_error_desc,
			icon = R.drawable.haptic,
			default = false
		)

	@SettingKey
	val alarmSound =
		boolean(
			title = R.string.alarm_sound,
			description = R.string.super_warning_mode_desc,
			icon = R.drawable.volume_up,
			default = false
		)

	@SettingKey
	val metalPipesSound =
		boolean(
			title = R.string.metal_pipes_sound,
			description = R.string.metal_pipes_sound_desc,
			icon = R.drawable.volume_up,
			default = false
		)

	@SettingKey
	val superWarningModeSound =
		int(
			default = 100,
			title = R.string.super_warning_mode_sound,
			description = R.string.super_warning_mode_sound_desc,
			allowedRange = 0..100
		)

	@SettingKey
	val promptForShortcutsWhenAddingApp =
		boolean(
			title = R.string.prompt_shortcuts_when_adding_app,
			description = R.string.prompt_shortcuts_when_adding_app_desc,
			icon = R.drawable.ic_action_pinned_shortcut,
			default = false
		)

	@SettingKey
	val offScreenTimeout =
		int(
			default = 10,
			title = R.string.off_screen_timeout,
			description = R.string.off_screen_timeout_desc,
			allowedRange = -1..60
		)

	@SettingKey
	val createLiveNestByDefaultWhenCreatingOpenCircleNestPoint =
		boolean(
			title = R.string.create_live_nest_by_default,
			description = R.string.create_live_nest_by_default_desc,
			icon = R.drawable.nest_icon,
			default = true
		)

	@SettingKey
	val openRootNestEachTime =
		boolean(
			title = R.string.open_root_nest_each_time,
			description = R.string.open_root_nest_each_time_desc,
			icon = R.drawable.nest_icon,
			default = false
		)

	/**
	 * Adds a secret unlock button in the unlock screen that you can press when
	 */
	@SettingKey
	val secretUnlockButton =
		boolean(
			title = R.string.secret_unlock_button,
			description = R.string.secret_unlock_button_desc,
			icon = R.drawable.lock_open,
			default = false
		)

	/**
	 * Only used when a pattern is used. determined the size of the used pattern
	 *
	 * CRITICAL: when the pattern size changes, the hash must be also recomputed!!
	 */
	@SettingKey
	val patternSize =
		int(
			title = R.string.pattern_size,
			description = R.string.pattern_size_desc,
			icon = R.drawable.apps,
			default = 3,
			allowedRange = 2..10
		)

	/**
	 * Only used when a pattern is used. determined the size of the used pattern
	 *
	 * CRITICAL: when the pattern size changes, the hash must be also recomputed!!
	 */
	@SettingKey
	val patternSensitivity =
		dp(
			title = R.string.pattern_sensitivity,
			description = R.string.pattern_sensitivity_desc,
			icon = R.drawable.apps,
			default = 50.dp,
			allowedRange = 5.dp..200.dp
		)

	/**
	 * The Launcher-wide dragging mode.
	 */
	@SettingKey
	val globalDraggingMode =
		string(
			title = R.string.global_dragging_mode,
			description = R.string.global_dragging_mode_desc,
			icon = R.drawable.shape_line,
			default = ""
		)
}
