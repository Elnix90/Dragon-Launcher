package org.elnix.dragonlauncher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import org.elnix.dragonlauncher.data.SwipeActionSerializable
import org.elnix.dragonlauncher.data.SwipePointSerializable
import org.elnix.dragonlauncher.data.datastore.SwipeDataStore
import org.elnix.dragonlauncher.ui.helpers.AddPointDialog
import java.util.UUID
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

// --------------------------
// CONFIG
// --------------------------

private const val MIN_ANGLE_GAP = 18.0       // minimum separation between points (°)
private const val POINT_RADIUS_PX = 30f
private const val TOUCH_THRESHOLD_PX = 48f

// Colors for different action types
private fun actionColor(action: SwipeActionSerializable?): Color =
    when (action) {
        is SwipeActionSerializable.LaunchApp -> Color(0xFF55AAFF)
        is SwipeActionSerializable.OpenUrl -> Color(0xFF66DD77)
        SwipeActionSerializable.NotificationShade -> Color(0xFFFFBB44)
        SwipeActionSerializable.ControlPanel -> Color(0xFFFF6688)
        SwipeActionSerializable.OpenAppDrawer -> Color(0xFFDD55FF)
        else -> Color.Red
    }

// --------------------------
// DATA MODEL (internal)
// --------------------------

data class UiSwipePoint(
    var id: String,
    var angleDeg: Double,
    var action: SwipeActionSerializable?,
    var isSelected: Boolean = false
)

// --------------------------
// MAIN SCREEN
// --------------------------

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
//    val scope = rememberCoroutineScope()

    var radius by remember { mutableFloatStateOf(0f) }
    var center by remember { mutableStateOf(Offset.Zero) }

    val points: SnapshotStateList<UiSwipePoint> = remember { mutableStateListOf() }
    var selectedPointId by remember { mutableStateOf<String?>(null) }


    var showAddDialog by remember { mutableStateOf(false) }

    var recomposeTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val saved = SwipeDataStore.getPoints(ctx)
        points.clear()
        points.addAll(saved.map {
            UiSwipePoint(it.id ?: UUID.randomUUID().toString(), it.angleDeg, it.action)
        })
    }


    // Save ONLY when points actually change
    LaunchedEffect(points) {
        snapshotFlow { points.toList() }
            .distinctUntilChanged()
            .collect { current ->
                SwipeDataStore.save(
                    ctx,
                    current.map { uiPoint ->
                        SwipePointSerializable(
                            id = uiPoint.id,
                            angleDeg = uiPoint.angleDeg,
                            action = uiPoint.action
                        )
                    }
                )
            }
    }



    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {

        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        points.forEach {
            Text(it.toString(), color = actionColor(it.action))
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(12.dp)
                .onSizeChanged {
                    radius = (min(it.width, it.height) * 0.35f)
                    center = Offset(it.width / 2f, it.height / 2f)
                }
        ) {

            // --------------------------
            // DRAWING
            // --------------------------
            key(recomposeTrigger) {
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color(0x92FF0000),
                        radius = radius,
                        center = center,
                        style = Stroke(3f)
                    )

                    points.forEach { p ->
                        val px = center.x + radius * sin(Math.toRadians(p.angleDeg)).toFloat()
                        val py = center.y - radius * cos(Math.toRadians(p.angleDeg)).toFloat()

                        drawCircle(
                            color = actionColor(p.action),
                            radius = POINT_RADIUS_PX + if (p.id == selectedPointId) 5 else 0,
                            center = Offset(px, py)
                        )
                        drawCircle(
                            color = Color.Black,
                            radius = POINT_RADIUS_PX - 4,
                            center = Offset(px, py)
                        )
                    }
                }
            }

            // --------------------------
            // DRAG HANDLING
            // --------------------------
            Box(
                Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                var closest: UiSwipePoint? = null
                                var best = Float.MAX_VALUE

                                points.forEach { p ->
                                    val px = center.x + radius * sin(Math.toRadians(p.angleDeg)).toFloat()
                                    val py = center.y - radius * cos(Math.toRadians(p.angleDeg)).toFloat()
                                    val dist = hypot(offset.x - px, offset.y - py)
                                    if (dist < best) {
                                        best = dist
                                        closest = p
                                    }
                                }

                                selectedPointId = if (best <= TOUCH_THRESHOLD_PX) {
                                    closest?.id
                                } else {
                                    null
                                }
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val selected = points.find { it.id == selectedPointId } ?: return@detectDragGestures

                                val dx = change.position.x - center.x
                                val dy = center.y - change.position.y
                                var angle = Math.toDegrees(atan2(dx.toDouble(), dy.toDouble()))
                                if (angle < 0) angle += 360.0

                                val idx = points.indexOf(selected)
                                if (idx >= 0) {
                                    points[idx] = selected.copy(angleDeg = angle)
                                }
                                recomposeTrigger++
                            },
                            onDragEnd = {
                                autoSeparate(points)
                                selectedPointId = null
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                // check if tapped on a point
                                var tapped: UiSwipePoint? = null
                                var d = Float.MAX_VALUE

                                points.forEach { p ->
                                    val px = center.x + radius * sin(Math.toRadians(p.angleDeg)).toFloat()
                                    val py = center.y - radius * cos(Math.toRadians(p.angleDeg)).toFloat()
                                    val dist = hypot(offset.x - px, offset.y - py)
                                    if (dist < d) {
                                        d = dist
                                        tapped = p
                                    }
                                }

                                selectedPointId = if (d <= TOUCH_THRESHOLD_PX) tapped?.id else null
                            }
                        )
                    }
            )

        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = { showAddDialog = true }) {
                Text("Add point")
            }


            Button(
                enabled = selectedPointId != null,
                onClick = {
                    val id = selectedPointId ?: return@Button
                    val index = points.indexOfFirst { it.id == id }
                    if (index >= 0) points.removeAt(index)
                    selectedPointId = null
                }
            ) {
                Text("Remove point")
            }
        }
    }

    // --------------------------
    // ADD POINT DIALOG
    // --------------------------
    if (showAddDialog) {
        AddPointDialog(
            onDismiss = { showAddDialog = false },
            onActionSelected = { action ->
                val newAngle = randomFreeAngle(points)
                points.add(
                    UiSwipePoint(
                        id = UUID.randomUUID().toString(),
                        angleDeg = newAngle,
                        action = action
                    )
                )
                autoSeparate(points)
                showAddDialog = false
            }
        )
    }
}

