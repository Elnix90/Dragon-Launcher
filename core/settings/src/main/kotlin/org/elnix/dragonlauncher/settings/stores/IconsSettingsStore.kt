package org.elnix.dragonlauncher.settings.stores

import androidx.compose.ui.graphics.Color
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.boolean
import org.elnix.dragonlauncher.settings.bases.color
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import org.elnix.dragonlauncher.settings.bases.string

object IconsSettingsStore : MapSettingsStore(DataStoreName.ICONS) {
    // unsing explicit this to avoid other stores that have the same name keys to be imported by mistake
    override val ALL: List<BaseSettingObject<*, *>>
        get() = listOf(
            this.selectedIconPack,
            this.iconPackTint
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
