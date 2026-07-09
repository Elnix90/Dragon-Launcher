package org.elnix.dragonlauncher.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape
import org.elnix.dragonlauncher.ui.helpers.customobjects.drawPathGlow
import org.elnix.dragonlauncher.ui.helpers.swipe.DrawParams

@Suppress("FunctionName")
public fun DrawScope.IntersectionShape(
    path: Path,
    shape: IntersectionShape,
    center: Offset,
    drawParams: DrawParams,
    erase: Boolean
) {

    val size = shape.getSize(this.density)
    val color = shape.color ?: drawParams.extraColors.shapes
    val strokeWith = shape.borderStroke ?: IntersectionShape.borderStrokeDefault
    val rotation = shape.angle
    val position = center + shape.offset
    val glow = shape.glow

    withTransform(
        {
            translate(
                left = position.x,
                top = position.y
            )
            rotate(
                degrees = rotation,
                pivot = Offset.Zero
            )
            translate(
                left = -size.width / 2f,
                top = -size.height / 2f
            )
        }
    ) {
        drawPathGlow(
            path = path,
            color = color,
            lineStrokeWidth = strokeWith,
            glow = glow,
            erase = erase
        )
    }
}