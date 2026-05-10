package org.elnix.dragonlauncher.settings.stores

import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.MapSettingsStore
import org.elnix.dragonlauncher.settings.bases.Settings

object SwipeMapSettingsStore : MapSettingsStore() {

    override val name: String = "Swipe Map"
    override val dataStoreName = DataStoreName.SWIPE_MAP

    override val ALL: List<BaseSettingObject <*, *> >
        get() = listOf(
            this.subNestDefaultRadius,
            this.showSubNestsSlider,
            this.showAdvancedPointTools,
            this.isInDragAroundMode
        )

    val subNestDefaultRadius = Settings.int(
        key = "subNestDefaultRadius",
        dataStoreName = dataStoreName,
        default = 35,
        allowedRange = 0..50
    )

    val showSubNestsSlider = Settings.boolean(
        key = "showSubNestsSlider",
        dataStoreName = UiSettingsStore.dataStoreName,
        default = false
    )

    val showAdvancedPointTools = Settings.boolean(
        key = "showAdvancedPointTools",
        dataStoreName = UiSettingsStore.dataStoreName,
        default = false
    )

    val isInDragAroundMode = Settings.boolean(
        key = "isInDragAroundMode",
        dataStoreName = UiSettingsStore.dataStoreName,
        default = false
    )
}
