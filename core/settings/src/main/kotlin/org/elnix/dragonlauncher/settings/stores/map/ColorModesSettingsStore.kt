package org.elnix.dragonlauncher.settings.stores.map

import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.BooleanSettingObject
import io.github.elnix90.core.objects.EnumSettingObject
import io.github.elnix90.core.objects.boolean
import io.github.elnix90.core.objects.enum
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.base.model.enumsui.select.ColorPickerMode
import org.elnix.dragonlauncher.base.model.enumsui.toggle.ColorPickerButtonAction
import org.elnix.dragonlauncher.base.model.enumsui.toggle.DefaultThemes
import org.elnix.dragonlauncher.i18n.R

@SettingsStore
public object ColorModesSettingsStore : MapSettingsStore() {

    @SettingKey
    public val colorPickerMode: EnumSettingObject<ColorPickerMode> = enum(ColorPickerMode.Default)

    @SettingKey
    public val defaultTheme: EnumSettingObject<DefaultThemes> = enum(DefaultThemes.Amoled)

    @SettingKey
    public val colorPickerButtonOne: EnumSettingObject<ColorPickerButtonAction> = enum(
        ColorPickerButtonAction.Random)

    @SettingKey
    public val colorPickerButtonTwo: EnumSettingObject<ColorPickerButtonAction> = enum(
        ColorPickerButtonAction.Copy)

    @SettingKey
    public val dynamicColors: BooleanSettingObject = boolean(
        title = R.string.dynamic_colors,
        description = R.string.dynamic_colors_desc,
        icon = R.drawable.cyclone,
        default = false
    )

    @SettingKey
    public val colorTestMode: BooleanSettingObject = boolean(false)

    /**
     * Whether to use my custom-made color schemes for objects, or the default Android colors schemes.
     * For ex: my switch uses no borders, and other colors channels than the default one, while the android one has borders
     * */
    @SettingKey
    public val useCustomColorChannels: BooleanSettingObject = boolean(
        title = R.string.use_custom_color_channels,
        description = R.string.use_custom_color_channels_desc,
        icon = R.drawable.display_settings,
        default = true
    )
}