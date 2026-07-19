package org.elnix.dragonlauncher.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape
import org.elnix.dragonlauncher.ui.helpers.customobjects.GlowDrawOrder
import org.elnix.dragonlauncher.ui.helpers.customobjects.drawPathGlow

@Suppress("FunctionName")
public fun DrawScope.IntersectionShape(
    path: Path,
    shape: IntersectionShape,
    center: Offset,
    shapesColor: Color,
    erase: Boolean,
    eraseColor: Color?
) {

    val size = shape.getSize(this.density)
    val color = shape.color ?: shapesColor
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
            drawOrder = GlowDrawOrder.AfterErase,
            erase = erase,
            eraseColor = eraseColor
        )
    }
}