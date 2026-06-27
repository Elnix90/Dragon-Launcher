package org.elnix.dragonlauncher.ui.helpers.nests

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import org.elnix.dragonlauncher.base.cache.DrawPathCache
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.resolveShape
import org.elnix.dragonlauncher.ui.helpers.customobjects.shapeToPath


@Suppress("FunctionName")
fun DrawScope.PointBg(
    point: Point,
    center: Offset,
    selected: Boolean,
    depth: Int,

    drawParams: DrawParams,
    content: DrawScope.() -> Unit
) {
    val pointsService = drawParams.pointsService
    val defaultPoint = drawParams.defaultPoint
    val extraColors = drawParams.extraColors


    val offset = point.offset
    val px: Float = center.x + offset.x
    val py: Float = center.y + offset.y


    val sizePx: Float = point.getSize(defaultPoint).toPx()
    val innerPaddingPx: Float = point.getInnerPadding(defaultPoint).toPx()

    val borderRadii: Float = ((sizePx / 2 + innerPaddingPx).coerceAtLeast(0f) / depth)

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

    drawContext.canvas.restore()

    content()

    if (drawParams.showConfiguratorDecorations){
        DecorationIcons(
            ctx = drawParams.ctx,
            center = center,
            iconSize = iconSize,
            point = point
        )
    }
//    Box(
//        modifier = modifier
//            .size(size + innerPadding)
//            .offset(px, py)
//            .background(backgroundColor)
//            .border(
//                width = borderStroke.dp,
//                color = borderColor,
//                shape = borderIconShape.resolveShape()
//            )
//            .padding(innerPadding),
//        content = content
//    )
}

