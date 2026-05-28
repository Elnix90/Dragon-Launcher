package org.elnix.dragonlauncher.icons.providers

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.common.search.Application
import org.elnix.dragonlauncher.icons.compat.AdaptiveIconDrawableCompat
import org.elnix.dragonlauncher.icons.compat.ClockIconConfig
import org.elnix.dragonlauncher.icons.compat.toLauncherIcon

class DynamicClockIconProvider(val ctx: Context, private val themed: Boolean) : IconProvider {
    override suspend fun getIcon(application: Application, size: Int): LauncherIcon? = withContext(Dispatchers.IO) {
        val componentName = application.componentName
        val pm = ctx.packageManager
        val appInfo = try {
            pm.getApplicationInfo(
                componentName.packageName,
                PackageManager.GET_META_DATA
            )
        } catch (e: PackageManager.NameNotFoundException) {
            return@withContext null
        }

        if (appInfo.metaData == null) return@withContext null

        val drawableId =
            appInfo.metaData.getInt("com.android.launcher3.LEVEL_PER_TICK_ICON_ROUND")

        if (drawableId == 0) return@withContext null
        val resources = try {
            pm.getResourcesForApplication(appInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            return@withContext null
        }

        val icon = AdaptiveIconDrawableCompat.from(resources, drawableId) ?: return@withContext null

        val defaultHour =
            appInfo.metaData.getInt("com.android.launcher3.DEFAULT_HOUR")
        val defaultMinute =
            appInfo.metaData.getInt("com.android.launcher3.DEFAULT_MINUTE")
        val defaultSecond =
            appInfo.metaData.getInt("com.android.launcher3.DEFAULT_SECOND")

        // Workaround for Google Clock themed icon because it is weird and I don't understand
        // how to get the correct layers from the drawable without hardcoding them here.
        val clockConfig = if (themed && componentName.packageName == "com.google.android.deskclock") {
            ClockIconConfig(
                hourLayer = 0,
                minuteLayer = 2,
                secondLayer = -1,
                defaultHour = defaultHour,
                defaultMinute = defaultMinute,
                defaultSecond = defaultSecond
            )
        } else {
            val hourLayer =
                appInfo.metaData.getInt("com.android.launcher3.HOUR_LAYER_INDEX", -1)
            val minuteLayer =
                appInfo.metaData.getInt("com.android.launcher3.MINUTE_LAYER_INDEX", -1)
            val secondLayer =
                appInfo.metaData.getInt("com.android.launcher3.SECOND_LAYER_INDEX", -1)
            ClockIconConfig(
                hourLayer = hourLayer,
                minuteLayer = minuteLayer,
                secondLayer = secondLayer,
                defaultHour = defaultHour,
                defaultMinute = defaultMinute,
                defaultSecond = defaultSecond
            )
        }

        return@withContext icon.toLauncherIcon(
            themed = themed,
            clock = clockConfig
        )
    }
}