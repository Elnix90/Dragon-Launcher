package org.elnix.dragonlauncher.icons.providers

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.applications.AppRepository
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.icons.compat.AdaptiveIconDrawableCompat
import org.elnix.dragonlauncher.icons.compat.toLauncherIcon

internal class CompatIconProvider(
    private val appRepository: AppRepository,
    private val ctx: Context,
    private val themed: Boolean = false,
    private val tint: Int?
) : IconProvider {
    override suspend fun getIcon(action: Action, size: Int): LauncherIcon? {
        if (action !is Action.LaunchApp) return null

        val componentName = appRepository.fromAction(action)?.componentName ?: return null

        val icon =
            withContext(Dispatchers.IO) {
                val activityInfo =
                    try {
                        ctx.packageManager.getActivityInfo(componentName, 0)
                    } catch (_: PackageManager.NameNotFoundException) {
                        return@withContext null
                    }
                val iconRes = activityInfo.iconResource
                val resources =
                    try {
                        ctx.packageManager.getResourcesForApplication(activityInfo.packageName)
                    } catch (e: PackageManager.NameNotFoundException) {
                        return@withContext null
                    }
                AdaptiveIconDrawableCompat.from(resources, iconRes)
            } ?: return null

        return icon.toLauncherIcon(themed = themed, tint = tint)
    }
}
