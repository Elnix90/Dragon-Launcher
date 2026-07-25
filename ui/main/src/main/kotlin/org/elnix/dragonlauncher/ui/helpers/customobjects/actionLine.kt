package org.elnix.dragonlauncher.ui.helpers.customobjects

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.isUnspecified
import org.elnix.dragonlauncher.base.model.models.AngleLineObjects
import org.elnix.dragonlauncher.base.model.models.AngleLineObjects.Angle
import org.elnix.dragonlauncher.base.model.models.AngleLineObjects.End
import org.elnix.dragonlauncher.base.model.models.AngleLineObjects.Line
import org.elnix.dragonlauncher.base.model.models.AngleLineObjects.Start
import org.elnix.dragonlauncher.base.model.serializables.CustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultAngleCustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultLineCustomObject
import org.elnix.dragonlauncher.ktx.toPath
import kotlin.math.abs

fun DrawScope.actionLine(
    start: Offset,
    end: Offset,
    sweepAngle: Float,
    lineColor: Color,
    eraseColor: Color?,

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
                        lineCustomObject = lineCustomObject,
                        eraseColor = eraseColor
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
                        shape = pickedRememberShapeAngle,
                        eraseColor = eraseColor
                    )
                }
            }

            Start -> {
                if (showStartObjectPreview) {
                    customObject(
                        customObject = startCustomObject,
                        angleColor = lineColor,
                        center = start,
                        rotation = pickedRememberRotationStart,
                        shape = pickedRememberShapeStart,
                        eraseColor = eraseColor
                    )
                }
            }

            End -> {
                if (showEndObjectPreview) {
                    customObject(
                        customObject = endCustomObject,
                        angleColor = lineColor,
                        center = end,
                        rotation = pickedRememberRotationEnd,
                        shape = pickedRememberShapeEnd,
                        eraseColor = eraseColor
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
    eraseColor: Color?
) {
    drawNeonGlowLine(
        start = start,
        end = end,
        color = lineCustomObject.color ?: lineColor,
        lineStrokeWidth = lineCustomObject.stroke.toPx(),
        glow = lineCustomObject.glow ?: defaultLineCustomObject.glow,
        erase = lineCustomObject.eraseBackground,
        eraseColor = eraseColor
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
    eraseColor: Color?
) {
    if (angleLineCustomObject.stroke.isUnspecified) return

    val radius = angleLineCustomObject.size.toPx() / 2
    val diameterPx = radius * 2

    val path = toPath(
        shape = shape,
        size = Size(diameterPx, diameterPx)
    )

    // Center path around (0,0) so translate(center) places it correctly
    val matrix = Matrix()
    matrix.translate(-diameterPx / 2f, -diameterPx / 2f)
    path.transform(matrix)

    // Derive progress and direction from sweepAngle
    val isAnticlockwise = sweepAngle < 0f
    val progress = (abs(sweepAngle) / 360f).coerceIn(0f, 1f)

    val pathMeasurer = PathMeasure()
    val destinationPath = Path()
    pathMeasurer.setPath(path, false)

    if (!isAnticlockwise) {
        pathMeasurer.getSegment(0f, pathMeasurer.length * progress, destinationPath)
    } else {
        pathMeasurer.getSegment(pathMeasurer.length * (1f - progress), pathMeasurer.length, destinationPath)
    }

    withTransform({
        if (angleLineCustomObject.mirror) { mirrorVertically(center) }
        rotate(degrees = rotation.toFloat(), pivot = center)
        // Put the path in the center
        translate(center.x, center.y)
    }) {
        drawPathGlow(
            path = destinationPath,
            color = angleLineCustomObject.color ?: lineColor,
            lineStrokeWidth = angleLineCustomObject.stroke.toPx(),
            glow = angleLineCustomObject.glow,
            erase = angleLineCustomObject.eraseBackground,
            eraseColor = eraseColor
        )
    }
}