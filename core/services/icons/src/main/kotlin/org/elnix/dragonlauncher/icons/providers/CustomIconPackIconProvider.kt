package org.elnix.dragonlauncher.icons.providers

import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.common.search.Application
import org.elnix.dragonlauncher.database.entities.IconEntity
import org.elnix.dragonlauncher.common.serializables.CustomIconPackIcon
import org.elnix.dragonlauncher.icons.IconPackAppIcon
import org.elnix.dragonlauncher.icons.IconPackManager

class CustomIconPackIconProvider(
    private val customIcon: CustomIconPackIcon,
    private val iconPackManager: IconPackManager,
) : IconProvider {
    override suspend fun getIcon(application: Application, size: Int): LauncherIcon? {
        val ent = IconEntity(
            type = customIcon.type,
            drawable = customIcon.drawable,
            extras = customIcon.extras,
            iconPack = customIcon.iconPackPackage,
            themed = customIcon.allowThemed,
        )
        val icon = IconPackAppIcon(ent) ?: return null
        return iconPackManager.getIcon(
            customIcon.iconPackPackage,
            icon,
            customIcon.allowThemed,
        )
    }
}