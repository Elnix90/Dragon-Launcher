package org.elnix.dragonlauncher.settings.stores.map

import androidx.compose.ui.graphics.Color
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.objects.BooleanSettingObject.Companion.boolean
import org.elnix.dragonlauncher.settings.bases.objects.ColorSettingObject.Companion.color
import org.elnix.dragonlauncher.settings.bases.objects.StringSettingObject.Companion.string
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

object IconsSettingsStore : MapSettingsStore(DataStoreName.ICONS) {

    override val ALL: List<BaseSettingObject<*, *>> by lazy {
        listOf(
            this.selectedIconPack,
            this.iconPackTint,
            this.themedIcons,
            this.forceThemed,
            this.adaptify
        )
    }

    val selectedIconPack by string(
        title = R.string.icon_pack,
        description = null,
        default = ""
    )

    val iconPackTint by color(
        title = R.string.icon_pack_tint,
        description = null,
        default = Color.Unspecified
    )


    val themedIcons by boolean(
        title = R.string.themed_icons,
        description = null,
        default = false
    )

    val forceThemed by boolean(
        title = R.string.force_themed,
        description = null,
        default = false
    )

    val adaptify by boolean(
        title = R.string.adaptify,
        description = null,
        default = false
    )
}