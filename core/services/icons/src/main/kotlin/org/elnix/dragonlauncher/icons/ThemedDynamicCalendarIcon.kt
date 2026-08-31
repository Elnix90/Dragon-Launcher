package org.elnix.dragonlauncher.icons

import android.content.res.Resources
import android.graphics.drawable.AdaptiveIconDrawable
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.base.icons.DynamicLauncherIcon
import org.elnix.dragonlauncher.base.icons.StaticIconLayer
import org.elnix.dragonlauncher.base.icons.StaticLauncherIcon
import org.elnix.dragonlauncher.base.icons.TextLayer
import org.elnix.dragonlauncher.base.icons.TransparentLayer
import org.elnix.dragonlauncher.icons.compat.AdaptiveIconDrawableCompat
import org.elnix.dragonlauncher.icons.compat.toLauncherIcon
import org.elnix.dragonlauncher.icons.transformations.LauncherIconTransformation
import java.time.Instant
import java.time.ZoneId

internal class ThemedDynamicCalendarIcon(
    val resources: Resources,
    val resourceIds: IntArray,
    val tint: Int?,
    private var transformations: List<LauncherIconTransformation> = emptyList()
) : DynamicLauncherIcon,
    TransformableDynamicLauncherIcon {
    override suspend fun getIcon(time: Long): StaticLauncherIcon =
        withContext(Dispatchers.IO) {
            val day = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).dayOfMonth
            val resId = resourceIds[day - 1]

            val adaptiveIconCompat = AdaptiveIconDrawableCompat.from(resources, resId)

            if (adaptiveIconCompat != null) {
                var icon = adaptiveIconCompat.toLauncherIcon(themed = true, tint = tint)
                for (transformation in transformations) {
                    icon = transformation.transform(icon)
                }
                return@withContext icon
            }

            val drawable =
                try {
                    ResourcesCompat.getDrawable(resources, resId, null)
                } catch (e: Resources.NotFoundException) {
                    null
                } ?: return@withContext StaticLauncherIcon(
                    foregroundLayer =
                        TextLayer(
                            text = day.toString(),
                            tint = tint
                        ),
                    backgroundLayer = TransparentLayer
                )

            var icon =
                when (drawable) {
                    is AdaptiveIconDrawable ->
                        StaticLauncherIcon(
                            foregroundLayer =
                                StaticIconLayer(
                                    icon = drawable.foreground,
                                    scale = 1.5f,
                                    tint = tint
                                ),
                            backgroundLayer = TransparentLayer
                        )

                    else ->
                        StaticLauncherIcon(
                            foregroundLayer =
                                StaticIconLayer(
                                    icon = drawable,
                                    scale = 0.65f,
                                    tint = tint
                                ),
                            backgroundLayer = TransparentLayer
                        )
                }

            for (transformation in transformations) {
                icon = transformation.transform(icon)
            }
            return@withContext icon
        }

    override fun setTransformations(transformations: List<LauncherIconTransformation>) {
        this.transformations = transformations
    }
}
