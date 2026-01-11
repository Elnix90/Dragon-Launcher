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
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ChangeCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.CircleNest
import org.elnix.dragonlauncher.data.SwipeActionSerializable
import org.elnix.dragonlauncher.data.UiCircle
import org.elnix.dragonlauncher.data.helpers.SwipePointSerializable
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore
import org.elnix.dragonlauncher.data.stores.DebugSettingsStore
import org.elnix.dragonlauncher.data.stores.SwipeSettingsStore
import org.elnix.dragonlauncher.data.stores.UiSettingsStore
import org.elnix.dragonlauncher.ui.components.AppPreviewTitle
import org.elnix.dragonlauncher.ui.components.dialogs.AddPointDialog
import org.elnix.dragonlauncher.ui.components.dialogs.EditPointDialog
import org.elnix.dragonlauncher.ui.components.dialogs.UserValidation
import org.elnix.dragonlauncher.ui.helpers.CircleIconButton
import org.elnix.dragonlauncher.ui.helpers.RepeatingPressButton
import org.elnix.dragonlauncher.ui.helpers.SliderWithLabel
import org.elnix.dragonlauncher.ui.helpers.actionsInCircle
import org.elnix.dragonlauncher.ui.theme.AmoledDefault
import org.elnix.dragonlauncher.ui.theme.LocalExtraColors
import org.elnix.dragonlauncher.ui.theme.addRemoveCirclesColor
import org.elnix.dragonlauncher.ui.theme.copyColor
import org.elnix.dragonlauncher.ui.theme.moveColor
import org.elnix.dragonlauncher.utils.ICONS_TAG
import org.elnix.dragonlauncher.utils.SWIPE_TAG
import org.elnix.dragonlauncher.utils.TAG
import org.elnix.dragonlauncher.utils.actions.actionColor
import org.elnix.dragonlauncher.utils.circles.autoSeparate
import org.elnix.dragonlauncher.utils.circles.normalizeAngle
import org.elnix.dragonlauncher.utils.circles.randomFreeAngle
import org.elnix.dragonlauncher.utils.circles.rememberNestNavigation
import org.elnix.dragonlauncher.utils.logs.logD
import org.elnix.dragonlauncher.utils.logs.logE
import org.elnix.dragonlauncher.utils.logs.logW
import org.elnix.dragonlauncher.utils.models.AppsViewModel
import org.elnix.dragonlauncher.utils.showToast
import java.math.RoundingMode
import java.util.UUID
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.round
import kotlin.math.sin
import kotlin.random.Random

// Config
private const val POINT_RADIUS_PX = 40f
private const val TOUCH_THRESHOLD_PX = 100f

private const val SNAP_STEP_DEG = 15.0


fun minAngleGapForCircle(circleRadius: Float): Double {
    val arcLength = 2 * POINT_RADIUS_PX
    val minAngleRad = arcLength / circleRadius
    return Math.toDegrees(minAngleRad.toDouble())
}


