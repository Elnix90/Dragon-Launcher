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
import org.elnix.dragonlauncher.ui.helpers.customobjects.erasePath

@Suppress("FunctionName")
fun DrawScope.IntersectionShape(
    path: Path,
    shape: IntersectionShape,
    defaultShape: IntersectionShape,
    center: Offset,
    extraColors: ExtraColors,
    erase: Boolean,
    isDefaultEditing: Boolean,
    eraseColor: Color?
) {

    val position = center + shape.getOffset(defaultShape, isDefaultEditing)
    val rotation = shape.getRotation(defaultShape, isDefaultEditing)
    val size = shape.getSize(this.density, defaultShape, isDefaultEditing)

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
        if (erase) {
            erasePath(path, shape.getBorderStroke(defaultShape, isDefaultEditing), eraseColor)
        } else {
            drawPathGlow(
                path = path,
                color = shape.getColor(defaultShape, extraColors, isDefaultEditing),
                lineStrokeWidth = shape.getBorderStroke(defaultShape, isDefaultEditing),
                glow = shape.getGlow(defaultShape, isDefaultEditing),
                drawOrder = GlowDrawOrder.First,
                erase = false,
                eraseColor = eraseColor
            )
        }
    }
}

@Composable
fun IntersectionShapePreview(
    shape: IntersectionShape,
    defaultShape: IntersectionShape,
    size: Dp,
    isDefaultEditing: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val resolvedShape = shape.getShape(defaultShape, isDefaultEditing).resolveShape()
    Box(
        modifier = modifier
            .size(size)
            .rotate(shape.getRotation(defaultShape, isDefaultEditing).toFloat())
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