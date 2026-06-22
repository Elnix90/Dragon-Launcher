package org.elnix.dragonlauncher.settings.stores.map

import androidx.compose.ui.graphics.Color
import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.color
import io.github.elnix90.core.objects.int
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.i18n.R

@SettingsStore
object StatusBarSettingsStore : MapSettingsStore() {

    @SettingKey
    val barBackgroundColor = color(
        title = R.string.status_bar_background,
        default = Color.Transparent
    )

    @SettingKey
    val barTextColor = color(
        title = R.string.status_bar_text_color,
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