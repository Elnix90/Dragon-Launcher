package org.elnix.dragonlauncher.icons.providers

import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.CustomIconPackIcon
import org.elnix.dragonlauncher.database.entities.IconEntity
import org.elnix.dragonlauncher.icons.IconPackManager
import org.elnix.dragonlauncher.icons.iconPackAppIcon

internal class CustomIconPackIconProvider(
    private val customIcon: CustomIconPackIcon,
    private val iconPackManager: IconPackManager
) : IconProvider {
    override suspend fun getIcon(action: Action, size: Int): LauncherIcon? {
        val ent =
            IconEntity(
                type = customIcon.packType,
                drawable = customIcon.drawable,
                extras = customIcon.extras,
                iconPack = customIcon.iconPackPackage,
                themed = customIcon.allowThemed
            )
        val icon = iconPackAppIcon(ent) ?: return null
        return iconPackManager.getIcon(
            iconPack = customIcon.iconPackPackage,
            icon = icon,
            tint = customIcon.tint,
            allowThemed = customIcon.allowThemed
        )
    }
}
