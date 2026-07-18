package org.elnix.dragonlauncher.ui.helpers.swipe

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.CustomGlow
import org.elnix.dragonlauncher.ui.helpers.customobjects.drawNeonGlowLine

private val lineSize = 20.dp
public fun DrawScope.centerOfNest(center: Offset) {
    val linePx = lineSize.toPx()

    val horizontalStart = Offset(center.x - linePx, center.y)
    val horizontalEnd = Offset(center.x + linePx, center.y)

    val verticalStart = Offset(center.x, center.y - linePx)
    val verticalEnd = Offset(center.x, center.y + linePx)

    drawNeonGlowLine(
        start = horizontalStart,
        end = horizontalEnd,
        color = Color.Red,
        lineStrokeWidth = 1f,
        erase = false,
        eraseColor = null,
        glow = CustomGlow(5f)
    )

    drawNeonGlowLine(
        start = verticalStart,
        end = verticalEnd,
        color = Color.Red,
        lineStrokeWidth = 1f,
        erase = false,
        eraseColor = null,
        glow = CustomGlow(5f)
    )
}
