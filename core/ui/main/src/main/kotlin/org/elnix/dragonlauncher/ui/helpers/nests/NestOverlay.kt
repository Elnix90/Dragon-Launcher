package org.elnix.dragonlauncher.ui.helpers.nests

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import org.elnix.dragonlauncher.base.model.serializables.Nest
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.components.IntersectionShape
import org.elnix.dragonlauncher.ui.helpers.nests.cache.PointStableCache

/**
 * Composable wrapper that renders a nest and its points inside a [Canvas].
 *
 * Use this from composable context when you need to draw a complete nest
 * with backgrounds, borders, action icons, and badges. Prefer the
 * [DrawScope.NestOverlay] version when you are already inside a Canvas.
 */
@Composable
fun NestOverlay(
    nest: Nest,
    center: Offset,
    modifier: Modifier = Modifier,
    preventBgErasing: Boolean = false,
    showConfiguratorDecorations: Boolean = false,
    forceShowAllActionsInCurrentNest: Boolean = false,
    allowShowPointCenter: Boolean = false,
    hideSelectedPoint: Boolean = false,
) {
    val drawParams = rememberDrawParams(
        preventBgErasing = preventBgErasing,
        showConfiguratorDecorations = showConfiguratorDecorations,
        forceShowAllActionsInCurrentNest = forceShowAllActionsInCurrentNest,
        allowShowPointCenter = allowShowPointCenter,
        hideSelectedPoint = hideSelectedPoint
    )
    val iconTrigger by PointStableCache.cacheTrigger.asState()

    key(iconTrigger) {
        Canvas(modifier) {
            this.NestOverlay(
                nest = nest,
                depth = 1,
                center = center,
                drawParams = drawParams,
                selectedAll = false,
            )
        }
    }
}

@Suppress("FunctionName")
fun DrawScope.NestOverlay(
    nest: Nest,
    depth: Int,
    center: Offset,

    drawParams: DrawParams,

    selectedAll: Boolean = false,
) {
    repeat(2) { pass ->
        nest.intersectionShapes.forEach { shape ->
            this.IntersectionShape(
                shape = shape,
                center = center,
                drawParams = drawParams,
                erase = pass == 0
            )
        }
    }

    val hideSelected = drawParams.hideSelectedPoint
    val filteredPoints = drawParams.pointsService.getPointsForNest(nest.id, hideSelected)
    val selectedPoint = drawParams.pointsService.selectedPoint.value

    filteredPoints.forEach { p ->
        val drawPoint: Point = selectedPoint?.takeIf { it.id == p.id } ?: p
        val pointOffset = center + drawParams.pointsService.computePointOffset(p)

        if (drawParams.nestDebugOverlay) {
            drawLine(
                color = Color.White,
                start = center,
                end = pointOffset
            )
        }

        PointIcon(
            depth = depth,
            point = drawPoint,
            center = pointOffset,
            selected = selectedAll || (p.id == selectedPoint?.id),
            drawParams = drawParams,
            customText = null
        )
    }

    if (drawParams.allowShowPointInCenter) {
        selectedPoint?.let { selectedPoint ->
            PointIcon(
                point = selectedPoint,
                depth = depth,
                center = center,
                selected = true,
                drawParams = drawParams.copy(allowShowPointInCenter = false),
                customText = null
            )
        }
    }
}
