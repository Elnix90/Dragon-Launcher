package org.elnix.dragonlauncher.ui.helpers

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import org.elnix.dragonlauncher.data.SwipeActionSerializable
import org.elnix.dragonlauncher.utils.actions.actionIconBitmap


fun actionsInCircle(
    drawScope: DrawScope,
    action: SwipeActionSerializable,
    circleColor: Color,
    backgroundColor: Color,
    colorAction: Color,
    px: Float,
    py: Float,
    ctx: Context
) {
    drawScope.drawCircle(
        color = circleColor,
        radius = 44f,
        center = Offset(px, py)
    )

    drawScope.drawCircle(
        color = backgroundColor,
        radius = 40f,
        center = Offset(px, py)
    )

    drawScope.drawImage(
        image = actionIconBitmap(
            action = action,
            context = ctx,
            tintColor = colorAction
        ),
        dstOffset = IntOffset(px.toInt() - 28, py.toInt() - 28),
        dstSize = IntSize(56, 56)
    )
}