@Suppress("AssignedValueIsNeverRead")
@Composable
fun SettingsScreen(
    appsViewModel: AppsViewModel,
    onAdvSettings: () -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val extraColors = LocalExtraColors.current
    val scope = rememberCoroutineScope()

    val pointIcons by appsViewModel.pointIcons.collectAsState()

    val circleColor by ColorSettingsStore.getCircleColor(ctx)
        .collectAsState(initial = AmoledDefault.CircleColor)
    val snapPoints by UiSettingsStore.getSnapPoints(ctx).collectAsState(initial = true)
    val autoSeparatePoints by UiSettingsStore.getAutoSeparatePoints(ctx).collectAsState(initial = true)

    val appLabelOverlaySize by UiSettingsStore.getAppLabelOverlaySize(ctx)
        .collectAsState(initial = 18)
    val appIconOverlaySize by UiSettingsStore.getAppIconOverlaySize(ctx)
        .collectAsState(initial = 22)

    val settingsDebugInfos by DebugSettingsStore.getSettingsDebugInfos(ctx)
        .collectAsState(initial = false)

    var center by remember { mutableStateOf(Offset.Zero) }

    val points: SnapshotStateList<SwipePointSerializable> = remember { mutableStateListOf() }


    val circles: SnapshotStateList<UiCircle> = remember { mutableStateListOf() }

    var selectedPoint by remember { mutableStateOf<SwipePointSerializable?>(null) }
    var lastSelectedCircle by remember { mutableIntStateOf(0) }
    val aPointIsSelected = selectedPoint != null

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<SwipePointSerializable?>(null) }
    var recomposeTrigger by remember { mutableIntStateOf(0) }

    var showDeleteNestDialog by remember { mutableStateOf<SwipePointSerializable?>(null) }


    /**
     * NESTS SYSTEM
     * - Collects the nests from the datastore, then initialize the base nest to 0 (always the default)
     * while all the other have a random id
     */


    val nests by SwipeSettingsStore.getNestsFlow(ctx)
        .collectAsState(initial = emptyList())

    val nestNavigation = rememberNestNavigation(nests)
    val currentNest = nestNavigation.currentNest
    val nestId = nestNavigation.nestId

    val filteredPoints by remember(points, nestId) {
        derivedStateOf {
            points.filter { it.nestId == nestId }
        }
    }

    val currentFilteredPoints by rememberUpdatedState(filteredPoints)

    LaunchedEffect(points, nestId) {
        logD(TAG, nestId.toString())
        logD(TAG, currentNest.toString())
        logD(TAG, points.filter { it.nestId == nestId }.toString())
    }

    /**
     * The number of circles; it's the size of the current nest, minus one, cause it ignores the
     * cancel zone
     */
    val circleNumber = currentNest.dragDistances.size - 1
