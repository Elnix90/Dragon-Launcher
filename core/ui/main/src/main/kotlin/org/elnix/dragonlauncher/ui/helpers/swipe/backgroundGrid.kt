package org.elnix.dragonlauncher.ui.helpers.swipe

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import org.elnix.dragonlauncher.ktx.alphaMultiplier

public fun DrawScope.backgroundGrid(
    cellSizePx: Float,
    color: Color,
    size: Size = this.size,
) {
    val color = color.alphaMultiplier(0.25f)

    // Vertical lines
    var x = 0f
    while (x <= size.width) {
        drawLine(
            color = color,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = Stroke.HairlineWidth
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
            strokeWidth = Stroke.HairlineWidth
        )
        y += cellSizePx
    }
}


/**
 * Draws a square grid of [cells] cells, centered in [center]
 *
 * @param center The center of the grid
 * @param cellSizePx the size if the cells
 * @param cells how many cells to draw in width and height (the grid is a square)
 * @param color what color the grid is drawn from (defaults to White with 0.25 alpha value)
 */
public fun DrawScope.backgroundCenteredSquareGrid(
    center: Offset,
    cellSizePx: Float,
    cells: Int,
    color: Color,
) {
    val cells = if (cells % 2 == 0) cells + 1 else cells
    val color = color.alphaMultiplier(0.25f)

    val size = (cells - 1) * cellSizePx
    val halfSize = size / 2f

    val topLeft = Offset(
        x = center.x - halfSize,
        y = center.y - halfSize
    )

    repeat(2) { pass ->
        val rotation = if (pass == 0) 0f else 90f
        rotate(rotation) {
            repeat(cells) { cell ->
                val x = topLeft.x + cell * cellSizePx
                drawLine(
                    color = color,
                    start = Offset(x, topLeft.y),
                    end = Offset(x, topLeft.y + size),
                    strokeWidth = Stroke.HairlineWidth
                )
            }
        }
    }
}