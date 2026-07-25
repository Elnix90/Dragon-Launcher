package org.elnix.dragonlauncher.ui.helpers.customobjects

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import org.elnix.dragonlauncher.ktx.toPath

fun DrawScope.drawShapeWithColor(
    shape: Shape,
    rotation: Int,
    center: Offset,
    size: Size,
    color: Color,
    strokeWidth: Float,
    erase: Boolean = false
) {
    val path = toPath(shape, size)

    withTransform(
        {
            rotate(
                degrees = rotation.toFloat(),
                pivot = center
            )
            translate(
                left = center.x - size.width / 2f,
                top = center.y - size.height / 2f
            )
        }
    ) {
        if (erase) {
            drawPath(
                path = path,
                color = Color.Transparent,
                style = Fill,
                blendMode = BlendMode.Clear
            )
        }

        drawPath(
            path = path,
            color = color,
            style = if (strokeWidth > 0f) Stroke(strokeWidth) else Fill
        )
    }
}