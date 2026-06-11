package org.elnix.dragonlauncher.settings.stores.map

import org.elnix.dragonlauncher.base.model.serializables.Point.Companion.defaultSwipePointsValues
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.objects.BooleanSettingObject.Companion.boolean
import org.elnix.dragonlauncher.settings.bases.objects.IntSettingObject.Companion.int
import org.elnix.dragonlauncher.settings.bases.objects.PointSettingObject.Companion.point
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

object SwipeMapSettingsStore : MapSettingsStore(DataStoreName.SWIPE_MAP) {

    override val ALL: List<BaseSettingObject<*, *>>
        get() = listOf(
            this.subNestDefaultRadius,
            this.showSubNestsSlider,
            this.showAdvancedPointTools,
            this.defaultPoint
        )

    val subNestDefaultRadius by int(
        title = R.string.empty_string,
        description = R.string.empty_string,
        default = 35,
        allowedRange = 0..50
    )

    val showSubNestsSlider by boolean(
        title = R.string.show_sub_nest_size_slider,
        description = null,
        default = false
    )

    val showAdvancedPointTools by boolean(
        title = R.string.show_advanced_edit_tools,
        description = null,
        default = false
    )

    val defaultPoint by point(
        title = null,
        description = null,
        default = defaultSwipePointsValues
    )
}