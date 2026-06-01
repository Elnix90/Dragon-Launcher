package org.elnix.dragonlauncher.icons.providers

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.icons.compat.AdaptiveIconDrawableCompat
import org.elnix.dragonlauncher.icons.compat.toLauncherIcon

class CompatIconProvider(
    private val ctx: Context,
    private val themed: Boolean = false,
) : IconProvider {
    override suspend fun getIcon(application: Application, size: Int): LauncherIcon? {
        val component = application.componentName

        val icon = withContext(Dispatchers.IO) {
            val activityInfo = try {
                ctx.packageManager.getActivityInfo(component, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                return@withContext null
            }
            val iconRes = activityInfo.iconResource
            val resources = try {
                ctx.packageManager.getResourcesForApplication(activityInfo.packageName)
            } catch (e: PackageManager.NameNotFoundException) {
                return@withContext null
            }
            AdaptiveIconDrawableCompat.from(resources, iconRes)
        } ?: return null

        return icon.toLauncherIcon(themed = themed)
    }
}