package org.elnix.dragonlauncher.ui.helpers.swipe

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.IntSize
import org.elnix.dragonlauncher.base.cache.DrawPathCache
import org.elnix.dragonlauncher.base.model.serializables.CustomGlow
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.resolveShape
import org.elnix.dragonlauncher.ui.helpers.customobjects.drawPathGlow
import org.elnix.dragonlauncher.ui.helpers.customobjects.shapeToPath
import org.elnix.dragonlauncher.ui.helpers.swipe.cache.nests.DrawScopeText
import org.elnix.dragonlauncher.ui.helpers.swipe.cache.nests.PointStableCache


@Suppress("FunctionName")
public fun DrawScope.PointBg(
    point: Point,
    selected: Boolean,
    center: Offset,
    drawParams: DrawParams,
    customText: Pair<DrawScopeText?, DrawScopeText?>?
) {
    val extraColors = drawParams.extraColors
    val defaultPoint = drawParams.pointsService.defaultPoint.value
    val cached = PointStableCache[point.id] ?: return


    // THIS IS EXPENSIVE TO DRAW THAT'S WHY THEY ARE ONLY DRAWN IN DEBUG
    val customTexts = customText ?: cached.customTexts
    val offsetScopeText = customTexts?.first
    if (offsetScopeText != null) {
        drawText(
            textLayoutResult = offsetScopeText.offsetTextLayoutResult,
            color = extraColors.shapes,
            topLeft = center - offsetScopeText.topLeft
        )
    }

    val idScopeText = customTexts?.second
    if (idScopeText != null) {
        drawText(
            textLayoutResult = idScopeText.offsetTextLayoutResult,
            color = extraColors.shapes,
            topLeft = center - idScopeText.topLeft
        )
    }


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
        } ?: extraColors.shapes

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


    val glowRadius: Float = if (selected) {
        point.glow?.radius ?: defaultPoint.glow?.radius ?: Point.defaultGlow.radius
    } else {
        point.glowSelected?.radius ?: defaultPoint.glowSelected?.radius ?: Point.defaultGlowSelected.radius
    }

    val glowColor: Color = if (selected) {
        point.glow?.color ?: defaultPoint.glow?.color ?: Point.defaultGlowSelected.color
    } else {
        point.glowSelected?.color ?: defaultPoint.glowSelected?.color ?: Point.defaultGlowSelected.color
    } ?: borderColor

    val borderShape = borderIconShape.resolveShape()

    val path = DrawPathCache.getOrCompute(Pair(borderIconShape, iconSize)) {
        shapeToPath(borderShape, iconSize)
    }


    withTransform(
        {
            translate(
                left = center.x + iconSize.width / -2f,
                top = center.y + iconSize.height / -2f
            )
        }
    ) {

        drawPathGlow(
            path = path,
            color = borderColor,
            lineStrokeWidth = borderStroke,
            glow = CustomGlow(
                radius = glowRadius,
                color = glowColor,
            ),
            erase = true
        )
        drawPath(
            path = path,
            color = backgroundColor,
            style = Fill,
            blendMode = BlendMode.Clear
        )
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
