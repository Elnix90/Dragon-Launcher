package org.elnix.dragonlauncher.settings.stores.map

import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.boolean
import io.github.elnix90.core.objects.enum
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.enumsui.select.ColorPickerMode
import org.elnix.dragonlauncher.enumsui.toggle.ColorPickerButtonAction
import org.elnix.dragonlauncher.enumsui.toggle.DefaultThemes
import org.elnix.dragonlauncher.i18n.R

@SettingsStore
object ColorModesSettingsStore : MapSettingsStore() {

    @SettingKey
    val colorPickerMode = enum(ColorPickerMode.Default)

    @SettingKey
    val defaultTheme = enum(DefaultThemes.Amoled)

    @SettingKey
    val colorPickerButtonOne = enum(ColorPickerButtonAction.Random)

    @SettingKey
    val colorPickerButtonTwo = enum(ColorPickerButtonAction.Copy)

    @SettingKey
    val dynamicColors = boolean(
        title = R.string.dynamic_colors,
        description = R.string.dynamic_colors_desc,
        default = false
    )

    @SettingKey
    val colorTestMode = boolean(false)

    /**
     * Whether to use my custom-made color schemes for objects, or the default Android colors schemes.
     * For ex: my switch uses no borders, and other colors channels than the default one, while the android one has borders
     * */
    @SettingKey
    val useCustomColorChannels = boolean(
        title = R.string.use_custom_color_channels,
        description = R.string.use_custom_color_channels_desc,
        default = true
    )
}