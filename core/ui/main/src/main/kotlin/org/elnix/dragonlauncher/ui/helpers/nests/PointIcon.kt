package org.elnix.dragonlauncher.ui.helpers.nests

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.ui.actions.rememberPointIconBitmaps

/**
 * Composable wrapper that renders a single point icon inside a [Canvas].
 *
 * Use this from composable context to draw a point's background, border,
 * action icon, and badges. Prefer the [DrawScope.PointIcon] version when
 * you are already inside a Canvas.
 *
 * @param iconBitmaps Pre-rendered icon bitmaps. If null, they are loaded
 *   automatically via [rememberPointIconBitmaps].
 */
@Composable
fun PointIcon(
    point: Point,
    center: Offset,
    modifier: Modifier = Modifier,
    selected: Boolean = false,

    preventBgErasing: Boolean = false,
    showConfiguratorDecorations: Boolean = false,
    forceShowAllActionsInCurrentNest: Boolean = false,

    iconBitmaps: Map<Int, ImageBitmap>
) {

    val iconVersion = iconBitmaps.size

    val drawParams = rememberDrawParams(
        preventBgErasing = preventBgErasing,
        showConfiguratorDecorations = showConfiguratorDecorations,
        iconBitmaps = iconBitmaps,
        iconBitmapsVersion = iconVersion,
        forceShowAllActionsInCurrentNest = forceShowAllActionsInCurrentNest,
        allowShowPointCenter = false,
    )

    Canvas(modifier = modifier) {
        this.PointIcon(
            point = point,
            depth = 1,
            center = center,
            selected = selected,
            drawParams = drawParams
        )
    }
}

@Suppress("FunctionName")
fun DrawScope.PointIcon(
    point: Point,
    depth: Int,
    center: Offset,
    selected: Boolean,

    drawParams: DrawParams
) {
    val action = point.action

    if (
        action !is Action.OpenCircleNest ||
        point.customIcon != null ||
        depth >= drawParams.maxNestsDepth ||
        drawParams.preventDrawingSubNests
    ) {
        PointBg(
            point = point,
            selected = selected,
            center = center,
            drawParams = drawParams,
            iconBitmap = drawParams.iconBitmaps[point.id]
        )
    } else {
        drawParams.pointsService.nests.value
            .find { it.id == action.nestId }
            ?.let { nest ->
                val newDepth = depth + 1
                val newScale = 1f / newDepth

                scale(
                    scale = newScale,
                    pivot = center
                ) {
                    NestOverlay(
                        nest = nest,
                        depth = newDepth,
                        center = center,
                        drawParams = drawParams,
                        selectedAll = selected
                    )
                }
            } ?: this.NestPlaceholder(center, drawParams) // This shouldn't render
    }
}
