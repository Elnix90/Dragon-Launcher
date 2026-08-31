package org.elnix.dragonlauncher.ui.helpers.swipe

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import org.elnix.dragonlauncher.base.cache.DrawPathCache
import org.elnix.dragonlauncher.base.cache.PointStableCache
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.resolveShape
import org.elnix.dragonlauncher.ktx.rect
import org.elnix.dragonlauncher.ktx.toPath
import org.elnix.dragonlauncher.ui.helpers.customobjects.drawPathGlow
import kotlin.math.roundToInt

@Suppress("FunctionName")
fun DrawScope.PointBg(
    point: Point,
    selected: Boolean,
    center: Offset,
    drawParams: DrawParams
) {
    val extraColors = drawParams.extraColors
    val colorScheme = drawParams.colorScheme
    val defaultPoint = drawParams.pointsService.defaultPoint.value
    val cached =
        PointStableCache[point.id] ?: run {
            missingPoint(drawParams, center)
            return
        }

    val iconBitmap = cached.imageBitmap
    val badgeBitmap = cached.badgeBitmap
    val iconSize = cached.iconSize
    val sizePx = cached.sizePx

    val borderColor = point.getBorderColor(selected, defaultPoint, extraColors, drawParams.isDefaultEditing)
    val backgroundColor = point.getBackgroundColor(selected, defaultPoint, drawParams.isDefaultEditing)
    val glow = point.getGlow(selected, defaultPoint, drawParams.isDefaultEditing)

    val borderIconShape = point.getBorderShape(selected, defaultPoint, drawParams.isDefaultEditing)
    val borderShape = borderIconShape.resolveShape()
    val borderStroke = point.getBorderStroke(selected, defaultPoint, drawParams.isDefaultEditing)

    val path =
        DrawPathCache.getOrCompute(Pair(borderIconShape, iconSize)) {
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
            glow = glow,
            erase = true,
            eraseColor = drawParams.eraseColor
        )

        drawPath(
            path = path,
            color = backgroundColor,
            style = Fill
        )
    }

    if (iconBitmap != null) {
        val size = Size.rect(sizePx)
        val iconPath =
            DrawPathCache.getOrCompute(
                Pair(drawParams.iconShape, size)
            ) {
                toPath(drawParams.iconShape.resolveShape(), size)
            }

        translate(
            left = center.x + sizePx / -2f,
            top = center.y + sizePx / -2f
        ) {
            clipPath(iconPath) {
                drawImage(
                    image = iconBitmap,
                    dstSize = IntSize.rect(sizePx.roundToInt())
                )
            }

            if (badgeBitmap != null) {
                val badgeDiameter = sizePx / 3f
                val badgeRadius = badgeDiameter / 2f

                val badgeCenter = Offset.rect(sizePx - badgeRadius)

                drawCircle(
                    color = colorScheme.tertiary,
                    radius = badgeRadius,
                    center = badgeCenter
                )

                val scale = badgeDiameter / badgeBitmap.width
                val scaledWidth = badgeBitmap.width * scale
                val scaledHeight = badgeBitmap.height * scale

                drawImage(
                    image = badgeBitmap,
                    dstOffset =
                        IntOffset(
                            x = (badgeCenter.x - scaledWidth / 2f).roundToInt(),
                            y = (badgeCenter.y - scaledHeight / 2f).roundToInt()
                        ),
                    dstSize =
                        IntSize(
                            width = scaledWidth.roundToInt(),
                            height = scaledHeight.roundToInt()
                        ),
                    colorFilter = ColorFilter.tint(colorScheme.onTertiary)
                )
            }
        }
    } else {
        unavailableAction(point, drawParams, center)
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
