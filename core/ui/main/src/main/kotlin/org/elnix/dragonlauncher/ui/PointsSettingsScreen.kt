@file:Suppress("RedundantVisibilityModifier")

package org.elnix.dragonlauncher.ui

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
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
import io.github.elnix90.logging.logD
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.Constants
import org.elnix.dragonlauncher.base.Constants.Settings.COLLIDING_SHAPE_THRESHOLD_PX
import org.elnix.dragonlauncher.base.Constants.Settings.TOUCH_THRESHOLD_PX
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Point
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
import org.elnix.dragonlauncher.ktx.distance
import org.elnix.dragonlauncher.ktx.redoTransformations
import org.elnix.dragonlauncher.ktx.rotateBy
import org.elnix.dragonlauncher.ktx.undoTransformations
import org.elnix.dragonlauncher.models.IconsViewModel
import org.elnix.dragonlauncher.models.InitializationViewModel
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
import org.elnix.dragonlauncher.ui.actions.rememberPointIconBitmaps
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.animation.bouncySpec
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
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun PointsSettingsScreen(
    iconsViewModel: IconsViewModel = activityViewModel(),
    pointsViewModel: PointsViewModel = activityViewModel(),
    initializationViewModel: InitializationViewModel = activityViewModel(),
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
    val aPointIsSelected = selectedPoint != null

    fun select(id: Int?) {
        pointsService.select(id)
    }

    fun deselect() {
        pointsService.select(null)
    }

    fun toggleDragAroundMode(checked: Boolean) {
        scope.launch {
            PrivateSettingsStore.isInDragAroundMode.set(ctx, checked)
        }
        if (checked) {
            deselect()
        }
    }

    var recomposeTrigger by remember { mutableIntStateOf(0) }

    var closestHoveredPoint by remember { mutableStateOf<Point?>(null) }
    var closestHoveredTempOffset by remember { mutableStateOf<Offset?>(null) }
    var ableToLaunchHoverAction by remember { mutableStateOf(false) }

    val hoveredPointRadialGradientProgress by animateFloatAsState(
        targetValue = if (ableToLaunchHoverAction) Constants.Settings.HOVER_GRADIENT_RADIUS else 1f
    )


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

    var showShapeManagementDialog by remember { mutableStateOf(false) }

    fun computePointMoved(
        point: Point,
        normalizedOffset: Offset
    ): Pair<Offset, Int?> {
        // Early return: if use don't want to snap to shapes, no need to compute them as it is a bit expensive
        if (!snapPoints) return normalizedOffset to null

        val newPointRaw: Point = point.copy(
            offset = normalizedOffset,
            collidingShapeId = null
        )

        /**
         * A set of all offsets the point could have if it collides with the shapes.
         * Later on I'll take the closest one, and if it is < [COLLIDING_SHAPE_THRESHOLD_PX]
         */
        val collidingShapesOffsets: Set<Pair<Offset, Int>> = currentNest.intersectionShapes.mapTo(mutableSetOf()) { shape ->
            pointsService.computePointOffset(
                newPointRaw.copy(collidingShapeId = shape.id)
            ) to shape.id
        }


        var minOffsetShapeId: Int? = null
        var minOffset: Offset? = null
        var minDistSquared: Float = Float.MAX_VALUE

        for ((offset, shapeId) in collidingShapesOffsets) {
            val offsetLength = offset.getDistanceSquared()
            if (offsetLength < minDistSquared) {
                minDistSquared = offsetLength
                minOffsetShapeId = shapeId
                minOffset = offset
            }
        }

        val minOffsetLength: Float = sqrt(minDistSquared)
        return if (minOffsetLength < COLLIDING_SHAPE_THRESHOLD_PX) {
            minOffset!! to minOffsetShapeId
        } else {
            normalizedOffset to null
        }
    }


    val handleBack = {
        if (isInManualPlacementMode) manualPlacementQueue = emptyList()
        else if (selectedPoint != null) deselect()
        else if (nestId != 0) nestNavigation.goBack()
        else if (isEditing) isEditing = false
        else onBack()
    }
    BackHandler(onBack = handleBack)


    val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val zoom = remember { Animatable(1f) }
    val angle = remember { Animatable(0f) }

    /**
     * Undo normalization of a point offset and returns the `transformed` [Offset] corresponding to the offset in local coordinates space, when transformations are applied
     *
     * @return
     */
    fun Offset.undoNormalization(): Offset = this + center

    /**
     * Transform a pointer position [Offset] into its coordinated, after applying [offset], [zoom] and [angle] transformations.
     * The resulted [Offset] is meant to be used within the [graphicsLayer] block in the Main drawing block
     */
    fun Offset.transform(): Offset = this.undoTransformations(
        angle = { angle.value },
        zoom = { zoom.value },
        offset = { offset.value }
    )

    /**
     * Undo transformation of the above [transform] function, basically applying the same calculation in the opposite direction.
     * It provides real screen [Offset] from a transformed [Offset]
     * It is uses in the [pointerInput] block in this file, to provide the real screen position of the computed point offset, when user want to snap point to the shapes
     *
     * May be removed in the future
     */
    fun Offset.undoTransformation(): Offset = undoNormalization().redoTransformations(
        angle = { -angle.value },
        zoom = { -zoom.value },
        offset = { -offset.value }
    )

    /**
     * Compute position of a point in the screen.
     *
     * @return
     */
    fun Point.computePosition(): Offset =
        pointsService.computePointOffset(this).undoNormalization()


    LaunchedEffect(closestHoveredPoint) {
        ableToLaunchHoverAction = false
        closestHoveredPoint?.let {
            closestHoveredTempOffset = it.computePosition()
            delay(Constants.Settings.HOVER_POINT_DURATION.milliseconds)
            ableToLaunchHoverAction = true
        }
    }

    /**
     * Holds an Offset and provides helper functions and value to manage it in the [PointsSettingsScreen] scope.
     */
    class TransformedOffset(
        /**
         * Original offset, in normal screen coordinates
         * It will be transformed to give the actual useful values
         */
        val offset: Offset
    ) {
        /**
         * Transformed offset, represents the coordinated in space of the [offset] after undoing the
         * transformations of [angle], [zoom], and [offset] that are only for visual in the settings screen
         */
        public val transformedOffset: Offset by lazy {
            this.offset.transform()
        }

        /**
         * Represents the offset of the point, if you do not account for both the [angle], [zoom], and [offset] transformations and the [center]
         * in the middle ot the screen.
         *
         * ### **It's the offset you want to save into the points property**
         * as it can be interpreted by the [org.elnix.dragonlauncher.points.PointsService] and be
         * converted back to screen coordinates.
         */
        public val normalizedOffset: Offset by lazy {
            this.transformedOffset - center
        }

        /**
         * Computes the closest point relative to this [transformedOffset].
         * @see org.elnix.dragonlauncher.points.PointsService.computeClosest
         */
        public val bestP: Point? by lazy {
            pointsService.computeClosest(this.normalizedOffset, currentNest.id)
        }

        private val distance: Float by lazy {
            val betsPOffset = this.bestP?.offset ?: return@lazy 0f
            distance(betsPOffset, this.normalizedOffset)
        }

        public inline infix fun ifDistanceIsSmallEnough(block: () -> Point?): Point? = if (distance <= TOUCH_THRESHOLD_PX) block() else null
    }

    fun Offset.toTr(): TransformedOffset = TransformedOffset(this)


    val selectedPointTempOffsetAnimatable = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var selectedPointTempOffset: Offset? by remember { mutableStateOf(null) }
    val isDragging = selectedPointTempOffset != null


    val filteredPoints by remember(points, nestId) {
        derivedStateOf {
            points.filter { it.nestId == nestId }
        }
    }

    // Shows all points, excepted the currently dragged one, if any, to draw them inside the canvas
//    val displayedFilteredPoints by remember(points, isDragging, selectedPoint?.id) {
//        derivedStateOf {
//            if (!isDragging || selectedPoint == null) points
//            else points - selectedPoint!!
//        }
//    }
    val iconBitmaps = rememberPointIconBitmaps()

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

                UndoRedoBlock(pointsService.undoRedo)


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
                                    offset.animateTo(Offset.Zero, bouncySpec())
                                }

                                ResetZoom -> scope.launch {
                                    zoom.animateTo(1f, bouncySpec())
                                }

                                ResetRotation -> scope.launch {
                                    angle.animateTo(0f, bouncySpec())
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
                    onCheckedChange = ::toggleDragAroundMode,
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
                                TODO()
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
                        nest = currentNest,
                        preventBgErasing = true,
                        showConfiguratorDecorations = true,
                        forceShowAllActionsInCurrentNest = true,
                        iconBitmaps = iconBitmaps
                    )

                    // Live Nest: semi-transparent target nest preview at the selected point (nest editor only).
                    selectedPoint?.let { p ->
                        // Animated Selected point
                        selectedPointTempOffset?.let { selectedPointTempOffset ->
                            PointIcon(
                                center = selectedPointTempOffset.transform(),
                                point = p,
                                selected = true,
                                preventBgErasing = true,
                                showConfiguratorDecorations = true,
                                iconBitmaps = iconBitmaps
                            )
                        }

                        val liveTargetId = p.liveNestTargetNestId ?: return@let
                        val nestedNest = nests.find { it.id == liveTargetId } ?: return@let
                        val nestScale = p.liveNestScale ?: 0.5f
                        val scaledNest = nestedNest scaledBy nestScale
                        val hostCenter = if (isDragging) {
                            selectedPointTempOffset!!.transform()
                        } else {
                            p.computePosition()
                        }

                        NestOverlay(
                            modifier = Modifier.graphicsLayer {
                                alpha = 0.4f
                            },
                            center = hostCenter,
                            nest = scaledNest,
                            preventBgErasing = true,
                            showConfiguratorDecorations = true,
                            forceShowAllActionsInCurrentNest = true,
                            iconBitmaps = iconBitmaps
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
                                    val tr = tapOffset.toTr()
                                    val newSelectedPoint = tr ifDistanceIsSmallEnough { tr.bestP }

                                    selectedPointTempOffset = tr.offset
                                    select(newSelectedPoint?.id)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val tr = change.position.toTr()

                                    // Update the selected point offset in real time (the dragging thing)
                                    selectedPoint?.let { p ->
                                        selectedPointTempOffset = if (freeMoveDraggedPoint) {
                                            tr.offset
                                        } else {
                                            val (newOffsetNormalized, _) = computePointMoved(
                                                point = p,
                                                normalizedOffset = tr.normalizedOffset
                                            )
                                            newOffsetNormalized.undoTransformation()
                                        }
                                    }

                                    val bestPExcept =
                                        if (selectedPoint != null) tr.bestP
                                        else pointsService.computeClosest(tr.normalizedOffset, nestId)

                                    closestHoveredPoint = tr ifDistanceIsSmallEnough { bestPExcept }
                                },
                                onDragEnd = {

                                    if (selectedPoint != null && selectedPointTempOffset != null) {
                                        val selectedPoint = selectedPoint!!
                                        val tr = selectedPointTempOffset!!.toTr()

                                        // 1) On finger release; if the user has hovered another point for long enough, (the glow overlay)
                                        //    do the computation to merge the 2 points
                                        if (ableToLaunchHoverAction && closestHoveredPoint != null) {

                                            // The hovered point
                                            val closest = closestHoveredPoint!!

                                            // When the action is to open a nest, put the point in that Nest
                                            if (closest.action is Action.OpenCircleNest) {
                                                pointsService.editPoint(selectedPoint.id) { old ->
                                                    val targetNestId = (closest.action as Action.OpenCircleNest).nestId
                                                    old.copy(nestId = targetNestId)
                                                }
                                            } else {
                                                val newNestId = pointsService.addNest()

                                                // Creates a new nest point, to open the newly created Nest
                                                val newNestPointId = pointsService.addPoint(false) { id ->
                                                    Point(
                                                        offset = closest.offset,
                                                        nestId = currentNest.id,
                                                        action = Action.OpenCircleNest(
                                                            newNestId
                                                        ),
                                                        id = id
                                                    )
                                                }
                                                select(newNestPointId)

                                                // Creates a new go parent nest that'll be put on top of the nest, to easily exit this nest
                                                pointsService.addPoint(false) { id ->
                                                    Point(
                                                        offset = Offset(0f, 150f),
                                                        nestId = newNestId,
                                                        action = Action.GoParentNest,
                                                        id = id,
                                                        liveNestTargetNestId = if (createLiveNestByDefaultWhenCreatingOpenCircleNestPoint) newNestId else null
                                                    )
                                                }

                                                // Update both merged points nestId to the one of the new nest
                                                pointsService.editPoint(selectedPoint.id) { old -> old.copy(nestId = newNestId) }
                                                pointsService.editPoint(closest.id) { old -> old.copy(nestId = newNestId) }
                                            }
                                        } else {
                                            // 2) No merging, just normal dragging and dropping
                                            val (newOffsetNormalized, shapeId) = computePointMoved(selectedPoint, tr.normalizedOffset)

                                            pointsService.editPoint(selectedPoint.id) { old ->
                                                old.copy(
                                                    offset = newOffsetNormalized,
                                                    collidingShapeId = shapeId
                                                )
                                            }

                                            if (autoSeparatePoints) {
                                                TODO()
                                            }

                                            if (freeMoveDraggedPoint) {
                                                scope.launch {
                                                    selectedPointTempOffsetAnimatable.animateTo(
                                                        targetValue = newOffsetNormalized.undoTransformation(),
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    selectedPointTempOffset = null
                                    closestHoveredPoint = null
                                    ableToLaunchHoverAction = false
                                },
                                onDragCancel = {
                                    selectedPointTempOffset = null
                                    closestHoveredPoint = null
                                    ableToLaunchHoverAction = false
                                }
                            )
                        }
                    }
                    .pointerInput(isInManualPlacementMode, isInDragAroundMode, nestId, filteredPoints) {
                        if (!isInDragAroundMode) {
                            detectTapGestures(
                                onTap = { tapOffset ->
                                    val tr = tapOffset.toTr()

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
                                                offset = tr.normalizedOffset,
                                                action = action,
                                                nestId = nestId,
                                                liveNestTargetNestId = newLiveNest
                                            )
                                        }

                                        if (autoSeparatePoints) {
                                            TODO()
                                        }

                                        manualPlacementQueue = manualPlacementQueue.drop(1)
                                    }

                                    val newSelectedPoint = tr ifDistanceIsSmallEnough {
                                        if (selectedPoint?.id == tr.bestP?.id) {
                                            // Same point tapped -> if circle next, open it, else edit point
                                            if (selectedPoint?.action is Action.OpenCircleNest) {
                                                nestNavigation.goToNest((selectedPoint?.action as Action.OpenCircleNest).nestId)
                                                null
                                            } else {
                                                showEditDialog = selectedPoint
                                                tr.bestP
                                            }

                                        } else tr.bestP
                                    }

                                    select(newSelectedPoint?.id)
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
            onActionsSelected = { actions ->
                toggleDragAroundMode(false)
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

            select(newPoint.id)
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
            pointsService.editDefaultPoint(newDefaultPoint = it)
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

            initializationViewModel.initialize()
            deselect()
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
