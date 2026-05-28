package org.elnix.dragonlauncher.settings.stores

import org.elnix.dragonlauncher.enumsui.select.ColorPickerMode
import org.elnix.dragonlauncher.enumsui.toggle.ColorPickerButtonAction
import org.elnix.dragonlauncher.enumsui.toggle.DefaultThemes
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.boolean
import org.elnix.dragonlauncher.settings.bases.enum
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

object ColorModesSettingsStore : MapSettingsStore(DataStoreName.COLOR_MODE) {

    override val ALL: List<BaseSettingObject <*, *> >
        get() = listOf(
            this.colorPickerMode,
            this.defaultTheme,
            this.colorPickerButtonOne,
            this.colorPickerButtonTwo,
            this.dynamicColor,
            this.colorTestMode
        )

    val colorPickerMode = enum(
        key = "colorPickerMode",
        default = ColorPickerMode.DEFAULTS,
        enumClass = ColorPickerMode::class.java,
    )

    val defaultTheme = enum(
        key = "defaultTheme",
        default = DefaultThemes.AMOLED,
        enumClass = DefaultThemes::class.java
    )

    val colorPickerButtonOne = enum(
        key = "colorPickerButton",
        default = ColorPickerButtonAction.RANDOM,
        enumClass = ColorPickerButtonAction::class.java
    )
    val colorPickerButtonTwo = enum(
        key = "colorPickerButtonTwo",
        default = ColorPickerButtonAction.COPY,
        enumClass = ColorPickerButtonAction::class.java
    )

    val dynamicColor = boolean(
        key = "dynamicColor",
        default = false
    )

    val colorTestMode = boolean(
        key = "colorTestMode",
        default = false
    )
}
