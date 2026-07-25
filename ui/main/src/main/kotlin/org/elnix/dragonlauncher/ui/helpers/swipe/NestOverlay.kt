package org.elnix.dragonlauncher.ui.helpers.swipe

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.cache.NestIntersectionShapesPathCache
import org.elnix.dragonlauncher.base.cache.PointStableCache
import org.elnix.dragonlauncher.base.model.serializables.Nest
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.components.IntersectionShape

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
    depth: Int = 1,
    eraseColor: Color,
    allowShowPointCenter: Boolean = false,
    pointSettingsDisplay: Boolean = false,
    showCancelZone: Boolean = false,
    hideShapes: Boolean = false,
    skipSelected: Boolean = false
) {
    val drawParams = rememberDrawParams(
        eraseColor = eraseColor,
        allowShowPointCenter = allowShowPointCenter,
        showCancelZone = showCancelZone,
        pointSettingsDisplay = pointSettingsDisplay,
        hideShapes = hideShapes,
        skipSelected = skipSelected
    )
    val iconTrigger by PointStableCache.cacheTrigger.asState()

    // The key is actually useful, I tried to remove it, but it messed up the drawing in the PointSettingScreen
    key(iconTrigger) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(modifier)
        ) {
            this.NestOverlay(
                nest = nest,
                depth = depth,
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
    require(depth > 0)

    val isSettingDisplay = drawParams.pointSettingsDisplay

    val defaultNest = drawParams.pointsService.defaultNest.value
    val interSectionShapes = nest.getInterSectionShapes(defaultNest)
    val defaultShape = drawParams.pointsService.defaultIntersectionShape.value

    val selectedPointsIds = drawParams.pointsService.selectedPointsIds.value
    val selectedShapes = drawParams.pointsService.getSelectedShapeIds(nest.id)

    if (!drawParams.hideShapes) {
        repeat(if (depth == 1) 1 else 2) { pass ->
            interSectionShapes.forEach { shape ->
                val showShape = depth > 1 ||
                        isSettingDisplay ||
                        (nest.getShowAllShapes(defaultNest, drawParams.showAllShapesInNest) && selectedPointsIds.isNotEmpty()) ||
                        (nest.getShowCurrentShape(defaultNest, drawParams.showShape) && shape.id in selectedShapes)

                if (showShape) {
                    val path = NestIntersectionShapesPathCache[shape] ?: return@forEach

                    this.IntersectionShape(
                        path = path,
                        shape = shape,
                        defaultShape = defaultShape,
                        center = center,
                        extraColors = drawParams.extraColors,
                        erase = pass == 0 && depth > 1,
                        eraseColor = drawParams.eraseColor
                    )
                }
            }
        }
    }

    if (drawParams.showCancelZone) {
        drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color.Red,
                    Color.Yellow,
                    Color.Green,
                    Color.Cyan,
                    Color.Blue,
                    Color.Magenta
                )
            ),
            radius = nest.getCancelZone(defaultNest).dp.toPx(),
            center = center,
            style = Stroke(Stroke.HairlineWidth)
        )
    }

    if (isSettingDisplay) {
        centerOfNest(center)
    }

    val filteredPoints = drawParams
        .pointsService
        .getPointsForNest(nestId = nest.id, skipSelected = drawParams.skipSelected && depth == 1)
        .filter { (id, point) ->
            when {
                depth > 1 -> true
                isSettingDisplay -> true
                nest.getShowAllPointsInCurrentNest(defaultNest, drawParams.showAllPointsInCurrentNest) -> true
                else -> {
                    (drawParams.showCurrentPoint && (id in selectedPointsIds)) ||
                            (nest.getShowAllPointsInCurrentNest(
                                defaultNest,
                                drawParams.showAllPointsInCurrentShape
                            ) && (point.shapeId in selectedShapes))
                }
            }
        }

    filteredPoints.forEach { (id, p) ->
        val drawPoint: Point = filteredPoints[id] ?: p
        val pointOffset = center + p.getPos()

        if (drawParams.nestDebugOverlay) {
            val endOffset: Offset = if (drawPoint.shapeId == null) {
                center
            } else {
                interSectionShapes.firstOrNull { it.id == drawPoint.shapeId }?.let { shape ->
                    center + shape.getOffset(defaultShape)
                } ?: center
            }

            drawLine(
                color = Color.White,
                start = endOffset,
                end = pointOffset
            )
        }

        PointIcon(
            depth = depth,
            point = drawPoint,
            center = pointOffset,
            selected = selectedAll || (p.id in selectedPointsIds),
            drawParams = drawParams,
            customText = null
        )
    }

    if (drawParams.allowShowPointInCenter) {
        if (selectedPointsIds.size == 1) {
            val point = filteredPoints[selectedPointsIds.first()] ?: return
            PointIcon(
                point = point,
                depth = depth,
                center = center,
                selected = true,
                drawParams = drawParams.copy(allowShowPointInCenter = false),
                customText = null
            )
        }
    }
}
