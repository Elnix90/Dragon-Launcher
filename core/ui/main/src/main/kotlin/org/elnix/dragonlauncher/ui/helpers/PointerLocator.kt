package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.drawText
import org.elnix.dragonlauncher.ui.helpers.swipe.cache.nests.DrawScopeText

fun DrawScope.PointerLocation(
    offset: Offset,
    color: Color = Color.White,
    stroke: Float = 4f,
    circleCenter: Boolean = true,
    centerText: DrawScopeText? = null
) {
    drawIntoCanvas { canvas ->
        canvas.save()

        drawLine(
            color = color,
            start = offset.copy(x = 0f),
            end = offset.copy(x = size.width),
            strokeWidth = stroke,
        )

        drawLine(
            color = color,
            start = offset.copy(y = 0f),
            end = offset.copy(y = size.height),
            strokeWidth = stroke,
        )

        if (circleCenter) {
            drawCircle(
                color = color,
                radius = 100f,
                center = offset,
                blendMode = BlendMode.Clear,
            )

            drawCircle(
                color = color,
                radius = 100f,
                center = offset,
                style = Stroke(stroke)
            )
        }

        if (centerText != null) {
            drawText(
                textLayoutResult = centerText.offsetTextLayoutResult,
                topLeft = offset - centerText.topLeft,
                color = color
            )
        }

        canvas.restore()
    }
}