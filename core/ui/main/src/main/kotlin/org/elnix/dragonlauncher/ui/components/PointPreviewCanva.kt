package org.elnix.dragonlauncher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.common.serializables.SwipeAction
import org.elnix.dragonlauncher.common.serializables.Point
import org.elnix.dragonlauncher.ui.composition.LocalDefaultPoint
import org.elnix.dragonlauncher.ui.helpers.nests.actionsInCircle
import org.elnix.dragonlauncher.ui.remembers.rememberSwipeDefaultParams

@Composable
fun PointPreviewCanvas(
    editPoint: Point,
    defaultPoint: Point,
    backgroundSurfaceColor: Color,
    modifier: Modifier = Modifier,
) {
    val drawParams = rememberSwipeDefaultParams(
        defaultPointSerializable = defaultPoint,
        backgroundColor = backgroundSurfaceColor
    )

    val defaultPoint = LocalDefaultPoint.current

    val height =
        when (editPoint.action) {
            is SwipeAction.OpenCircleNest -> 100
            else -> (editPoint.size ?: defaultPoint.size ?: Point.defaultSwipePointsValues.size!!) +
                    (editPoint.innerPadding ?: defaultPoint.innerPadding ?: Point.defaultSwipePointsValues.innerPadding!!) * 2

        }


    Canvas(modifier = modifier.height(height.dp)) {
        drawIntoCanvas { canvas ->

            val bounds = Rect(0f, 0f, size.width, size.height)
            canvas.saveLayer(bounds, Paint())

            val centerY = size.height / 2f
            val leftX = size.width * 0.25f
            val rightX = size.width * 0.75f

            // Left action
            actionsInCircle(
                selected = false,
                point = editPoint,
                center = Offset(leftX, centerY),
                depth = 1,
                drawParams = drawParams,
                preventBgErasing = true,
                showConfiguratorDecorations = true,
            )

            // Right action
            actionsInCircle(
                selected = true,
                point = editPoint,
                center = Offset(rightX, centerY),
                depth = 1,
                drawParams = drawParams,
                preventBgErasing = true,
                showConfiguratorDecorations = true,
            )

            canvas.restore()
        }
    }
}