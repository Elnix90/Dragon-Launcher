package org.elnix.dragonlauncher.ui.helpers.nests.cache

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.TextLayoutResult
import org.elnix.dragonlauncher.base.DragonCache
import org.elnix.dragonlauncher.ui.remembers.CustomTexts

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
 * @property iconDrawSize size of the drawn icon in pixels
 */
data class StablePointValues(
    val sizePx: Float,
    val innerPaddingPx: Float,
    val borderRadii: Float,
    val iconSize: Size,
    val customTexts: CustomTexts,
    val imageBitmap: ImageBitmap?
)

/**
 * LRU cache of [StablePointValues] keyed by point identifier.
 *
 * The cache is populated by [RememberPointStableCaches] and should be
 * read inside DrawScope functions such as [org.elnix.dragonlauncher.ui.helpers.nests.PointBg]. Lookups that
 * return null fall back to inline computation (first-frame window).
 *
 * @see RememberPointStableCaches
 * @see org.elnix.dragonlauncher.ui.helpers.nests.PointBg
 */
object PointStableCache : DragonCache<Int, StablePointValues>(200)

data class DrawScopeText(
    val offsetTextLayoutResult: TextLayoutResult,
    val topLeft: Offset
)