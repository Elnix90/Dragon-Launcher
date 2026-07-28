package org.elnix.dragonlauncher.ui.helpers.customobjects

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import org.elnix.dragonlauncher.base.model.serializables.CustomObject
import org.elnix.dragonlauncher.ktx.toPath

fun DrawScope.customObject(
    customObject: CustomObject,
    rotation: Int,
    shape: Shape,
    angleColor: Color,
    center: Offset,
    eraseColor: Color?
) {

    val sizePx = customObject.size.toPx()
    val size = Size(sizePx, sizePx)
    val path = toPath(shape, size)

    withTransform(
        {
            if (customObject.mirror) {
                mirrorVertically(center)
            }

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
        drawPathGlow(
            path = path,
            color = customObject.color ?: angleColor,
            lineStrokeWidth = customObject.stroke,
            glow = customObject.glow,
            erase = customObject.eraseBackground,
            eraseColor = eraseColor
        )
    }
}
