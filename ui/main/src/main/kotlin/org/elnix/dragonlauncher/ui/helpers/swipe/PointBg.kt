package org.elnix.dragonlauncher.ui.helpers.swipe

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.IntSize
import org.elnix.dragonlauncher.base.cache.DrawPathCache
import org.elnix.dragonlauncher.base.cache.DrawScopeText
import org.elnix.dragonlauncher.base.cache.PointStableCache
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.resolveShape
import org.elnix.dragonlauncher.ktx.toPath
import org.elnix.dragonlauncher.ui.helpers.customobjects.GlowDrawOrder
import org.elnix.dragonlauncher.ui.helpers.customobjects.drawPathGlow


@Suppress("FunctionName")
fun DrawScope.PointBg(
    point: Point,
    selected: Boolean,
    center: Offset,
    drawParams: DrawParams,
    customText: Pair<DrawScopeText?, DrawScopeText?>?
) {
    val extraColors = drawParams.extraColors
    val defaultPoint = drawParams.pointsService.defaultPoint.value
    val cached = PointStableCache[point.id] ?: run {
        missingPoint(drawParams, center)
        return
    }


    // THIS IS EXPENSIVE TO DRAW THAT'S WHY THEY ARE ONLY DRAWN IN DEBUG
    val customTexts = customText ?: cached.customTexts
    val firstText = customTexts?.first
    if (firstText != null) {
        drawText(
            textLayoutResult = firstText.offsetTextLayoutResult,
            color = extraColors.shapes,
            topLeft = center - firstText.topLeft
        )
    }

    val secondText = customTexts?.second
    if (secondText != null) {
        drawText(
            textLayoutResult = secondText.offsetTextLayoutResult,
            color = extraColors.shapes,
            topLeft = center - secondText.topLeft
        )
    }


    val iconBitmap = cached.imageBitmap
    val iconSize = cached.iconSize
    val sizePx = cached.sizePx


    val borderColor = point.getBorderColor(selected, defaultPoint, extraColors)
    val backgroundColor =  point.getBackgroundColor(selected, defaultPoint)
    val glow = point.getGlow(selected, defaultPoint)

    val borderIconShape = point.getBorderShape(selected, defaultPoint)
    val borderShape = borderIconShape.resolveShape()
    val borderStroke = point.getBorderStroke(selected, defaultPoint)

    val path = DrawPathCache.getOrCompute(Pair(borderIconShape, iconSize)) {
        toPath(borderShape, iconSize)
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
            drawOrder = GlowDrawOrder.First,
            glow = glow,
            erase = true,
            eraseColor = drawParams.eraseColor
        )

        drawPath(
            path = path,
            color = backgroundColor,
            style = Fill,
            blendMode = BlendMode.Src
        )
    }

    if (iconBitmap != null) {
        val iconPath = DrawPathCache.getOrCompute(
            Pair(borderIconShape, Size(sizePx, sizePx))
        ) {
            toPath(borderShape, Size(sizePx, sizePx))
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

    if (drawParams.pointSettingsDisplay) {
        DecorationIcons(
            ctx = drawParams.ctx,
            center = center,
            iconSize = iconSize,
            point = point
        )
    }
}
