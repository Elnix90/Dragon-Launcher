package org.elnix.dragonlauncher.settings.stores.map

import androidx.compose.ui.graphics.Color
import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.BooleanSettingObject
import io.github.elnix90.core.objects.ColorSettingObject
import io.github.elnix90.core.objects.StringSettingObject
import io.github.elnix90.core.objects.boolean
import io.github.elnix90.core.objects.color
import io.github.elnix90.core.objects.string
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.i18n.R

@SettingsStore
public object IconsSettingsStore : MapSettingsStore() {

    @SettingKey
    public val selectedIconPack: StringSettingObject = string(
        title = R.string.icon_packs,
        default = ""
    )

    @SettingKey
    public val useIconTint: BooleanSettingObject = boolean(
        title = R.string.use_icon_tint,
        description = R.string.use_icon_tint_desc,
        default = false
    )

    @SettingKey
    public val iconsTint: ColorSettingObject = color(
        title = R.string.icons_tint,
        default = Color.Unspecified
    )


    @SettingKey
    public val themedIcons: BooleanSettingObject = boolean(
        title = R.string.themed_icons,
        description = R.string.themed_icons_desc,
        default = false
    )

    @SettingKey
    public val forceThemed: BooleanSettingObject = boolean(
        title = R.string.force_themed,
        description = R.string.force_themed_icons_desc,
        default = false
    )

    @SettingKey
    public val adaptify: BooleanSettingObject = boolean(
        title = R.string.adaptify,
        description = R.string.adaptify_icons_desc,
        default = false
    )
}