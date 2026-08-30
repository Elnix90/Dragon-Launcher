package org.elnix.dragonlauncher.ui.helpers.swipe

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Point

/**
 * Composable wrapper that renders a single point icon inside a [Canvas].
 *
 * Use this from composable context to draw a point's background, border,
 * action icon, and badges. Prefer the [DrawScope.PointIcon] version when
 * you are already inside a Canvas.
 */
@Composable
fun PointIcon(
    point: Point,
    center: Offset,
    eraseColor: Color,
    modifier: Modifier = Modifier,
    depth: Int = 1,
    selected: Boolean = false,
    pointSettingsDisplay: Boolean = false,
    hideShapes: Boolean = false
) {
    val drawParams =
        rememberDrawParams(
            eraseColor = eraseColor,
            isDefaultEditing = false,
            pointSettingsDisplay = pointSettingsDisplay,
            showCancelZone = false,
            allowShowPointCenter = false,
            hideShapes = hideShapes,
            skipSelected = false
        )

    Canvas(
        modifier =
            modifier
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
    ) {
        this.PointIcon(
            point = point,
            depth = depth,
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
    require(depth > 0)

    val action = point.action

    if (
        action is Action.OpenNest &&
        point.customIcon == null &&
        depth < drawParams.maxNestsDepth &&
        !drawParams.preventDrawingSubNests
    ) {
        val nest = drawParams.pointsService.findNestById(action.nestId)
        val scaleFactor = nest.getPreviewScaleFactor(drawParams.pointsService.defaultNest.value, drawParams.isDefaultEditing)

        val newDepth = depth + 1
        val newScale = 1f / (newDepth * scaleFactor)

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
    } else {
        PointBg(
            point = point,
            selected = selected,
            center = center,
            drawParams = drawParams
        )
    }
}
