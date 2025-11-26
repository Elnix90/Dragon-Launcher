package org.elnix.dragonlauncher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.text.font.FontWeight

@Composable
fun MainScreenOverlay(
    start: Offset?,
    current: Offset?,
    isDragging: Boolean,
    surface: IntSize
) {
    val dx: Float
    val dy: Float
    val angleRad: Double
    val angleDeg: Double
    val angle0to360: Double

    if (start != null && current != null) {
        dx = current.x - start.x
        dy = current.y - start.y

        // angle relative to UP = 0°
        angleRad = atan2(dx.toDouble(), -dy.toDouble())
        angleDeg = Math.toDegrees(angleRad)
        angle0to360 = if (angleDeg < 0) angleDeg + 360 else angleDeg
    } else {
        dx = 0f; dy = 0f
        angleRad = 0.0
        angleDeg = 0.0
        angle0to360 = 0.0
    }

    Box(Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Text("start = ${start?.let { "%.1f, %.1f".format(it.x, it.y) } ?: "—"}",
                color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text("current = ${current?.let { "%.1f, %.1f".format(it.x, it.y) } ?: "—"}",
                color = Color.White, fontSize = 14.sp)
            Text("dx = %.1f   dy = %.1f".format(dx, dy),
                color = Color.White, fontSize = 14.sp)
            Text("angle raw = %.1f°".format(angleDeg),
                color = Color.White, fontSize = 14.sp)
            Text("angle 0–360 = %.1f°".format(angle0to360),
                color = Color.White, fontSize = 14.sp)
            Text("drag = $isDragging, size = ${surface.width}×${surface.height}",
                color = Color.White, fontSize = 12.sp)
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            if (start != null) {
                val circleRadius = 48f
                drawCircle(
                    color = Color.Red,
                    radius = circleRadius,
                    center = start,
                    style = Stroke(width = 3f)
                )

                if (current != null) {
                    drawLine(
                        color = Color.Red,
                        start = start,
                        end = current,
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )

                    drawCircle(
                        color = Color.Red,
                        radius = 8f,
                        center = current,
                        style = Fill
                    )

                    val arcRadius = 72f
                    val rect = Rect(
                        start.x - arcRadius,
                        start.y - arcRadius,
                        start.x + arcRadius,
                        start.y + arcRadius
                    )

                    val sweep = angleDeg.toFloat()

                    drawArc(
                        color = Color.Red,
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = rect.topLeft,
                        size = Size(rect.width, rect.height),
                        style = Stroke(width = 3f)
                    )

                    val endX = start.x + arcRadius * sin(angleRad).toFloat()
                    val endY = start.y - arcRadius * cos(angleRad).toFloat()

                    drawLine(
                        color = Color.Red,
                        start = start,
                        end = Offset(endX, endY),
                        strokeWidth = 2f
                    )
                }
            }
        }
    }
}
