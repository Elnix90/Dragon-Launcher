package org.elnix.dragonlauncher.icons.providers

import android.content.Context
import org.elnix.dragonlauncher.base.icons.ClockLayer
import org.elnix.dragonlauncher.base.icons.ColorLayer
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.icons.LauncherIconLayer
import org.elnix.dragonlauncher.base.icons.StaticIconLayer
import org.elnix.dragonlauncher.base.icons.StaticLauncherIcon
import org.elnix.dragonlauncher.base.icons.TextLayer
import org.elnix.dragonlauncher.base.icons.TintedClockLayer
import org.elnix.dragonlauncher.base.icons.TintedIconLayer
import org.elnix.dragonlauncher.base.icons.TransparentLayer
import org.elnix.dragonlauncher.base.icons.VectorLayer
import org.elnix.dragonlauncher.common.search.Application

internal class ThemedPlaceholderIconProvider(
    private val ctx: Context,
) : IconProvider {

    override suspend fun getIcon(application: Application, size: Int): LauncherIcon {
        val icon = application.getPlaceholderIcon(ctx)

        return StaticLauncherIcon(
            foregroundLayer = asThemed(icon.foregroundLayer),
            backgroundLayer = asThemed(icon.backgroundLayer),
        )
    }

    private fun asThemed(layer: LauncherIconLayer): LauncherIconLayer {
        return when (layer) {
            is ClockLayer -> TintedClockLayer(
                scale = layer.scale,
                color = 0,
                defaultHour = layer.defaultHour,
                defaultMinute = layer.defaultMinute,
                defaultSecond = layer.defaultSecond,
                sublayers = layer.sublayers,
            )
            is ColorLayer -> layer.copy(color = 0)
            is StaticIconLayer -> TintedIconLayer(
                icon = layer.icon,
                color = 0,
                scale = layer.scale,
            )
            is VectorLayer -> layer.copy(color = 0)
            is TextLayer -> layer.copy(color = 0)
            is TintedIconLayer -> layer.copy(color = 0)
            is TintedClockLayer -> layer.copy(color = 0)
            is TransparentLayer -> layer
        }
    }

}