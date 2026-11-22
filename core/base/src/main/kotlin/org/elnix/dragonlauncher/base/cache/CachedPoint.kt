package org.elnix.dragonlauncher.base.cache

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.TextLayoutResult
import org.elnix.dragonlauncher.base.DragonCache
import org.elnix.dragonlauncher.base.model.serializables.IconShape

/**
 * Density-dependent drawing values cached per point.
 *
 * These values depend only on the point's configuration, the current density,
 * the resolved [org.elnix.dragonlauncher.base.icons.LauncherIcon], and the
 * active theme colors. They are computed by [org.elnix.dragonlauncher.models.PointsViewModel]
 * and written to [PointStableCache].
 *
 * @property sizePx point outer diameter in pixels
 * @property innerPaddingPx inner padding in pixels
 * @property borderRadii effective radius of the border circle
 * @property iconSize size of the icon bounding box derived from [borderRadii]
 * @property imageBitmap the rendered icon bitmap, or null if the point has no icon
 * @property iconShape per-icon shape override, or null to use the global icon shape
 */
data class StablePointValues(
	val sizePx: Float,
	val innerPaddingPx: Float,
	val borderRadii: Float,
	val iconSize: Size,
	val imageBitmap: ImageBitmap?,
	val iconShape: IconShape?,
	val badgeBitmap: ImageBitmap?
)

object PointStableCache : DragonCache<Int, StablePointValues>(200)

data class DrawScopeText(
	val offsetTextLayoutResult: TextLayoutResult,
	val topLeft: Offset
)
