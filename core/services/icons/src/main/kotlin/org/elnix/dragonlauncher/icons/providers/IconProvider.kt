package org.elnix.dragonlauncher.icons.providers

import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.model.models.Application


interface IconProvider {
    suspend fun getIcon(application: Application, size: Int): LauncherIcon?
}

internal suspend fun Iterable<IconProvider>.getFirstIcon(
    application: Application,
    size: Int
): LauncherIcon? {
    for (provider in this) {
        val icon = provider.getIcon(application, size)
        if (icon != null) {
            return icon
        }
    }
    return null
}