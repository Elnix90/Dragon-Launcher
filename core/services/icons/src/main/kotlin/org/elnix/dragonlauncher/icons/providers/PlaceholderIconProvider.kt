package org.elnix.dragonlauncher.icons.providers

import android.content.Context
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.model.models.Application

class PlaceholderIconProvider(val ctx: Context) : IconProvider {
    override suspend fun getIcon(application: Application, size: Int): LauncherIcon {
        return application.getPlaceholderIcon(ctx)
    }
}