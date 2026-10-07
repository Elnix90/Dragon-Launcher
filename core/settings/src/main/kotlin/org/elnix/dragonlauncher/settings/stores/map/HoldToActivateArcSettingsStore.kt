package org.elnix.dragonlauncher.settings.stores.map

import androidx.compose.ui.unit.dp
import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.boolean
import io.github.elnix90.core.objects.dp
import io.github.elnix90.core.objects.float
import io.github.elnix90.core.objects.int
import io.github.elnix90.core.objects.string
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.i18n.R

@SettingsStore
object HoldToActivateArcSettingsStore : MapSettingsStore() {
	@SettingKey
	val holdDelayBeforeStartingLongClickSettings =
		int(
			title = R.string.hold_delay_before_starting_long_click_settings,
			description = R.string.hold_delay_before_starting_long_click_settings_desc,
			icon = R.drawable.timer,
			default = 500,
			allowedRange = 0..2000
		)

	@SettingKey
	val longCLickSettingsDuration =
		int(
			title = R.string.long_click_settings_duration,
			description = R.string.long_click_settings_duration_desc,
			icon = R.drawable.timer,
			default = 2000,
			allowedRange = 0..5000
		)

	@SettingKey
	val holdToActivateSettingsTolerance =
		dp(
			title = R.string.hold_to_activate_tolerance,
			description = R.string.hold_to_activate_tolerance_desc,
			icon = R.drawable.circle,
			default = 10.dp,
			allowedRange = 5.dp..200.dp
		)

	@SettingKey
	val showToleranceOnMainScreen =
		boolean(
			title = R.string.show_tolerance_on_main_screen,
			description = R.string.show_tolerance_on_main_screen_desc,
			icon = R.drawable.circle,
			default = false
		)

	@SettingKey
	val rotationsPerSecond =
		float(
			title = R.string.rotation_per_second,
			description = R.string.rotation_per_second_desc,
			icon = R.drawable.trhee_d_rotation,
			default = 0f,
			allowedRange = 0f..5f
		)

	@SettingKey
	val holdMenuEntriesJson = string("")

	/** Use the computing of HSV color to produce a color that depends on the angle / progress */
	@SettingKey
	val holdRgbLoading =
		boolean(
			title = R.string.rgb_loading_settings,
			description = R.string.rgb_loading_description,
			icon = R.drawable.palette,
			default = false
		)

	@SettingKey
	val pulsingRadius =
		float(
			title = R.string.pulsing_radius,
			description = R.string.pulsing_radius_desc,
			icon = R.drawable.circle,
			default = 1.0f,
			allowedRange = 0.5f..4f
		)

	@SettingKey
	val pulsingRDuration =
		int(
			title = R.string.pulsing_duration,
			description = R.string.pulsing_duration_desc,
			icon = R.drawable.circle,
			default = 500,
			allowedRange = 1..6000
		)
}
