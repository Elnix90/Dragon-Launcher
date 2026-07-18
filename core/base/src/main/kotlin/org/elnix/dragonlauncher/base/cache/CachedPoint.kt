package org.elnix.dragonlauncher.base.cache

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.TextLayoutResult
import org.elnix.dragonlauncher.base.DragonCache

/**
 * Density-dependent drawing values cached per point.
 *
 * These values depend only on the point's configuration, the current density,
 * the resolved [org.elnix.dragonlauncher.base.icons.LauncherIcon], and the
 * active theme colours. They are computed by [org.elnix.dragonlauncher.models.PointsViewModel]
 * and written to [PointStableCache].
 *
 * [customTexts] is intentionally left as `null` by the ViewModel cache since text
 * measurement is Compose-dependent. The UI layer may supply debug overlay text as
 * a draw-time override when `LocalNestDebugOverlay` is enabled.
 *
 * @property sizePx point outer diameter in pixels
 * @property innerPaddingPx inner padding in pixels
 * @property borderRadii effective radius of the border circle
 * @property iconSize size of the icon bounding box derived from [borderRadii]
 * @property customTexts optional debug overlay text layouts, or null when not in debug mode
 * @property imageBitmap the rendered icon bitmap, or null if the point has no icon
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
