package org.elnix.dragonlauncher.icons.providers

import android.content.Context
import org.elnix.dragonlauncher.applications.AppRepository
import org.elnix.dragonlauncher.base.icons.ClockLayer
import org.elnix.dragonlauncher.base.icons.ColorLayer
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.icons.LauncherIconLayer
import org.elnix.dragonlauncher.base.icons.StaticIconLayer
import org.elnix.dragonlauncher.base.icons.StaticLauncherIcon
import org.elnix.dragonlauncher.base.icons.TextLayer
import org.elnix.dragonlauncher.base.icons.TransparentLayer
import org.elnix.dragonlauncher.base.icons.VectorLayer
import org.elnix.dragonlauncher.base.model.serializables.Action

internal class ThemedPlaceholderIconProvider(
    private val appRepository: AppRepository,
    private val ctx: Context,
) : IconProvider {

    override suspend fun getIcon(action: Action, size: Int): LauncherIcon? {
        if (action !is Action.LaunchApp) return null

        val application = appRepository.fromAction(action) ?: return null
        val icon = application.getPlaceholderIcon(ctx)

        return StaticLauncherIcon(
            foregroundLayer = asThemed(icon.foregroundLayer),
            backgroundLayer = asThemed(icon.backgroundLayer),
        )
    }

    private fun asThemed(layer: LauncherIconLayer): LauncherIconLayer {
        return when (layer) {
            is ColorLayer -> layer.copy(tint = 0)
            is VectorLayer -> layer.copy(tint = 0)
            is TextLayer -> layer.copy(tint = 0)
            is StaticIconLayer -> layer.copy(tint = 0)
            is ClockLayer -> layer.copy(tint = 0)
            is TransparentLayer -> layer
        }
    }
}