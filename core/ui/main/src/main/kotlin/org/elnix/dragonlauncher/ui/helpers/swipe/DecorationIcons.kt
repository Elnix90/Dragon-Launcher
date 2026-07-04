package org.elnix.dragonlauncher.ui.helpers.swipe

import android.content.Context
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.DragonCache
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.util.ImageUtils.loadDrawableResAsImageBitmap
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.points.PointsService
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState


@Suppress("FunctionName")
fun DrawScope.DecorationIcons(
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

private object DecorationCache : DragonCache<Int, ImageBitmap>(2)


@Deprecated("Using the drawscope one")
@Composable
private fun BoxScope.DecorationIcons(
    showConfiguratorDecorations: Boolean,
    point: Point,
    pointsViewModel: PointsViewModel = activityViewModel()
) {
    val pointsService: PointsService = pointsViewModel.pointsService
    val defaultPoint: Point by pointsService.defaultPoint.asState()

    val badgeSize: Dp = remember(defaultPoint, point.size) {
        val size: Dp = point.getSize(defaultPoint)
        (size / 3f).coerceIn(14.dp, 36.dp)
    }

    // Small `+1` icon top left to indicate a Cycle Actions
    if (showConfiguratorDecorations && !point.cycleActions.isNullOrEmpty()) {
        Icon(
            painter = painterResource(R.drawable.ic_plus_one),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(badgeSize)
        )
    }

    // Small bolt icon top right to indicate a Hold & Run
    if (showConfiguratorDecorations && point.holdAndRunDelayMs != null) {
        Icon(
            painter = painterResource(R.drawable.ic_hold_and_run_bolt),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(badgeSize)
        )
    }
}
