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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import org.elnix.dragonlauncher.data.SwipeActionSerializable
import org.elnix.dragonlauncher.data.SwipePointSerializable
import org.elnix.dragonlauncher.data.UiCircle
import org.elnix.dragonlauncher.data.UiSwipePoint
import org.elnix.dragonlauncher.data.datastore.SwipeDataStore
import org.elnix.dragonlauncher.ui.helpers.AddPointDialog
import org.elnix.dragonlauncher.utils.actions.actionColor
import org.elnix.dragonlauncher.utils.actions.actionIconBitmap
import org.elnix.dragonlauncher.utils.circles.autoSeparate
import org.elnix.dragonlauncher.utils.circles.randomFreeAngle
import org.elnix.dragonlauncher.utils.circles.updatePointPosition
import java.util.UUID
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

// Config
const val MIN_ANGLE_GAP = 18.0
private const val POINT_RADIUS_PX = 40f
private const val TOUCH_THRESHOLD_PX = 100f


@Composable
fun SettingsScreen(
    onAdvSettings: () -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current

    var center by remember { mutableStateOf(Offset.Zero) }

    val points: SnapshotStateList<UiSwipePoint> = remember { mutableStateListOf() }
    val circles: SnapshotStateList<UiCircle> = remember { mutableStateListOf() }
    var selectedPointId by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var recomposeTrigger by remember { mutableIntStateOf(0) }

    val circleColor = MaterialTheme.colorScheme.primary.copy(0.5f)

    // Load
    LaunchedEffect(Unit) {
        val saved = SwipeDataStore.getPoints(ctx)
        points.clear()
        points.addAll(saved.map {
            UiSwipePoint(
                it.id ?: UUID.randomUUID().toString(),
                it.angleDeg,
                it.action ?: SwipeActionSerializable.ControlPanel,
                it.circleNumber
            )
        })

        circles.clear()

        repeat(3) { index ->
            circles.add(
                UiCircle(
                    id = index,
                    radius = 200f + (index * 140f),
                    points = mutableStateListOf()
                )
            )
        }

        // assign points into circles
        points.forEach { p ->
            val circle = circles.getOrNull(p.circleNumber)
            circle?.points?.add(p)
        }
    }


    // Save
    LaunchedEffect(Unit, recomposeTrigger) {
        snapshotFlow { points.toList() }
            .distinctUntilChanged()
            .collect { list ->
                SwipeDataStore.save(
                    ctx,
                    list.map {
                        SwipePointSerializable(
                            id = it.id,
                            angleDeg = it.angleDeg,
                            action = it.action,
                            circleNumber = it.circleNumber
                        )
                    }
                )
            }
    }



    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(20.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }

            IconButton(onClick = onAdvSettings) {
                Icon(Icons.Default.Settings, null, tint = Color.White)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(12.dp)
                .onSizeChanged { center = Offset(it.width / 2f, it.height / 2f) }
        ) {


            key(recomposeTrigger) {
                Canvas(Modifier.fillMaxSize()) {

                    // 1. Draw all circles
                    circles.forEach { circle ->
                        drawCircle(
                            color = circleColor,
                            radius = circle.radius,
                            center = center,
                            style = Stroke(4f)
                        )
                    }

                    // 2. Draw all non-selected points
                    points.filter { it.id != selectedPointId }.forEach { p ->
                        val circle = circles.getOrNull(p.circleNumber) ?: return@forEach
                        val px = center.x + circle.radius * sin(Math.toRadians(p.angleDeg)).toFloat()
                        val py = center.y - circle.radius * cos(Math.toRadians(p.angleDeg)).toFloat()

//                        drawCircle(
//                            color = actionColor(p.action),
//                            radius = POINT_RADIUS_PX,
//                            center = Offset(px, py)
//                        )
                        drawCircle(
                            color = Color.Black,
                            radius = POINT_RADIUS_PX,
                            center = Offset(px, py)
                        )
                        drawImage(
                            image = actionIconBitmap(
                                action = p.action,
                                context = ctx,
                                tintColor = actionColor(p.action)
                            ),
                            dstOffset = IntOffset(px.toInt() - 28, py.toInt() - 28),
                            dstSize = IntSize(56, 56)
                        )
                    }

                    // 3. Selected point drawn last
                    val selected = points.find { it.id == selectedPointId }
                    selected?.let { p ->
                        val circle = circles.getOrNull(p.circleNumber) ?: return@let
                        val px = center.x + circle.radius * sin(Math.toRadians(p.angleDeg)).toFloat()
                        val py = center.y - circle.radius * cos(Math.toRadians(p.angleDeg)).toFloat()

                        drawCircle(
                            color = actionColor(p.action),
                            radius = POINT_RADIUS_PX + 5,
                            center = Offset(px, py)
                        )
                        drawCircle(
                            color = Color.Black,
                            radius = POINT_RADIUS_PX - 2,
                            center = Offset(px, py)
                        )
                        drawImage(
                            image = actionIconBitmap(
                                action = p.action,
                                context = ctx,
                                tintColor = actionColor(p.action)
                            ),
                            dstOffset = IntOffset(px.toInt() - 28, py.toInt() - 28),
                            dstSize = IntSize(56, 56)
                        )
                    }
                }
            }


            Box(
                Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                var closest: UiSwipePoint? = null
                                var best = Float.MAX_VALUE

                                points.forEach { p ->
                                    val circle = circles.getOrNull(p.circleNumber) ?: return@forEach
                                    val px =
                                        center.x + circle.radius * sin(Math.toRadians(p.angleDeg)).toFloat()
                                    val py =
                                        center.y - circle.radius * cos(Math.toRadians(p.angleDeg)).toFloat()
                                    val dist = hypot(offset.x - px, offset.y - py)

                                    if (dist < best) {
                                        best = dist
                                        closest = p
                                    }
                                }

                                selectedPointId =
                                    if (best <= TOUCH_THRESHOLD_PX) closest?.id else null
                            },
                            onDrag = { change, _ ->
                                change.consume()

                                val selected = points.find { it.id == selectedPointId } ?: return@detectDragGestures

                                // All points that are part of the same circle
                                val sameCirclePoints = points.filter { it.circleNumber == selected.circleNumber }
                                if (sameCirclePoints.isEmpty()) return@detectDragGestures

                                val p = points.find { it.id == selectedPointId } ?: return@detectDragGestures
                                updatePointPosition(p, circles, center, change.position)
                                recomposeTrigger++
                            },
                            onDragEnd = {
                                val p = points.find { it.id == selectedPointId } ?: return@detectDragGestures
                                autoSeparate(points, p.circleNumber)
                                selectedPointId = null
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                var tapped: UiSwipePoint? = null
                                var best = Float.MAX_VALUE

                                points.forEach { p ->
                                    val circle = circles.getOrNull(p.circleNumber) ?: return@forEach
                                    val px =
                                        center.x + circle.radius * sin(Math.toRadians(p.angleDeg)).toFloat()
                                    val py =
                                        center.y - circle.radius * cos(Math.toRadians(p.angleDeg)).toFloat()
                                    val dist = hypot(offset.x - px, offset.y - py)

                                    if (dist < best) {
                                        best = dist
                                        tapped = p
                                    }
                                }

                                selectedPointId =
                                    if (best <= TOUCH_THRESHOLD_PX)
                                        if (selectedPointId == tapped?.id) null else tapped?.id
                                    else null
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

    if (showAddDialog) {
        AddPointDialog(
            onDismiss = {
                @Suppress("AssignedValueIsNeverRead")
                showAddDialog = false
            },
            onActionSelected = { action ->
                val circleNumber = 0
                val newAngle = randomFreeAngle(points)

                val point = UiSwipePoint(
                    id = UUID.randomUUID().toString(),
                    angleDeg = newAngle,
                    action = action,
                    circleNumber = circleNumber
                )

                points.add(point)
                autoSeparate(points, circleNumber)

                @Suppress("AssignedValueIsNeverRead")
                showAddDialog = false
            }
        )
    }
}
