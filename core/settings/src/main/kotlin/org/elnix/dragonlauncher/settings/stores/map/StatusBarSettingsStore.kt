package org.elnix.dragonlauncher.settings.stores.map

import androidx.compose.ui.graphics.Color
import io.github.elnix90.settings.SettingKey
import io.github.elnix90.settings.SettingStore
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.ColorSettingObject.Companion.color
import org.elnix.dragonlauncher.settings.bases.objects.IntSettingObject.Companion.int
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

@SettingStore
object StatusBarSettingsStore : MapSettingsStore(DataStoreName.StatusBar) {

    @SettingKey
    val barBackgroundColor = color(
        title = R.string.status_bar_background,
        description = null,
        default = Color.Transparent
    )

    @SettingKey
    val barTextColor = color(
        title = R.string.status_bar_text_color,
        description = null,
        default = Color.White
    )

    @SettingKey
    val leftPadding = int(
        title = R.string.left_padding,
        description = R.string.left_padding_status_bar_desc,
        default = 5,
        allowedRange = 0..300
    )

    @SettingKey
    val rightPadding = int(
        title = R.string.right_padding,
        description = R.string.right_padding_status_bar_desc,
        default = 5,
        allowedRange = 0..300
    )

    @SettingKey
    val topPadding = int(
        title = R.string.top_padding,
        description = R.string.top_padding_status_bar_desc,
        default = 2,
        allowedRange = 0..300
    )

    @SettingKey
    val bottomPadding = int(
        title = R.string.bottom_padding,
        description = R.string.bottom_padding_status_bar_desc,
        default = 2,
        allowedRange = 0..300
    )
}