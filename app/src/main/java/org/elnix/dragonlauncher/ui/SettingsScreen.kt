package org.elnix.dragonlauncher.ui


import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import kotlin.math.*

import org.elnix.dragonlauncher.data.SwipeActionSerializable

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var radius by remember { mutableFloatStateOf(0f) }
    var center by remember { mutableStateOf(Offset.Zero) }

    // Use a SnapshotStateList so Compose reacts to add/remove
    val points: SnapshotStateList<UiSwipePoint> = remember { mutableStateListOf() }

    Column(Modifier
        .fillMaxSize()
        .background(Color.Black)
        .padding(WindowInsets.systemBars.asPaddingValues())
        .imePadding()
    ) {
        IconButton(onClick = { onBack() }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(12.dp)
                .onSizeChanged {
                    radius = (minOf(it.width, it.height) * 0.35f)
                    center = Offset(it.width / 2f, it.height / 2f)
                }
        ) {

            // draw circle + points
            Canvas(Modifier.fillMaxSize()) {
                // circle
                drawCircle(
                    color = Color.Gray,
                    radius = radius,
                    center = center,
                    style = Stroke(3f)
                )

                // draw points
                points.forEach { p ->
                    val px = center.x + radius * sin(Math.toRadians(p.angleDeg)).toFloat()
                    val py = center.y - radius * cos(Math.toRadians(p.angleDeg)).toFloat()
                    drawCircle(Color.Red, radius = 12f, center = Offset(px, py))
                }
            }

            // Drag handling overlay
            Box(modifier = Modifier
                .matchParentSize()
                .pointerInput(points) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            // pick closest point within threshold (in px)
                            val threshold = 48f
                            var closest: UiSwipePoint? = null
                            var bestDist = Float.MAX_VALUE
                            points.forEach { p ->
                                val px = center.x + radius * sin(Math.toRadians(p.angleDeg)).toFloat()
                                val py = center.y - radius * cos(Math.toRadians(p.angleDeg)).toFloat()
                                val d = hypot(offset.x - px, offset.y - py)
                                if (d < bestDist) {
                                    bestDist = d
                                    closest = p
                                }
                            }
                            if (closest != null && bestDist <= threshold) {
                                points.forEach { it.isSelected = false }
                                closest.isSelected = true
                            } else {
                                points.forEach { it.isSelected = false }
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val selected = points.find { it.isSelected }
                            if (selected != null) {
                                // compute angle from center: up=0°, clockwise positive
                                val dx = change.position.x - center.x
                                val dy = center.y - change.position.y
                                val angle = Math.toDegrees(atan2(dx.toDouble(), dy.toDouble()))
                                selected.angleDeg = if (angle < 0) angle + 360.0 else angle
                            }
                        },
                        onDragEnd = {
                            points.forEach { it.isSelected = false }
                        }
                    )
                }
            )
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            Button(onClick = {
                points.add(UiSwipePoint(angleDeg = 0.0))
            }) { Text("Add point") }

            Button(onClick = {
                if (points.isNotEmpty()) points.removeAt(points.lastIndex)
            }) { Text("Remove point") }
        }
    }
}

/** UI model — separate from serializable model used for persistence. */
data class UiSwipePoint(
    var angleDeg: Double,
    var action: SwipeActionSerializable? = null, // optional link to serializable action type
    var isSelected: Boolean = false
)
