package org.elnix.dragonlauncher.settings.stores.map

import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.boolean
import org.elnix.dragonlauncher.settings.bases.float
import org.elnix.dragonlauncher.settings.bases.int
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import org.elnix.dragonlauncher.settings.bases.string

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

    val holdDelayBeforeStartingLongClickSettings = int(
        key = "holdDelayBeforeStartingLongClickSettings",
        default = 500,
        allowedRange = 0..2000
    )

    val longCLickSettingsDuration = int(
        key = "longCLickSettingsDuration",
        default = 1000,
        allowedRange = 0..5000
    )

    val holdToActivateSettingsTolerance = float(
        key = "holdToActivateSettingsTolerance",
        default = 24f,
        allowedRange = 1f..200f
    )

    val showToleranceOnMainScreen = boolean(
        key = "showToleranceOnMainScreen",
        default = false,
    )

    val holdToActivateArcCustomObject = string(
        key = "holdToActivateArcCustomObject",
        default = "",
    )

    val rotationPerSecond = float(
        key = "rotationPerSecond",
        default = 0f,
        allowedRange = 0f..5f
    )

    val holdMenuEntries = string(
        key = "holdMenuEntries2",
        default = ""
    )
}