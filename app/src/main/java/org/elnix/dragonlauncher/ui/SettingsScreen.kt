package org.elnix.dragonlauncher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import org.elnix.dragonlauncher.data.SwipeActionSerializable
import org.elnix.dragonlauncher.data.SwipePointSerializable
import org.elnix.dragonlauncher.data.UiCircle
import org.elnix.dragonlauncher.data.UiSwipePoint
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore
import org.elnix.dragonlauncher.data.stores.SwipeSettingsStore
import org.elnix.dragonlauncher.ui.helpers.AddPointDialog
import org.elnix.dragonlauncher.ui.theme.AmoledDefault
import org.elnix.dragonlauncher.utils.AppDrawerViewModel
import org.elnix.dragonlauncher.utils.actions.actionColor
import org.elnix.dragonlauncher.utils.actions.actionIcon
import org.elnix.dragonlauncher.utils.actions.actionIconBitmap
import org.elnix.dragonlauncher.utils.actions.actionLabel
import org.elnix.dragonlauncher.utils.circles.autoSeparate
import org.elnix.dragonlauncher.utils.circles.randomFreeAngle
import org.elnix.dragonlauncher.utils.circles.updatePointPosition
import org.elnix.dragonlauncher.utils.colors.adjustBrightness
import java.util.UUID
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

// Config
const val MIN_ANGLE_GAP = 18.0
private const val POINT_RADIUS_PX = 40f
private const val TOUCH_THRESHOLD_PX = 100f


@Suppress("AssignedValueIsNeverRead")
@Composable
fun SettingsScreen(
    appsViewModel: AppDrawerViewModel,
    onAdvSettings: () -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current

    val icons by appsViewModel.icons.collectAsState()

    val circleColor by ColorSettingsStore.getCircleColor(ctx)
        .collectAsState(initial = AmoledDefault.CircleColor)

    var center by remember { mutableStateOf(Offset.Zero) }

    val points: SnapshotStateList<UiSwipePoint> = remember { mutableStateListOf() }
    val circles: SnapshotStateList<UiCircle> = remember { mutableStateListOf() }
    var selectedPoint by remember { mutableStateOf<UiSwipePoint?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var recomposeTrigger by remember { mutableIntStateOf(0) }


    var bannerVisible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (bannerVisible) 1f else 0f,
        animationSpec = tween(150)
    )
    val offsetY by animateDpAsState(
        targetValue = if (bannerVisible) 0.dp else (-20).dp,
        animationSpec = tween(150)
    )

    // Load
    LaunchedEffect(Unit) {
        val saved = SwipeSettingsStore.getPoints(ctx)
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
                SwipeSettingsStore.save(
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


    BackHandler { onBack() }

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
                            color = circleColor ?: AmoledDefault.CircleColor,
                            radius = circle.radius,
                            center = center,
                            style = Stroke(4f)
                        )
                    }

                    // 2. Draw all non-selected points
                    points.filter { it.id != selectedPoint?.id }.forEach { p ->
                        val circle = circles.getOrNull(p.circleNumber) ?: return@forEach
                        val px = center.x + circle.radius * sin(Math.toRadians(p.angleDeg)).toFloat()
                        val py = center.y - circle.radius * cos(Math.toRadians(p.angleDeg)).toFloat()

                        drawCircle(
                            color = circleColor ?: AmoledDefault.CircleColor,
                            radius = POINT_RADIUS_PX + 4,
                            center = Offset(px, py)
                        )

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
                    val selected = points.find { it.id == selectedPoint?.id }
                    selected?.let { p ->
                        val circle = circles.getOrNull(p.circleNumber) ?: return@let
                        val px = center.x + circle.radius * sin(Math.toRadians(p.angleDeg)).toFloat()
                        val py = center.y - circle.radius * cos(Math.toRadians(p.angleDeg)).toFloat()

                        drawCircle(
                            color = (circleColor ?: AmoledDefault.CircleColor).adjustBrightness(2f),
                            radius = POINT_RADIUS_PX + 10,
                            center = Offset(px, py)
                        )

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

                                selectedPoint =
                                    if (best <= TOUCH_THRESHOLD_PX) closest else null
                                bannerVisible = selectedPoint != null
                            },
                            onDrag = { change, _ ->
                                change.consume()

                                val selected = points.find { it.id == selectedPoint?.id } ?: return@detectDragGestures

                                // All points that are part of the same circle
                                val sameCirclePoints = points.filter { it.circleNumber == selected.circleNumber }
                                if (sameCirclePoints.isEmpty()) return@detectDragGestures

                                val p = points.find { it.id == selectedPoint?.id } ?: return@detectDragGestures
                                updatePointPosition(p, circles, center, change.position)
                                recomposeTrigger++
                            },
                            onDragEnd = {
                                val p = points.find { it.id == selectedPoint?.id } ?: return@detectDragGestures
                                autoSeparate(points, p.circleNumber)
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

                                selectedPoint =
                                    if (best <= TOUCH_THRESHOLD_PX)
                                        if (selectedPoint?.id == tapped?.id) null else tapped
                                    else null
                                bannerVisible = selectedPoint != null
                            }
                        )
                    }
            )
        }


        Row(
            Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add point",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { showAddDialog = true }
                    .background(MaterialTheme.colorScheme.primary.copy(0.2f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(0.5f),
                        shape = CircleShape
                    )
                    .padding(25.dp)
            )

            val removeEnabled = selectedPoint != null

            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Remove point",
                tint = MaterialTheme.colorScheme.error.copy(if (removeEnabled) 1f else 0.2f),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(removeEnabled) {
                        selectedPoint?.id.let { id ->
                            val index = points.indexOfFirst { it.id == id }
                            if (index >= 0) points.removeAt(index)
                            selectedPoint = null
                        }
                    }
                    .background(MaterialTheme.colorScheme.error.copy(if (removeEnabled) 0.2f else 0f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.error.copy(if (removeEnabled) 1f else 0.2f),
                        shape = CircleShape
                    )
                    .padding(25.dp)
            )

            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy point",
                tint = Color(0xFFE19807).copy(if (removeEnabled) 1f else 0.2f),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(removeEnabled) {
                        selectedPoint?.let { point ->
                            val circleNumber = 0
                            val newAngle = randomFreeAngle(points)

                            val point = UiSwipePoint(
                                id = UUID.randomUUID().toString(),
                                angleDeg = newAngle,
                                action = point.action,
                                circleNumber = circleNumber
                            )

                            points.add(point)
                            autoSeparate(points, circleNumber)
                        }
                    }
                    .background(Color(0xFFE19807).copy(if (removeEnabled) 0.2f else 0f))
                    .border(
                        width = 1.dp,
                        color = Color(0xFFE19807).copy(if (removeEnabled) 1f else 0.2f),
                        shape = CircleShape
                    )
                    .padding(25.dp)
            )
        }
    }

    if (showAddDialog) {
        AddPointDialog(
            appsViewModel = appsViewModel,
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

    if (selectedPoint != null) {
        val currentAction = selectedPoint!!.action
        Box(
            Modifier
                .fillMaxWidth()
                .offset(y = offsetY)
                .padding(WindowInsets.systemBars.asPaddingValues())
                .padding(20.dp)
                .alpha(alpha),
            contentAlignment = Alignment.TopCenter
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = actionIcon(currentAction, icons),
                    contentDescription = actionLabel(currentAction),
                    tint = actionTint(currentAction),
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = actionLabel(currentAction),
                    color = actionColor(currentAction),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
