package org.elnix.dragonlauncher.settings.stores.map

import androidx.compose.ui.graphics.Color
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.objects.ColorSettingObject.Companion.color
import org.elnix.dragonlauncher.settings.bases.objects.IntSettingObject.Companion.int
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

object StatusBarSettingsStore : MapSettingsStore(DataStoreName.STATUS_BAR) {

    override val ALL: List<BaseSettingObject<*, *>> by lazy {
        listOf(
            this.barBackgroundColor,
            this.barTextColor,
            this.leftPadding,
            this.rightPadding,
            this.topPadding,
            this.bottomPadding
        )
    }

    val barBackgroundColor by color(
        title = R.string.status_bar_background,
        description = null,
        default = Color.Transparent
    )
    val barTextColor by color(
        title = R.string.status_bar_text_color,
        description = null,
        default = Color.White
    )

    val leftPadding by int(
        title = R.string.left_padding,
        description = R.string.left_padding_status_bar_desc,
        default = 5,
        allowedRange = 0..300
    )

    val rightPadding by int(
        title = R.string.right_padding,
        description = R.string.right_padding_status_bar_desc,
        default = 5,
        allowedRange = 0..300
    )

    val topPadding by int(
        title = R.string.top_padding,
        description = R.string.top_padding_status_bar_desc,
        default = 2,
        allowedRange = 0..300
    )

    val bottomPadding by int(
        title = R.string.bottom_padding,
        description = R.string.bottom_padding_status_bar_desc,
        default = 2,
        allowedRange = 0..300
    )
}