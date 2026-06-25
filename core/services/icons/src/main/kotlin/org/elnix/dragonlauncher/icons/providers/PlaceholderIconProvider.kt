package org.elnix.dragonlauncher.icons.providers

import android.content.Context
import org.elnix.dragonlauncher.applications.AppRepository
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.model.serializables.Action

class PlaceholderIconProvider(
    private val ctx: Context,
    private val appRepository: AppRepository
) : IconProvider {
    override suspend fun getIcon(action: Action, size: Int): LauncherIcon? {
        val application = appRepository.fromAction(action as Action.LaunchApp) ?: return null
        return application.getPlaceholderIcon(ctx)
    }
}