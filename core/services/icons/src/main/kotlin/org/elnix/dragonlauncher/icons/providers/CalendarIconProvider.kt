package org.elnix.dragonlauncher.icons.providers

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.applications.AppRepository
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.icons.DynamicCalendarIcon
import org.elnix.dragonlauncher.ktx.obtainTypedArrayOrNull

class CalendarIconProvider(
    val ctx: Context,
    val appRepository: AppRepository,
    val themed: Boolean,
    val tint: Int?
): IconProvider {
    override suspend fun getIcon(action: Action, size: Int): LauncherIcon? = withContext(Dispatchers.IO) {
        val component = appRepository.fromAction(action as Action.LaunchApp)?.componentName ?: return@withContext null
        val pm = ctx.packageManager
        val ai = try {
            pm.getActivityInfo(component, PackageManager.GET_META_DATA)
        } catch (e: PackageManager.NameNotFoundException) {
            return@withContext null
        }
        var arrayId = ai.metaData?.getInt("com.teslacoilsw.launcher.calendarIconArray") ?: 0
        if (arrayId == 0) arrayId = ai.metaData?.getInt("com.google.android.calendar.dynamic_icons")
            ?: return@withContext null
        if (arrayId == 0) arrayId = ai.metaData?.getInt("org.lineageos.etar.dynamic_icons")
            ?: return@withContext null
        if (arrayId == 0) return@withContext null
        val resources = try {
            pm.getResourcesForActivity(component)
        } catch (e: PackageManager.NameNotFoundException) {
            return@withContext null
        }
        val typedArray = resources.obtainTypedArrayOrNull(arrayId) ?: return@withContext null
        if (typedArray.length() != 31) {
            typedArray.recycle()
            return@withContext null
        }
        val drawableIds = IntArray(31)
        for (i in 0 until 31) {
            drawableIds[i] = typedArray.getResourceId(i, 0)
        }
        typedArray.recycle()
        return@withContext DynamicCalendarIcon(
            resources = resources,
            resourceIds = drawableIds,
            isThemed = themed,
            tint = tint
        )
    }
}