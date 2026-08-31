package org.elnix.dragonlauncher.icons.providers

import org.elnix.dragonlauncher.applications.AppRepository
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.model.serializables.Action

internal class SystemIconProvider(
    private val appRepository: AppRepository,
    private val themedIcons: Boolean,
    private val tint: Int?
) : IconProvider {
    override suspend fun getIcon(action: Action, size: Int): LauncherIcon? {
        return when (action) {
            is Action.LaunchApp -> {
                val application = appRepository.fromAction(action) ?: return null
                application.loadIcon(themedIcons, tint)
            }
            else -> null
        }
    }
}
