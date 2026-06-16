package org.elnix.dragonlauncher.settings.stores.map

import io.github.elnix90.settings.SettingKey
import io.github.elnix90.settings.SettingStore
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.BooleanSettingObject.Companion.boolean
import org.elnix.dragonlauncher.settings.bases.objects.FloatSettingObject.Companion.float
import org.elnix.dragonlauncher.settings.bases.objects.IntSettingObject.Companion.int
import org.elnix.dragonlauncher.settings.bases.objects.StringSettingObject.Companion.string
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

@SettingStore
object HoldToActivateArcSettingsStore : MapSettingsStore(DataStoreName.HOLD_TO_ACTIVATE) {

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
        title = null,
        description = null,
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
    val holdMenuEntries = string(
        title = null,
        description = null,
        default = ""
    )
}