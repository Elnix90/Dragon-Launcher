package org.elnix.dragonlauncher.icons.providers

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import androidx.core.content.getSystemService
import io.github.elnix90.logging.logD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.ICONS_TAG
import org.elnix.dragonlauncher.applications.AppRepository
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.icons.IconPack
import org.elnix.dragonlauncher.icons.IconPackManager

internal class IconPackIconProvider(
    private val appRepository: AppRepository,
    private val ctx: Context,
    private val iconPack: IconPack,
    private val tint: Int?,
    private val iconPackManager: IconPackManager,
    private val allowThemed: Boolean,
) : IconProvider {
    override suspend fun getIcon(action: Action, size: Int): LauncherIcon? {
        if (action !is Action.LaunchApp) return null

        val application = appRepository.fromAction(action) ?: return null
        val componentName = application.componentName

        return iconPackManager.getIcon(
            iconPack = iconPack.packageName,
            packageName = application.packageName,
            activityName = componentName.className,
            tint = tint,
            allowThemed = allowThemed
        )
            ?: run {
                logD(ICONS_TAG) { "Generating new icon for ${application.packageName} with tint=t$tint" }
                iconPackManager.generateIcon(
                    ctx = ctx,
                    iconPack = iconPack.packageName,
                    tint = tint,
                    baseIcon = withContext(Dispatchers.IO) {
                        val ai = ctx.getSystemService<LauncherApps>()?.resolveActivity(
                            Intent().setComponent(componentName),
                            application.user
                        )
                        ai?.getIcon(ctx.resources.displayMetrics.densityDpi)
                    } ?: return null,
                    size = size,
                )
            }
    }

}