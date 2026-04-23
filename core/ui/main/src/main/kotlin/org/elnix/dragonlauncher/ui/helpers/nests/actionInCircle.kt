package org.elnix.dragonlauncher.ui.helpers.nests

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.serializables.IconShape
import org.elnix.dragonlauncher.common.serializables.SwipeActionSerializable
import org.elnix.dragonlauncher.common.serializables.SwipePointSerializable
import org.elnix.dragonlauncher.common.serializables.SwipePointSerializable.Companion.defaultSwipePointsValues
import org.elnix.dragonlauncher.common.serializables.applyColorAction
import org.elnix.dragonlauncher.common.utils.ImageUtils.loadDrawableResAsBitmap
import org.elnix.dragonlauncher.common.utils.UiCircle
import org.elnix.dragonlauncher.common.utils.cycleLayerIconCacheKey
import org.elnix.dragonlauncher.common.utils.resolveShape
import org.elnix.dragonlauncher.ui.actions.actionColor
import org.elnix.dragonlauncher.ui.base.cache.SwipeDrawParams
import org.elnix.dragonlauncher.ui.helpers.customobjects.shapeToPath


fun DrawScope.actionsInCircle(
    drawParams: SwipeDrawParams,

    center: Offset,
    depth: Int,
    point: SwipePointSerializable,
    selected: Boolean,
    preventBgErasing: Boolean = false,
    preventDrawingSubNests: Boolean = false,
    /** Cycle stack + Hold & Run bolt — only in settings / edit previews, not on the home overlay. */
    showConfiguratorDecorations: Boolean = false,
) {
    val ctx = drawParams.ctx
    val nests = drawParams.nests
    val defaultPoint = drawParams.defaultPoint
    val icons = drawParams.icons
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


    /*  ─────────────  Stroke computation  ─────────────  */
    val borderStroke = if (selected) {
        point.borderStrokeSelected ?: defaultPoint.borderStrokeSelected ?: 8f
    } else {
        point.borderStroke ?: defaultPoint.borderStroke ?: 4f
    }

    /*  ─────────────  Foreground / background colors computation  ─────────────  */
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

    /*  ─────────────  Shapes computation  ─────────────  */
    val borderIconShape = if (selected) {
        point.borderShapeSelected ?: defaultPoint.borderShapeSelected
    } else {
        point.borderShape ?: defaultPoint.borderShape
    } ?: IconShape.Circle


    // Prevent overloading since the drawing is recursive
    if (depth <= maxDepth) {

        if (action !is SwipeActionSerializable.OpenCircleNest || point.customIcon != null) {


            // if no background color provided, erases the background
            val eraseBg = backgroundColor == Color.Transparent && !preventBgErasing

            val iconSizeF = borderRadii * 2f
            val iconSize = Size(iconSizeF, iconSizeF)


            val borderShape = borderIconShape.resolveShape()


            /** The path is cached by (shape, size) and reused across frames to avoid
             * re-allocating and re-computing the outline on every draw call.
             *See [org.elnix.dragonlauncher.ui.base.cache.DrawPathCache] for eviction strategy and sizing guidance.
             */
            val path = drawParams.drawPathCache.getOrCompute(borderIconShape, iconSize) {
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

//            val colorAction = actionColor(point.action, extraColors)
//
//            val icon = icons[point.id]
//            if (icon != null) {
//                drawImage(
//                    image = icon,
//                    dstOffset = dstOffset,
//                    dstSize = intSize,
//                    colorFilter =
//                        if (point.applyColorAction()) ColorFilter.tint(colorAction)
//                        else null
//                )
//            }

            val colorAction = actionColor(point.action, extraColors)

            val persisted = drawParams.points.find { it.id == point.id } ?: point
            // Edit dialog passes live [point] state; nest list can lag until save.
            val modelForCycle = if (showConfiguratorDecorations) point else persisted
            val cycleStages = modelForCycle.cycleActions

            // 4. Icon — home overlay: single bitmap only. Settings / edit: optional cycle stack + bolt.
            if (showConfiguratorDecorations && !cycleStages.isNullOrEmpty()) {
                val n = cycleStages.size
                val stepPx = (intSize.width * 0.08f).toInt().coerceIn(4, 14)
                val cacheId = point.id
                val shadowAlpha = 0.45f
                val shadowShift = (stepPx / 5).coerceAtLeast(1)

                for (i in n downTo 1) {
                    val bmp = icons[cycleLayerIconCacheKey(cacheId, i)] ?: continue
                    val layerAction = cycleStages[i - 1].action
                    val layerPoint = modelForCycle.copy(action = layerAction)
                    val layerOffset = dstOffset + IntOffset(i * stepPx * 4, i * stepPx * 2)

                    // 1. Layer shadow
                    drawImage(
                        image = bmp,
                        dstOffset = layerOffset + IntOffset(shadowShift, shadowShift),
                        dstSize = intSize,
                        colorFilter = ColorFilter.tint(Color.Black.copy(alpha = shadowAlpha))
                    )

                    // 2. Layer icon
                    drawImage(
                        image = bmp,
                        dstOffset = layerOffset,
                        dstSize = intSize,
                        colorFilter =
                            if (layerPoint.applyColorAction()) ColorFilter.tint(
                                actionColor(layerAction, extraColors)
                            )
                            else null
                    )
                }

                val baseBmp = icons[cycleLayerIconCacheKey(cacheId, 0)] ?: icons[point.id]
                if (baseBmp != null) {
                    // 1. Base shadow
                    drawImage(
                        image = baseBmp,
                        dstOffset = dstOffset + IntOffset(shadowShift, shadowShift),
                        dstSize = intSize,
                        colorFilter = ColorFilter.tint(Color.Black.copy(alpha = shadowAlpha))
                    )

                    // 2. Base icon
                    drawImage(
                        image = baseBmp,
                        dstOffset = dstOffset,
                        dstSize = intSize,
                        colorFilter =
                            if (modelForCycle.applyColorAction()) ColorFilter.tint(
                                actionColor(modelForCycle.action, extraColors)
                            )
                            else null
                    )
                }
            } else {
                val icon = point.id.let { icons[it] }
                if (icon != null) {
                    drawImage(
                        image = icon,
                        dstOffset = dstOffset,
                        dstSize = intSize,
                        colorFilter =
                            if (point.applyColorAction()) ColorFilter.tint(colorAction)
                            else null
                    )
                }
            }

            if (showConfiguratorDecorations && point.holdAndRunDelayMs != null) {
                val iconPx = intSize.width
                val badgeSize = (iconPx / 3f).toInt().coerceIn(14, 36)
                val bolt = ctx.loadDrawableResAsBitmap(
                    R.drawable.ic_hold_and_run_bolt,
                    badgeSize,
                    badgeSize
                )
                val leftI = px.toInt() - iconPx / 2
                val topI = py.toInt() - iconPx / 2
                val boltLeft = leftI + iconPx - badgeSize
                val boltTop = topI - (badgeSize / 4).coerceAtLeast(1)
                val shadowDy = (badgeSize / 6).coerceAtLeast(2)
                drawImage(
                    image = bolt,
                    dstOffset = IntOffset(boltLeft, boltTop + shadowDy),
                    dstSize = IntSize(badgeSize, badgeSize),
                    colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.42f)),
                )
                drawImage(
                    image = bolt,
                    dstOffset = IntOffset(boltLeft, boltTop),
                    dstSize = IntSize(badgeSize, badgeSize)
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
                image = ctx.loadDrawableResAsBitmap(R.drawable.ic_action_target, 48, 48),
                dstOffset = dstOffset,
                dstSize = intSize
            )
        }
    }
}
