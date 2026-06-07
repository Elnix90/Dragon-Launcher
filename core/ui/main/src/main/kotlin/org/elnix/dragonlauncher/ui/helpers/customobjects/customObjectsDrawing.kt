package org.elnix.dragonlauncher.ui.helpers.customobjects

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.CustomObject

fun DrawScope.customObject(
    customObject: CustomObject,
    default: CustomObject,
    rotation: Int,
    shape: Shape,
    angleColor: Color,
    center: Offset,
) {
    val size = (customObject.size ?: default.size!!).dp.toPx()

    val baseSize = Size(size, size)

    // Apply glow first (background effect)
    customObject.glow?.let { glow ->
        val glowRadius = (glow.radius ?: default.glow!!.radius!!).dp.toPx()
        if (glowRadius > 0f) {
            glowOverlay(
                center = center,
                color = glow.color ?: angleColor,
                radius = glowRadius
            )
        }
    }

    // Main shape
    val shapeColor = customObject.color ?: angleColor
    val shapeStroke = (customObject.stroke ?: default.stroke!!).dp.toPx()


    // Not zero size
    if (baseSize.width > 0) {
        drawShapeWithColor(
            shape = shape,
            rotation = rotation,
            center = center,
            size = baseSize,
            color = shapeColor,
            strokeWidth = shapeStroke,
            erase = customObject.eraseBackground ?: default.eraseBackground!!
        )
    }
}
