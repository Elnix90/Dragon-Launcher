package org.elnix.dragonlauncher.settings.stores.map

import androidx.compose.ui.graphics.Color
import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.ColorSettingObject
import io.github.elnix90.core.objects.IntSettingObject
import io.github.elnix90.core.objects.color
import io.github.elnix90.core.objects.int
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.i18n.R

@SettingsStore
public object StatusBarSettingsStore : MapSettingsStore() {
    @SettingKey
    public val barBackgroundColor: ColorSettingObject =
        color(
            title = R.string.status_bar_background,
            default = Color.Transparent
        )

    @SettingKey
    public val barTextColor: ColorSettingObject =
        color(
            title = R.string.status_bar_text_color,
            default = Color.White
        )

    @SettingKey
    public val leftPadding: IntSettingObject =
        int(
            title = R.string.left_padding,
            description = R.string.left_padding_status_bar_desc,
            icon = R.drawable.format_align_left,
            default = 5,
            allowedRange = 0..300
        )

    @SettingKey
    public val rightPadding: IntSettingObject =
        int(
            title = R.string.right_padding,
            description = R.string.right_padding_status_bar_desc,
            icon = R.drawable.format_align_right,
            default = 5,
            allowedRange = 0..300
        )

    @SettingKey
    public val topPadding: IntSettingObject =
        int(
            title = R.string.top_padding,
            description = R.string.top_padding_status_bar_desc,
            icon = R.drawable.arrow_drop_up,
            default = 2,
            allowedRange = 0..300
        )

    @SettingKey
    public val bottomPadding: IntSettingObject =
        int(
            title = R.string.bottom_padding,
            description = R.string.bottom_padding_status_bar_desc,
            icon = R.drawable.arrow_drop_down,
            default = 2,
            allowedRange = 0..300
        )
}
