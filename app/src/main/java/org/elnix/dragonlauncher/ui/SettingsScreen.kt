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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ChangeCircle
import androidx.compose.material.icons.outlined.Grid3x3
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.SwipeActionSerializable
import org.elnix.dragonlauncher.data.SwipePointSerializable
import org.elnix.dragonlauncher.data.UiCircle
import org.elnix.dragonlauncher.data.UiSwipePoint
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore
import org.elnix.dragonlauncher.data.stores.SwipeSettingsStore
import org.elnix.dragonlauncher.data.stores.UiSettingsStore
import org.elnix.dragonlauncher.ui.helpers.AddPointDialog
import org.elnix.dragonlauncher.ui.helpers.RepeatingPressButton
import org.elnix.dragonlauncher.ui.helpers.SliderWithLabel
import org.elnix.dragonlauncher.ui.theme.AmoledDefault
import org.elnix.dragonlauncher.ui.theme.LocalExtraColors
import org.elnix.dragonlauncher.utils.AppDrawerViewModel
import org.elnix.dragonlauncher.utils.actions.actionColor
import org.elnix.dragonlauncher.utils.actions.actionIcon
import org.elnix.dragonlauncher.utils.actions.actionIconBitmap
import org.elnix.dragonlauncher.utils.actions.actionLabel
import org.elnix.dragonlauncher.utils.circles.autoSeparate
import org.elnix.dragonlauncher.utils.circles.normalizeAngle
import org.elnix.dragonlauncher.utils.circles.randomFreeAngle
import org.elnix.dragonlauncher.utils.circles.updatePointPosition
import org.elnix.dragonlauncher.utils.colors.adjustBrightness
import java.math.RoundingMode
import java.util.UUID
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

// Config
val MIN_ANGLE_GAP = listOf(
    27.0,
    16.0,
    11.0
)

