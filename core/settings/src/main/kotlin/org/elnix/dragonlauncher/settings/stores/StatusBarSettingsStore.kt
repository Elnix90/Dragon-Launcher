package org.elnix.dragonlauncher.settings.stores

import androidx.compose.ui.graphics.Color
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.color
import org.elnix.dragonlauncher.settings.bases.int
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

object StatusBarSettingsStore : MapSettingsStore(DataStoreName.STATUS_BAR) {

    val barBackgroundColor = color(
        key = "barBackgroundColor",
        default = Color.Transparent
    )

    val barTextColor = color(
        key = "barTextColor",
        default = Color.White
    )

    val leftPadding = int(
        key = "leftPadding",
        default = 5,
        allowedRange = 0..300
    )

    val rightPadding = int(
        key = "rightPadding",
        default = 5,
        allowedRange = 0..300
    )

    val topPadding = int(
        key = "topPadding",
        default = 2,
        allowedRange = 0..300
    )

    val bottomPadding = int(
        key = "bottomPadding",
        default = 2,
        allowedRange = 0..300
    )


    override val ALL: List<BaseSettingObject<*, *>> = listOf(
        this.barBackgroundColor,
        this.barTextColor,
        this.leftPadding,
        this.rightPadding,
        this.topPadding,
        this.bottomPadding
    )
}
