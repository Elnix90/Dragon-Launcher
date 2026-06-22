package org.elnix.dragonlauncher.settings.stores.map

import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.boolean
import io.github.elnix90.core.objects.float
import io.github.elnix90.core.objects.int
import io.github.elnix90.core.objects.string
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.i18n.R

@SettingsStore
object HoldToActivateArcSettingsStore : MapSettingsStore() {

    @SettingKey
    val holdDelayBeforeStartingLongClickSettings = int(
        title = R.string.hold_delay_before_starting_long_click_settings,
        description = R.string.hold_delay_before_starting_long_click_settings_desc,
        default = 500,
        allowedRange = 0..2000
    )

    @SettingKey
    val longCLickSettingsDuration = int(
        title = R.string.long_click_settings_duration,
        description = R.string.long_click_settings_duration_desc,
        default = 1000,
        allowedRange = 0..5000
    )

    @SettingKey
    val holdToActivateSettingsTolerance = float(
        title = R.string.hold_to_activate_tolerance,
        description = R.string.hold_to_activate_tolerance_desc,
        default = 24f,
        allowedRange = 1f..200f
    )

    @SettingKey
    val showToleranceOnMainScreen = boolean(
        title = R.string.show_tolerance_on_main_screen,
        description = R.string.show_tolerance_on_main_screen_desc,
        default = false,
    )

    @SettingKey
    val holdToActivateArcCustomObject = string(
        default = "",
    )

    @SettingKey
    val rotationPerSecond = float(
        title = R.string.rotation_per_second,
        description = R.string.rotation_per_second_desc,
        default = 0f,
        allowedRange = 0f..5f
    )

    @SettingKey
    val holdMenuEntriesJson = string("")
}