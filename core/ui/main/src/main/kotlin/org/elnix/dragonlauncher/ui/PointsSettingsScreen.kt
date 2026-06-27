package org.elnix.dragonlauncher.ui

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elnix90.logging.NESTS_TAG
import io.github.elnix90.logging.SWIPE_TAG
import io.github.elnix90.logging.logD
import io.github.elnix90.logging.logV
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.Constants
import org.elnix.dragonlauncher.base.Constants.Settings.TOUCH_THRESHOLD_PX
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.common.circles.autoSeparate
import org.elnix.dragonlauncher.common.circles.computePosition
import org.elnix.dragonlauncher.enumsui.toggle.MoveAroundTools
import org.elnix.dragonlauncher.enumsui.toggle.MoveAroundTools.Center
import org.elnix.dragonlauncher.enumsui.toggle.MoveAroundTools.ResetRotation
import org.elnix.dragonlauncher.enumsui.toggle.MoveAroundTools.ResetZoom
import org.elnix.dragonlauncher.enumsui.toggle.NestEditTools
import org.elnix.dragonlauncher.enumsui.toggle.NestEditTools.EnterNest
import org.elnix.dragonlauncher.enumsui.toggle.NestEditTools.GoParentNest
import org.elnix.dragonlauncher.enumsui.toggle.NestEditTools.NestManagement
import org.elnix.dragonlauncher.enumsui.toggle.PointsEditTools
import org.elnix.dragonlauncher.enumsui.toggle.PointsEditTools.AutoSeparate
import org.elnix.dragonlauncher.enumsui.toggle.PointsEditTools.FreeMove
import org.elnix.dragonlauncher.enumsui.toggle.PointsEditTools.SnapPoints
import org.elnix.dragonlauncher.enumsui.toggle.SelectedPointEditTools
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.rotateBy
import org.elnix.dragonlauncher.ktx.undoTransformations
import org.elnix.dragonlauncher.models.IconsViewModel
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.settings.stores.map.BehaviorSettingsStore.createLiveNestByDefaultWhenCreatingOpenCircleNestPoint
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore.isInDragAroundMode
import org.elnix.dragonlauncher.settings.stores.map.SwipeMapSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore.autoSeparatePoints
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore.freeMoveDraggedPoint
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore.snapPoints
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.components.AnimatedFab
import org.elnix.dragonlauncher.ui.base.components.RowWithScrollIndicator
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.components.ToggleAnimatedFab
import org.elnix.dragonlauncher.ui.components.AppPreviewTitle
import org.elnix.dragonlauncher.ui.dialogs.AddPointDialog
import org.elnix.dragonlauncher.ui.dialogs.EditPointSheet
import org.elnix.dragonlauncher.ui.dialogs.NestManagementDialog
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.dialogs.UserValidation
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsSlider
import org.elnix.dragonlauncher.ui.helpers.DebugZone
import org.elnix.dragonlauncher.ui.helpers.UndoRedoBlock
import org.elnix.dragonlauncher.ui.helpers.customobjects.GlowOverlay
import org.elnix.dragonlauncher.ui.helpers.nests.NestOverlay
import org.elnix.dragonlauncher.ui.helpers.nests.PointIcon
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.helpers.settings.SpecialSettingsTitle
import org.elnix.dragonlauncher.ui.remembers.rememberNestNavigation
import kotlin.math.hypot
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun PointsSettingsScreen(
    iconsViewModel: IconsViewModel = activityViewModel(),
    pointsViewModel: PointsViewModel = activityViewModel(),
    onAdvSettings: () -> Unit,
    onNestEdit: (nest: Int) -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current

    val pointsService = pointsViewModel.pointsService
    val defaultPoint by pointsService.defaultPoint.asState()

    val scope = rememberCoroutineScope()

    val showAdvancedEditTools by SwipeMapSettingsStore.showAdvancedPointTools.asState()
    val showSubNestsSlider by SwipeMapSettingsStore.showSubNestsSlider.asState()
    val isInDragAroundMode by isInDragAroundMode.asState()

    val primaryColor = MaterialTheme.colorScheme.primary

    val snapPoints by snapPoints.asState()
    val autoSeparatePoints by autoSeparatePoints.asState()
    val freeMoveDraggedPoint by freeMoveDraggedPoint.asState()

    val createLiveNestByDefaultWhenCreatingOpenCircleNestPoint by createLiveNestByDefaultWhenCreatingOpenCircleNestPoint.asState()

    var center by remember { mutableStateOf(Offset.Zero) }

    val points by pointsService.points.asState()
    val nests by pointsService.nests.asState()
    val selectedPoint by pointsService.selectedPoint.asState()
    fun select(point: Point?) {
        pointsService.select(point)
    }

    fun deselect() {
        pointsService.select(null)
    }

    var recomposeTrigger by remember { mutableIntStateOf(0) }


    val selectedPointTempOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    fun animateHomingTempOffset(home: Offset) {
        scope.launch {
            selectedPointTempOffset.animateTo(
                home,
                animationSpec = tween(
                    300,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    val isDragging = selectedPointTempOffset.value != Offset.Zero

    var closestHoveredPoint by remember { mutableStateOf<Point?>(null) }
    var closestHoveredTempOffset by remember { mutableStateOf<Offset?>(null) }
    var ableToLaunchHoverAction by remember { mutableStateOf(false) }

    val hoveredPointRadialGradientProgress by animateFloatAsState(
        targetValue = if (ableToLaunchHoverAction) Constants.Settings.HOVER_GRADIENT_RADIUS else 1f
    )


    val aPointIsSelected = selectedPoint != null

    var showEditDefaultPoint by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Point?>(null) }

    // Manual placement mode state (multi-select "Place one by one")
    var manualPlacementQueue by remember { mutableStateOf<List<Action>>(emptyList()) }
    val isInManualPlacementMode = manualPlacementQueue.isNotEmpty()
    var isEditing by remember { mutableStateOf(false) }

    var showNestManagementDialog by remember { mutableStateOf(false) }
    var showResetPointsAndNestsDialog by remember { mutableStateOf(false) }


    val rowsScrollStates = List(3) { rememberScrollState() }

    /** Nests System
     * - Collects the nests from the datastore, then initialize the base nest to 0 (always the default)
     * while all the other have a random id
     */
    val nestNavigation = rememberNestNavigation()
    val currentNest = nestNavigation.currentNest
    val nestId = currentNest.id


//    /**
//     * The number of circles; it's the size of the current nest, minus one, cause it ignores the
//     * cancel zone
//     */
//    val circleNumber = currentNest.dragDistances.size - 1

//    /**
//     * Computes an even distance for the circles spacing, for clean integration
//     */
//    val circlesWidthIncrement = (1f / circleNumber).takeIf { it != 0f } ?: 1f

    /**
     * Used to ensure that there is always a 0-id nest, the default one, the most important
     */
    LaunchedEffect(Unit, nestId, nests.size) {
        if (nests.none { it.id == nestId }) {
            logD(NESTS_TAG) { "Creating missing nest $nestId" }
            pointsService.addNest(nestId)
        }
    }

    fun recenterSelectedTempOffset() {
        selectedPoint?.let { p ->
            scope.launch {
                selectedPointTempOffset.snapTo(p.computePosition(nests, center))
            }
        }
    }


    var showShapeManagementDialog by remember { mutableStateOf(false) }

//    fun addShape() {
//        applyChange {
//            pointsService.editNest(nestId) { nest ->
//                val newCircleNumber =
//                    nest.intersectionShapes
//                        .map { it.id }
//                        .filter { it >= 0 }
//                        .maxOrNull()
//                        ?.plus(1) ?: 0
//
//
//            }
//        }
//    }


//    /**
//     * Removes the last added circle from the specified nest (or the currently selected nest by default).
//     *
//     * The last circle is determined as the one with the highest circle number greater than 0.
//     * The nest list is updated immutably, and the change is recorded via `applyChange` for
//     * undo/redo support.
//     *
//     * Safely checks if there is mor than 1 circle to avoid deleting the last one
//     */
//    fun removeLastCircle() {
//        applyChange {
//            pointsService.editNest(nestId) { nest ->
//                val maxCircle =
//                    nest.dragDistances
//                        .keys
//                        .filter { k -> k > 0 }
//                        .maxOrNull()
//                        ?: return@editNest nest
//
//                nest.copy(
//                    dragDistances = nest.dragDistances - maxCircle
//                )
//            }
//        }
//    }


//    fun addPoint(select: Boolean = true, point: (Int) -> Point) {
//
//        pointsService.addPoint(select, point)
//        iconsViewModel.incrementPointCacheSize()
//        iconsViewModel.reloadIcon(point)
//    }

    /**
     * Compute the position of a moved point, on the circle, and returns a [Pair] composed of:
     *  - `first`: the new angleDeg of the point on the circles
     *  - `second`: the new circleNumber
     *
     * @param point which point to move
     * @param circles the circles to move the point on
     * @param pos the new position of the points, can be anywhere on the screen, not only on a circle
     * @return Pair with elements or null if the points hasn't moved
     */
    fun computePointMoved(
        point: Point,
        pos: Offset
    ): Offset? {

//        // 1. Compute raw angle from center -> pos
//        val dx = pos.x - center.x
//        val dy = center.y - pos.y
//        var angle = Math.toDegrees(atan2(dx.toDouble(), dy.toDouble()))
//        if (angle < 0) angle += 360.0

        // 2. Apply snapping if enabled
//        TODO()
//        val finalAngle = if (snapPoints) {
//            round(angle / SNAP_STEP_DEG) * SNAP_STEP_DEG
//        } else {
//            angle
//        }

        // 3. Find nearest circle based on radius
//        val distFromCenter = hypot(dx, dy)
//        val closestCircle = circles.minByOrNull { c -> abs(c.radius - distFromCenter) }
//            ?: error("Failed to find circle: BIG ISSUE")


//        // Only return the angle and circle number if they have changed
//        if (
//            (point.angleDeg != finalAngle) ||
//            (point.circleNumber != closestCircle.id)
//        ) {
//            return (finalAngle to closestCircle.id)
//        }

        // Position is the same as before, return null to tell the updater to not move the point
        return null
    }

    /**
     * Update point position; first compute the new point position using `computePointMoved` and then apply the change to the stack if it was moved
     *
     * @param this@updatePointPosition which point to move
     * @param circles the circles to move the point on
     * @param pos the new position of the points, can be anywhere on the screen, not only on a circle
     */
    fun Point.updatePosition(
        pos: Offset
    ) {
        val newPointOffset = computePointMoved(
            point = this,
            pos = pos
        )

        // Only apply the changed if the point has been changed
        newPointOffset?.let { newOffset ->
            pointsService.editPoint(id) {
                it.copy(offset = newOffset)
            }
        }
    }

//    // Load points & nests
//    LaunchedEffect(Unit, showResetPointsAndNestsDialog) {
//        if (!showResetPointsAndNestsDialog) {
//            scope.launch {
//                points.clear()
//                points.addAll(pointViewModel.points.first())
//
//                nests.clear()
//                nests.addAll(pointViewModel.nests.first())
//            }
//        }
//    }


    val handleBack = {
        if (isInManualPlacementMode) manualPlacementQueue = emptyList()
        else if (selectedPoint != null) deselect()
        else if (nestId != 0) nestNavigation.goBack()
        else if (isEditing) isEditing = false
        else onBack()
    }
    BackHandler(onBack = handleBack)

//    /**
//     * Computes and updates the radii for all circles in the current nest whenever
//     * the nest, available width, or center changes.
//     *
//     * Each circle's radius is proportional to the available width of the container,
//     * scaled by circlesWidthIncrement, and distributed evenly so that the largest
//     * circle nearly fits the box.
//     *
//     * This updates the mutable list circles, which is used both for rendering
//     * and for hit detection of points on the circles.
//     *
//     * - `currentNest`: The currently selected nest containing the drag distances for each circle.
//     * - `availableWidth`: The width available for drawing the circles, used to scale the radii proportionally.
//     * - `center`: The center of the container, used to compute offsets and positions for points.
//     */
//    LaunchedEffect(currentNest, center) {
//
//        createCirclesFromDragDistances(
//            dragDistances = currentNest.dragDistances,
//            circles = circles
//        )
//    }

    LaunchedEffect(closestHoveredPoint) {
        ableToLaunchHoverAction = false
        closestHoveredPoint?.let {

            val finalOffset = it.computePosition(
                nests,
                center
            )

            closestHoveredTempOffset = finalOffset

            val startDuration = System.currentTimeMillis()
            while (System.currentTimeMillis() - startDuration < Constants.Settings.HOVER_POINT_DURATION) {
                delay(50.milliseconds)
            }
            ableToLaunchHoverAction = true
        }
    }


    val filteredPoints by remember(points, nestId) {
        derivedStateOf {
            points.filter { it.nestId == nestId }
        }
    }

    // Shows all points, excepted the currently dragged one, if any, to draw them inside the canva
    val displayedFilteredPoints by remember(points, isDragging, selectedPoint?.id) {
        derivedStateOf {
            if (!isDragging || selectedPoint == null) points
            else points - selectedPoint!!
        }
    }

    val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val zoom = remember { Animatable(1f) }
    val angle = remember { Animatable(0f) }

    // Helper function to transform pointer coordinates back to original space
    fun Offset.transformPointerCoordinates(): Offset = undoTransformations(
        angle = { angle.value },
        zoom = { zoom.value },
        offset = { offset.value }
    )

    SettingsScaffold(
        title = "",
        onBack = handleBack,
        helpText = "",
        onReset = null,
        resetText = null,
        horizontalPadding = 0.dp,
        scrollableContent = false,
        specialSettingsTitle = {
            SpecialSettingsTitle(
                onSettings = onAdvSettings,
                onEditDefaultPoint = { showEditDefaultPoint = true },
                onEditNest = { onNestEdit(currentNest.id) },
                onResetPoints = { showResetPointsAndNestsDialog = true },
                onBack = handleBack
            )
        },
        bottomContent = {
            if (showAdvancedEditTools) { // Row with nest toolbar and toggle buttons toolbar
                RowWithScrollIndicator(rowsScrollStates[0]) {
                    // Nests toolbar
                    val nestToGo =
                        if (selectedPoint?.action is Action.OpenCircleNest) {
                            (selectedPoint!!.action as Action.OpenCircleNest).nestId
                        } else null

                    val canGoNest = nestToGo != null

                    val canGoback = currentNest.id != 0

                    MultiSelectConnectedButtonRow(
                        entries = NestEditTools.entries,
                        enabled = {
                            when (it) {
                                NestManagement -> true
                                GoParentNest -> canGoback
                                EnterNest -> canGoNest
                            }
                        },
                        checked = {
                            when (it) {
                                NestManagement -> true
                                GoParentNest -> canGoback
                                EnterNest -> canGoNest
                            }
                        }
                    ) { entry ->
                        when (entry) {
                            NestManagement -> {
                                showNestManagementDialog = true
                            }

                            GoParentNest -> {
                                nestNavigation.goBack()
                                deselect()
                            }

                            EnterNest -> {
                                nestToGo?.let {
                                    nestNavigation.goToNest(it)
                                    deselect()
                                }
                            }
                        }
                    }

                    Spacer(12.dp)

                    // The 3 points settings tools: Snap points / Auto separate / Lock to circle
                    MultiSelectConnectedButtonRow(
                        entries = PointsEditTools.entries,
                        checked = {
                            when (it) {
                                SnapPoints -> snapPoints
                                AutoSeparate -> autoSeparatePoints
                                FreeMove -> freeMoveDraggedPoint
                            }
                        }
                    ) {
                        scope.launch {
                            when (it) {
                                SnapPoints -> UiSettingsStore.snapPoints.set(ctx, !snapPoints)
                                AutoSeparate -> UiSettingsStore.autoSeparatePoints.set(ctx, !autoSeparatePoints)
                                FreeMove -> UiSettingsStore.freeMoveDraggedPoint.set(ctx, !freeMoveDraggedPoint)
                            }
                        }
                    }
                }


                // Undo/Redo and move bars
                RowWithScrollIndicator(rowsScrollStates[1]) {
//                    // The move left/right and text field entry, that animates on avery selected point
//                    Row(
//                        modifier = Modifier
//                            .height(70.dp)
//                            .padding(5.dp),
//                        horizontalArrangement = Arrangement.Center,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        DragonTooltip(R.string.move_point_clockwise) {
//                            ToggleButton(
//                                checked = false, // For the shape to be always the right one, and not a circle
//                                onCheckedChange = {
//                                    selectedPoint?.let { point ->
//                                            point.angleDeg = normalizeAngle(point.angleDeg + 1)
//                                            if (snapPoints) point.angleDeg = point.angleDeg
//                                                .toInt()
//                                                .toDouble()
//                                            if (autoSeparatePoints) autoSeparate(
//                                                points,
//                                                nestId,
//                                                circles.find { it.id == point.circleNumber },
//                                                point
//                                            )
//
//                                    }
//                                },
//                                enabled = aPointIsSelected,
//                                shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
//                                colors = AppObjectsColors.toggleButtonColors(),
//                            ) {
//                                Icon(
//                                    painter = painterResource(R.drawable.chevron_left),
//                                    contentDescription = stringResource(R.string.move_point_clockwise)
//                                )
//                            }
//                        }
//
//
//                        val angleTextValue = selectedPoint
//                            ?.angleDeg
//                            ?.toBigDecimal()
//                            ?.setScale(1, RoundingMode.UP)
//                            ?.toDouble()
//                            ?.toString()
//                            ?: ""
//
//
//                        var angleText by remember { mutableStateOf(angleTextValue) }
//                        LaunchedEffect(angleTextValue) { angleText = angleTextValue }
//
//
//                        fun commitEditTExt() {
//                            try {
//                                selectedPoint?.let { point ->
//                                    applyChange {
//                                        point.angleDeg = normalizeAngle(angleText.toDouble())
//                                        if (snapPoints) point.angleDeg = point.angleDeg
//                                            .toInt()
//                                            .toDouble()
//                                        if (autoSeparatePoints) autoSeparate(
//                                            points,
//                                            nestId,
//                                            circles.find { it.id == point.circleNumber },
//                                            point
//                                        )
//                                    }
//                                }
//                            } catch (e: Exception) {
//                                ctx.showToast("Failed to set value: $e")
//                                logE(SWIPE_TAG, e) { "Failed to set value for point via text field" }
//                            }
//                            isEditing = false
//                        }
//
//                        Spacer(ButtonGroupDefaults.ConnectedSpaceBetween)
//                        AnimatedVisibility(aPointIsSelected) {
//                            EditValueTextField(
//                                value = angleText,
//                                onValueChange = { angleText = it },
//                                enabled = aPointIsSelected,
//                                textColor = MaterialTheme.colorScheme.onPrimary,
//                                backgroundColor = MaterialTheme.colorScheme.primary,
//                                onFocusChange = { isEditing = it },
//                                onDone = ::commitEditTExt
//                            )
//                        }
//
//                        AnimatedVisibility(isEditing) {
//                            DragonIconButton(
//                                onClick = ::commitEditTExt,
//                                colors = IconButtonDefaults.iconButtonColors(
//                                    MaterialTheme.colorScheme.primary,
//                                    MaterialTheme.colorScheme.onPrimary
//                                ),
//                                icon = R.drawable.check,
//                                contentDescription = "Validate"
//                            )
//                        }
//
//                        Spacer(ButtonGroupDefaults.ConnectedSpaceBetween)
//
//                        DragonTooltip(R.string.move_point_anticlockwise) {
//                            ToggleButton(
//                                checked = false,
//                                onCheckedChange = {
//                                    selectedPoint?.let { point ->
//                                        applyChange {
//                                            point.angleDeg = normalizeAngle(point.angleDeg - 1)
//                                            if (snapPoints) point.angleDeg = point.angleDeg
//                                                .toInt()
//                                                .toDouble()
//                                            if (autoSeparatePoints) autoSeparate(
//                                                points,
//                                                nestId,
//                                                circles.find { it.id == point.circleNumber },
//                                                point
//                                            )
//                                        }
//                                    }
//                                },
//                                enabled = aPointIsSelected,
//                                shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
//                                colors = AppObjectsColors.toggleButtonColors(),
//                            ) {
//                                Icon(
//                                    painter = painterResource(R.drawable.chevron_right),
//                                    contentDescription = stringResource(R.string.move_point_anticlockwise),
//                                )
//                            }
//                        }
//                    }

//                    Spacer(12.dp)
                    UndoRedoBlock(pointsService.undoRedo)
                }


                // 3. Reset offset/zoom/rotation - add/remove circle
                RowWithScrollIndicator(rowsScrollStates[2]) {
                    val canResetOffset = offset.value != Offset.Zero
                    val canResetZoom = zoom.value != 1f
                    val canResetRotation = angle.value != 0f

                    MultiSelectConnectedButtonRow(
                        entries = MoveAroundTools.entries,
                        enabled = {
                            when (it) {
                                Center -> canResetOffset
                                ResetZoom -> canResetZoom
                                ResetRotation -> canResetRotation
                            }
                        },
                        checked = {
                            when (it) {
                                Center -> canResetOffset
                                ResetZoom -> canResetZoom
                                ResetRotation -> canResetRotation
                            }
                        }
                    ) { entry ->
                        scope.launch {
                            when (entry) {

                                Center -> scope.launch {
                                    offset.animateTo(Offset.Zero)
                                }

                                ResetZoom -> scope.launch {
                                    zoom.animateTo(1f)
                                }

                                ResetRotation -> scope.launch {
                                    angle.animateTo(0f)
                                }
                            }
                        }
                    }

                    Spacer(12.dp)


                    DragonIconButton(
                        icon = R.drawable.shapes,
                        contentDescription = R.string.shapes
                    ) { showShapeManagementDialog = true }
                }
            }

            // Last Buttons Row, containing the Add/Remove/Copy and the Add circle and Remove circle buttons
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {

                AnimatedFab(
                    onClick = { showAddDialog = true },
                    icon = R.drawable.add,
                    minSize = 70.dp,
                    containerColor = MaterialTheme.colorScheme.secondary
                )

                ToggleAnimatedFab(
                    checked = isInDragAroundMode,
                    onCheckedChange = {
                        val newValue = !isInDragAroundMode
                        scope.launch {
                            PrivateSettingsStore.isInDragAroundMode.set(ctx, newValue)
                        }
                        if (newValue) {
                            deselect()
                        }
                    },
                    minSize = 60.dp,
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    if (it) {
                        R.drawable.pan_zoom
                    } else {
                        R.drawable.pan_tool
                    }
                }

                MultiSelectConnectedButtonRow(
                    entries = SelectedPointEditTools.entries,
                    checked = { aPointIsSelected },
                    enabled = { aPointIsSelected }
                ) { option ->
                    when (option) {
                        SelectedPointEditTools.Edit -> showEditDialog = selectedPoint

                        SelectedPointEditTools.Remove -> {
                            selectedPoint?.let { point ->
                                pointsService.removePoint(point.id)
                                deselect()
                            }
                        }

                        SelectedPointEditTools.Duplicate -> {
                            selectedPoint?.let { oldPoint ->
                                pointsService.addPoint {
                                    oldPoint.copy(id = it)
                                }
//                                autoSeparate(
//                                    points = points,
//                                    nest = currentNest,
//                                    draggedPoint = newPoint
//                                )
                            }
                        }
                    }
                }
            }
        },
        modifier = Modifier.imePadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    // Updates the center and available width variables, that depends on the phone size and orientation.
                    // Computes the larger size between width and height to ensure all points belongs to the hittable zone
                    // The visual points and hitboxes are separated due to the need of a precise pointer input.
                    // Should be synchronized using the [computePointPosition] function that relies on common
                    // center to output the points position on screen

                    val w = size.width.toFloat()
                    val h = size.height.toFloat()
                    center = Offset(w / 2f, h / 2f)
                }
        ) {
            /**
             * Main Canva, draws the circles, and sub nests by recursivity
             *
             * if the user is dragging a point, I draw it in the offset of where the finger is.
             * if the user has hovered a point for more than 500ms, a radial circle overlay spawns and indicates that
             * it can release to merge the 2 points
             */
            key(recomposeTrigger) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX = -offset.value.x * zoom.value
                            translationY = -offset.value.y * zoom.value
                            scaleX = zoom.value
                            scaleY = zoom.value
                            rotationZ = angle.value
                            transformOrigin = TransformOrigin(0f, 0f)
                        }

                ) {

                    NestOverlay(
                        center = center,
                        depth = 1,
                        nest = currentNest,
                        preventBgErasing = true,
                        showConfiguratorDecorations = true,
                        forceShowAllActionsInCurrentNest = true
                    )

                    // Live Nest: semi-transparent target nest preview at the selected point (nest editor only).
                    selectedPoint?.let { p ->
                        val liveTargetId = p.liveNestTargetNestId ?: return@let
                        val nestedNest = nests.find { it.id == liveTargetId } ?: return@let
                        val nestScale = p.liveNestScale ?: 0.5f
                        val scaledNest = nestedNest scaledBy nestScale
                        val hostCenter = if (isDragging) {
                            selectedPointTempOffset.value
                        } else {
                            p.computePosition(nests, center)
                        }

                        NestOverlay(
                            modifier = Modifier.graphicsLayer {
                                alpha = 0.4f
                            },
                            center = hostCenter,
                            depth = 1,
                            nest = scaledNest,
                            preventBgErasing = true,
                            showConfiguratorDecorations = true,
                            forceShowAllActionsInCurrentNest = true
                        )
                    }

                    // Animated Selected point
                    if (selectedPoint != null) {
                        PointIcon(
                            center = selectedPointTempOffset.value,
                            depth = 1,
                            point = selectedPoint!!,
                            selected = true,
                            preventBgErasing = true,
                            showConfiguratorDecorations = true
                        )
                    }

                    // Glow that indicates the merge
                    if (isDragging && closestHoveredTempOffset != null && ableToLaunchHoverAction) {
                        GlowOverlay(
                            center = closestHoveredTempOffset!!,
                            color = primaryColor,
                            radius = hoveredPointRadialGradientProgress.dp
                        )
                    }
                }
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit, isInDragAroundMode, nestId, filteredPoints) {
                        if (isInDragAroundMode) {
                            detectTransformGestures(true) { centroid, pan, gestureZoom, gestureRotate ->


                                val oldScale = zoom.value
                                val newScale = zoom.value * gestureZoom

                                // For natural zooming and rotating, the centroid of the gesture should
                                // be the fixed point where zooming and rotating occurs.
                                // We compute where the centroid was (in the pre-transformed coordinate
                                // space), and then compute where it will be after this delta.
                                // We then compute what the new offset should be to keep the centroid
                                // visually stationary for rotating and zooming, and also apply the pan.
                                scope.launch {
                                    offset.snapTo(
                                        (offset.value + centroid / oldScale).rotateBy(gestureRotate) -
                                                (centroid / newScale + pan / oldScale)
                                    )
                                    zoom.snapTo(newScale)
                                    angle.snapTo(angle.value + gestureRotate)
                                }
                            }
                        } else {
                            detectDragGestures(
                                onDragStart = { tapOffset ->
                                    val transformedOffset = tapOffset.transformPointerCoordinates()

                                    var closest: Point? = null
                                    var best = Float.MAX_VALUE

                                    // Can only select points on the same nest
                                    filteredPoints.forEach { p ->
                                        val pointOffset = p.computePosition(
                                            nests = nests,
                                            center = center
                                        )
                                        val dist = hypot(
                                            transformedOffset.x - pointOffset.x,
                                            transformedOffset.y - pointOffset.y
                                        )

                                        if (dist < best) {
                                            best = dist
                                            closest = p
                                        }
                                    }

                                    select(if (best <= TOUCH_THRESHOLD_PX) closest else null)

                                    selectedPoint?.let {
                                        scope.launch {
                                            selectedPointTempOffset.snapTo(transformedOffset)
                                        }
                                    }
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val transformedPosition = change.position.transformPointerCoordinates()

                                    // Update the selected point offset in real time (the dragging thing)
                                    selectedPoint?.let { p ->

                                        val newPosition: Offset = if (freeMoveDraggedPoint) {
                                            transformedPosition
                                        } else {
                                            val newPointValues = computePointMoved(
                                                point = p,
                                                pos = transformedPosition
                                            )

                                            p.copy(
                                                offset = newPointValues ?: p.offset
                                            ).computePosition(
                                                nests = nests,
                                                center = center
                                            )
                                        }

                                        scope.launch {
                                            selectedPointTempOffset.snapTo(newPosition)
                                        }
                                    }


                                    var closest: Point? = null
                                    var best = Float.MAX_VALUE

                                    // Can only see points on the same nest
                                    filteredPoints.filter { it.id != selectedPoint?.id }
                                        .forEach { p ->

                                            val pointOffset = p.computePosition(
                                                nests = nests,
                                                center = center
                                            )
                                            val dist = hypot(
                                                transformedPosition.x - pointOffset.x,
                                                transformedPosition.y - pointOffset.y
                                            )

                                            if (dist < best) {
                                                best = dist
                                                closest = p
                                            }
                                        }

                                    closestHoveredPoint =
                                        if (best <= TOUCH_THRESHOLD_PX) closest else null

                                },
                                onDragEnd = {
                                    selectedPoint?.let { p ->
                                        val position = selectedPointTempOffset.value


                                        // 1) On finger release; if the user has hovered another point for long enough, (the glow overlay)
                                        //    do the computation to merge the 2 points
                                        if (ableToLaunchHoverAction && closestHoveredPoint != null) {

                                            // The hovered point
                                            val closest = closestHoveredPoint!!

                                            // When the action is to open a nest, put the point in that Nest
                                            if (closest.action is Action.OpenCircleNest) {
                                                pointsService.editPoint(p.id) { old ->
                                                    val targetNestId = (closest.action as Action.OpenCircleNest).nestId
                                                    old.copy(nestId = targetNestId)
                                                }
                                            } else {
                                                val newNestId = pointsService.addNest()

                                                pointsService.addPoint(false) { id ->
                                                    Point(
                                                        offset = closest.offset,
                                                        nestId = newNestId,
                                                        action = Action.OpenCircleNest(
                                                            newNestId
                                                        ),
                                                        id = id
                                                    )
                                                }

                                                // Creates a new go parent nest that'll be put on top of the nest, to easily exit this nest
                                                pointsService.addPoint(false) { id ->
                                                    Point(
                                                        offset = Offset(0f, 50f),
                                                        nestId = newNestId,
                                                        action = Action.GoParentNest,
                                                        id = id,
                                                        liveNestTargetNestId = if (createLiveNestByDefaultWhenCreatingOpenCircleNestPoint) newNestId else null
                                                    )
                                                }

                                                pointsService.editPoint(p.id) { old ->
                                                    old.copy(nestId = newNestId)
                                                }

                                                pointsService.editPoint(closest.id) { old ->
                                                    old.copy(nestId = newNestId)
                                                }
                                            }
                                        } else {
                                            // 2) No merging, just normal dragging and dropping

                                            p.updatePosition(position)

                                            if (autoSeparatePoints) {
                                                autoSeparate(
                                                    points = points,
                                                    nest = currentNest,
                                                    draggedPoint = p
                                                )
                                            }

                                            // Compute final snapped position
                                            val finalOffset = p.computePosition(nests, center)

                                            if (freeMoveDraggedPoint) {
                                                animateHomingTempOffset(finalOffset)
                                            }
                                        }

                                        // Clear dragging state and other points residues
                                        closestHoveredPoint = null
                                        ableToLaunchHoverAction = false
                                    }
                                }
                            )
                        }
                    }
                    .pointerInput(isInManualPlacementMode, isInDragAroundMode, nestId, filteredPoints) {
                        if (!isInDragAroundMode) {
                            detectTapGestures(
                                onTap = { tapOffset ->
                                    val transformedOffset = tapOffset.transformPointerCoordinates()

                                    // Manual placement mode: place the current queued app where user tapped
                                    if (isInManualPlacementMode) {
                                        val action = manualPlacementQueue.first()

                                        val newLiveNest =
                                            if (action is Action.OpenCircleNest && createLiveNestByDefaultWhenCreatingOpenCircleNestPoint) {
                                                action.nestId
                                            } else null

                                        pointsService.addPoint { id ->

                                            Point(
                                                id = id,
                                                offset = tapOffset,
                                                action = action,
                                                nestId = nestId,
                                                liveNestTargetNestId = newLiveNest
                                            )
                                        }


//                                            if (autoSeparatePoints) autoSeparate(
//                                                points = points,
//                                                nestId = nestId,
//                                                circle = circles.find { it.id == circleId },
//                                                draggedPoint = point
//                                            )

                                        manualPlacementQueue = manualPlacementQueue.drop(1)
                                    }

                                    // Normal tap mode
                                    var tapped: Point? = null
                                    var best = Float.MAX_VALUE

                                    filteredPoints.forEach { p ->
                                        logV(SWIPE_TAG) { "Checking point ${p.id}" }

                                        val pointPos = p.computePosition(nests, center)
                                        val dist = hypot(transformedOffset.x - pointPos.x, transformedOffset.y - pointPos.y)

                                        if (dist < best) {
                                            best = dist
                                            tapped = p
                                        }
                                    }

                                    logV(SWIPE_TAG) { "Best: $best, tapped: $tapped" }

                                    select(
                                        if (best <= TOUCH_THRESHOLD_PX)
                                            if (selectedPoint?.id == tapped?.id) {
                                                // Same point tapped -> if circle next, open it, else edit point
                                                if (selectedPoint?.action is Action.OpenCircleNest) {
                                                    nestNavigation.goToNest((selectedPoint?.action as Action.OpenCircleNest).nestId)
                                                    null
                                                } else {
                                                    showEditDialog = selectedPoint
                                                    tapped
                                                }

                                            } else tapped
                                        else null
                                    )

                                    selectedPoint?.let {
                                        recenterSelectedTempOffset()
                                    }
                                }
                            )
                        }
                    }
            )
        }
    }

    AnimatedVisibility(showSubNestsSlider) {
        SettingsSlider(
            setting = SwipeMapSettingsStore.subNestDefaultRadius,
            modifier = Modifier
                .height(50.dp)
                .width(150.dp)
                .offset(x = 20.dp, y = 50.dp)
        )
    }


    if (showAddDialog) {
        AddPointDialog(
            onDismiss = {
                showAddDialog = false
            },
            onMultipleActionsSelected = { actions ->
                manualPlacementQueue = actions
                showAddDialog = false
            }
        )
    }

    if (showEditDialog != null) {
        val editPoint = showEditDialog!!

        EditPointSheet(
            point = editPoint,
            onDismiss = {
                showEditDialog = null
                iconsViewModel.reloadIcon(editPoint)
            },
        ) { newPoint ->
            iconsViewModel.reloadIcon(newPoint)

            pointsService.editPoint(newPoint.id) { newPoint }

            select(newPoint)
            showEditDialog = null
        }
    }


    if (showNestManagementDialog) {
        NestManagementDialog(
            onDismissRequest = { showNestManagementDialog = false },
            onSelect = {
                nestNavigation.goToNest(it.id)
                deselect()
                showNestManagementDialog = false
            }
        )
    }

    AppPreviewTitle(
        point = selectedPoint,
        topPadding = 100.dp,
        showLabel = true,
        showIcon = true
    )

    if (isInManualPlacementMode) {
        val appName = when (val currentAction = manualPlacementQueue.first()) {
            is Action.LaunchApp -> {
                ctx.packageManager.runCatching {
                    getApplicationLabel(
                        getApplicationInfo(currentAction.packageName, 0)
                    ).toString()
                }.getOrDefault(currentAction.packageName)
            }

            else -> currentAction::class.simpleName ?: ""
        }
        val remaining = manualPlacementQueue.size

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = stringResource(R.string.place_app_where, appName),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
                Text(
                    text = stringResource(R.string.multi_select_count, remaining),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
                Text(
                    text = stringResource(R.string.tap_circle_to_place),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }
        }
    }

    if (showEditDefaultPoint) {
        EditPointSheet(
            point = defaultPoint,
            isDefaultEditing = true,
            onDismiss = {
                showEditDefaultPoint = false
            }
        ) {
            pointsService.set(newDefaultPoint = it)
            iconsViewModel.reloadAllPointsIcons()

            showEditDefaultPoint = false
        }
    }

    if (showResetPointsAndNestsDialog) {
        UserValidation(
            title = stringResource(R.string.reset_all_points),
            message = stringResource(R.string.reset_all_points_desc),
            onDismiss = { showResetPointsAndNestsDialog = false }
        ) {
            pointsService.reset(
                resetPoints = true,
                resetNests = true,
                resetDefaultPoint = true
            )

            deselect()
            showResetPointsAndNestsDialog = false
            showResetPointsAndNestsDialog = false
        }
    }

    /**
     * Debug Infos section
     * Shows various information about the current settings state, may be unreadable when lots of points
     */
    DebugZone(DebugSettingsStore.settingsDebugInfo) {
        Text("isDragging: $isDragging")
        Text("nests id: $nestId")
        Text("current nests id: ${currentNest.id}")
        Text("nests number: ${nests.size}")
        Text("currentNest shapes number: ${currentNest.intersectionShapes.size}")
        Text("current shapes: ${currentNest.intersectionShapes}")
        Text("closest hovered point: $closestHoveredTempOffset")
        Text("current nest: $currentNest")
        selectedPoint?.let { Text(it.toString()) }
    }
}