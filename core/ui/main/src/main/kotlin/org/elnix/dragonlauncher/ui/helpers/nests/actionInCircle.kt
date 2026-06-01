package org.elnix.dragonlauncher.ui.helpers.nests

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.models.UiCircle
import org.elnix.dragonlauncher.base.resolveShape
import org.elnix.dragonlauncher.common.serializables.IconShape

.Companion.actionColor
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.model.serializables.Point.Companion.applyColorAction
import org.elnix.dragonlauncher.base.model.serializables.Point.Companion.defaultSwipePointsValues
import org.elnix.dragonlauncher.base.util.ImageUtils.loadDrawableResAsImageBitmap
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.base.DragonCache
import org.elnix.dragonlauncher.base.model.models.SwipeDrawParams
import org.elnix.dragonlauncher.ui.helpers.customobjects.shapeToPath
import org.elnix.dragonlauncher.ui.remembers.DrawPathCache


fun DrawScope.actionsInCircle(
    drawParams: SwipeDrawParams,

    center: Offset,
    depth: Int,
    point: Point,
    selected: Boolean,
    preventBgErasing: Boolean = false,
    preventDrawingSubNests: Boolean = false,
    /** Cycle stack + Hold & Run bolt — only in settings / edit previews, not on the home overlay. */
    showConfiguratorDecorations: Boolean = false,
) {
    val ctx = drawParams.ctx
    val nests = drawParams.nests
    val defaultPoint = drawParams.defaultPoint
    val surfaceColorDraw = drawParams.surfaceColorDraw
    val extraColors = drawParams.extraColors
    val maxDepth = drawParams.maxDepth
    val subNestDefaultRadius = drawParams.subNestDefaultRadius

    val action = point.action

    val px = center.x
    val py = center.y


    // Now uses density pixels to display for consistent drawing across device
    val sizePx = (point.size ?: defaultPoint.size ?: defaultSwipePointsValues.size!!)
        .coerceAtLeast(1)
        .dp
        .toPx()
        .toInt()

    val innerPaddingPx = (point.innerPadding ?: defaultPoint.innerPadding ?: defaultSwipePointsValues.innerPadding!!)
        .dp
        .toPx()
        .toInt()

    val iconSize = sizePx / depth
    val borderRadii = ((sizePx / 2 + innerPaddingPx).coerceAtLeast(0) / depth).toFloat()

    val dstOffset = IntOffset(px.toInt() - iconSize / 2, py.toInt() - iconSize / 2)
    val intSize = IntSize(iconSize, iconSize)


    val borderStroke = if (selected) {
        point.borderStrokeSelected ?: defaultPoint.borderStrokeSelected ?: 8f
    } else {
        point.borderStroke ?: defaultPoint.borderStroke ?: 4f
    }

    val borderColor = if (selected) {
        point.borderColorSelected?.let { Color(it) }
            ?: defaultPoint.borderColorSelected?.let { Color(it) }
    } else {
        point.borderColor?.let { Color(it) } ?: defaultPoint.borderColor?.let { Color(it) }
    } ?: extraColors.circle

    val backgroundColor = if (selected) {
        point.backgroundColorSelected?.let { Color(it) }
            ?: defaultPoint.backgroundColorSelected?.let { Color(it) }
    } else {
        point.backgroundColor?.let { Color(it) } ?: defaultPoint.backgroundColor?.let { Color(it) }
    } ?: if (preventBgErasing) {
        surfaceColorDraw
    } else {
        Color.Transparent
    }

    val borderIconShape = if (selected) {
        point.borderShapeSelected ?: defaultPoint.borderShapeSelected
    } else {
        point.borderShape ?: defaultPoint.borderShape
    } ?: IconShape.Circle


    // Prevent overloading since the drawing is recursive
    if (depth <= maxDepth) {

        if (action !is Action.OpenCircleNest || point.customIcon != null) {


            // if no background color provided, erases the background
            val eraseBg = backgroundColor == Color.Transparent && !preventBgErasing

            val iconSizeF = borderRadii * 2f
            val iconSize = Size(iconSizeF, iconSizeF)


            val borderShape = borderIconShape.resolveShape()



            val path = DrawPathCache.getOrCompute(Pair(borderIconShape, iconSize)) {
                shapeToPath(borderShape, iconSize)
            }

            /**
             * The cached path is always origin-centered (top-left at 0,0).
             * Instead of translating the path itself — which would require a new Path
             * allocation — we translate the canvas matrix directly. save/restore is a
             * pure matrix stack operation with zero allocations, and unlike withTransform,
             * it does not create an offscreen layer that would intercept BlendMode.Clear.
             */
            val tx = center.x - iconSize.width / 2f
            val ty = center.y - iconSize.height / 2f

            drawContext.canvas.save()
            drawContext.canvas.translate(tx, ty)

            // 1. Erases the color, instead of putting it, that lets the wallpaper pass through
            drawPath(
                path = path,
                color = Color.Transparent,
                style = Fill,
                blendMode = BlendMode.Clear
            )

            // 2. If requested to not erase the bg, draw it (this avoids the more tinted bg when using a half transparent bg color
            if (!eraseBg) {
                drawPath(
                    path = path,
                    color = backgroundColor,
                    style = Fill
                )
            }

            // 3. Draws the border
            if (borderStroke > 0f) {
                if (borderColor.alpha != 0f) {

                    drawPath(
                        path = path,
                        color = borderColor,
                        style = Stroke(width = borderStroke)
                    )
                }
            }

            drawContext.canvas.restore()


            decorationIcons(
                ctx = ctx,
                center = center,
                intSize = intSize,
                point = point,
                showConfiguratorDecorations = showConfiguratorDecorations
            )


            // The actual app icon
            val icon = drawParams.pointsIconsCache.getOrLazyCompute(point.key) {
                drawParams.computeIcon(point)
            }

            if (icon != null) {
                val colorAction = point.action.actionColor(extraColors)
                drawImage(
                    image = icon,
                    dstOffset = dstOffset,
                    dstSize = intSize,
                    colorFilter =
                        if (point.applyColorAction()) ColorFilter.tint(colorAction)
                        else null
                )
            }

        } else {
            nests
                .find { it.id == action.nestId }
                ?.takeIf { !preventDrawingSubNests }
                ?.let { nest ->

                    val circlesWidthIncrement = 1f / (nest.dragDistances.size - 1)

                    val newCircles = mutableListOf<UiCircle>()

                    val subRadius = nest.nestRadius?.dp?.toPx() ?: subNestDefaultRadius

                    nest.dragDistances.filter { it.key != -1 }.forEach { (index, _) ->
                        val radius = (subRadius / depth) * circlesWidthIncrement * (index + 1)
                        newCircles.add(
                            UiCircle(index, radius)
                        )
                    }

                    circlesSettingsOverlay(
                        drawParams = drawParams,

                        center = center,
                        depth = depth + 1,

                        circles = newCircles,
                        selectedPoint = point,
                        nestId = nest.id,
                        selectedAll = selected,
                        preventBgErasing = preventBgErasing,
                        showConfiguratorDecorations = showConfiguratorDecorations
                    )
                } ?: drawImage(
                /**
                 *  If this is drawn there is either a big bug, it means no nests was found, and shouldn't happen,
                 *  or that the user is in the edit nest screen and that the nest should not be recursively drawn
                 */
                image = ctx.loadDrawableResAsImageBitmap(R.drawable.ic_action_target, 48, 48),
                dstOffset = dstOffset,
                dstSize = intSize
            )
        }
    }
}

