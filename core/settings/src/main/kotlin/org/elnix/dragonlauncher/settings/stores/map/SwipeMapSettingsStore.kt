package org.elnix.dragonlauncher.settings.stores.map

import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.BooleanSettingObject
import io.github.elnix90.core.objects.boolean
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.i18n.R

@SettingsStore
public object SwipeMapSettingsStore : MapSettingsStore() {

    @SettingKey
    public val showAdvancedPointTools: BooleanSettingObject = boolean(
        title = R.string.show_advanced_edit_tools,
        default = false
    )
}