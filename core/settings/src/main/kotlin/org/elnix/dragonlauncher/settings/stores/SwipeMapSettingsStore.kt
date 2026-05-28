package org.elnix.dragonlauncher.settings.stores

import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.boolean
import org.elnix.dragonlauncher.settings.bases.int
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

object SwipeMapSettingsStore : MapSettingsStore(DataStoreName.SWIPE_MAP) {

    override val ALL: List<BaseSettingObject <*, *> >
        get() = listOf(
            this.subNestDefaultRadius,
            this.showSubNestsSlider,
            this.showAdvancedPointTools,
            this.isInDragAroundMode
        )

    val subNestDefaultRadius = int(
        key = "subNestDefaultRadius",
        default = 35,
        allowedRange = 0..50
    )

    val showSubNestsSlider = boolean(
        key = "showSubNestsSlider",
        default = false
    )

    val showAdvancedPointTools = boolean(
        key = "showAdvancedPointTools",
        default = false
    )

    val isInDragAroundMode = boolean(
        key = "isInDragAroundMode",
        default = false
    )
}
