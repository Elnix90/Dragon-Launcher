package org.elnix.dragonlauncher.ui.helpers.customobjects

import android.graphics.BlurMaskFilter
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.CustomGlow


public fun DrawScope.glowOverlay(
    center: Offset,
    glow: CustomGlow
) {
    toPxOrNull(glow.radius)?.let { radius ->
        drawIntoCanvas { canvas ->

            val frameworkPaint = customGlowPaint(glow.color ?: Color.White, radius)

            canvas.nativeCanvas.drawCircle(
                center.x,
                center.y,
                radius,
                frameworkPaint
            )
        }
    }
}

@Composable
public fun GlowOverlay(
    center: Offset,
    glow: CustomGlow
) {
    Canvas(Modifier.fillMaxSize()) {
        glowOverlay(
            center = center,
            glow = glow
        )
    }
}


public fun DrawScope.drawNeonGlowLine(
    start: Offset,
    end: Offset,
    color: Color,
    lineStrokeWidth: Float,
    glow: CustomGlow?,
    erase: Boolean,
    eraseColor: Color?
) {
    toPxOrNull(glow?.radius)?.let { radius ->
        drawIntoCanvas { canvas ->
            val frameworkPaint = customGlowPaint(glow.color ?: color, radius)

            canvas.nativeCanvas.drawLine(
                start.x,
                start.y,
                end.x,
                end.y,
                frameworkPaint
            )
        }
    }


    if (lineStrokeWidth >= 0f) {
        val width = lineStrokeWidth.dp.toPx()
        if (erase) {
            drawLine(
                color = eraseColor ?: Color.Transparent,
                start = start,
                end = end,
                strokeWidth = width,
                cap = StrokeCap.Round,
                blendMode = BlendMode.Dst
            )
        }

        drawLine(
            color = color,
            start = start,
            end = end,
            strokeWidth = width,
            cap = StrokeCap.Round
        )
    }
}

public fun DrawScope.drawPathGlow(
    path: Path,
    color: Color,
    lineStrokeWidth: Float,
    glow: CustomGlow?,
    erase: Boolean,
    eraseColor: Color?
) {

    val nativePath = path.asAndroidPath()

    toPxOrNull(glow?.radius)?.let { radius ->
        drawIntoCanvas { canvas ->
            val frameworkPaint = customGlowPaint(glow.color ?: color, radius)
            canvas.nativeCanvas.drawPath(nativePath, frameworkPaint)
        }
    }

    val width = lineStrokeWidth * this.density

    val style = when {
        lineStrokeWidth == -1f -> return
        lineStrokeWidth < 0f -> Fill
        lineStrokeWidth == 0.0f -> Stroke(Stroke.HairlineWidth)
        else -> Stroke(width)
    }

    if (erase) {
        drawPath(
            path = path,
            color = eraseColor ?: Color.Transparent,
            style = Fill,
            blendMode = BlendMode.Src
        )
    }

    drawPath(
        path = path,
        color = color,
        style = style
    )
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
