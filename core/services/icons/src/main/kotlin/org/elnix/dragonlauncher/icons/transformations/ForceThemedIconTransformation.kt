package org.elnix.dragonlauncher.icons.transformations

import org.elnix.dragonlauncher.base.icons.ClockLayer
import org.elnix.dragonlauncher.base.icons.ColorLayer
import org.elnix.dragonlauncher.base.icons.LauncherIconLayer
import org.elnix.dragonlauncher.base.icons.StaticIconLayer
import org.elnix.dragonlauncher.base.icons.StaticLauncherIcon
import org.elnix.dragonlauncher.base.icons.TextLayer
import org.elnix.dragonlauncher.base.icons.TintedClockLayer
import org.elnix.dragonlauncher.base.icons.TintedIconLayer

internal class ForceThemedIconTransformation : LauncherIconTransformation {
    override suspend fun transform(icon: StaticLauncherIcon): StaticLauncherIcon {
        return StaticLauncherIcon(
            foregroundLayer = asThemed(icon.foregroundLayer),
            backgroundLayer = ColorLayer(0),
        )
    }

    private fun asThemed(layer: LauncherIconLayer): LauncherIconLayer {
        return when(layer) {
            is ClockLayer -> TintedClockLayer(
                scale = layer.scale,
                defaultHour = layer.defaultHour,
                defaultMinute = layer.defaultMinute,
                defaultSecond = layer.defaultSecond,
                sublayers = layer.sublayers,
            )
            is ColorLayer -> layer.copy(color = 0)
            is StaticIconLayer -> TintedIconLayer(
                color = 0,
                icon = layer.icon,
                scale = layer.scale / 1.2f,
            )
            is TextLayer -> layer.copy(
                color = 0
            )
            else -> layer
        }
    }

}