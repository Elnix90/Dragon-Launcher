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
        icon = R.drawable.colorize,
        default = false
    )

    @SettingKey
    public val iconsTint: ColorSettingObject = color(
        title = R.string.icons_tint,
        default = Color.Unspecified
    )


    @SettingKey
    public val onlyTintIconPack: BooleanSettingObject = boolean(
        title = R.string.only_tint_icon_packs,
        description = R.string.only_tint_icon_packs_desc,
        icon = R.drawable.apps,
        default = true
    )


    @SettingKey
    public val renderForeground: BooleanSettingObject = boolean(
        title = R.string.render_foreground,
        description = R.string.render_foreground_desc,
        icon = R.drawable.wallpaper,
        default = true
    )


    @SettingKey
    public val renderBackground: BooleanSettingObject = boolean(
        title = R.string.render_background,
        description = R.string.render_background_desc,
        icon = R.drawable.wallpaper,
        default = true
    )

    @SettingKey
    public val themedIcons: BooleanSettingObject = boolean(
        title = R.string.themed_icons,
        description = R.string.themed_icons_desc,
        icon = R.drawable.wallpaper,
        default = false
    )

    @SettingKey
    public val forceThemed: BooleanSettingObject = boolean(
        title = R.string.force_themed,
        description = R.string.force_themed_icons_desc,
        icon = R.drawable.wallpaper,
        default = false
    )

    @SettingKey
    public val adaptify: BooleanSettingObject = boolean(
        title = R.string.adaptify,
        description = R.string.adaptify_icons_desc,
        icon = R.drawable.question_mark,
        default = false
    )
}