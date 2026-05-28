package org.elnix.dragonlauncher.icons.providers

import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.common.search.Application

class SystemIconProvider(
    private val themedIcons: Boolean,
) : IconProvider {
    override suspend fun getIcon(application: Application, size: Int): LauncherIcon? {
        return application.loadIcon(themedIcons)
    }
}