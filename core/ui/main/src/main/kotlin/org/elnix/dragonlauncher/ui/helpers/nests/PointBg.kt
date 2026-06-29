package org.elnix.dragonlauncher.ui.helpers.nests

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.IntSize
import org.elnix.dragonlauncher.base.cache.DrawPathCache
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.resolveShape
import org.elnix.dragonlauncher.ui.helpers.customobjects.shapeToPath


@Suppress("FunctionName")
fun DrawScope.PointBg(
    point: Point,
    selected: Boolean,
    center: Offset,
    drawParams: DrawParams,
    iconBitmap: ImageBitmap?,
) {
    val defaultPoint = drawParams.defaultPoint
    val extraColors = drawParams.extraColors

    val sizePx: Float = point.getSize(defaultPoint).toPx()
    val innerPaddingPx: Float = point.getInnerPadding(defaultPoint).toPx()

    val borderRadii: Float = ((sizePx / 2 + innerPaddingPx).coerceAtLeast(0f))

    val borderStroke: Float =
        if (selected) {
            point.borderStrokeSelected ?: defaultPoint.borderStrokeSelected ?: 8f
        } else {
            point.borderStroke ?: defaultPoint.borderStroke ?: 4f
        }

    val borderColor: Color =
        if (selected) {
            point.borderColorSelected?.let { Color(it) }
                ?: defaultPoint.borderColorSelected?.let { Color(it) }
        } else {
            point.borderColor?.let { Color(it) } ?: defaultPoint.borderColor?.let { Color(it) }
        } ?: extraColors.circle

    val backgroundColor: Color =
        if (selected) {
            point.backgroundColorSelected?.let { Color(it) }
                ?: defaultPoint.backgroundColorSelected?.let { Color(it) }
        } else {
            point.backgroundColor?.let { Color(it) } ?: defaultPoint.backgroundColor?.let { Color(it) }
        } ?: Color.Transparent

    val borderIconShape: IconShape = if (selected) {
        point.borderShapeSelected ?: defaultPoint.borderShapeSelected
    } else {
        point.borderShape ?: defaultPoint.borderShape
    } ?: IconShape.Circle

    val borderShape = borderIconShape.resolveShape()

    val iconSizeF = borderRadii * 2f
    val iconSize = Size(iconSizeF, iconSizeF)

    val path = DrawPathCache.getOrCompute(Pair(borderIconShape, iconSize)) {
        shapeToPath(borderShape, iconSize)
    }

    translate(
        left = center.x + iconSize.width / -2f,
        top = center.y + iconSize.height / -2f
    ) {
        drawPath(
            path = path,
            color = backgroundColor,
            style = Fill
        )

        if (borderStroke > 0f) {
            if (borderColor.alpha != 0f) {
                drawPath(
                    path = path,
                    color = borderColor,
                    style = Stroke(width = borderStroke)
                )
            }
        }
    }

    if (iconBitmap != null) {
        val iconDrawSize = sizePx.coerceAtLeast(1f)
        val iconPath = DrawPathCache.getOrCompute(
            Pair(borderIconShape, Size(iconDrawSize, iconDrawSize))
        ) {
            shapeToPath(borderShape, Size(iconDrawSize, iconDrawSize))
        }


        translate(
            left = center.x + iconDrawSize / -2f,
            top = center.y + iconDrawSize / -2f
        ) {
            clipPath(iconPath) {
                drawImage(
                    image = iconBitmap,
                    dstSize = IntSize(iconDrawSize.toInt(), iconDrawSize.toInt())
                )
            }
        }
    }

    if (drawParams.showConfiguratorDecorations) {
        DecorationIcons(
            ctx = drawParams.ctx,
            center = center,
            iconSize = iconSize,
            point = point
        )
    }
}
