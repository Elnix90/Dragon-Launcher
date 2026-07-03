package org.elnix.dragonlauncher.ui.helpers.customobjects

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.models.AngleLineObjects
import org.elnix.dragonlauncher.base.model.models.AngleLineObjects.Angle
import org.elnix.dragonlauncher.base.model.models.AngleLineObjects.End
import org.elnix.dragonlauncher.base.model.models.AngleLineObjects.Line
import org.elnix.dragonlauncher.base.model.models.AngleLineObjects.Start
import org.elnix.dragonlauncher.base.model.serializables.CustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultAngleCustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultEndCustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultLineCustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultStartCustomObject
import kotlin.math.abs

fun DrawScope.actionLine(
    start: Offset,
    end: Offset,
    sweepAngle: Float,
    lineColor: Color,

    order: List<AngleLineObjects>,

    showLineObjectPreview: Boolean,
    showAngleLineObjectPreview: Boolean,
    showStartObjectPreview: Boolean,
    showEndObjectPreview: Boolean,

    pickedRememberShapeAngle: Shape,
    pickedRememberRotationStart: Int,

    pickedRememberShapeStart: Shape,
    pickedRememberRotationEnd: Int,

    pickedRememberShapeEnd: Shape,
    pickedRememberRotationAngle: Int,

    lineCustomObject: CustomObject,
    angleLineCustomObject: CustomObject,
    startCustomObject: CustomObject,
    endCustomObject: CustomObject
) {

    order.forEach { drawObject ->
        when (drawObject) {
            Line -> {
                if (showLineObjectPreview) {
                    lineObject(
                        start = start,
                        end = end,
                        lineColor = lineColor,
                        lineCustomObject = lineCustomObject
                    )
                }
            }

            Angle -> {
                // The "do you hate it?" thing in settings
                if (showAngleLineObjectPreview) {
                    angleObject(
                        center = start,
                        sweepAngle = sweepAngle,
                        lineColor = lineColor,
                        angleLineCustomObject = angleLineCustomObject,
                        rotation = pickedRememberRotationAngle,
                        shape = pickedRememberShapeAngle
                    )
                }
            }

            Start -> {
                if (showStartObjectPreview) {
                    customObject(
                        customObject = startCustomObject,
                        default = defaultStartCustomObject,
                        angleColor = lineColor,
                        center = start,
                        rotation = pickedRememberRotationStart,
                        shape = pickedRememberShapeStart
                    )
                }
            }

            End -> {
                if (showEndObjectPreview) {
                    customObject(
                        customObject = endCustomObject,
                        default = defaultEndCustomObject,
                        angleColor = lineColor,
                        center = end,
                        rotation = pickedRememberRotationEnd,
                        shape = pickedRememberShapeEnd
                    )
                }
            }
        }
    }
}


private fun DrawScope.lineObject(
    start: Offset,
    end: Offset,
    lineColor: Color,
    lineCustomObject: CustomObject,
) {
    val lineGlow = lineCustomObject.glow
    val lineStrokeWidth = (lineCustomObject.stroke ?: defaultLineCustomObject.stroke!!).dp.toPx()

    val glowRadius = if (lineGlow != null) {
        (lineGlow.radius ?: defaultAngleCustomObject.glow!!.radius!!).dp.toPx()
    } else 0f

    val glowColor = if (lineGlow != null) {
        lineGlow.color ?: defaultAngleCustomObject.glow!!.color
    } else null


    drawNeonGlowLine(
        start = start,
        end = end,
        color = lineCustomObject.color ?: lineColor,
        lineStrokeWidth = lineStrokeWidth,
        glowRadius = glowRadius,
        glowColor = glowColor,
        erase = lineCustomObject.eraseBackground ?: defaultLineCustomObject.eraseBackground!!
    )
}

/**
 * Draws a custom-shaped angle indicator around a center point, trimmed proportionally
 * to the given [sweepAngle].
 *
 * The shape outline is sourced from [angleLineCustomObject] (falling back to
 * [defaultAngleCustomObject]), centered on [center], and partially revealed
 * using [PathMeasure] based on the sweep ratio.
 *
 * @param center The point around which the shape is drawn and rotated.
 * @param sweepAngle The angle in degrees, in the range `-360f..360f`.
 *   - Positive values draw the shape **clockwise** from the top (12 o'clock).
 *   - Negative values draw the shape **anticlockwise** from the top.
 *   - `±360f` results in a fully drawn shape outline.
 * @param lineColor The fallback color used if [angleLineCustomObject] has no color set.
 * @param angleLineCustomObject The customization data driving shape, size, stroke,
 *   glow, color, and erase behavior.
 */
private fun DrawScope.angleObject(
    center: Offset,
    sweepAngle: Float,
    lineColor: Color,
    rotation: Int,
    shape: Shape,
    angleLineCustomObject: CustomObject,
) {
    val strokeWidth = (angleLineCustomObject.stroke ?: defaultAngleCustomObject.stroke!!).dp.toPx()
    if (strokeWidth <= 0f) return

//    val shape = (angleLineCustomObject.shape ?: org.elnix.dragonlauncher.ui.base.defaultAngleCustomObject.shape!!).resolveShape()

    val radius = (angleLineCustomObject.size ?: defaultAngleCustomObject.size!!).dp.toPx() / 2
    val diameterPx = radius * 2

    val glowRadius = angleLineCustomObject.glow?.radius?.dp?.toPx() ?: 0f

    val glowColor = angleLineCustomObject.glow?.color
        ?: defaultAngleCustomObject.glow?.color

    val composePath = when (val outline = shape.createOutline(
        size = Size(diameterPx, diameterPx),
        layoutDirection = layoutDirection,
        density = this
    )) {
        is Outline.Generic -> outline.path
        is Outline.Rounded -> Path().apply { addRoundRect(outline.roundRect) }
        is Outline.Rectangle -> Path().apply { addRect(outline.rect) }
    }

    // Center path around (0,0) so translate(center) places it correctly
    val matrix = Matrix()
    matrix.translate(-diameterPx / 2f, -diameterPx / 2f)
    composePath.transform(matrix)

    // Derive progress and direction from sweepAngle
    val isAnticlockwise = sweepAngle < 0f
    val progress = (abs(sweepAngle) / 360f).coerceIn(0f, 1f)

    val pathMeasurer = PathMeasure()
    val destinationPath = Path()
    pathMeasurer.setPath(composePath, false)

    if (!isAnticlockwise) {
        pathMeasurer.getSegment(0f, pathMeasurer.length * progress, destinationPath)
    } else {
        pathMeasurer.getSegment(pathMeasurer.length * (1f - progress), pathMeasurer.length, destinationPath)
    }


    withTransform({
        rotate(degrees = rotation.toFloat(), pivot = center)

        scale(
            scaleX = -1f,
            scaleY = 1f,
            pivot = center
        )
        // Put the path tin the center
        translate(center.x, center.y)
    }) {
        drawNeonGlowShapePath(
            path = destinationPath,
            color = angleLineCustomObject.color ?: lineColor,
            lineStrokeWidth = strokeWidth,
            glowRadius = glowRadius,
            glowColor = glowColor ?: lineColor,
            erase = angleLineCustomObject.eraseBackground
                ?: defaultLineCustomObject.eraseBackground!!
        )
    }
}