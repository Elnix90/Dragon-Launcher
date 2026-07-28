package org.elnix.dragonlauncher.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape
import org.elnix.dragonlauncher.base.resolveShape
import org.elnix.dragonlauncher.base.theme.ExtraColors
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.ui.base.modifiers.conditional
import org.elnix.dragonlauncher.ui.helpers.customobjects.GlowDrawOrder
import org.elnix.dragonlauncher.ui.helpers.customobjects.drawPathGlow

@Suppress("FunctionName")
fun DrawScope.IntersectionShape(
    path: Path,
    shape: IntersectionShape,
    defaultShape: IntersectionShape,
    center: Offset,
    extraColors: ExtraColors,
    erase: Boolean,
    eraseColor: Color?
) {

    val size = shape.getSize(this.density, defaultShape)
    val color = shape.getColor(defaultShape, extraColors)
    val strokeWith = shape.getBorderStroke(defaultShape)
    val rotation = shape.getRotation(defaultShape)
    val position = center + shape.getOffset(defaultShape)
    val glow = shape.getGlow(defaultShape)

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

@Composable
fun IntersectionShapePreview(
    shape: IntersectionShape,
    defaultShape: IntersectionShape,
    size: Dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val resolvedShape = shape.getShape(defaultShape).resolveShape()
    Box(
        modifier = modifier
            .size(size)
            .rotate(shape.getRotation(defaultShape).toFloat())
            .clip(MaterialTheme.shapes.medium)
            .conditional(onClick) {
                clickable(onClick = it)
            }
            .clip(resolvedShape)
            .border(1.dp, shape.color ?: LocalExtraColors.current.shapes, resolvedShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = shape.id.toString(),
            style = MaterialTheme.typography.labelSmall
        )
    }
}