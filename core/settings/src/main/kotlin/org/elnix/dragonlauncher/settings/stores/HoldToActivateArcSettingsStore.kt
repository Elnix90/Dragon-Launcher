package org.elnix.dragonlauncher.settings.stores

import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.MapSettingsStore
import org.elnix.dragonlauncher.settings.bases.Settings

object HoldToActivateArcSettingsStore : MapSettingsStore() {

    override val name: String = "Hold to activate"

    override val dataStoreName = DataStoreName.HOLD_TO_ACTIVATE

    override val ALL: List<BaseSettingObject <*, *> >
        get() = listOf(
            this.holdDelayBeforeStartingLongClickSettings,
            this.longCLickSettingsDuration,
            this.holdToActivateSettingsTolerance,
            this.holdToActivateArcCustomObject,
            this.showToleranceOnMainScreen,
            this.rotationPerSecond,
            this.holdMenuEntries
        )


    val holdDelayBeforeStartingLongClickSettings = Settings.int(
        key = "holdDelayBeforeStartingLongClickSettings",
        dataStoreName = dataStoreName,
        default = 500,
        allowedRange = 0..2000
    )

    val longCLickSettingsDuration = Settings.int(
        key = "longCLickSettingsDuration",
        dataStoreName = dataStoreName,
        default = 1000,
        allowedRange = 0..5000
    )

    val holdToActivateSettingsTolerance = Settings.float(
        key = "holdToActivateSettingsTolerance",
        dataStoreName = dataStoreName,
        default = 24f,
        allowedRange = 1f..200f
    )

    val showToleranceOnMainScreen = Settings.boolean(
        key = "showToleranceOnMainScreen",
        dataStoreName = dataStoreName,
        default = false,
    )

    val holdToActivateArcCustomObject = Settings.string(
        key = "holdToActivateArcCustomObject",
        dataStoreName = dataStoreName,
        default = "",
    )

    val rotationPerSecond = Settings.float(
        key = "rotationPerSecond",
        dataStoreName = dataStoreName,
        default = 0f,
        allowedRange = 0f..5f
    )

    val holdMenuEntries = Settings.string(
        key = "holdMenuEntries2",
        dataStoreName = dataStoreName,
        default = ""
    )
}
