package org.elnix.dragonlauncher.settings.stores.map

import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.BooleanSettingObject
import io.github.elnix90.core.objects.FloatSettingObject
import io.github.elnix90.core.objects.IntSettingObject
import io.github.elnix90.core.objects.StringSettingObject
import io.github.elnix90.core.objects.boolean
import io.github.elnix90.core.objects.float
import io.github.elnix90.core.objects.int
import io.github.elnix90.core.objects.string
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.i18n.R

@SettingsStore
public object HoldToActivateArcSettingsStore : MapSettingsStore() {

    @SettingKey
    public val holdDelayBeforeStartingLongClickSettings: IntSettingObject = int(
        title = R.string.hold_delay_before_starting_long_click_settings,
        description = R.string.hold_delay_before_starting_long_click_settings_desc,
        default = 500,
        allowedRange = 0..2000
    )

    @SettingKey
    public val longCLickSettingsDuration: IntSettingObject = int(
        title = R.string.long_click_settings_duration,
        description = R.string.long_click_settings_duration_desc,
        default = 1000,
        allowedRange = 0..5000
    )

    @SettingKey
    public val holdToActivateSettingsTolerance: FloatSettingObject = float(
        title = R.string.hold_to_activate_tolerance,
        description = R.string.hold_to_activate_tolerance_desc,
        default = 24f,
        allowedRange = 1f..200f
    )

    @SettingKey
    public val showToleranceOnMainScreen: BooleanSettingObject = boolean(
        title = R.string.show_tolerance_on_main_screen,
        description = R.string.show_tolerance_on_main_screen_desc,
        default = false,
    )

    @SettingKey
    public val holdToActivateArcCustomObject: StringSettingObject = string(
        default = "",
    )

    @SettingKey
    public val rotationPerSecond: FloatSettingObject = float(
        title = R.string.rotation_per_second,
        description = R.string.rotation_per_second_desc,
        default = 0f,
        allowedRange = 0f..5f
    )

    @SettingKey
    public val holdMenuEntriesJson: StringSettingObject = string("")
}