package org.elnix.dragonlauncher.settings.stores.map

import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.BooleanSettingObject
import io.github.elnix90.core.objects.IntSettingObject
import io.github.elnix90.core.objects.boolean
import io.github.elnix90.core.objects.int
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.base.model.serializables.Point.Companion.defaultSwipePointsValues
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.specialObjects.PointSettingObject
import org.elnix.dragonlauncher.settings.specialObjects.point

@SettingsStore
public object SwipeMapSettingsStore : MapSettingsStore() {

    @SettingKey
    public val subNestDefaultRadius: IntSettingObject = int(
        default = 35,
        allowedRange = 0..50
    )

    @SettingKey
    public val showSubNestsSlider: BooleanSettingObject = boolean(
        title = R.string.show_sub_nest_size_slider,
        default = false
    )

    @SettingKey
    public val showAdvancedPointTools: BooleanSettingObject = boolean(
        title = R.string.show_advanced_edit_tools,
        default = false
    )

    @SettingKey
    public val defaultPoint: PointSettingObject = point(defaultSwipePointsValues)
}