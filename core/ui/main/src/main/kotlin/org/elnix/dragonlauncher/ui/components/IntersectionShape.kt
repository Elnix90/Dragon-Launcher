package org.elnix.dragonlauncher.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape
import org.elnix.dragonlauncher.ui.helpers.customobjects.drawNeonGlowShapePath
import org.elnix.dragonlauncher.ui.helpers.swipe.DrawParams

@Suppress("FunctionName")
fun DrawScope.IntersectionShape(
    path: Path,
    shape: IntersectionShape,
    center: Offset,
    drawParams: DrawParams,
    erase: Boolean
) {

    val size = shape.getSize(this.density)
    val color = shape.color ?: drawParams.extraColors.circle
    val strokeWith = (shape.borderStroke?.dp ?: IntersectionShape.borderStrokeDefault).toPx()
    val rotation = shape.angle
    val position = center + shape.offset
    val glow = shape.glow
    val glowColor = glow?.color ?: color
    val glowStrokeWidth = glow?.radius ?: strokeWith

    withTransform(
        {
            translate(
                left = position.x,
                top = position.y
            )
            rotate(
                degrees = rotation.toFloat(),
                pivot = Offset.Zero
            )
            translate(
                left = -size.width / 2f,
                top = -size.height / 2f
            )
        }
    ) {
        drawNeonGlowShapePath(
            path = path,
            color = color,
            lineStrokeWidth = strokeWith,
            glowRadius = glowStrokeWidth,
            glowColor = glowColor,
            erase = erase
        )
    }
}