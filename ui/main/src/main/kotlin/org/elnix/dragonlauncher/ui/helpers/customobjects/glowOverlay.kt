@file:Suppress("NOTHING_TO_INLINE")

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import org.elnix.dragonlauncher.base.model.serializables.CustomGlow
import org.elnix.dragonlauncher.ui.helpers.customobjects.GlowDrawOrder.AfterErase
import org.elnix.dragonlauncher.ui.helpers.customobjects.GlowDrawOrder.First
import org.elnix.dragonlauncher.ui.helpers.customobjects.GlowDrawOrder.Last
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


enum class GlowDrawOrder {
    First,
    AfterErase,
    Last
}

fun DrawScope.glowOverlay(
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
fun GlowOverlay(
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


fun DrawScope.drawNeonGlowLine(
    start: Offset,
    end: Offset,
    color: Color,
    lineStrokeWidth: Float,
    glow: CustomGlow?,
    drawOrder: GlowDrawOrder = First,
    erase: Boolean,
    eraseColor: Color?
) {
    when(drawOrder) {
        First -> {
            glowLine(glow, color, start, end)
            line(lineStrokeWidth, erase, eraseColor, start, end, color)
        }
        AfterErase, Last -> {
            line(lineStrokeWidth, erase, eraseColor, start, end, color)
            glowLine(glow, color, start, end)
        }
    }
}

private fun DrawScope.line(
    lineStrokeWidth: Float,
    erase: Boolean,
    eraseColor: Color?,
    start: Offset,
    end: Offset,
    color: Color
) {
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

private inline fun DrawScope.glowLine(
    glow: CustomGlow?,
    color: Color,
    start: Offset,
    end: Offset
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
}

fun DrawScope.drawPathGlow(
    path: Path,
    color: Color,
    lineStrokeWidth: Float,
    glow: CustomGlow?,
    drawOrder: GlowDrawOrder = First,
    erase: Boolean,
    eraseColor: Color?
) {

    when (drawOrder) {
        First -> {
            glow(glow, path, color)
            erasePath(path, erase, eraseColor)
            path(lineStrokeWidth, path, color)
        }

        AfterErase -> {
            erasePath(path, erase, eraseColor)
            glow(glow, path, color)
            path(lineStrokeWidth, path, color)
        }

        Last -> {
            erasePath(path, erase, eraseColor)
            path(lineStrokeWidth, path, color)
            glow(glow, path, color)
        }
    }
}


private inline fun DrawScope.glow(
    glow: CustomGlow?,
    path: Path,
    color: Color
) {
    val nativePath = path.asAndroidPath()

    toPxOrNull(glow?.radius)?.let { radius ->
        drawIntoCanvas { canvas ->
            val frameworkPaint = customGlowPaint(glow.color ?: color, radius)
            canvas.nativeCanvas.drawPath(nativePath, frameworkPaint)
        }
    }
}

private inline fun DrawScope.path(lineStrokeWidth: Float, path: Path, color: Color) {
    val style = when {
        lineStrokeWidth == -1f -> return
        lineStrokeWidth < 0f -> Fill
        lineStrokeWidth == 0.0f -> Stroke(Stroke.HairlineWidth, cap = StrokeCap.Round)
        else -> Stroke(lineStrokeWidth, cap = StrokeCap.Round)
    }

    drawPath(
        path = path,
        color = color,
        style = style
    )
}

private inline fun DrawScope.erasePath(
    path: Path,
    erase: Boolean,
    eraseColor: Color?
) {
    if (erase) {
        drawPath(
            path = path,
            color = eraseColor ?: Color.Transparent,
            style = Fill,
            blendMode = BlendMode.Src
        )
    }
}


private inline fun customGlowPaint(
    glowColor: Color,
    glowPx: Float
): Paint {
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

/**
 * Converts the given [value] to pixels only if it is higher than 0 or returns `null`
 * @param value the [Float] you want to convert to pixels
 * @return the [value] in pixels of `null`
 */
@OptIn(ExperimentalContracts::class)
fun DrawScope.toPxOrNull(value: Dp?): Float? {
    contract {
        returnsNotNull() implies (value != null)
    }

    return value?.takeIf { it.isSpecified && it.value > 0f }?.toPx()
}