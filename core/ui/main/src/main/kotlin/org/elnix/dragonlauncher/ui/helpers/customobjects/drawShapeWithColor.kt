package org.elnix.dragonlauncher.ui.helpers.customobjects

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

fun DrawScope.drawShapeWithColor(
    shape: Shape,
    rotation: Int,
    center: Offset,
    size: Size,
    color: Color,
    strokeWidth: Float,
    erase: Boolean = false
) {
    val path = shapeToPath(shape, size)

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
            style = if (strokeWidth > 0f)
                Stroke(strokeWidth)
            else
                Fill
        )
    }
}

fun DrawScope.shapeToPath(
    shape: Shape,
    size: Size
): Path = shapeToPath(shape, size, this)


fun shapeToPath(
    shape: Shape,
    size: Size,
    density: Density
): Path {
    val outline = shape.createOutline(
        size = size,
        layoutDirection = LayoutDirection.Ltr,
        density = density
    )

    return when (outline) {
        is Outline.Rectangle -> Path().apply { addRect(outline.rect) }
        is Outline.Rounded -> Path().apply { addRoundRect(outline.roundRect) }
        is Outline.Generic -> outline.path
    }
}
