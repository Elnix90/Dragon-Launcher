package org.elnix.dragonlauncher.base.cache

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.TextLayoutResult
import org.elnix.dragonlauncher.base.DragonCache

/**
 * Density-dependent drawing values cached per point.
 *
 * These values depend only on the point's configuration and the current
 * density, never on transient visual state such as selection. They are
 * computed once inside a [androidx.compose.runtime.remember] block and
 * written to [PointStableCache] via [androidx.compose.runtime.LaunchedEffect].
 *
 * @property sizePx point outer diameter in pixels
 * @property innerPaddingPx inner padding in pixels
 * @property borderRadii effective radius of the border circle
 * @property iconSize size of the icon bounding box derived from [borderRadii]
 */
public data class StablePointValues(
    val sizePx: Float,
    val innerPaddingPx: Float,
    val borderRadii: Float,
    val iconSize: Size,
    val customTexts: Pair<DrawScopeText?, DrawScopeText?>?,
    val imageBitmap: ImageBitmap?
)


public object PointStableCache : DragonCache<Int, StablePointValues>(200)

public data class DrawScopeText(
    val offsetTextLayoutResult: TextLayoutResult,
    val topLeft: Offset
)
