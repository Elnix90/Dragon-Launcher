package org.elnix.dragonlauncher.icons.providers

import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.model.serializables.Action

internal interface IconProvider {
    suspend fun getIcon(action: Action, size: Int): LauncherIcon?
}

internal suspend fun Iterable<IconProvider>.getFirstIcon(
    action: Action,
    size: Int
): LauncherIcon? {
    for (provider in this) {
        val icon = provider.getIcon(action, size)
        if (icon != null) {
            return icon
        }
    }
    return null
}
