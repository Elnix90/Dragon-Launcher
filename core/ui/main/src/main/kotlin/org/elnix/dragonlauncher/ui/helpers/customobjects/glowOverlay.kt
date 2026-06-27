package org.elnix.dragonlauncher.ui.helpers.customobjects

import android.graphics.BlurMaskFilter
import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.ktx.px


fun DrawScope.glowOverlay(
    center: Offset,
    color: Color,
    radius: Float
) {
    if (radius > 0f) {
        drawCircle(
            brush = Brush.radialGradient(
                0.0f to color,
                1.0f to Color.Transparent,
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )
    }
}



@Composable
fun GlowOverlay(
    center: Offset,
    color: Color,
    radius: Dp
) {
    if (radius > 0.dp) {
        Box(
            modifier = Modifier
                .size(radius)
                .background(
                    brush = Brush.radialGradient(
                        0.0f to color,
                        1.0f to Color.Transparent,
                        center = center,
                        radius = radius.px
                    )
                )
        )
    }
}


fun DrawScope.drawNeonGlowLine(
    start: Offset,
    end: Offset,
    color: Color,
    lineStrokeWidth: Float,
    glowRadius: Float,
    glowColor: Color?,
    erase: Boolean
) {

    // Glow overlay (behind)
    if (glowRadius > 0f) {
        drawIntoCanvas { canvas ->
            val frameworkPaint = customGlowPaint(glowColor ?: color, glowRadius)

            canvas.nativeCanvas.drawLine(
                start.x,
                start.y,
                end.x,
                end.y,
                frameworkPaint
            )
        }
    }

    if (lineStrokeWidth > 0f) {
        if (erase) {
            drawLine(
                color = Color.Transparent,
                start = start,
                end = end,
                strokeWidth = lineStrokeWidth,
                cap = StrokeCap.Round,
                blendMode = BlendMode.Clear
            )
        }

        // Sharp center line
        drawLine(
            color = color,
            start = start,
            end = end,
            strokeWidth = lineStrokeWidth,
            cap = StrokeCap.Round
        )
    }
}

fun DrawScope.drawNeonGlowShapePath(
    path: Path,
    color: Color,
    lineStrokeWidth: Float,
    glowRadius: Float,
    glowColor: Color?,
    erase: Boolean
) {

    val nativePath = path.asAndroidPath()

    if (glowRadius > 0f) {
        drawIntoCanvas { canvas ->
            val frameworkPaint = customGlowPaint(glowColor ?: color, glowRadius)
            canvas.nativeCanvas.drawPath(nativePath, frameworkPaint)
        }
    }

    if (lineStrokeWidth > 0f) {
        if (erase) {
            drawPath(
                path = path,
                color = Color.Transparent,
                style = Stroke(width = lineStrokeWidth, cap = StrokeCap.Round),
                blendMode = BlendMode.Clear
            )
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = lineStrokeWidth, cap = StrokeCap.Round)
        )
    }
}

private fun customGlowPaint(glowColor: Color, glowPx: Float): Paint {
    require(glowPx > 0f) { "Glow px < 0f: $glowPx" }
    return Paint().apply {
        this.color = glowColor.copy(alpha = 0.7f).toArgb()
        style = Paint.Style.STROKE
        strokeWidth = glowPx
        maskFilter = BlurMaskFilter(
            glowPx,
            BlurMaskFilter.Blur.NORMAL
        )
        isAntiAlias = true
    }
}
