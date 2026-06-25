package org.elnix.dragonlauncher.icons

import android.content.res.Resources
import android.graphics.drawable.AdaptiveIconDrawable
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.base.icons.DynamicLauncherIcon
import org.elnix.dragonlauncher.base.icons.StaticLauncherIcon
import org.elnix.dragonlauncher.base.icons.TextLayer
import org.elnix.dragonlauncher.base.icons.StaticIconLayer
import org.elnix.dragonlauncher.base.icons.TransparentLayer
import org.elnix.dragonlauncher.icons.compat.AdaptiveIconDrawableCompat
import org.elnix.dragonlauncher.icons.compat.toLauncherIcon
import org.elnix.dragonlauncher.icons.transformations.LauncherIconTransformation
import java.time.Instant
import java.time.ZoneId

internal class DynamicCalendarIcon(
    val resources: Resources,
    val resourceIds: IntArray,
    val tint: Int?,
    val isThemed: Boolean = false,
    private var transformations: List<LauncherIconTransformation> = emptyList(),
) : DynamicLauncherIcon, TransformableDynamicLauncherIcon {

    init {
        if (resourceIds.size < 31) throw IllegalArgumentException("DynamicCalendarIcon resourceIds must at least have 31 items")
    }

    override suspend fun getIcon(time: Long): StaticLauncherIcon = withContext(Dispatchers.IO) {
        val day = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).dayOfMonth
        val resId = resourceIds[day - 1]

        val adaptiveIcon = AdaptiveIconDrawableCompat.from(resources, resId)

        var icon = adaptiveIcon?.toLauncherIcon(themed = isThemed, tint = tint)
            ?: (try {
                val drawable = ResourcesCompat.getDrawable(resources, resId, null)

                when {
                    drawable is AdaptiveIconDrawable -> AdaptiveIconDrawableCompat.from(drawable).toLauncherIcon(themed = isThemed, tint = tint)
                    drawable != null -> StaticLauncherIcon(
                        foregroundLayer = StaticIconLayer(
                            icon = drawable,
                            scale = 1f,
                            tint = tint
                        ),
                        backgroundLayer = TransparentLayer,
                    )

                    else -> null
                }
            } catch (e: Resources.NotFoundException) {
                null
            } ?: return@withContext StaticLauncherIcon(
                foregroundLayer = TextLayer(
                    text = day.toString(),
                    tint = tint
                ),
                backgroundLayer = TransparentLayer
            ))

        for (transformation in transformations) {
            icon = transformation.transform(icon)
        }
        return@withContext icon
    }

    override fun setTransformations(transformations: List<LauncherIconTransformation>) {
        this.transformations = transformations
    }
}