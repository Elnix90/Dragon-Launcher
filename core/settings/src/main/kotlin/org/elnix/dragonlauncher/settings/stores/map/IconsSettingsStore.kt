package org.elnix.dragonlauncher.settings.stores.map

import androidx.compose.ui.graphics.Color
import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.boolean
import io.github.elnix90.core.objects.color
import io.github.elnix90.core.objects.string
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.i18n.R

@SettingsStore
object IconsSettingsStore : MapSettingsStore() {

    @SettingKey
    val selectedIconPack = string(
        title = R.string.icon_pack,
        default = ""
    )

    @SettingKey
    val iconPackTint = color(
        title = R.string.icon_pack_tint,
        default = Color.Unspecified
    )


    @SettingKey
    val themedIcons = boolean(
        title = R.string.themed_icons,
        default = false
    )

    @SettingKey
    val forceThemed = boolean(
        title = R.string.force_themed,
        default = false
    )

    @SettingKey
    val adaptify = boolean(
        title = R.string.adaptify,
        default = false
    )
}