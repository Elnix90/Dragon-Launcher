package org.elnix.dragonlauncher.icons.transformations

import org.elnix.dragonlauncher.base.icons.ColorLayer
import org.elnix.dragonlauncher.base.icons.LauncherIconLayer
import org.elnix.dragonlauncher.base.icons.StaticLauncherIcon
import org.elnix.dragonlauncher.base.icons.TextLayer
import org.elnix.dragonlauncher.base.icons.StaticIconLayer

internal class ForceThemedIconTransformation : LauncherIconTransformation {
    override suspend fun transform(icon: StaticLauncherIcon): StaticLauncherIcon {
        return StaticLauncherIcon(
            foregroundLayer = asThemed(icon.foregroundLayer),
            backgroundLayer = ColorLayer(0),
        )
    }

    private fun asThemed(layer: LauncherIconLayer): LauncherIconLayer {
        return when(layer) {
            is ColorLayer -> layer.copy(tint = 0)
            is StaticIconLayer -> layer.copy(
                tint = 0,
                icon = layer.icon,
                scale = layer.scale / 1.2f,
            )
            is TextLayer -> layer.copy(
                tint = 0
            )
            else -> layer
        }
    }

}