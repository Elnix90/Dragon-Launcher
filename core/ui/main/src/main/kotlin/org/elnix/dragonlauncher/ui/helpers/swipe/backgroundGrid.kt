package org.elnix.dragonlauncher.ui.helpers.swipe

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope



public fun DrawScope.backgroundGrid(
    cellSizePx: Float,
    color: Color? = null
) {
    val lineWidth = 1f
    val color = color ?: Color.White.copy(alpha = 0.25f)

    // Vertical lines
    var x = 0f
    while (x <= size.width) {
        drawLine(
            color = color,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = lineWidth
        )
        x += cellSizePx
    }

    // Horizontal lines
    var y = 0f
    while (y <= size.height) {
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = lineWidth
        )
        y += cellSizePx
    }
}