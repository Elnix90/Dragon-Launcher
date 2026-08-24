package org.elnix.dragonlauncher.icons.providers

import android.content.Context
import org.elnix.dragonlauncher.applications.AppRepository
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.model.serializables.Action

internal class PlaceholderIconProvider(
    private val ctx: Context,
    private val appRepository: AppRepository
) : IconProvider {
    override suspend fun getIcon(action: Action, size: Int): LauncherIcon? {
        if (action !is Action.LaunchApp) return null

        val application = appRepository.fromAction(action) ?: return null
        return application.getPlaceholderIcon(ctx)
    }
}