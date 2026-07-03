package org.elnix.dragonlauncher.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape
import org.elnix.dragonlauncher.base.resolveShape
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.ui.helpers.customobjects.drawShapeWithColor
import org.elnix.dragonlauncher.ui.helpers.nests.DrawParams

@Composable
fun IntersectionShape(
    shape: IntersectionShape,
    modifier: Modifier = Modifier
) {

    val size = shape.size.dp
    val color = shape.color ?: LocalExtraColors.current.circle
    val borderStroke = shape.borderStroke?.dp ?: IntersectionShape.Companion.Defaults.borderStrokeDefault
    val shape = shape.shape.resolveShape()

    Box(
        modifier = modifier
            .size(size)
            .border(borderStroke, color, shape)
    )
}

@Suppress("FunctionName")
fun DrawScope.IntersectionShape(
    shape: IntersectionShape,
    center: Offset,
    drawParams: DrawParams,
    erase: Boolean
) {

    val sizePx = shape.size.dp.toPx()
    val size = Size(sizePx, sizePx)
    val color = shape.color ?: drawParams.extraColors.circle
    val strokeWith = (shape.borderStroke?.dp ?: IntersectionShape.Companion.Defaults.borderStrokeDefault).toPx()
    val rotation = shape.angle ?: IntersectionShape.Companion.Defaults.angleDefault
    val position = center + shape.centerOffset
    val shape = shape.shape.resolveShape()


    drawShapeWithColor(
        shape = shape,
        rotation = rotation,
        center = position,
        size = size,
        color = color,
        strokeWidth = strokeWith,
        erase = erase
    )
}