// --------------------------
// ANGLE MANAGEMENT
// --------------------------

private fun randomFreeAngle(list: List<UiSwipePoint>): Double {
    if (list.isEmpty()) return (0..359).random().toDouble()

    repeat(200) {
        val a = (0..359).random().toDouble()
        if (list.none { absAngleDiff(it.angleDeg, a) < MIN_ANGLE_GAP }) return a
    }

    // fallback: pick biggest gap
    val sorted = list.map { it.angleDeg }.sorted()
    var bestA = 0.0
    var bestDist = -1.0

    for (i in sorted.indices) {
        val a1 = sorted[i]
        val a2 = sorted[(i + 1) % sorted.size]
        val gap = ((a2 - a1 + 360) % 360)
        if (gap > bestDist) {
            bestDist = gap
            bestA = (a1 + gap / 2) % 360
        }
    }
    return bestA
}

private fun absAngleDiff(a: Double, b: Double): Double {
    val diff = abs(a - b)
    return min(diff, 360 - diff)
}

private fun autoSeparate(points: SnapshotStateList<UiSwipePoint>) {
    if (points.size <= 1) return

    for (i in 0 until 20) {
        var adjusted = false

        for (i in points.indices) {
            for (j in i + 1 until points.size) {

                val p1 = points[i]
                val p2 = points[j]
                val diff = absAngleDiff(p1.angleDeg, p2.angleDeg)

                if (diff < MIN_ANGLE_GAP) {
                    val mid = (p1.angleDeg + p2.angleDeg) / 2
                    p1.angleDeg = (mid - MIN_ANGLE_GAP / 2 + 360) % 360
                    p2.angleDeg = (mid + MIN_ANGLE_GAP / 2 + 360) % 360
                    adjusted = true
                }
            }
        }

        if (!adjusted) break
    }
}
