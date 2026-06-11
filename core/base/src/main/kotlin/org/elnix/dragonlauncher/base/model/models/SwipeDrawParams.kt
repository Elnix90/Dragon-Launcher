package org.elnix.dragonlauncher.base.model.models

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.base.model.serializables.Nest
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.theme.ExtraColors

@Stable
@Immutable
data class SwipeDrawParams(
    val nests: Set<Nest>,
    val points: Set<Point>,
    val ctx: Context,
    val defaultPoint: Point,
    val surfaceColorDraw: Color,
    val extraColors: ExtraColors,
    val maxDepth: Int,
    val iconShape: IconShape,
    val subNestDefaultRadius: Float,
    val showAllActionsOnCurrentCircle: Boolean,
    val showAppCirclePreview: Boolean,
    val showAppLaunchPreview: Boolean,
    val showAllActionsOnCurrentNest: Boolean,
    val showAppPreviewIconCenterStartPosition: Boolean,
    val computeIcon: (Point) -> Unit
)