private object DecorationCache : DragonCache<Int, ImageBitmap>(2)


private fun DrawScope.decorationIcons(
    ctx: Context,
    center: Offset,
    intSize: IntSize,
    showConfiguratorDecorations: Boolean,
    point: Point
) {

    val px = center.x
    val py = center.y

    // Small `+1` icon top left to indicate a Cycle Actions
    if (showConfiguratorDecorations && !point.cycleActions.isNullOrEmpty()) {
        val iconPx = intSize.width
        val badgeSize = (iconPx / 3f).toInt().coerceIn(14, 36)

        val plusOneIcon = DecorationCache.getOrCompute(0) {
            ctx.loadDrawableResAsImageBitmap(
                R.drawable.ic_plus_one,
                badgeSize,
                badgeSize
            )
        }

        val leftI = px.toInt() - iconPx / 2
        val topI = py.toInt() - iconPx / 2
        val plusOneTop = topI - (badgeSize / 4).coerceAtLeast(1)

        drawImage(
            image = plusOneIcon,
            dstOffset = IntOffset(leftI, plusOneTop),
            dstSize = IntSize(badgeSize, badgeSize)
        )
    }


    // Small bolt icon top right to indicate a Hold & Run
    if (showConfiguratorDecorations && point.holdAndRunDelayMs != null) {

        val iconPx = intSize.width
        val badgeSize = (iconPx / 3f).toInt().coerceIn(14, 36)
        val boltIcon =  DecorationCache.getOrCompute(1) {
            ctx.loadDrawableResAsImageBitmap(
                R.drawable.ic_hold_and_run_bolt,
                badgeSize,
                badgeSize
            )
        }

        val leftI = px.toInt() - iconPx / 2
        val topI = py.toInt() - iconPx / 2
        val boltLeft = leftI + iconPx - badgeSize
        val boltTop = topI - (badgeSize / 4).coerceAtLeast(1)

        drawImage(
            image = boltIcon,
            dstOffset = IntOffset(boltLeft, boltTop),
            dstSize = IntSize(badgeSize, badgeSize)
        )
    }
}
