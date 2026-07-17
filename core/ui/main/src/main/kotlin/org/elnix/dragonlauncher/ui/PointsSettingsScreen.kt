@file:Suppress("RedundantVisibilityModifier")

package org.elnix.dragonlauncher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elnix90.logging.POINTS_TAG
import io.github.elnix90.logging.logD
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.Constants
import org.elnix.dragonlauncher.base.Constants.Settings.COLLIDING_SHAPE_THRESHOLD_PX
import org.elnix.dragonlauncher.base.Constants.Settings.TOUCH_THRESHOLD_PX
import org.elnix.dragonlauncher.base.cache.NestIntersectionShapesPathCache
import org.elnix.dragonlauncher.base.cache.PointStableCache
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.CustomGlow
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape.Companion.highlightedIfSelected
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.navigaton.ManipulationSystem
import org.elnix.dragonlauncher.base.resolveShape
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.base.util.ColorUtils.alphaMultiplier
import org.elnix.dragonlauncher.enumsui.toggle.MoveAroundTools
import org.elnix.dragonlauncher.enumsui.toggle.MoveAroundTools.Center
import org.elnix.dragonlauncher.enumsui.toggle.MoveAroundTools.ResetRotation
import org.elnix.dragonlauncher.enumsui.toggle.MoveAroundTools.ResetZoom
import org.elnix.dragonlauncher.enumsui.toggle.NestEditTools
import org.elnix.dragonlauncher.enumsui.toggle.NestEditTools.EnterNest
import org.elnix.dragonlauncher.enumsui.toggle.NestEditTools.GoParentNest
import org.elnix.dragonlauncher.enumsui.toggle.NestEditTools.NestManagement
import org.elnix.dragonlauncher.enumsui.toggle.PointsEditTools
import org.elnix.dragonlauncher.enumsui.toggle.PointsEditTools.AutoMerge
import org.elnix.dragonlauncher.enumsui.toggle.PointsEditTools.AutoSeparate
import org.elnix.dragonlauncher.enumsui.toggle.PointsEditTools.SnapPoints
import org.elnix.dragonlauncher.enumsui.toggle.SelectedPointEditTools
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.distanceTo
import org.elnix.dragonlauncher.ktx.px
import org.elnix.dragonlauncher.ktx.rotateBy
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.models.IconsViewModel
import org.elnix.dragonlauncher.models.InitializationViewModel
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.settings.stores.map.BehaviorSettingsStore.createLiveNestByDefaultWhenCreatingOpenCircleNestPoint
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore.isInDragAroundMode
import org.elnix.dragonlauncher.settings.stores.map.SwipeMapSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.animation.bouncySpec
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.components.AnimatedFab
import org.elnix.dragonlauncher.ui.base.components.RowWithScrollIndicator
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.components.ToggleAnimatedFab
import org.elnix.dragonlauncher.ui.base.modifiers.selfAlignHorizontally
import org.elnix.dragonlauncher.ui.components.IntersectionShape
import org.elnix.dragonlauncher.ui.components.SelectedPointsTopBar
import org.elnix.dragonlauncher.ui.composition.LocalNestDebugOverlay
import org.elnix.dragonlauncher.ui.dialogs.AddPointDialog
import org.elnix.dragonlauncher.ui.dialogs.EditPointSheet
import org.elnix.dragonlauncher.ui.dialogs.NestManagementDialog
import org.elnix.dragonlauncher.ui.dragon.dialogs.UserValidation
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.helpers.DebugZone
import org.elnix.dragonlauncher.ui.helpers.SelfCheckNestPresent
import org.elnix.dragonlauncher.ui.helpers.UndoRedoBlock
import org.elnix.dragonlauncher.ui.helpers.customobjects.GlowOverlay
import org.elnix.dragonlauncher.ui.helpers.customobjects.toPath
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.helpers.settings.SpecialSettingsTitle
import org.elnix.dragonlauncher.ui.helpers.swipe.NestOverlay
import org.elnix.dragonlauncher.ui.helpers.swipe.PointIcon
import org.elnix.dragonlauncher.ui.remembers.rememberDrawScopeText
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
public fun PointsSettingsScreen(
    iconsViewModel: IconsViewModel = activityViewModel(),
    pointsViewModel: PointsViewModel = activityViewModel(),
    drawerViewModel: DrawerViewModel = activityViewModel(),
    initializationViewModel: InitializationViewModel = activityViewModel(),
    onAdvSettings: () -> Unit,
    onNestEdit: (nest: Int) -> Unit,
    onBack: () -> Unit
) {
    val density = LocalDensity.current
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val pointsService = pointsViewModel.pointsService
    val defaultPoint by pointsService.defaultPoint.asState()

    val points by pointsService.points.collectAsState()
    val nests by pointsService.nests.collectAsState()

    val showAdvancedEditTools by SwipeMapSettingsStore.showAdvancedPointTools.asState()
    val isInDragAroundMode by isInDragAroundMode.asState()

    val primaryColor = MaterialTheme.colorScheme.primary

    val snapPoints by UiSettingsStore.snapPoints.asState()
    val autoSeparatePoints by UiSettingsStore.autoSeparatePoints.asState()
    val autoMerge by UiSettingsStore.autoMerge.asState()

    val createLiveNestByDefaultWhenCreatingOpenCircleNestPoint by createLiveNestByDefaultWhenCreatingOpenCircleNestPoint.asState()

    var center by remember { mutableStateOf(Offset.Zero) }

    val selectedPointsIds: List<Int> by pointsService.selectedPointsIds.asState()
    val aSinglePointIsSelected = selectedPointsIds.size == 1

    var temporarySelectedShape by remember { mutableStateOf<Int?>(null) }

    var closestHoveredPoint by remember { mutableStateOf<Point?>(null) }
    var closestHoveredTempOffset by remember { mutableStateOf<Offset?>(null) }
    var ableToLaunchHoverAction by remember { mutableStateOf(false) }

    val hoveredPointRadialGradientProgress by animateFloatAsState(
        targetValue = if (ableToLaunchHoverAction) Constants.Settings.HOVER_GRADIENT_RADIUS else 1f
    )


    var showEditDefaultPoint by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Int?>(null) }

    // Manual placement mode state (multi-select "Place one by one")
    var manualPlacementQueue by remember { mutableStateOf<List<Action>>(emptyList()) }
    val isInManualPlacementMode = manualPlacementQueue.isNotEmpty()
    var isEditing by remember { mutableStateOf(false) }

    var showNestManagementDialog by remember { mutableStateOf(false) }
    var showResetPointsAndNestsDialog by remember { mutableStateOf(false) }

    val rowsScrollStates = List(2) { rememberScrollState() }

    val nestsNavigationService = pointsViewModel.nestsNavigationService
    val nestId by nestsNavigationService.currentNestId.collectAsState()
    val currentNest = pointsService.findNestById(nestId)

    SelfCheckNestPresent()

    /**
     * Computes the new offset for the selected point.
     *
     * When [snapPoints] is `false`, it simply skips the computation and returns the normalized offset.
     * Otherwise, the function checks for each potential shapes the closest and determines the most probable shape to collide with
     *
     * TODO: when the point has already a shape, stick to it no matter what, except if the end point is really far away.
     *
     * @param point which point to move
     * @param normalizedOffset the offset in which the gesture ends (normalized)
     * @return the optional [shapeId][Int] the point will take if dropped here
     */
    fun computePointMoved(
        point: Point,
        normalizedOffset: Offset
    ): Int? {
        // Early return: if user don't want to snap to shapes, no need to compute them as it is a bit expensive
        if (!snapPoints) return point.shapeId

        val shapes = currentNest.intersectionShapes
        if (shapes.isEmpty()) return null

        val (minOffset, shapeId) = shapes
            .map { shape ->
                pointsService.computePointOffset(
                    point.copy(
                        offset = normalizedOffset,
                        shapeId = shape.id
                    )
                ) to shape.id
            }
            .minBy { (offset, _) -> offset distanceTo normalizedOffset }

        /**
         * The distance between the landing [Offset] and the finger's position
         */
        val minOffsetDistanceToNormalized: Float = minOffset distanceTo normalizedOffset

        logD(POINTS_TAG) { "normalizedOffset: $normalizedOffset; Min Offset : $minOffset, dist: $minOffsetDistanceToNormalized , shapeId: $shapeId" }

        return if (minOffsetDistanceToNormalized < COLLIDING_SHAPE_THRESHOLD_PX) {
            shapeId
        } else {
            null
        }
    }

    val manipulationSystem = retain { ManipulationSystem(center) }
    LaunchedEffect(center) {
        manipulationSystem.center = center
    }

    val offset = manipulationSystem.offset
    val angle = manipulationSystem.angle
    val zoom = manipulationSystem.zoom

    /**
     * Holds an Offset and provides helper functions and value to manage it in the [PointsSettingsScreen] scope.
     */
    class TransformedOffset(
        /**
         * Original offset, in normal screen coordinates
         * It will be transformed to give the actual useful values
         */
        private val offset: Offset
    ) {
        /**
         * Transformed offset, represents the coordinated in space of the [offset] after undoing the
         * transformations of [angle], [zoom], and [offset] that are only for visual in the settings screen
         */
        public val transformedOffset: Offset by lazy {
            manipulationSystem.transform(this.offset)
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
            manipulationSystem.normalize(this.transformedOffset)
        }

        /**
         * Computes the closest point relative to this [transformedOffset].
         * @see org.elnix.dragonlauncher.points.PointsService.computeClosest
         */
        public val bestP: Point? by lazy {
            pointsService.computeClosest(this.normalizedOffset, nestId)
        }

        private val distance: Float by lazy {
            val betsPOffset = this.bestP?.getPos() ?: return@lazy Float.MAX_VALUE
            betsPOffset distanceTo this.normalizedOffset
        }

        /**
         * Whether the distance to the closest point is inferior to an arbitrary [TOUCH_THRESHOLD_PX].
         *
         * TODO Make this threshold dependent on the [zoom]
         */
        private val distanceSmallEnough: Boolean by lazy { distance <= TOUCH_THRESHOLD_PX }


        /** Executes [block] if [distanceSmallEnough] */
        public inline infix fun ifDistanceIsSmallEnough(block: () -> Point?): Point? = if (distanceSmallEnough) block() else null

        override fun toString(): String =
            "TR(\n" +
                    "   offset = ${this.offset}\n" +
                    "   transformedOffset = $transformedOffset\n" +
                    "   normalizedOffset = $normalizedOffset\n" +
                    "   bestP = $bestP\n" +
                    "   distance = $distance${if (!distanceSmallEnough) " (Too Far!)" else ""}\n" +
                    ")"
    }

    fun Offset.toTr(): TransformedOffset = TransformedOffset(this)

    /**
     * Compute position of a point in the screen.
     *
     * ### NEVER TOUCH THAT AGAIN IT WORKS!!
     * Mb I touched it but it still works :)
     * @return the `transformed offset` of the point.
     */
    fun Point.computePosition(): Offset = manipulationSystem.undoBoth(this.getPos())


    val selectedPointTempOffset = retain { mutableStateMapOf<Int, Offset>() }


    fun select(id: Int) {
        pointsService.select(id)
        val point = pointsService.findPointById(id) ?: return
        selectedPointTempOffset[id] = point.computePosition()
    }

    fun deselect(id: Int) {
        pointsService.deselect(id)
        selectedPointTempOffset -= id
    }


    fun toggleDragAroundMode(checked: Boolean) {
        scope.launch {
            PrivateSettingsStore.isInDragAroundMode.set(ctx, checked)
        }
        if (checked) {
            pointsService.deselectAll()
        }
    }

    val recomposeTrigger by pointsService.recomposeTrigger.asState()
    LaunchedEffect(recomposeTrigger) {
        selectedPointsIds.forEach { id ->
            val point = pointsService.findPointById(id) ?: return@forEach
            selectedPointTempOffset[id] = point.computePosition()
        }
        selectedPointTempOffset.keys.forEach { key ->
            if (key !in selectedPointsIds) {
                selectedPointTempOffset.remove(key)
            }
        }
    }

    LaunchedEffect(closestHoveredPoint) {
        ableToLaunchHoverAction = false
        closestHoveredPoint?.let {
            closestHoveredTempOffset = it.computePosition()
            delay(Constants.Settings.HOVER_POINT_DURATION.milliseconds)
            ableToLaunchHoverAction = true
        }
    }

    val paths: SnapshotStateMap<IntersectionShape, Path> = remember { mutableStateMapOf() }

    fun addPath(shape: IntersectionShape) {
        paths[shape] = shape.shape.resolveShape().toPath(shape.getSize(density.density), density)
    }

    LaunchedEffect(currentNest.intersectionShapes) {
        paths.clear()
        currentNest.intersectionShapes.forEach { shape ->
            addPath(shape)
        }
    }

    val extraColors = LocalExtraColors.current

    val handleBack = {
        if (isInManualPlacementMode) manualPlacementQueue = emptyList()
        else if (selectedPointsIds.isNotEmpty()) pointsService.deselectAll()
        else if (nestId != 0) nestsNavigationService.goBack()
        else if (isEditing) isEditing = false
        else onBack()
    }
    BackHandler(onBack = handleBack)

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
                nestId = nestId,
                onSettings = onAdvSettings,
                onSelectAll = { pointsService.selectAll(nestId) },
                onEditDefaultPoint = { showEditDefaultPoint = true },
                onEditNest = {
                    pointsService.deselectAll()
                    onNestEdit(nestId)
                },
                onResetPoints = { showResetPointsAndNestsDialog = true },
                onGamble = {
                    repeat(it) {
                        pointsService.addPoint { id ->
                            val newOffset = Offset(
                                x = (-1000..1000).random().toFloat(),
                                y = (-1000..1000).random().toFloat()
                            )
                            val newAction = Action.LaunchApp(drawerViewModel.userApps.value.random())
                            Point(
                                id = id,
                                offset = newOffset,
                                nestId = nestId,
                                action = newAction,
                                shapeId = null
                            )
                        }
                    }
                },
                onBack = handleBack
            )
        },
        bottomContent = {
            if (showAdvancedEditTools) {
                RowWithScrollIndicator(rowsScrollStates[0]) {
                    val nestToGo =
                        if (selectedPointsIds.size == 1) {
                            val point = pointsService.findPointById(selectedPointsIds.first())
                            if (point != null && point.action is Action.OpenCircleNest) (point.action as Action.OpenCircleNest).nestId else null
                        } else null

                    val canGoNest = nestToGo != null
                    val canGoback = nestId != 0

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
                                nestsNavigationService.goBack()
                                pointsService.deselectAll()
                            }

                            EnterNest -> {
                                nestToGo?.let {
                                    nestsNavigationService.goToNest(it)
                                    pointsService.deselectAll()
                                }
                            }
                        }
                    }

                    Spacer(12.dp)

                    MultiSelectConnectedButtonRow(
                        entries = PointsEditTools.entries,
                        checked = {
                            when (it) {
                                SnapPoints -> snapPoints
                                AutoSeparate -> autoSeparatePoints
                                AutoMerge -> autoMerge
                            }
                        }
                    ) {
                        scope.launch {
                            when (it) {
                                SnapPoints -> UiSettingsStore.snapPoints.set(ctx, !snapPoints)
                                AutoSeparate -> UiSettingsStore.autoSeparatePoints.set(ctx, !autoSeparatePoints)
                                AutoMerge -> UiSettingsStore.autoMerge.set(ctx, !autoMerge)
                            }
                        }
                    }
                }

                Spacer(5.dp)

                RowWithScrollIndicator(rowsScrollStates[1]) {
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

                    UndoRedoBlock(pointsService.undoRedo)
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
                    checked = {
                        when (it) {
                            SelectedPointEditTools.Edit -> aSinglePointIsSelected
                            SelectedPointEditTools.Duplicate -> selectedPointsIds.isNotEmpty()
                            SelectedPointEditTools.Remove -> selectedPointsIds.isNotEmpty()
                        }
                    },
                    enabled = {
                        when (it) {
                            SelectedPointEditTools.Edit -> aSinglePointIsSelected
                            SelectedPointEditTools.Duplicate -> selectedPointsIds.isNotEmpty()
                            SelectedPointEditTools.Remove -> selectedPointsIds.isNotEmpty()
                        }
                    }
                ) { option ->
                    when (option) {
                        SelectedPointEditTools.Edit -> {
                            showEditDialog = selectedPointsIds.firstOrNull() ?: return@MultiSelectConnectedButtonRow
                        }

                        SelectedPointEditTools.Remove -> {

                            selectedPointsIds.forEach { id ->
                                pointsService.removePoint(id)
                            }
                            pointsService.deselectAll()

                        }

                        SelectedPointEditTools.Duplicate -> {
                            selectedPointsIds.forEach { id ->
                                val oldPoint = pointsService.findPointById(id) ?: return@MultiSelectConnectedButtonRow
                                val newId = pointsService.addPoint { newId ->
                                    oldPoint.copy(id = newId)
                                }
                                select(newId)
                                pointsService.autoSeparate(nestId, newId)
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
             * Main Canva, draws the circles, and sub nests by recursivity.
             *
             * Uses [graphicsLayer] to apply transformation of [offset], [zoom] and [angle] and provide an easy way to navigate in the canva
             *
             * - If the user drags a point, I draw it in the offset of where the finger is.
             * - If the user has hovered a point for more than 500ms, a radial circle overlay spawns and indicates
             *   that it can release to merge the 2 points
             * - If the selected point is a live nest, it is drawn in transparency on top of it.
             *   **Only if the nest isn't a OpenCircleNest that points to the same nest action**
             */
            key(selectedPointsIds, points.size, nests.size, defaultPoint) {
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
                    Canvas(Modifier.fillMaxSize()) {

                        repeat(2) { pass ->
                            paths.forEach { (shape, path) ->
                                val selected: Boolean = temporarySelectedShape == shape.id
                                this.IntersectionShape(
                                    path = path,
                                    shape = shape.highlightedIfSelected(selected, primaryColor),
                                    center = center,
                                    shapesColor = extraColors.shapes,
                                    erase = pass == 0
                                )
                            }
                        }

                        drawRect(color = Color.Blue.alphaMultiplier(0.2f))
                    }

                    NestOverlay(
                        center = center,
                        nest = currentNest,
                        preventBgErasing = true,
                        pointSettingsDisplay = true,
                        hideShapes = true
                    )

                    // Animated Selected point
                    selectedPointTempOffset.forEach { (id, offset) ->
                        val point = pointsService.findPointById(id) ?: return@forEach
                        val tr = offset.toTr()


                        if (LocalNestDebugOverlay.current) {
                            Canvas(Modifier.fillMaxSize()) {

                                val endOffset: Offset = if (point.shapeId == null) {
                                    center
                                } else {
                                    currentNest.intersectionShapes.firstOrNull { it.id == point.shapeId }?.let {
                                        center + it.offset
                                    } ?: center
                                }

                                drawLine(
                                    color = Color.White,
                                    start = endOffset,
                                    end = tr.transformedOffset
                                )
                            }
                        }
                        val pointSize = point.getSize(defaultPoint).px
                        val customText = rememberDrawScopeText(point.copy(offset = tr.normalizedOffset), pointSize, defaultPoint)

                        PointIcon(
                            center = tr.transformedOffset,
                            point = point,
                            selected = true,
                            pointSettingsDisplay = true,
                            preventBgErasing = true,
                            customText = customText
                        )


                        val liveTargetId = point.liveNestTargetNestId ?: return@forEach

                        // Don't draw if the action is opening the same nest it is displaying
                        if (point.action is Action.OpenCircleNest && (point.action as Action.OpenCircleNest).nestId == liveTargetId) return@forEach

                        val nestedNest = nests[liveTargetId] ?: return@forEach
                        val nestScale = point.liveNestScale ?: Point.defaultLiveNestScale
                        val scaledNest = nestedNest scaledBy nestScale

                        NestOverlay(
                            modifier = Modifier.graphicsLayer { alpha = 0.4f },
                            center = tr.transformedOffset,
                            nest = scaledNest,
                            preventBgErasing = true,
                            pointSettingsDisplay = true
                        )
                    }

                    // Glow that indicates the merge
                    if (closestHoveredTempOffset != null && ableToLaunchHoverAction) {
                        GlowOverlay(
                            center = closestHoveredTempOffset!!,
                            glow = CustomGlow(
                                color = primaryColor,
                                radius = hoveredPointRadialGradientProgress
                            )
                        )
                    }
                }
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit, isInDragAroundMode, nestId) {
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
                                    val newSelectedPoint = (tr ifDistanceIsSmallEnough { tr.bestP })

                                    // Only select if not already
                                    if (newSelectedPoint != null && newSelectedPoint.id !in selectedPointsIds) {
                                        select(newSelectedPoint.id)

                                        // Ensures the value for this point id is present in the offsets
                                        // Since it is a delegated property, its update comes right after the newt line of code runs,
                                        // and therefore it ain't present in the list
                                        // In Any cases, if it weren't to be in the list it would be overriden with the same value.
//                                        selectedPointTempOffset[newSelectedPoint.id] = newSelectedPoint.computePosition()
                                    }

//                                    selectedPointsIds.forEach { id ->
//                                        val point = pointsService.findPointById(id) ?: return@forEach
//                                        selectedPointTempOffset[id] = point.computePosition()
//                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val tr = change.position.toTr()

                                    // When dragging:
                                    // 1. the selected points are updated in real time to provide visual feedback
                                    // 2. If snapShapes is enabled, the closest shape should light up to indicate
                                    //    that when the user releases its finger, the point snaps to the shape
                                    // 3. If autoMerge is enabled, the closest point is analyzed and optionally light up
                                    //    to indicate a merge with the current dragged point


                                    // 1. selected points updating
                                    selectedPointsIds.forEach { id ->
                                        val previousOffset = selectedPointTempOffset[id] ?: return@forEach
                                        selectedPointTempOffset[id] = previousOffset + dragAmount
                                    }


                                    // 2. snapShapes
                                    if (snapPoints) {
                                        selectedPointTempOffset.forEach { (id, _) ->
                                            val point = pointsService.findPointById(id) ?: return@forEach
                                            val pos = selectedPointTempOffset[id] ?: return@forEach
                                            val tr = pos.toTr()
                                            val shapeId = computePointMoved(point, tr.normalizedOffset)
                                            temporarySelectedShape = shapeId
                                        }
                                    }


                                    // 3. autoMerge computing
                                    if (autoMerge) {
                                        val bestPExcept = pointsService.computeClosestExcept(
                                            ignoredPointId = selectedPointsIds.toTypedArray(),
                                            normalizedPos = tr.normalizedOffset,
                                            nestId = nestId
                                        )

                                        val bestPExceptOffset = bestPExcept?.offset ?: return@detectDragGestures

                                        closestHoveredPoint = if (bestPExceptOffset distanceTo tr.normalizedOffset <= TOUCH_THRESHOLD_PX) {
                                            bestPExcept
                                        } else null
                                    }
                                },
                                onDragEnd = {

                                    // 1) On finger release; if the user has hovered another point for long enough, (the glow overlay)
                                    //    do the computation to merge the 2 points
                                    if (ableToLaunchHoverAction && closestHoveredPoint != null) {
                                        val closest = closestHoveredPoint!!

                                        // When the action is to open a nest, put the point in that Nest
                                        if (closest.action is Action.OpenCircleNest) {
                                            // Put all selected points into that nest
                                            selectedPointTempOffset.forEach { (id, _) ->
                                                pointsService.editPoint(id) { old ->
                                                    val targetNestId = (closest.action as Action.OpenCircleNest).nestId
                                                    old.copy(nestId = targetNestId)
                                                }
                                            }
                                        } else {
                                            val newNestId = pointsService.addNest()

                                            // Creates a new nest point, to open the newly created Nest
                                            val newNestPointId = pointsService.addPoint(false) { id ->
                                                Point(
                                                    offset = closest.offset,
                                                    nestId = nestId,
                                                    action = Action.OpenCircleNest(
                                                        newNestId
                                                    ),
                                                    id = id,
                                                    liveNestTargetNestId = if (createLiveNestByDefaultWhenCreatingOpenCircleNestPoint) newNestId else null
                                                )
                                            }
                                            select(newNestPointId)

                                            // Creates a new go parent nest that'll be put on top of the nest, to easily exit this nest
                                            pointsService.addPoint(false) { id ->
                                                Point(
                                                    offset = Offset(0f, 150f),
                                                    nestId = newNestId,
                                                    action = Action.GoParentNest,
                                                    id = id
                                                )
                                            }

                                            // Update both merged points nestId to the all the selected points
                                            selectedPointTempOffset.forEach { (id, _) ->
                                                pointsService.editPoint(id) { old ->
                                                    old.copy(nestId = newNestId)
                                                }
                                            }
                                            pointsService.editPoint(closest.id) { old -> old.copy(nestId = newNestId) }
                                        }
                                    } else {
                                        // 2) No merging, just normal dragging and dropping
                                        selectedPointTempOffset.forEach { (id, offset) ->
                                            val tr = offset.toTr()

                                            val point = pointsService.findPointById(id) ?: return@forEach
                                            val shapeId = computePointMoved(point, tr.normalizedOffset)

                                            pointsService.editPoint(id) { old ->
                                                old.copy(
                                                    offset = tr.normalizedOffset,
                                                    shapeId = shapeId
                                                )
                                            }

                                            if (autoSeparatePoints) {
                                                pointsService.autoSeparate(nestId, id)
                                            }
                                        }
                                    }

                                    temporarySelectedShape = null
                                    closestHoveredPoint = null
                                    ableToLaunchHoverAction = false
                                },
                                onDragCancel = {
                                    selectedPointTempOffset.clear()
                                    pointsService.deselectAll()
                                    temporarySelectedShape = null
                                    closestHoveredPoint = null
                                    ableToLaunchHoverAction = false
                                }
                            )
                        }
                    }
                    .pointerInput(isInManualPlacementMode, isInDragAroundMode, nestId) {
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

                                    val newPoint = Point(
                                        id = -1,
                                        offset = tr.normalizedOffset,
                                        action = action,
                                        nestId = nestId,
                                        liveNestTargetNestId = newLiveNest
                                    )
                                    val shapeId = computePointMoved(newPoint, tr.normalizedOffset)

                                    val newPointId = pointsService.addPoint { id ->
                                        newPoint.copy(
                                            id = id,
                                            shapeId = shapeId
                                        )
                                    }

                                    if (autoSeparatePoints) {
                                        pointsService.autoSeparate(nestId, newPointId)
                                    }

                                    manualPlacementQueue = manualPlacementQueue.drop(1)
                                }

                                val bestP = tr ifDistanceIsSmallEnough { tr.bestP }

                                if (bestP == null) {
                                    pointsService.deselectAll()
                                } else {
                                    val id = bestP.id

                                    if (isInDragAroundMode) {
                                        toggleDragAroundMode(false)
                                    }

                                    // Checks whether if there are only 1 point selected, and if it is the case, open its editor or nest
                                    if (selectedPointsIds.size == 1 && id in selectedPointsIds) {
                                        // Same point tapped -> if circle nest, open it, else edit point
                                        if (bestP.action is Action.OpenCircleNest) {
                                            pointsService.deselectAll()
                                            nestsNavigationService.goToNest((bestP.action as Action.OpenCircleNest).nestId)
                                        } else {
                                            showEditDialog = bestP.id
                                        }
                                    } else if (bestP.id in selectedPointsIds) {
                                        deselect(id)
                                    } else {
                                        select(id)
                                    }
                                }
                            }
                        )
                    }
            )
        }
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
        val editPointId = showEditDialog!!
        val editPoint = pointsService.findPointById(editPointId)

        editPoint?.let {
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
    }


    if (showNestManagementDialog) {
        NestManagementDialog(
            onDismissRequest = { showNestManagementDialog = false },
            onSelect = {
                nestsNavigationService.goToNest(it.id)
                pointsService.deselectAll()
                showNestManagementDialog = false
            }
        )
    }

    SelectedPointsTopBar(
        modifier = Modifier.selfAlignHorizontally(),
        points = points,
        selectedPointsIds = selectedPointsIds,
        onDeselect = { id -> select(id) },
        onInvert = { pointsService.invertSelection(nestId) },
        onSelectAll = { pointsService.selectAll(nestId) },
        onDeselectAll = { pointsService.deselectAll() }
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

            PointStableCache.clear()
            NestIntersectionShapesPathCache.clear()

            initializationViewModel.initialize()
            pointsService.deselectAll()
            showResetPointsAndNestsDialog = false
        }
    }

    /**
     * Debug Infos section
     * Shows various information about the current settings state, may be unreadable when lots of points
     */
    DebugZone(DebugSettingsStore.settingsDebugInfo) {
        Text("current nests id: $nestId")
        Text("Points number: ${points.size}")
        Text("Nests number: ${nests.size}")
        Text("current Nest shapes number: ${currentNest.intersectionShapes.size}")
        val firstPoint = selectedPointsIds.firstOrNull()?.let {
            pointsService.findPointById(it)
        }
        Text("first selected point: $firstPoint")
    }
}
