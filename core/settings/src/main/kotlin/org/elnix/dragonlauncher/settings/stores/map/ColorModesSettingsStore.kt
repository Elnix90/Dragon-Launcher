package org.elnix.dragonlauncher.settings.stores.map

import io.github.elnix90.settings.SettingKey
import io.github.elnix90.settings.SettingStore
import org.elnix.dragonlauncher.enumsui.select.ColorPickerMode
import org.elnix.dragonlauncher.enumsui.toggle.ColorPickerButtonAction
import org.elnix.dragonlauncher.enumsui.toggle.DefaultThemes
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.BooleanSettingObject.Companion.boolean
import org.elnix.dragonlauncher.settings.bases.objects.EnumSettingObject.Companion.enum
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

@SettingStore
object ColorModesSettingsStore : MapSettingsStore(DataStoreName.ColorMode) {

    @SettingKey
    val colorPickerMode = enum(
        title = null,
        description = null,
        default = ColorPickerMode.Default
    )

    @SettingKey
    val defaultTheme = enum(
        title = null,
        description = null,
        default = DefaultThemes.Amoled
    )

    @SettingKey
    val colorPickerButtonOne = enum(
        title = null,
        description = null,
        default = ColorPickerButtonAction.Random
    )

    @SettingKey
    val colorPickerButtonTwo = enum(
        title = null,
        description = null,
        default = ColorPickerButtonAction.Copy
    )

    @SettingKey
    val dynamicColors = boolean(
        title = R.string.dynamic_colors,
        description = R.string.dynamic_colors_desc,
        default = false
    )

    @SettingKey
    val colorTestMode = boolean(
        title = null,
        description = null,
        default = false
    )

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