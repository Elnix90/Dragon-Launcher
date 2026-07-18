package org.elnix.dragonlauncher.ui.helpers.swipe

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.DragonCache
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.util.ImageUtils.loadDrawableResAsImageBitmap
import org.elnix.dragonlauncher.i18n.R


@Suppress("FunctionName")
public fun DrawScope.DecorationIcons(
    ctx: Context,
    center: Offset,
    iconSize: Size,
    point: Point
) {

    val px = center.x
    val py = center.y

    val iconPx = iconSize.width.toInt()
    val badgeSize = (iconPx / 3f).toInt().coerceIn(14, 36)
    val badgeDtsSize = IntSize(badgeSize, badgeSize)

    // Small `+1` icon top left to indicate a Cycle Actions
    if (!point.cycleActions.isNullOrEmpty()) {

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
            dstSize = badgeDtsSize
        )
    }


    // Small bolt icon top right to indicate a Hold & Run
    if (point.holdAndRunDelayMs != null) {
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
            dstSize = badgeDtsSize
        )
    }
}



public fun DrawScope.missingPoint(drawParams: DrawParams, center: Offset) {
    val size = 30.dp.toPx().toInt()

    val boltIcon =  DecorationCache.getOrCompute(3) {
        drawParams.ctx.loadDrawableResAsImageBitmap(
            resId = R.drawable.question_mark,
            width = size,
            height = size
        )
    }

    val leftI = center.x.toInt() - size / 2
    val topI = center.y.toInt() - size / 2

    drawImage(
        image = boltIcon,
        dstOffset = IntOffset(leftI, topI),
        dstSize = IntSize(size, size),
        colorFilter = ColorFilter.tint(Color.White)
    )
}

private object DecorationCache : DragonCache<Int, ImageBitmap>(3)