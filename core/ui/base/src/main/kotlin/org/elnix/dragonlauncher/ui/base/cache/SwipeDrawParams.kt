package org.elnix.dragonlauncher.ui.base.cache

import android.content.Context
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import org.elnix.dragonlauncher.base.theme.ExtraColors
import org.elnix.dragonlauncher.common.serializables.CircleNest
import org.elnix.dragonlauncher.common.serializables.IconShape
import org.elnix.dragonlauncher.common.serializables.SwipePointSerializable
import org.elnix.dragonlauncher.common.messyfolder.IconsCache

@Stable
data class SwipeDrawParams(
    val nests: List<CircleNest>,
    val points: List<SwipePointSerializable>,
    val ctx: Context,
    val defaultPoint: SwipePointSerializable,
    val icons: IconsCache<String>,
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
    val computeIcon: (SwipePointSerializable) -> Unit
)
