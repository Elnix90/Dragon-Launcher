package org.elnix.dragonlauncher.settings.stores.map

import org.elnix.dragonlauncher.enumsui.select.ColorPickerMode
import org.elnix.dragonlauncher.enumsui.toggle.ColorPickerButtonAction
import org.elnix.dragonlauncher.enumsui.toggle.DefaultThemes
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.objects.BooleanSettingObject.Companion.boolean
import org.elnix.dragonlauncher.settings.bases.objects.EnumSettingObject.Companion.enum
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import org.elnix.settings.SettingKey
import org.elnix.settings.SettingStore

@SettingStore
object ColorModesSettingsStore : MapSettingsStore(DataStoreName.COLOR_MODE) {

    override val ALL: List<BaseSettingObject<*, *>> by lazy {
        listOf(
            this.colorPickerMode,
            this.defaultTheme,
            this.colorPickerButtonOne,
            this.colorPickerButtonTwo,
            this.dynamicColor,
            this.colorTestMode
        )
    }

    @SettingKey
    val colorPickerMode = enum(
        title = null,
        description = null,
        default = ColorPickerMode.Default,
        enumClass = ColorPickerMode::class.java,
    )

    @SettingKey
    val defaultTheme = enum(
        title = null,
        description = null,
        default = DefaultThemes.Amoled,
        enumClass = DefaultThemes::class.java
    )

    @SettingKey
    val colorPickerButtonOne = enum(
        title = null,
        description = null,
        default = ColorPickerButtonAction.Random,
        enumClass = ColorPickerButtonAction::class.java
    )

    @SettingKey
    val colorPickerButtonTwo = enum(
        title = null,
        description = null,
        default = ColorPickerButtonAction.Copy,
        enumClass = ColorPickerButtonAction::class.java
    )

    @SettingKey
    val dynamicColor = boolean(
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
}