@Immutable
data class CircleData(
    val name: String,
    val color: Color,
    val radius: Int
)

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
    val scope = rememberCoroutineScope()

    val icons by appsViewModel.icons.collectAsState()

    val circleColor by ColorSettingsStore.getCircleColor(ctx)
        .collectAsState(initial = AmoledDefault.CircleColor)
    val snapPoints by UiSettingsStore.getSnapPoints(ctx).collectAsState(initial = true)

    var center by remember { mutableStateOf(Offset.Zero) }

    val points: SnapshotStateList<UiSwipePoint> = remember { mutableStateListOf() }
    val circles: SnapshotStateList<UiCircle> = remember { mutableStateListOf() }

    var selectedPoint by remember { mutableStateOf<UiSwipePoint?>(null) }
    val aPointIsSelected = selectedPoint != null

    var showAddDialog by remember { mutableStateOf(false) }
    var recomposeTrigger by remember { mutableIntStateOf(0) }


    val circleR1 by UiSettingsStore.getFirstCircleDragDistance(ctx)
        .collectAsState(initial = 400)
    val circleR2 by UiSettingsStore.getSecondCircleDragDistance(ctx)
        .collectAsState(initial = 700)
    val cancelZone by UiSettingsStore.getCancelZoneDragDistance(ctx)
        .collectAsState(initial = 150)

    val circleDataList by remember {
        derivedStateOf {
            listOf(
                CircleData("circleR1", Color.Green, circleR1),
                CircleData("circleR2", Color.Red, circleR2),
                CircleData("cancelZone", Color.Gray, cancelZone)
            )
        }
    }

    var isCircleDistanceMode by remember { mutableStateOf(false) }

    var bannerVisible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (bannerVisible) 1f else 0f,
        animationSpec = tween(150)
    )
    val offsetY by animateDpAsState(
        targetValue = if (bannerVisible) 0.dp else (-20).dp,
        animationSpec = tween(150)
    )

    var availableWidth by remember { mutableFloatStateOf(0f) }


    val backgroundColor = MaterialTheme.colorScheme.background
    val extraColors = LocalExtraColors.current


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


    BackHandler {
        if (isCircleDistanceMode) isCircleDistanceMode = false
        if (selectedPoint != null) selectedPoint = null
        else onBack()
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

            Text(
                text = if (isCircleDistanceMode) stringResource(R.string.dragging_distance_selection)
                       else stringResource(R.string.swipe_points_selection),
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onAdvSettings) {
                Icon(Icons.Default.Settings, null, tint = Color.White)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(12.dp)
                .onSizeChanged { size ->
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()
                    center = Offset(w / 2f, h / 2f)
                    availableWidth = w - (POINT_RADIUS_PX * 2)  // Safe space for points + padding

                    // Proportional radii: largest fits screen, others reduce evenly
                    val baseRadius = availableWidth * 0.65f  // ~35% of screen width
                    circles.clear()
                    circles.add(
                        UiCircle(
                            id = 0,
                            radius = baseRadius * 0.35f,
                            points = mutableStateListOf()
                        )
                    )
                    circles.add(
                        UiCircle(
                            id = 1,
                            radius = baseRadius * 0.60f,
                            points = mutableStateListOf()
                        )
                    )
                    circles.add(
                        UiCircle(
                            id = 2,
                            radius = baseRadius * 0.85f,
                            points = mutableStateListOf()
                        )
                    )
                }
        ) {


            key(recomposeTrigger) {
                if (!isCircleDistanceMode) {
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
                                color = backgroundColor,
                                radius = POINT_RADIUS_PX,
                                center = Offset(px, py)
                            )

                            drawImage(
                                image = actionIconBitmap(
                                    action = p.action,
                                    context = ctx,
                                    tintColor = actionColor(p.action, extraColors)
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
                                color = backgroundColor,
                                radius = POINT_RADIUS_PX,
                                center = Offset(px, py)
                            )
                            drawImage(
                                image = actionIconBitmap(
                                    action = p.action,
                                    context = ctx,
                                    tintColor = actionColor(p.action, extraColors)
                                ),
                                dstOffset = IntOffset(px.toInt() - 28, py.toInt() - 28),
                                dstSize = IntSize(56, 56)
                            )
                        }
                    }
                } else {
                    Canvas(Modifier.fillMaxSize()) {
                        circleDataList.sortedBy { it.radius }.reversed().forEach { circleData ->

//                            val color = circleColors.getOrElse(index) { Color.Gray }

                            drawCircle(
                                color = backgroundColor,
                                radius = circleData.radius.toFloat(),
                                center = center,
                                style = Fill
                            )
                            drawCircle(
                                color = circleData.color.copy(0.1f),
                                radius = circleData.radius.toFloat(),
                                center = center,
                                style = Fill
                            )

                            drawCircle(
                                color = circleData.color,
                                radius = circleData.radius.toFloat(),
                                center = center,
                                style = Stroke(4f)
                            )
                        }
                    }
                }
            }


            if (!isCircleDistanceMode) {
                Box(
                    Modifier
                        .matchParentSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    var closest: UiSwipePoint? = null
                                    var best = Float.MAX_VALUE

                                    points.forEach { p ->
                                        val circle =
                                            circles.getOrNull(p.circleNumber) ?: return@forEach
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

                                    val selected = points.find { it.id == selectedPoint?.id }
                                        ?: return@detectDragGestures

                                    // All points that are part of the same circle
                                    val sameCirclePoints =
                                        points.filter { it.circleNumber == selected.circleNumber }
                                    if (sameCirclePoints.isEmpty()) return@detectDragGestures

                                    val p = points.find { it.id == selectedPoint?.id }
                                        ?: return@detectDragGestures
                                    updatePointPosition(
                                        p,
                                        circles,
                                        center,
                                        change.position,
                                        snapPoints
                                    )
                                    recomposeTrigger++
                                },
                                onDragEnd = {
                                    val p = points.find { it.id == selectedPoint?.id }
                                        ?: return@detectDragGestures
                                    autoSeparate(points, p.circleNumber, p)
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { offset ->
                                    var tapped: UiSwipePoint? = null
                                    var best = Float.MAX_VALUE

                                    points.forEach { p ->
                                        val circle =
                                            circles.getOrNull(p.circleNumber) ?: return@forEach
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
        }

        Icon(
            imageVector = if (isCircleDistanceMode) {
                Icons.Filled.ChangeCircle
            } else {
                Icons.Outlined.ChangeCircle
            },
            contentDescription = "Toggle drag circle editing",
            tint = MaterialTheme.colorScheme.primary.copy(if (isCircleDistanceMode) 1f else 0.5f),
            modifier = Modifier
                .clip(CircleShape)
                .clickable {
                    isCircleDistanceMode = !isCircleDistanceMode
                    selectedPoint = null
                }
                .background(
                    MaterialTheme.colorScheme.primary.copy(if (isCircleDistanceMode) 0.2f else 0.1f)
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(if (isCircleDistanceMode) 1f else 0.5f),
                    shape = CircleShape
                )
                .padding(15.dp)
        )

        if (!isCircleDistanceMode) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = if (snapPoints) {
                        Icons.Filled.Grid3x3
                    } else {
                        Icons.Outlined.Grid3x3
                    },
                    contentDescription = "Snap to rounded angles",
                    tint = MaterialTheme.colorScheme.primary.copy(if (snapPoints) 1f else 0.2f),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            scope.launch {
                                UiSettingsStore.setSnapPoints(ctx, !snapPoints)
                            }
                        }
                        .background(
                            MaterialTheme.colorScheme.primary.copy(if (snapPoints) 0.2f else 0f)
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(if (snapPoints) 1f else 0.2f),
                            shape = CircleShape
                        )
                        .padding(15.dp)
                )


                RepeatingPressButton(
                    enabled = aPointIsSelected,
                    intervalMs = 35L,
                    onPress = {
                        selectedPoint?.let {
                            it.angleDeg = normalizeAngle(it.angleDeg + 1)
                            if (snapPoints) it.angleDeg = it.angleDeg
                                .toInt()
                                .toDouble()
                            autoSeparate(points, it.circleNumber, it)
                            recomposeTrigger++
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Move point to left",
                        tint = Color(0xFF14E7EE).copy(if (aPointIsSelected) 1f else 0.2f),
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF14E7EE).copy(if (aPointIsSelected) 0.2f else 0f))
                            .border(
                                width = 1.dp,
                                color = Color(0xFF14E7EE).copy(if (aPointIsSelected) 1f else 0.2f),
                                shape = CircleShape
                            )
                            .padding(15.dp)
                    )
                }


                val angleText = if (selectedPoint != null) {
                    "${
                        selectedPoint?.angleDeg?.toBigDecimal()?.setScale(1, RoundingMode.UP)
                            ?.toDouble()
                    }°"
                } else {
                    ""
                }

                Text(
                    text = angleText,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    modifier = Modifier.width(50.dp)
                )

                RepeatingPressButton(
                    enabled = aPointIsSelected,
                    intervalMs = 35L,
                    onPress = {
                        selectedPoint?.let {
                            it.angleDeg = normalizeAngle(it.angleDeg - 1)
                            if (snapPoints) it.angleDeg = it.angleDeg
                                .toInt()
                                .toDouble()
                            autoSeparate(points, it.circleNumber, it)
                            recomposeTrigger++
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Move point to right",
                        tint = Color(0xFF14E7EE).copy(if (aPointIsSelected) 1f else 0.2f),
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF14E7EE).copy(if (aPointIsSelected) 0.2f else 0f))
                            .border(
                                width = 1.dp,
                                color = Color(0xFF14E7EE).copy(if (aPointIsSelected) 1f else 0.2f),
                                shape = CircleShape
                            )
                            .padding(15.dp)
                    )
                }
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

                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Remove point",
                    tint = MaterialTheme.colorScheme.error.copy(if (aPointIsSelected) 1f else 0.2f),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(aPointIsSelected) {
                            selectedPoint?.id.let { id ->
                                val index = points.indexOfFirst { it.id == id }
                                if (index >= 0) points.removeAt(index)
                                selectedPoint = null
                            }
                        }
                        .background(MaterialTheme.colorScheme.error.copy(if (aPointIsSelected) 0.2f else 0f))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.error.copy(if (aPointIsSelected) 1f else 0.2f),
                            shape = CircleShape
                        )
                        .padding(25.dp)
                )

                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy point",
                    tint = Color(0xFFE19807).copy(if (aPointIsSelected) 1f else 0.2f),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(aPointIsSelected) {
                            selectedPoint?.let { oldPoint ->
                                val circleNumber = oldPoint.circleNumber
                                val newAngle = randomFreeAngle(circleNumber, points)

                                val newPoint = UiSwipePoint(
                                    id = UUID.randomUUID().toString(),
                                    angleDeg = newAngle,
                                    action = oldPoint.action,
                                    circleNumber = circleNumber
                                )

                                points.add(newPoint)
                                autoSeparate(points, circleNumber, newPoint)
                                selectedPoint = newPoint
                            }
                        }
                        .background(Color(0xFFE19807).copy(if (aPointIsSelected) 0.2f else 0f))
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE19807).copy(if (aPointIsSelected) 1f else 0.2f),
                            shape = CircleShape
                        )
                        .padding(25.dp)
                )
            }
        } else {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                SliderWithLabel(
                    label = stringResource(R.string.circleR1),
                    value = circleR1,
                    valueRange = 0f..1000f,
                    showValue = false,
                    color = circleDataList.find { it.name == "circleR1" }!!.color,
                    onReset = { scope.launch { UiSettingsStore.setFirstCircleDragDistance(ctx, 400) } }
                ) {
                    scope.launch { UiSettingsStore.setFirstCircleDragDistance(ctx, it) }
                }

                SliderWithLabel(
                    label = stringResource(R.string.circleR2),
                    value = circleR2,
                    valueRange = 0f..1000f,
                    showValue = false,
                    color = circleDataList.find { it.name == "circleR2" }!!.color,
                    onReset = { scope.launch { UiSettingsStore.setSecondCircleDragDistance(ctx, 700) } }
                ) {
                    scope.launch { UiSettingsStore.setSecondCircleDragDistance(ctx, it) }
                }

                SliderWithLabel(
                    label = stringResource(R.string.cancelZone),
                    value = cancelZone,
                    valueRange = 0f..1000f,
                    showValue = false,
                    color = circleDataList.find { it.name == "cancelZone" }!!.color,
                    onReset = { scope.launch { UiSettingsStore.setCancelZoneDragDistance(ctx, 150) } }
                ) {
                    scope.launch { UiSettingsStore.setCancelZoneDragDistance(ctx, it) }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPointDialog(
            appsViewModel = appsViewModel,
            onDismiss = {
                showAddDialog = false
            },
            onActionSelected = { action ->
                val circleNumber = 0
                val newAngle = randomFreeAngle(circleNumber, points)

                val point = UiSwipePoint(
                    id = UUID.randomUUID().toString(),
                    angleDeg = newAngle,
                    action = action,
                    circleNumber = circleNumber
                )

                points.add(point)
                autoSeparate(points, circleNumber, point)

                showAddDialog = false
            }
        )
    }

    if (selectedPoint != null) {
        val currentPoint = selectedPoint!!
        val currentAction = currentPoint.action
        Box(
            Modifier
                .fillMaxWidth()
                .offset(y = offsetY)
                .padding(WindowInsets.systemBars.asPaddingValues())
                .padding(top = 50.dp)
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
                    tint = actionTint(currentAction, extraColors),
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = actionLabel(currentAction),
                    color = actionColor(currentAction, extraColors),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
