package org.elnix.dragonlauncher.icons.providers

import android.content.Context
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.common.search.Application

class PlaceholderIconProvider(val ctx: Context) : IconProvider {
    override suspend fun getIcon(application: Application, size: Int): LauncherIcon {
        return application.getPlaceholderIcon(ctx)
    }
}