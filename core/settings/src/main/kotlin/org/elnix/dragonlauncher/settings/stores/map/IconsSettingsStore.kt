package org.elnix.dragonlauncher.settings.stores.map

import androidx.compose.ui.graphics.Color
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.objects.BooleanSettingObject.Companion.boolean
import org.elnix.dragonlauncher.settings.bases.objects.ColorSettingObject.Companion.color
import org.elnix.dragonlauncher.settings.bases.objects.StringSettingObject.Companion.string
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

object IconsSettingsStore : MapSettingsStore(DataStoreName.ICONS) {

    override val ALL: List<BaseSettingObject<*, *>>
        get() = listOf(
            this.selectedIconPack,
            this.iconPackTint,
            this.themedIcons,
            this.forceThemed,
            this.adaptify
        )

    val selectedIconPack = string(
        key = "selectedIconPack",
        default = ""
    )

    val iconPackTint = color(
        key = "iconPackTint",
        default = Color.Unspecified
    )


    val themedIcons = boolean(
        key = "themedIcons",
        default = false
    )

    val forceThemed = boolean(
        key = "forceThemed",
        default = false
    )

    val adaptify = boolean(
        key = "adaptify",
        default = false
    )
}