package org.elnix.dragonlauncher.ui.helpers.nests

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.IntSize
import org.elnix.dragonlauncher.base.cache.DrawPathCache
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.resolveShape
import org.elnix.dragonlauncher.ui.helpers.customobjects.shapeToPath
import org.elnix.dragonlauncher.ui.helpers.nests.cache.PointStableCache
import org.elnix.dragonlauncher.ui.remembers.CustomTexts


@Suppress("FunctionName")
fun DrawScope.PointBg(
    point: Point,
    selected: Boolean,
    center: Offset,
    drawParams: DrawParams,
    customText: CustomTexts
) {
    val extraColors = drawParams.extraColors
    val defaultPoint = drawParams.pointsService.defaultPoint.value

    val cached = PointStableCache[point.id] ?: return

    val iconBitmap = cached.imageBitmap
    val iconSize = cached.iconSize
    val sizePx = cached.sizePx

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

    val path = DrawPathCache.getOrCompute(Pair(borderIconShape, iconSize)) {
        shapeToPath(borderShape, iconSize)
    }

    val customTexts = customText ?: cached.customTexts
    val offsetScopeText = customTexts?.first
    if (offsetScopeText != null) {
        drawText(
            textLayoutResult = offsetScopeText.offsetTextLayoutResult,
            topLeft = center - offsetScopeText.topLeft
        )
    }

    val idScopeText = customTexts?.second
    if (idScopeText != null) {
        drawText(
            textLayoutResult = idScopeText.offsetTextLayoutResult,
            topLeft = center - idScopeText.topLeft
        )
    }

    translate(
        left = center.x + iconSize.width / -2f,
        top = center.y + iconSize.height / -2f
    ) {
        drawPath(
            path = path,
            color = backgroundColor,
            style = Fill,
            blendMode = BlendMode.Clear
        )

        if (borderStroke > 0f && borderColor.alpha != 0f) {
            drawPath(
                path = path,
                color = borderColor,
                style = Stroke(width = borderStroke)
            )
        }
    }

    if (iconBitmap != null) {
        val iconPath = DrawPathCache.getOrCompute(
            Pair(borderIconShape, Size(sizePx, sizePx))
        ) {
            shapeToPath(borderShape, Size(sizePx, sizePx))
        }

        translate(
            left = center.x + sizePx / -2f,
            top = center.y + sizePx / -2f
        ) {
            clipPath(iconPath) {
                drawImage(
                    image = iconBitmap,
                    dstSize = IntSize(sizePx.toInt(), sizePx.toInt())
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