//    val circleKeys = currentNest.dragDistances.keys.filter { it > -1 }
//    val circleNumber = circleKeys.maxOrNull() ?: 0  // Highest circle ID

    /**
     * Computes an even distance for the circles spacing, for clean integration
     */
    val circlesWidthIncrement = (1f / circleNumber).takeIf { it != 0f } ?: 1f

    var pendingNestUpdate by remember { mutableStateOf<List<CircleNest>?>(null) }


    /**
     * Used to ensure that there is always a 0-id nest, the default one, the most important
     */
    LaunchedEffect(nestId, nests.size) {
        if (nests.isNotEmpty() && nests.none { it.id == nestId }) {
            logD(TAG, "Creating missing nest $nestId")
            pendingNestUpdate = nests + CircleNest(id = nestId, parentId = 0)
        }
    }



    LaunchedEffect(points, nestId) {
        appsViewModel.preloadPointIcons(points.filter { it.nestId == nestId })
    }

    /**
     * Saving system, the nests are immutable, they are saved using a pending value, that
     * asynchronously saves the nests in the datastore
     */
    LaunchedEffect(pendingNestUpdate) {
        pendingNestUpdate?.let { nests ->
            logE(TAG, "Saving: ${nests.size} nests")
            SwipeSettingsStore.saveNests(ctx, nests)
            pendingNestUpdate = null
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


    var undoStack by remember { mutableStateOf<List<List<SwipePointSerializable>>>(emptyList()) }
    var redoStack by remember { mutableStateOf<List<List<SwipePointSerializable>>>(emptyList()) }

    fun snapshotPoints(): List<SwipePointSerializable> = points.map { it.copy() }

    fun applyChange(mutator: () -> Unit) {
        // Save current state into undo before mutation
        undoStack = undoStack + listOf(snapshotPoints())
        // Any new user change invalidates redo history
        redoStack = emptyList()
        // Now apply the change
        mutator()
        recomposeTrigger++
    }

    fun undo() {
        if (undoStack.isEmpty()) return

        // Current state goes to redo
        redoStack = redoStack + listOf(snapshotPoints())

        // Pop last from undo and set it as current
        val last = undoStack.last()
        undoStack = undoStack.dropLast(1)

        points.clear()
        points.addAll(last.map { it.copy() })

        selectedPoint = points.find { it.id == (selectedPoint?.id ?: "") }
    }

    fun redo() {
        if (redoStack.isEmpty()) return

        // Current state goes back to undo
        undoStack = undoStack + listOf(snapshotPoints())

        val last = redoStack.last()
        redoStack = redoStack.dropLast(1)

        points.clear()
        points.addAll(last.map { it.copy() })

        selectedPoint = points.find { it.id == (selectedPoint?.id ?: "") }
    }


    fun updatePointPosition(
        point: SwipePointSerializable,
        circles: SnapshotStateList<UiCircle>,
        center: Offset,
        pos: Offset,
        snap: Boolean
    ) {
        var haveToApplyToStack = false

        // 1. Compute raw angle from center -> pos
        val dx = pos.x - center.x
        val dy = center.y - pos.y
        var angle = Math.toDegrees(atan2(dx.toDouble(), dy.toDouble()))
        if (angle < 0) angle += 360.0

        // 2. Apply snapping if enabled
        val finalAngle = if (snap) {
            round(angle / SNAP_STEP_DEG) * SNAP_STEP_DEG
        } else {
            angle
        }

        if (point.angleDeg != finalAngle) {
            haveToApplyToStack = true
        }

        // 3. Find nearest circle based on radius
        val distFromCenter = hypot(dx, dy)
        val closest = circles.minByOrNull { c -> abs(c.radius - distFromCenter) }
            ?: return


        if (haveToApplyToStack) applyChange {
            point.angleDeg = finalAngle
            point.circleNumber = closest.id
        }
    }

    // Load
    LaunchedEffect(Unit) {
        val saved = SwipeSettingsStore.getPoints(ctx)
        points.clear()
        try {
            points.addAll(saved)
        } catch (e: NullPointerException) {
            logE(SWIPE_TAG, "Error loading swipe points: $e")
            ctx.showToast("Error loading swipe points: $e")

            // Fallback load them the old way
            try {
                saved.map {
                    @Suppress("USELESS_ELVIS")
                    points.add(
                        it.copy(action = it.action ?: SwipeActionSerializable.OpenDragonLauncherSettings)
                    )
                }
            } catch (e: Exception) {
                logE(SWIPE_TAG, "Fallback loading also failed, clearing all points: $e")
            }
        }
    }


    // Save
    LaunchedEffect(Unit, recomposeTrigger) {
        snapshotFlow { points.toList() }
            .distinctUntilChanged()
            .collect { list ->
                SwipeSettingsStore.savePoints(ctx, list)
            }
    }


    BackHandler {
        if (isCircleDistanceMode) isCircleDistanceMode = false
        else if (selectedPoint != null) selectedPoint = null
        else if (nestId != 0) nestNavigation.goBack()
        else onBack()
    }



    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.exclude(WindowInsets.ime))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = stringResource(R.string.home),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = if (isCircleDistanceMode) stringResource(R.string.dragging_distance_selection)
                       else stringResource(R.string.swipe_points_selection),
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onAdvSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .onSizeChanged { size ->
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()
                    center = Offset(w / 2f, h / 2f)
                    availableWidth = w - (POINT_RADIUS_PX * 2)  // Safe space for points + padding

                    // Proportional radii: largest fits screen, others reduce evenly
                    val baseRadius = availableWidth / 2 * 0.95f // almost half of screen width
                    circles.clear()

                    currentNest.dragDistances.filter { it.key != -1 }
                        .forEach { (circleNumber, _) ->

                            // Computes the radius from the base radius and increase it evenly depending on the circle number
                            val radius = circlesWidthIncrement * (circleNumber + 1) * baseRadius

                            circles.add(
                                UiCircle(
                                    id = circleNumber,
                                    radius = radius,
                                )
                            )
                        }
                }
        ) {


            key(recomposeTrigger) {
                if (!isCircleDistanceMode) {
                    Canvas(Modifier.fillMaxSize()) {

                        // 1. Draw all circles
                        circles.forEach { circle ->
                            drawCircle(
                                color = circleColor,
                                radius = circle.radius,
                                center = center,
                                style = Stroke(4f)
                            )


                            // 2. Draw all points that belongs to the actual circle, selected last
                            currentFilteredPoints
                                .filter { it.circleNumber == circle.id }
                                .sortedBy { it.id == selectedPoint?.id }
                                .forEach { p ->

                                val px =
                                    center.x + circle.radius * sin(Math.toRadians(p.angleDeg)).toFloat()
                                val py =
                                    center.y - circle.radius * cos(Math.toRadians(p.angleDeg)).toFloat()

                                    actionsInCircle(
                                        selected = p.id == selectedPoint?.id,
                                        point = p,
                                        nests = nests,
                                        px = px,
                                        py = py,
                                        ctx = ctx,
                                        circleColor = circleColor,
                                        colorAction = actionColor(p.action, extraColors),
                                        pointIcons = pointIcons
                                    )
                            }
                        }
                    }
                } else {
                    Canvas(Modifier.fillMaxSize()) {

                        currentNest.dragDistances.forEach { (_, distance)->
                            drawCircle(
                                color = circleColor.copy(0.1f),
                                radius = distance.toFloat(),
                                center = center,
                                style = Fill
                            )

                            drawCircle(
                                color = circleColor,
                                radius = distance.toFloat(),
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
                        .then(
                            if (settingsDebugInfos) {
                                Modifier.background(Color.DarkGray.copy(0.3f))
                            } else Modifier
                        )
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    var closest: SwipePointSerializable? = null
                                    var best = Float.MAX_VALUE

                                    // Can only select points on the same nest
                                    currentFilteredPoints.forEach { p ->
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

                                    selectedPoint?.let { lastSelectedCircle = it.circleNumber }

                                    bannerVisible = selectedPoint != null
                                },
                                onDrag = { change, _ ->
                                    change.consume()

                                    val selected = points.find { it.id == selectedPoint?.id }
                                        ?: return@detectDragGestures

                                    lastSelectedCircle = selected.circleNumber

                                    // All points that are part of the same circle
                                    val sameCirclePoints =
                                        currentFilteredPoints.filter { it.circleNumber == selected.circleNumber }
                                    if (sameCirclePoints.isEmpty()) return@detectDragGestures

                                    val p =
                                        currentFilteredPoints.find { it.id == selectedPoint?.id }
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
                                    val p =
                                        currentFilteredPoints.find { it.id == selectedPoint?.id }
                                            ?: return@detectDragGestures
                                    if (autoSeparatePoints) autoSeparate(points, nestId, circles.find { it.id == p.circleNumber }, p)
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { offset ->
                                    var tapped: SwipePointSerializable? = null
                                    var best = Float.MAX_VALUE

                                    logD(TAG, currentFilteredPoints.toString())
                                    currentFilteredPoints.forEach { p ->
                                        logW(TAG, p.toString())
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
                                    logW(TAG, "Tapped: $tapped")

                                    selectedPoint =
                                        if (best <= TOUCH_THRESHOLD_PX)
                                            if (selectedPoint?.id == tapped?.id) null else tapped
                                        else null

                                    selectedPoint?.let { lastSelectedCircle = it.circleNumber }

                                    bannerVisible = selectedPoint != null
                                }
                            )
                        }
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ){

            if (!isCircleDistanceMode) {
                Row (
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    val nestToGo =
                        if (selectedPoint?.action is SwipeActionSerializable.OpenCircleNest) {
                            (selectedPoint!!.action as SwipeActionSerializable.OpenCircleNest).nestId
                        } else null

                    val canGoNest = nestToGo != null


                    CircleIconButton(
                        icon = Icons.Filled.Fullscreen,
                        contentDescription = stringResource(R.string.open_nest_circle),
                        color = extraColors.goParentNest,
                        enabled = canGoNest,
                        clickable = canGoNest,
                        padding = 7.dp
                    ) {
                        nestToGo?.let {
                            nestNavigation.goToNest(it)
                            selectedPoint = null
                        }
                    }


                    val canGoback = currentNest.parentId != nestId

                    CircleIconButton(
                        icon = Icons.Filled.FullscreenExit,
                        contentDescription = stringResource(R.string.go_parent_nest),
                        color = extraColors.goParentNest,
                        enabled = canGoback,
                        clickable = canGoback,
                        padding = 7.dp
                    ) {
                        nestNavigation.goBack()
                        selectedPoint = null
                    }
                }
            }


            CircleIconButton(
                icon =if (isCircleDistanceMode) {
                    Icons.Filled.ChangeCircle
                } else {
                    Icons.Outlined.ChangeCircle
                },
                contentDescription = stringResource(R.string.toggle_drag_distances_editing),
                color = MaterialTheme.colorScheme.primary,
                padding = 10.dp
            ) {
                isCircleDistanceMode = !isCircleDistanceMode
                selectedPoint = null
            }


            if (!isCircleDistanceMode) {
                IconButton(onClick = { undo() }, enabled = undoStack.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo"
                    )
                }

                IconButton(onClick = { redo() }, enabled = redoStack.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo"
                    )
                }
            }

        }

        if (!isCircleDistanceMode) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleIconButton(
                    icon = Icons.Default.Grid3x3,
                    contentDescription = stringResource(R.string.auto_separate),
                    color = MaterialTheme.colorScheme.primary,
                    enabled = snapPoints,
                    padding = 10.dp
                ) {
                    scope.launch {
                        UiSettingsStore.setSnapPoints(ctx, !snapPoints)
                    }
                }

                CircleIconButton(
                    icon = Icons.Default.AutoMode,
                    contentDescription = stringResource(R.string.auto_separate),
                    color = MaterialTheme.colorScheme.primary,
                    enabled = autoSeparatePoints,
                    padding = 10.dp
                ) {
                    scope.launch {
                        UiSettingsStore.setAutoSeparatePoints(ctx, !autoSeparatePoints)
                    }
                }


                RepeatingPressButton(
                    enabled = aPointIsSelected,
                    intervalMs = 35L,
                    onPress = {
                        selectedPoint?.let { point ->
                            applyChange {
                                point.angleDeg = normalizeAngle(point.angleDeg + 1)
                                if (snapPoints) point.angleDeg = point.angleDeg
                                    .toInt()
                                    .toDouble()
                                if (autoSeparatePoints) autoSeparate(points, nestId, circles.find { it.id == point.circleNumber }, point)
                                recomposeTrigger++
                            }
                        }
                    }
                ) {

                    CircleIconButton(
                        icon = Icons.Default.ChevronLeft,
                        contentDescription = stringResource(R.string.move_point_to_left),
                        color = moveColor,
                        clickable = false,
                        enabled = aPointIsSelected,
                        padding = 10.dp,
                        onClick = null
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
                    maxLines = 1,
                    overflow = TextOverflow.Visible,
                    modifier = Modifier.width(50.dp)
                )

                RepeatingPressButton(
                    enabled = aPointIsSelected,
                    intervalMs = 35L,
                    onPress = {
                        selectedPoint?.let { point ->
                            applyChange {
                                point.angleDeg = normalizeAngle(point.angleDeg - 1)
                                if (snapPoints) point.angleDeg = point.angleDeg
                                    .toInt()
                                    .toDouble()
                                if (autoSeparatePoints) autoSeparate(points, nestId, circles.find { it.id == point.circleNumber }, point)
                                recomposeTrigger++
                            }
                        }
                    }
                ) {


                    CircleIconButton(
                        icon = Icons.Default.ChevronRight,
                        contentDescription = stringResource(R.string.move_point_to_right),
                        color = moveColor,
                        clickable = false,
                        enabled = aPointIsSelected,
                        padding = 10.dp,
                        onClick = null
                    )
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {

                CircleIconButton(
                    icon = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_point),
                    color = MaterialTheme.colorScheme.primary,
                    padding = 20.dp
                ) { showAddDialog = true }



                CircleIconButton(
                    icon = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit_point),
                    color = MaterialTheme.colorScheme.secondary,
                    enabled = aPointIsSelected,
                    padding = 20.dp
                ) { showEditDialog = selectedPoint }


                CircleIconButton(
                    icon = Icons.Default.Remove,
                    contentDescription = stringResource(R.string.remove_point),
                    color = MaterialTheme.colorScheme.error,
                    enabled = aPointIsSelected,
                    padding = 20.dp
                ) {
                    selectedPoint?.let { point ->

                        if (point.action is SwipeActionSerializable.OpenCircleNest) {
                            showDeleteNestDialog = point
                        } else {
                            val index = points.indexOfFirst { it.id == point.id }
                            if (index >= 0) {
                                applyChange {
                                    points.removeAt(index)
                                }
                            }
                            selectedPoint = null
                        }
                    }
                }


                CircleIconButton(
                    icon = Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.copy_point),
                    enabled = aPointIsSelected,
                    color = copyColor,
                    padding = 20.dp
                ) {
                    selectedPoint?.let { oldPoint ->
                        val circleNumber = oldPoint.circleNumber
                        val newAngle =
                            randomFreeAngle(circles.find { it.id == oldPoint.circleNumber }, points) ?: run {
                                ctx.showToast("Error: no circle belonging to this point found")
                                return@CircleIconButton
                            }

                        val newPoint = SwipePointSerializable(
                            id = UUID.randomUUID().toString(),
                            angleDeg = newAngle,
                            action = oldPoint.action,
                            circleNumber = circleNumber,
                            nestId = nestId
                        )

                        appsViewModel.reloadPointIcon(newPoint)

                        applyChange {
                            points.add(newPoint)
                            autoSeparate(
                                points,
                                nestId,
                                circles.find { it.id == newPoint.circleNumber },
                                newPoint
                            )
                        }
                        selectedPoint = newPoint
                    }
                }



                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {

                                logD(TAG, nests.toString())
                                logD(TAG, "Received add update, current nests size: ${nests.size}")


                                // The new circle id is the size minus one, cause circleIndexes
                                // starts at 0 and the cancel zone is always in the list
                                val newCircleNumber = currentNest.dragDistances.size - 1

                                logD(TAG, "new circle number: $newCircleNumber")

                                logD(TAG, nests.map {
                                    if (it.id == nestId) {
                                        it.copy(
                                            dragDistances = it.dragDistances + (newCircleNumber to defaultDragDistance(
                                                newCircleNumber
                                            ))
                                        )
                                    } else it
                                }.toString())
                                // Add a new circle
                                pendingNestUpdate = nests.map {
                                    if (it.id == nestId) {
                                        it.copy(
                                            dragDistances = it.dragDistances + (newCircleNumber to defaultDragDistance(
                                                newCircleNumber
                                            ))
                                        )
                                    } else it
                                }
                            }
                            .background(addRemoveCirclesColor.copy(0.2f))
                            .border(
                                width = 1.dp,
                                color = addRemoveCirclesColor,
                                shape = CircleShape
                            )
                            .size(40.dp)
                            .padding(7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+1",
                            color = addRemoveCirclesColor,
                        )
                    }

                    val canRemoveCircle = circleNumber > 1

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(canRemoveCircle) {

                                // Remove last circle
                                pendingNestUpdate = nests.map {
                                    if (it.id == nestId) {

                                        // Filters keys that are above zero (cannot remove if only one circle, it's a safe guard
                                        val maxCircle =
                                            it.dragDistances.keys.filter { k -> k > 0 }.maxOrNull()
                                        val updatedDistances = if (maxCircle != null) {
                                            it.dragDistances - maxCircle
                                        } else {
                                            it.dragDistances
                                        }
                                        it.copy(dragDistances = updatedDistances)
                                    } else it
                                }
                            }
                            .background(addRemoveCirclesColor.copy(if (canRemoveCircle) 0.2f else 0f))
                            .border(
                                width = 1.dp,
                                color = addRemoveCirclesColor.copy(if (canRemoveCircle) 1f else 0.2f),
                                shape = CircleShape
                            )
                            .size(40.dp)
                            .padding(7.dp),
                        contentAlignment = Alignment.Center

                    ) {
                        Text(
                            text = "-1",
                            color = addRemoveCirclesColor.copy(if (canRemoveCircle) 1f else 0.2f),
                        )
                    }
                }
            }
        } else {
            Column(
                Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                currentNest.dragDistances.forEach { (index, distance) ->
                    SliderWithLabel(
                        label = if (index == -1) stringResource(R.string.cancel_zone)
                        else "${stringResource(R.string.circle)}: $index",
                        value = distance,
                        valueRange = 0..1000,
                        showValue = false,
                        color = MaterialTheme.colorScheme.primary,
                        onReset = {
                           pendingNestUpdate = nests.map { nest ->
                                if (nest.id == nestId) {
                                    val newDistances = nest.dragDistances.toMutableMap().apply {
                                        this[index] = defaultDragDistance(index)
                                    }
                                    nest.copy(dragDistances = newDistances)
                                } else nest
                            }
                        }
                    ) { newValue ->
                        pendingNestUpdate = nests.map { nest ->
                            if (nest.id == currentNest.id) {
                                val newDistances = nest.dragDistances.toMutableMap().apply {
                                    this[index] = newValue
                                }
                                nest.copy(dragDistances = newDistances)
                            } else nest
                        }
                    }
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
                val circleNumber = lastSelectedCircle.coerceAtMost(circleNumber - 1)
                val newAngle = randomFreeAngle(circles.find { it.id == circleNumber }, points) ?: run {
                    ctx.showToast("Error: no circle belonging to this point found")
                    return@AddPointDialog
                }


                var finalAction = action

                if (action is SwipeActionSerializable.OpenCircleNest) {
                    finalAction = action.copy(nestId = Random.nextInt())
                    pendingNestUpdate = nests + CircleNest(id = finalAction.nestId, parentId = nestId)
                }

                val point = SwipePointSerializable(
                    id = UUID.randomUUID().toString(),
                    angleDeg = newAngle,
                    action = finalAction,
                    circleNumber = circleNumber,
                    nestId = nestId
                )

                appsViewModel.reloadPointIcon(
                    point = point,
                )

                applyChange {
                    points.add(point)
                    autoSeparate(points, nestId, circles.find { it.id == point.circleNumber }, point)
                }

                showAddDialog = false
            }
        )
    }

    if (showEditDialog != null) {
        val editPoint = showEditDialog!!

        EditPointDialog(
            appsViewModel = appsViewModel,
            point = editPoint,
            onDismiss = {
                showEditDialog = null
            },
        ) { newPoint ->
            if (newPoint.action is SwipeActionSerializable.OpenCircleNest) {
                // If changing to nest action, create the nest
                pendingNestUpdate = nests + CircleNest(id = newPoint.nestId ?: 0, parentId = nestId)
            }
            ctx.logE(ICONS_TAG, "Received edit of point id: ${editPoint.id} (new: ${newPoint.id}")

            applyChange {
                val index = points.indexOfFirst { it.id == editPoint.id }
                if (index >= 0) {
                    points[index] = newPoint
                }
            }
            selectedPoint = newPoint
            showEditDialog = null
        }
    }

    if (showDeleteNestDialog != null) {
        val nestToDelete = showDeleteNestDialog!!
        UserValidation(
            title = stringResource(R.string.delete_circle_nest),
            message = stringResource(R.string.are_you_sure_to_delete_this_nest),
            onCancel = { showDeleteNestDialog = null }
        ) {
            // Delete nest, leave points on it for now
            pendingNestUpdate = nests.filter { it.id != nestToDelete.nestId}

            val index = points.indexOfFirst { it.id == nestToDelete.id }
            if (index >= 0) {
                applyChange {
                    points.removeAt(index)
                }
            }

            selectedPoint = null
            showDeleteNestDialog = null

        }
    }

    if (selectedPoint != null) {
        val currentPoint = selectedPoint!!
        AppPreviewTitle(
            offsetY = offsetY,
            alpha = alpha,
            pointIcons = pointIcons,
            point = currentPoint,
            topPadding = 80.dp,
            labelSize = appLabelOverlaySize,
            iconSize = appIconOverlaySize,
            showLabel = true,
            showIcon = true
        )
    }

    if (settingsDebugInfos) {
        Box(
            modifier = Modifier
                .background(Color.DarkGray.copy(0.5f))
                .padding(5.dp)
        ) {
            Column {
                Text("nests id: $nestId")
                Text("current nests id: ${currentNest.id}")
                Text("nests number: ${nests.size}")
                Text("circle number: $circleNumber")
                Text("currentNest size: ${currentNest.dragDistances.size}")
                Text("circle width incr: $circlesWidthIncrement")
                Text("current dragDistances: ${currentNest.dragDistances}")
                selectedPoint?.let {
                    Text(it.toString())
                }
                 Column(
                     verticalArrangement = Arrangement.spacedBy(5.dp)
                 ) {
                     nests.forEach { Text(it.toString()) }
                 }

                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {

                    circles.forEach {
                        Text(
                            "${it.id } ${minAngleGapForCircle(it.radius)}"
                        )
                    }
                }
            }
        }
    }
}

private fun defaultDragDistance(id: Int): Int = when (id) {
    -1 -> 150 // Cancel Zone (below no actions activation)
    0 -> 400  // First circle (between 150 and 400)
    1 -> 700  // Second circle (between 400 and 700)
    2 -> 800  // Third (default there are 3 so its supposed to be infinite)
    else -> 600 + 100*id // 900 for 4th circle, and so on, nobody's dumb enough to use 10 circles
}
