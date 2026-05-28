package org.elnix.dragonlauncher.ui.base.cache

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import org.elnix.dragonlauncher.base.theme.ExtraColors
import org.elnix.dragonlauncher.common.serializables.Nest
import org.elnix.dragonlauncher.common.serializables.IconShape
import org.elnix.dragonlauncher.common.serializables.Point
import org.elnix.dragonlauncher.icons.PointIconCache

@Stable
@Immutable
data class SwipeDrawParams(
    val nests: List<Nest>,
    val points: List<Point>,
    val ctx: Context,
    val defaultPoint: Point,
    val pointsIconsCache: PointIconCache,
    val surfaceColorDraw: Color,
    val extraColors: ExtraColors,
    val maxDepth: Int,
    val iconShape: IconShape,
    val subNestDefaultRadius: Float,
    val drawPathCache: DrawPathCache,
    val showAllActionsOnCurrentCircle: Boolean,
    val showAppCirclePreview: Boolean,
    val showAppLaunchPreview: Boolean,
    val showAllActionsOnCurrentNest: Boolean,
    val showAppPreviewIconCenterStartPosition: Boolean,
    val computeIcon: (Point) -> Unit
)
