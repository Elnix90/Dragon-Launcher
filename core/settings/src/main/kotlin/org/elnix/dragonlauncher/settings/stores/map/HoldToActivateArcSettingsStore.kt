package org.elnix.dragonlauncher.settings.stores.map

import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.objects.BooleanSettingObject.Companion.boolean
import org.elnix.dragonlauncher.settings.bases.objects.FloatSettingObject.Companion.float
import org.elnix.dragonlauncher.settings.bases.objects.IntSettingObject.Companion.int
import org.elnix.dragonlauncher.settings.bases.objects.StringSettingObject.Companion.string
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

object HoldToActivateArcSettingsStore : MapSettingsStore(DataStoreName.HOLD_TO_ACTIVATE) {

    override val ALL: List<BaseSettingObject<*, *>>
        get() = listOf(
            this.holdDelayBeforeStartingLongClickSettings,
            this.longCLickSettingsDuration,
            this.holdToActivateSettingsTolerance,
            this.holdToActivateArcCustomObject,
            this.showToleranceOnMainScreen,
            this.rotationPerSecond,
            this.holdMenuEntries
        )

    val holdDelayBeforeStartingLongClickSettings by int(
        title = R.string.hold_delay_before_starting_long_click_settings,
        description = R.string.hold_delay_before_starting_long_click_settings_desc,
        default = 500,
        allowedRange = 0..2000
    )

    val longCLickSettingsDuration by int(
        title = R.string.long_click_settings_duration,
        description = R.string.long_click_settings_duration_desc,
        default = 1000,
        allowedRange = 0..5000
    )

    val holdToActivateSettingsTolerance by float(
        title = R.string.hold_to_activate_tolerance,
        description = R.string.hold_to_activate_tolerance_desc,
        default = 24f,
        allowedRange = 1f..200f
    )

    val showToleranceOnMainScreen by boolean(
        title = R.string.show_tolerance_on_main_screen,
        description = R.string.show_tolerance_on_main_screen_desc,
        default = false,
    )

    val holdToActivateArcCustomObject by string(
        title = null,
        description = null,
        default = "",
    )

    val rotationPerSecond by float(
        title = R.string.rotation_per_second,
        description = R.string.rotation_per_second_desc,
        default = 0f,
        allowedRange = 0f..5f
    )

    val holdMenuEntries by string(
        title = null,
        description = null,
        default = ""
    )
}