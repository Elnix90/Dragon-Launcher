package org.elnix.dragonlauncher.icons.providers

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import androidx.core.content.getSystemService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.common.search.Application
import org.elnix.dragonlauncher.icons.IconPack
import org.elnix.dragonlauncher.icons.IconPackManager

class IconPackIconProvider(
    private val ctx: Context,
    private val iconPack: IconPack,
    private val tint: Int?,
    private val iconPackManager: IconPackManager,
    private val allowThemed: Boolean,
) : IconProvider {
    override suspend fun getIcon(application: Application, size: Int): LauncherIcon? =
        iconPackManager.getIcon(iconPack.packageName, application.componentName.packageName, application.componentName.className, allowThemed)
            ?: iconPackManager.generateIcon(
                ctx = ctx,
                iconPack = iconPack.packageName,
                tint = tint,
                baseIcon = withContext(Dispatchers.IO) {
                    val ai = ctx.getSystemService<LauncherApps>()?.resolveActivity(
                        Intent().setComponent(application.componentName),
                        application.user
                    )
                    ai?.getIcon(ctx.resources.displayMetrics.densityDpi)
                } ?: return null,
                size = size,
            )

}