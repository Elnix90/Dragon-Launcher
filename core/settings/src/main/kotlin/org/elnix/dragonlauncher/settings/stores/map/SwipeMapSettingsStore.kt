package org.elnix.dragonlauncher.settings.stores.map

import io.github.elnix90.settings.SettingKey
import io.github.elnix90.settings.SettingStore
import org.elnix.dragonlauncher.base.model.serializables.Point.Companion.defaultSwipePointsValues
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.BooleanSettingObject.Companion.boolean
import org.elnix.dragonlauncher.settings.bases.objects.IntSettingObject.Companion.int
import org.elnix.dragonlauncher.settings.bases.objects.PointSettingObject.Companion.point
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

@SettingStore
object SwipeMapSettingsStore : MapSettingsStore(DataStoreName.SWIPE_MAP) {

    @SettingKey
    val subNestDefaultRadius = int(
        title = null,
        description = null,
        default = 35,
        allowedRange = 0..50
    )

    @SettingKey
    val showSubNestsSlider = boolean(
        title = R.string.show_sub_nest_size_slider,
        description = null,
        default = false
    )

    @SettingKey
    val showAdvancedPointTools = boolean(
        title = R.string.show_advanced_edit_tools,
        description = null,
        default = false
    )

    @SettingKey
    val defaultPoint = point(
        title = null,
        description = null,
        default = defaultSwipePointsValues
    )
}