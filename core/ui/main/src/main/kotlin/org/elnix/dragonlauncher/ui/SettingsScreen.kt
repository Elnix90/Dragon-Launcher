package org.elnix.dragonlauncher.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.messyfolder.Constants
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.NESTS_TAG
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.SWIPE_TAG
import org.elnix.dragonlauncher.common.messyfolder.Constants.Settings.SNAP_STEP_DEG
import org.elnix.dragonlauncher.common.messyfolder.Constants.Settings.TOUCH_THRESHOLD_PX
import org.elnix.dragonlauncher.common.messyfolder.UiCircle
import org.elnix.dragonlauncher.common.messyfolder.circles.autoSeparate
import org.elnix.dragonlauncher.common.messyfolder.circles.computePosition
import org.elnix.dragonlauncher.common.messyfolder.circles.createCirclesFromDragDistances
import org.elnix.dragonlauncher.common.messyfolder.circles.normalizeAngle
import org.elnix.dragonlauncher.common.messyfolder.circles.randomFreeAngle
import org.elnix.dragonlauncher.common.messyfolder.circles.rememberNestNavigation
import org.elnix.dragonlauncher.common.messyfolder.circles.scaleDragDistances
import org.elnix.dragonlauncher.common.messyfolder.circles.uiCirclesFromScaledDragDistances
import org.elnix.dragonlauncher.common.messyfolder.circles.undoTransformations
import org.elnix.dragonlauncher.common.messyfolder.showToast
import org.elnix.dragonlauncher.common.serializables.CircleNest
import org.elnix.dragonlauncher.common.serializables.SwipeActionSerializable
import org.elnix.dragonlauncher.common.serializables.SwipePointSerializable
import org.elnix.dragonlauncher.common.undoredo.UndoRedoManager
import org.elnix.dragonlauncher.enumsui.toggle.AddRemoveCircleTools
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
import org.elnix.dragonlauncher.enumsui.toggle.UndRedoEditTools
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.models.PointSettingsViewModel
import org.elnix.dragonlauncher.settings.stores.BehaviorSettingsStore
import org.elnix.dragonlauncher.settings.stores.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.SwipeMapSettingsStore
import org.elnix.dragonlauncher.settings.stores.SwipeSettingsStore
import org.elnix.dragonlauncher.settings.stores.UiSettingsStore
import org.elnix.dragonlauncher.settings.stores.UiSettingsStore.autoSeparatePoints
import org.elnix.dragonlauncher.settings.stores.UiSettingsStore.freeMoveDraggedPoint
import org.elnix.dragonlauncher.settings.stores.UiSettingsStore.snapPoints
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.components.AnimatedFab
import org.elnix.dragonlauncher.ui.base.components.RowWithScrollIndicator
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.components.ToggleAnimatedFab
import org.elnix.dragonlauncher.ui.components.AppPreviewTitle
import org.elnix.dragonlauncher.ui.composition.LocalAppsViewModel
import org.elnix.dragonlauncher.ui.composition.LocalDefaultPoint
import org.elnix.dragonlauncher.ui.dialogs.AddPointDialog
import org.elnix.dragonlauncher.ui.dialogs.EditPointSheet
import org.elnix.dragonlauncher.ui.dialogs.NestManagementDialog
import org.elnix.dragonlauncher.ui.dragon.components.DragonColumnGroup
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonTooltip
import org.elnix.dragonlauncher.ui.dragon.components.EditValueTextField
import org.elnix.dragonlauncher.ui.dragon.dialogs.UserValidation
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsSlider
import org.elnix.dragonlauncher.ui.helpers.customobjects.glowOverlay
import org.elnix.dragonlauncher.ui.helpers.nests.actionsInCircle
import org.elnix.dragonlauncher.ui.helpers.nests.circlesSettingsOverlay
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.helpers.settings.SpecialSettingsTitle
import org.elnix.dragonlauncher.ui.remembers.rememberSwipeDefaultParams
import org.elnix.dragonlauncher.ui.settings.customization.rotateBy
import java.math.RoundingMode
import java.util.UUID
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.round

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@SuppressLint("LocalContextGetResourceValueCall")
@Suppress("AssignedValueIsNeverRead")
@Composable
fun SettingsScreen(
    pointSettingsViewModel: PointSettingsViewModel = hiltViewModel(),
    onAdvSettings: () -> Unit,
    onNestEdit: (nest: Int) -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val defaultPoint = LocalDefaultPoint.current
    val extraColors = LocalExtraColors.current
    val appsViewModel = LocalAppsViewModel.current

    val scope = rememberCoroutineScope()

    val pointsIconsTrigger by appsViewModel.pointsIconsCache.iconsTrigger.collectAsState()
    val showAdvancedEditTools by pointSettingsViewModel.showAdvancedPointTools.collectAsState()
    val showSubNestSlider by pointSettingsViewModel.showSubNestSlider.collectAsState()
    val isInDragAroundMode by pointSettingsViewModel.isInDragAroundMode.collectAsState()

    val backgroundColor = MaterialTheme.colorScheme.background
    val primaryColor = MaterialTheme.colorScheme.primary

    val snapPoints by snapPoints.asState()
    val autoSeparatePoints by autoSeparatePoints.asState()
    val freeMoveDraggedPoint by freeMoveDraggedPoint.asState()
    val appLabelOverlaySize by UiSettingsStore.appLabelOverlaySize.asState()
    val appIconOverlaySize by UiSettingsStore.appIconOverlaySize.asState()

    val createLiveNestByDefaultWhenCreatingOpenCircleNestPoint by BehaviorSettingsStore.createLiveNestByDefaultWhenCreatingOpenCircleNestPoint.asState()
    val settingsDebugInfos by DebugSettingsStore.settingsDebugInfo.asState()

    var center by remember { mutableStateOf(Offset.Zero) }
//    var availableWidth by remember { mutableFloatStateOf(0f) }

    val points: SnapshotStateList<SwipePointSerializable> = remember { mutableStateListOf() }
    val nests: SnapshotStateList<CircleNest> = remember { mutableStateListOf() }

    var recomposeTrigger by remember { mutableIntStateOf(0) }

    val circles: SnapshotStateList<UiCircle> = remember { mutableStateListOf() }

    var selectedPoint by remember { mutableStateOf<SwipePointSerializable?>(null) }

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

    var closestHoveredPoint by remember { mutableStateOf<SwipePointSerializable?>(null) }
    var closestHoveredTempOffset by remember { mutableStateOf<Offset?>(null) }
    var ableToLaunchHoverAction by remember { mutableStateOf(false) }

    val hoveredPointRadialGradientProgress by animateFloatAsState(
        targetValue = if (ableToLaunchHoverAction) Constants.Settings.HOVER_GRADIENT_RADIUS else 1f
    )


    var lastSelectedCircle by remember { mutableIntStateOf(0) }
    val aPointIsSelected = selectedPoint != null

    var showEditDefaultPoint by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<SwipePointSerializable?>(null) }

    // Manual placement mode state (multi-select "Place one by one")
    var manualPlacementQueue by remember { mutableStateOf<List<SwipeActionSerializable>>(emptyList()) }
    val isInManualPlacementMode = manualPlacementQueue.isNotEmpty()
    var isEditing by remember { mutableStateOf(false) }

    var showNestManagementDialog by remember { mutableStateOf(false) }
    var showResetPointsAndNestsDialog by remember { mutableStateOf(false) }


    val rowsScrollStates = List(3) { rememberScrollState() }

    /** ──────────────────── NESTS SYSTEM ────────────────────
     * - Collects the nests from the datastore, then initialize the base nest to 0 (always the default)
     * while all the other have a random id
     */


    val nestNavigation = rememberNestNavigation(nests)
    val currentNest = nestNavigation.currentNest
    val nestId = currentNest.id


    /**
     * The number of circles; it's the size of the current nest, minus one, cause it ignores the
     * cancel zone
     */
    val circleNumber = currentNest.dragDistances.size - 1

    /**
     * Computes an even distance for the circles spacing, for clean integration
     */
    val circlesWidthIncrement = (1f / circleNumber).takeIf { it != 0f } ?: 1f

    /**
     * Used to ensure that there is always a 0-id nest, the default one, the most important
     */
    LaunchedEffect(nestId, nests.size) {
        if (nests.isNotEmpty() && nests.none { it.id == nestId }) {
            logD(NESTS_TAG) { "Creating missing nest $nestId" }
            nests.add(CircleNest(id = nestId))
        }
    }


    fun reloadIcons() {
        appsViewModel.preloadPointIcons(points)
    }

    val undoRedo = remember { UndoRedoManager() }

    LaunchedEffect(Unit) {
        undoRedo.register(
            key = "points",
            snapshot = { points.map { it.copy() } },
            restore = {
                points.clear()
                points.addAll(it.map { p -> p.copy() })
                selectedPoint = points.find { p -> p.id == (selectedPoint?.id ?: "") }
            }
        )
        undoRedo.register(
            key = "nests",
            snapshot = { nests.map { it.copy() } },
            restore = {
                nests.clear()
                nests.addAll(it)
            }
        )
    }

    fun save() {
        scope.launch {
            SwipeSettingsStore.savePoints(ctx, points.map { it.copy() })
            SwipeSettingsStore.saveNests(ctx, nests.map { it.copy() })
        }
    }

    fun applyChange(mutator: () -> Unit) {
        undoRedo.applyChange(mutator)
        recomposeTrigger++
        save()
    }

    fun undo() {
        undoRedo.undo()
        save()
    }

    fun redo() {
        undoRedo.redo()
        save()
    }

    fun undoAll() {
        undoRedo.undoAll()
        save()
    }

    fun redoAll() {
        undoRedo.redoAll()
        save()
    }


    /**
     * Adds a new nest to the current list of nests.
     *
     * This function generates a unique, human-readable ID for the new nest,
     * ensures it does not conflict with existing nest IDs, and initializes
     * its drag distances for all circles in the range [-1, circleNumber + 1].
     *
     * The new nest is then added to the `nests` list and the state is saved.
     *
     * @param circleNumber The number of circles for which to initialize drag distances.
     *                     Default is 3.
     * @return The unique ID of the newly created nest.
     */
    fun addNewNest(circleNumber: Int = 3): Int {
        // Generate a new, unique nest ID
        val existingIds = nests.map { it.id }.toSet()
        var newNestId = nests.size
        while (newNestId in existingIds) {
            newNestId++
        }

        val dragDistances = mutableStateMapOf<Int, Int>().apply {
            for (id in -1..<circleNumber) {
                this[id] = defaultDragDistance(id)
            }
        }

        // Add the new nest
        nests += CircleNest(
            id = newNestId,
            dragDistances = dragDistances
        )

        // Persist changes
        save()

        return newNestId
    }

    fun renameNest(id: Int, newName: String) {
        applyChange {
            val index = nests.indexOfFirst { it.id == id }

            if (index != -1) {
                nests[index] = nests[index].copy(
                    name = newName
                )
            }
        }
    }

    fun deleteNest(nestToDelete: Int) {
        applyChange {
            // Delete nest, leave points on it for now
            val index = nests.indexOfFirst { it.id == nestToDelete }

            if (index != -1) {
                nests -= nests[index]
            }
        }
    }


    /**
     * Adds a new circle to the specified nest (or the currently selected nest by default).
     *
     * The new circle ID is computed as the next integer after the current maximum circle number
     * in the nest (ignoring the special -1 key). The drag distance for the new circle is
     * initialized using [defaultDragDistance]. The nest list is updated immutably, and the
     * change is recorded via `applyChange` for undo/redo support.
     *
     * @param nestToTouch Optional nest ID to target. If null, the currently selected nest is used.
     */
    fun addCircle(nestToTouch: Int? = null) {

        val nestIdRequested = nestToTouch ?: nestId

        val index = nests.indexOfFirst { it.id == nestIdRequested }
        if (index != -1) {
            val nest = nests[index]

            val newCircleNumber =
                nest.dragDistances
                    .keys
                    .filter { it >= 0 }
                    .maxOrNull()
                    ?.plus(1) ?: 0

            applyChange {
                val updatedNest = nest.copy(
                    dragDistances = nest.dragDistances +
                            (newCircleNumber to defaultDragDistance(newCircleNumber))
                )

                nests[index] = updatedNest
            }
        }
    }


    /**
     * Removes the last added circle from the specified nest (or the currently selected nest by default).
     *
     * The last circle is determined as the one with the highest circle number greater than 0.
     * The nest list is updated immutably, and the change is recorded via `applyChange` for
     * undo/redo support.
     *
     * Safely checks if there is mor than 1 circle to avoid deleting the last one
     *
     * @param nestToTouch Optional nest ID to target. If null, the currently selected nest is used.
     */
    fun removeLastCircle(nestToTouch: Int? = null) {

        val nestIdRequested = nestToTouch ?: nestId
        // Remove last circle
        val index = nests.indexOfFirst { it.id == nestIdRequested }
        if (index != -1) {
            val nest = nests[index]

            val maxCircle =
                nest.dragDistances.keys.filter { k -> k > 0 }
                    .maxOrNull()
                    ?: return

            val updatedDistances = nest.dragDistances - maxCircle
            applyChange {
                nests[index] = nest.copy(dragDistances = updatedDistances)
            }
        }
    }


    fun addPoint(point: SwipePointSerializable) {
        points.add(point)
        selectedPoint = point
        appsViewModel.pointsIconsCache.incrementCacheSize()
        appsViewModel.reloadPointIcon(point)
    }

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
        point: SwipePointSerializable,
        circles: List<UiCircle>,
        pos: Offset
    ): Pair<Double, Int>? {

        // 1. Compute raw angle from center -> pos
        val dx = pos.x - center.x
        val dy = center.y - pos.y
        var angle = Math.toDegrees(atan2(dx.toDouble(), dy.toDouble()))
        if (angle < 0) angle += 360.0

        // 2. Apply snapping if enabled
        val finalAngle = if (snapPoints) {
            round(angle / SNAP_STEP_DEG) * SNAP_STEP_DEG
        } else {
            angle
        }

        // 3. Find nearest circle based on radius
        val distFromCenter = hypot(dx, dy)
        val closestCircle = circles.minByOrNull { c -> abs(c.radius - distFromCenter) }
            ?: error("Failed to find circle: BIG ISSUE") // Shouldn't happen


        // Only return the angle and circle number if they have changed
        if (
            (point.angleDeg != finalAngle) ||
            (point.circleNumber != closestCircle.id)
        ) {
            return (finalAngle to closestCircle.id)
        }

        // Position is the same as before, return null to tell the updater to not move the point
        return null
    }

    /**
     * Update point position; first compute the new point position using `computePointMoved` and then apply the change to the stack if it was moved
     *
     * @param point which point to move
     * @param circles the circles to move the point on
     * @param pos the new position of the points, can be anywhere on the screen, not only on a circle
     */
    fun updatePointPosition(
        point: SwipePointSerializable,
        circles: List<UiCircle>,
        pos: Offset
    ) {
        val newPointValues = computePointMoved(
            point = point,
            circles = circles,
            pos = pos
        )

        // Only apply the changed if the point has been changed
        newPointValues?.let { newPoint ->
            applyChange {
                point.angleDeg = newPoint.first
                point.circleNumber = newPoint.second
            }
        }
    }

    // Load points & nests
    LaunchedEffect(showResetPointsAndNestsDialog) {
        if (!showResetPointsAndNestsDialog) {
            loadLivePointsList(ctx, points)
            loadNestsList(ctx, nests)
        }
    }


    val handleBack = {
        if (isInManualPlacementMode) manualPlacementQueue = emptyList()
        else if (selectedPoint != null) selectedPoint = null
        else if (nestId != 0) nestNavigation.goBack()
        else if (isEditing) isEditing = false
        else onBack()
    }
    BackHandler(onBack = handleBack)

    /**
     * Computes and updates the radii for all circles in the current nest whenever
     * the nest, available width, or center changes.
     *
     * Each circle's radius is proportional to the available width of the container,
     * scaled by circlesWidthIncrement, and distributed evenly so that the largest
     * circle nearly fits the box.
     *
     * This updates the mutable list circles, which is used both for rendering
     * and for hit detection of points on the circles.
     *
     * - `currentNest`: The currently selected nest containing the drag distances for each circle.
     * - `availableWidth`: The width available for drawing the circles, used to scale the radii proportionally.
     * - `center`: The center of the container, used to compute offsets and positions for points.
     */
    LaunchedEffect(currentNest, center) {

        createCirclesFromDragDistances(
            dragDistances = currentNest.dragDistances,
            circles = circles
        )
    }

    LaunchedEffect(closestHoveredPoint) {
        ableToLaunchHoverAction = false
        closestHoveredPoint?.let {

            val finalOffset = it.computePosition(
                circles,
                center
            )

            closestHoveredTempOffset = finalOffset

            val startDuration = System.currentTimeMillis()
            while (System.currentTimeMillis() - startDuration < Constants.Settings.HOVER_POINT_DURATION) {
                delay(50L)
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
            if (!isDragging) points
            else points.filter { it.id != selectedPoint?.id }
        }
    }


    val baseDrawParams = rememberSwipeDefaultParams(
        nests = nests,
        forceShowAllActionsInCurrentNest = true,
        backgroundColor = MaterialTheme.colorScheme.background
    )


    val subNestDefaultRadius by SwipeMapSettingsStore.subNestDefaultRadius.asState()

    val drawParams by remember(
        subNestDefaultRadius,
        pointsIconsTrigger,
        points,
        nests,
        displayedFilteredPoints,
        backgroundColor,
        extraColors
    ) {
        derivedStateOf {
            baseDrawParams.copy(
                points = displayedFilteredPoints,
                surfaceColorDraw = backgroundColor,
                extraColors = extraColors
            )
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

    val localWindow = LocalWindowInfo.current
    val screenSize = localWindow.containerSize

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
                onReloadPoints = { appsViewModel.preloadPointIcons(points) },
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
                        if (selectedPoint?.action is SwipeActionSerializable.OpenCircleNest) {
                            (selectedPoint!!.action as SwipeActionSerializable.OpenCircleNest).nestId
                        } else null

                    val canGoNest = nestToGo != null

                    val canGoback = currentNest.id != 0

                    MultiSelectConnectedButtonRow(
                        entries = NestEditTools.entries,
                        isEnabled = {
                            when (it) {
                                NestManagement -> true
                                GoParentNest -> canGoback
                                EnterNest -> canGoNest
                            }
                        },
                        isChecked = {
                            when (it) {
                                NestManagement -> true
                                GoParentNest -> canGoback
                                EnterNest -> canGoNest
                            }
                        }
                    ) { entry ->
                        scope.launch {
                            when (entry) {
                                NestManagement -> {
                                    showNestManagementDialog = true
                                }

                                GoParentNest -> {
                                    nestNavigation.goBack()
                                    selectedPoint = null
                                }

                                EnterNest -> {
                                    nestToGo?.let {
                                        nestNavigation.goToNest(it)
                                        selectedPoint = null
                                    }
                                }
                            }
                        }
                    }

                    Spacer(12.dp)

                    // The 3 points settings tools: Snap points / Auto separate / Lock to circle
                    MultiSelectConnectedButtonRow(
                        entries = PointsEditTools.entries,
                        isChecked = {
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
                    // The move left/right and text field entry, that animates on avery selected point
                    Row(
                        modifier = Modifier
                            .height(70.dp)
                            .padding(5.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DragonTooltip(R.string.move_point_clockwise) {
                            ToggleButton(
                                checked = false, // For the shape to be always the right one, and not a circle
                                onCheckedChange = {
                                    selectedPoint?.let { point ->
                                        applyChange {
                                            point.angleDeg = normalizeAngle(point.angleDeg + 1)
                                            if (snapPoints) point.angleDeg = point.angleDeg
                                                .toInt()
                                                .toDouble()
                                            if (autoSeparatePoints) autoSeparate(
                                                points,
                                                nestId,
                                                circles.find { it.id == point.circleNumber },
                                                point
                                            )
                                        }
                                    }
                                },
                                enabled = aPointIsSelected,
                                shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                                colors = AppObjectsColors.toggleButtonColors(),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.chevron_left),
                                    contentDescription = stringResource(R.string.move_point_clockwise)
                                )
                            }
                        }


                        val angleTextValue = selectedPoint
                            ?.angleDeg
                            ?.toBigDecimal()
                            ?.setScale(1, RoundingMode.UP)
                            ?.toDouble()
                            ?.toString()
                            ?: ""


                        var angleText by remember { mutableStateOf(angleTextValue) }
                        LaunchedEffect(angleTextValue) { angleText = angleTextValue }


                        fun commitEditTExt() {
                            try {
                                selectedPoint?.let { point ->
                                    applyChange {
                                        point.angleDeg = normalizeAngle(angleText.toDouble())
                                        if (snapPoints) point.angleDeg = point.angleDeg
                                            .toInt()
                                            .toDouble()
                                        if (autoSeparatePoints) autoSeparate(
                                            points,
                                            nestId,
                                            circles.find { it.id == point.circleNumber },
                                            point
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                ctx.showToast("Failed to set value: $e")
                                logE(SWIPE_TAG, e) { "Failed to set value for point via text field" }
                            }
                            isEditing = false
                        }

                        Spacer(Modifier.width(ButtonGroupDefaults.ConnectedSpaceBetween))
                        AnimatedVisibility(aPointIsSelected) {
                            EditValueTextField(
                                value = angleText,
                                onValueChange = { angleText = it },
                                enabled = aPointIsSelected,
                                textColor = MaterialTheme.colorScheme.onPrimary,
                                backgroundColor = MaterialTheme.colorScheme.primary,
                                onFocusChange = { isEditing = it },
                                onDone = ::commitEditTExt
                            )
                        }

                        AnimatedVisibility(isEditing) {
                            DragonIconButton(
                                onClick = ::commitEditTExt,
                                colors = IconButtonDefaults.iconButtonColors(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.onPrimary
                                ),
                                icon = R.drawable.check,
                                contentDescription = "Validate"
                            )
                        }

                        Spacer(ButtonGroupDefaults.ConnectedSpaceBetween)

                        DragonTooltip(R.string.move_point_anticlockwise) {
                            ToggleButton(
                                checked = false,
                                onCheckedChange = {
                                    selectedPoint?.let { point ->
                                        applyChange {
                                            point.angleDeg = normalizeAngle(point.angleDeg - 1)
                                            if (snapPoints) point.angleDeg = point.angleDeg
                                                .toInt()
                                                .toDouble()
                                            if (autoSeparatePoints) autoSeparate(
                                                points,
                                                nestId,
                                                circles.find { it.id == point.circleNumber },
                                                point
                                            )
                                        }
                                    }
                                },
                                enabled = aPointIsSelected,
                                shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                                colors = AppObjectsColors.toggleButtonColors(),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.chevron_right),
                                    contentDescription = stringResource(R.string.move_point_anticlockwise),
                                )
                            }
                        }
                    }

                    Spacer(12.dp)

                    // Undo/Redo bar
                    val undoButtonEnabled = undoRedo.canUndo
                    val redoButtonEnabled = undoRedo.canRedo
                    MultiSelectConnectedButtonRow(
                        entries = UndRedoEditTools.entries,
                        isEnabled = {
                            when (it) {
                                UndRedoEditTools.UndoAll -> undoButtonEnabled
                                UndRedoEditTools.Undo -> undoButtonEnabled
                                UndRedoEditTools.Redo -> redoButtonEnabled
                                UndRedoEditTools.RedoAll -> redoButtonEnabled
                            }
                        }
                    ) { entry ->
                        scope.launch {
                            when (entry) {
                                UndRedoEditTools.UndoAll -> undoAll()
                                UndRedoEditTools.Undo -> undo()
                                UndRedoEditTools.Redo -> redo()
                                UndRedoEditTools.RedoAll -> redoAll()
                            }
                        }
                    }
                }


                // 3. Reset offset/zoom/rotation - add/remove circle
                RowWithScrollIndicator(rowsScrollStates[2]) {
                    val canResetOffset = offset.value != Offset.Zero
                    val canResetZoom = zoom.value != 1f
                    val canResetRotation = angle.value != 0f

                    MultiSelectConnectedButtonRow(
                        entries = MoveAroundTools.entries,
                        isEnabled = {
                            when (it) {
                                Center -> canResetOffset
                                ResetZoom -> canResetZoom
                                ResetRotation -> canResetRotation
                            }
                        },
                        isChecked = {
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

                    MultiSelectConnectedButtonRow(
                        entries = AddRemoveCircleTools.entries,
                        isEnabled = {
                            when(it) {
                                AddRemoveCircleTools.Add -> true
                                AddRemoveCircleTools.Remove -> circles.size > 1
                            }
                        },
                    ) { entry ->
                        scope.launch {
                            when (entry) {
                                AddRemoveCircleTools.Add -> addCircle()
                                AddRemoveCircleTools.Remove -> removeLastCircle()
                            }
                        }
                    }
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
                        pointSettingsViewModel.toggleIsInDragAroundMode()
                        if (isInDragAroundMode) {
                            selectedPoint = null
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
                    isChecked = { true },
                    isEnabled = { aPointIsSelected }
                ) { option ->
                    scope.launch {
                        when (option) {
                            SelectedPointEditTools.Edit -> showEditDialog = selectedPoint
                            SelectedPointEditTools.Remove -> {

                                selectedPoint?.let { point ->
                                    val index = points.indexOfFirst { p -> p.id == point.id }
                                    if (index >= 0) {
                                        applyChange {
                                            points.removeAt(index)
                                        }
                                    }
                                    selectedPoint = null
                                }
                            }

                            SelectedPointEditTools.Duplicate -> {
                                selectedPoint?.let { oldPoint ->
                                    val newPoint = oldPoint.copy(
                                        id = UUID.randomUUID().toString(),
                                    )


                                    applyChange {
                                        addPoint(newPoint)
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
                        }
                    }
                }
            }
        },
        modifier = Modifier.imePadding()
    ) {
        // Main content box: Adapts its size to the screen,
        // computes the circles radii and host the pointer input for the points selection
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    // Updates the center and available width variables, that depends on the phone size and orientation.
                    // Computes the larger size between width and height to ensure all points belongs to the hittable zone
                    // The visual points and hitboxes are separated due to the need of a precise pointer input and
                    // should be synchronized using the clever [computePointPosition] function that relies on common
                    // center and circles to output the points position on screen

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
                Canvas(
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
                        .drawWithCache {
                            onDrawBehind {
                                circlesSettingsOverlay(
                                    drawParams = drawParams,
                                    center = center,
                                    depth = 1,
                                    circles = circles,
                                    selectedPoint = selectedPoint,
                                    nestId = nestId,
                                    preventBgErasing = true,
                                    showConfiguratorDecorations = true,
                                )
                            }
                        }
                ) {

                    // Live Nest: semi-transparent target nest preview at the selected point (nest editor only).
                    selectedPoint?.let { p ->
                        val liveTargetId = p.liveNestTargetNestId ?: return@let
                        val nestedNest = nests.find { it.id == liveTargetId } ?: return@let
                        val nestScale = p.liveNestScale ?: 0.5f
                        val scaledCircles = uiCirclesFromScaledDragDistances(
                            scaleDragDistances(nestedNest.dragDistances, nestScale)
                        )
                        if (scaledCircles.isEmpty()) return@let
                        val hostCenter = if (isDragging) {
                            selectedPointTempOffset.value
                        } else {
                            p.computePosition(circles, center)
                        }
                        drawIntoCanvas { canvas ->
                            val bounds = Rect(0f, 0f, screenSize.width.toFloat(), screenSize.height.toFloat())
                            canvas.saveLayer(bounds, Paint().apply { alpha = 0.4f })
                            circlesSettingsOverlay(
                                drawParams = drawParams,
                                center = hostCenter,
                                depth = 1,
                                circles = scaledCircles,
                                selectedPoint = null,
                                nestId = nestedNest.id,
                                preventBgErasing = true,
                                showConfiguratorDecorations = true
                            )
                            canvas.restore()
                        }
                    }

                    // Animated Selected point
                    if (selectedPoint != null) {
                        actionsInCircle(
                            drawParams = drawParams,
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
                        glowOverlay(
                            center = closestHoveredTempOffset!!,
                            color = primaryColor,
                            radius = hoveredPointRadialGradientProgress.dp.toPx()
                        )
                    }
                }
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .then(
                        if (settingsDebugInfos) {
                            Modifier.background(Color.DarkGray.copy(0.3f))
                        } else Modifier
                    )
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

                                    var closest: SwipePointSerializable? = null
                                    var best = Float.MAX_VALUE

                                    // Can only select points on the same nest
                                    filteredPoints.forEach { p ->
                                        val pointOffset = p.computePosition(
                                            circles = circles,
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

                                    selectedPoint =
                                        if (best <= TOUCH_THRESHOLD_PX) closest else null

                                    selectedPoint?.let {
                                        lastSelectedCircle = it.circleNumber
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
                                                circles = circles,
                                                pos = transformedPosition
                                            )

                                            p.copy(
                                                angleDeg = newPointValues?.first ?: p.angleDeg,
                                                circleNumber = newPointValues?.second ?: p.circleNumber
                                            ).computePosition(
                                                circles = circles,
                                                center = center
                                            )
                                        }

                                        scope.launch {
                                            selectedPointTempOffset.snapTo(newPosition)
                                        }
                                    }


                                    var closest: SwipePointSerializable? = null
                                    var best = Float.MAX_VALUE

                                    // Can only see points on the same nest
                                    filteredPoints.filter { it.id != selectedPoint?.id }
                                        .forEach { p ->

                                            val pointOffset = p.computePosition(
                                                circles = circles,
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

                                            if (closest.action is SwipeActionSerializable.OpenCircleNest) {
                                                // Put the hovered point in the hovered nest

                                                val targetNestId =
                                                    (closest.action as SwipeActionSerializable.OpenCircleNest).nestId

                                                // Adjust the merged nest circle size if the point belongs to higher circles and the nest has less
                                                nests.find { it.id == targetNestId }
                                                    ?.let { targetNest ->

                                                        // I remove 1 because the dragDistances counts the cancel zone
                                                        val targetNestCircleNumbers =
                                                            targetNest.dragDistances.size - 1

                                                        // Add 1 because the circle number starts at 0
                                                        val selectedPointCircleNumber =
                                                            p.circleNumber + 1

                                                        if (selectedPointCircleNumber > targetNestCircleNumbers) {
                                                            repeat(selectedPointCircleNumber - targetNestCircleNumbers) {
                                                                logD(NESTS_TAG) {
                                                                    "Adding a circle to nest n°$targetNestId "
                                                                }
                                                                addCircle(targetNestId)
                                                            }
                                                        }
                                                    }

                                                applyChange {
                                                    p.nestId = targetNestId
                                                }

                                            } else {
                                                // Create new nest and put both points in it at 90° and 270° (left and right)
                                                // Tee new nest has only one circle and a Go parent nest in the top, for easier access
                                                applyChange {
                                                    val newNestId = addNewNest(1)

                                                    val newNestPoint = SwipePointSerializable(
                                                        circleNumber = closest.circleNumber,
                                                        angleDeg = closest.angleDeg,
                                                        nestId = closest.nestId,
                                                        action = SwipeActionSerializable.OpenCircleNest(
                                                            newNestId
                                                        ),
                                                        id = UUID.randomUUID().toString()
                                                    )

                                                    // Creates a new go parent nest that'll be put on top of the nest, to easily exit this nest
                                                    val newGoParentNestPoint =
                                                        SwipePointSerializable(
                                                            circleNumber = 0,
                                                            angleDeg = 0.0,
                                                            nestId = newNestId,
                                                            action = SwipeActionSerializable.GoParentNest,
                                                            id = UUID.randomUUID().toString(),
                                                            liveNestTargetNestId = if (createLiveNestByDefaultWhenCreatingOpenCircleNestPoint) newNestId else null
                                                        )

                                                    addPoint(newGoParentNestPoint)
                                                    addPoint(newNestPoint)

                                                    // Move the 2 points to the new nest and change their position
                                                    p.nestId = newNestId
                                                    p.circleNumber = 0
                                                    p.angleDeg = 270.0


                                                    closest.nestId = newNestId
                                                    closest.circleNumber = 0
                                                    closest.angleDeg = 90.0
                                                }
                                            }
                                        } else {
                                            // 2) No merging, just normal dragging and dropping

                                            updatePointPosition(
                                                point = p,
                                                circles = circles,
                                                pos = position
                                            )

                                            if (autoSeparatePoints) autoSeparate(
                                                points,
                                                nestId,
                                                circles.find { it.id == p.circleNumber },
                                                p
                                            )


                                            // Compute final snapped position
                                            val finalOffset = p.computePosition(
                                                circles,
                                                center
                                            )

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
                                        val targetCircle =
                                            lastSelectedCircle.coerceAtMost(circleNumber - 1)

                                        // Compute angle from center
                                        val dx = transformedOffset.x - center.x
                                        val dy = center.y - transformedOffset.y
                                        var angle =
                                            Math.toDegrees(atan2(dx.toDouble(), dy.toDouble()))
                                        if (angle < 0) angle += 360.0
                                        val finalAngle = if (snapPoints) {
                                            round(angle / SNAP_STEP_DEG) * SNAP_STEP_DEG
                                        } else angle

                                        // Find nearest circle based on tap distance from center
                                        val distFromCenter = hypot(dx, dy)
                                        val closestCircle = circles.minByOrNull { c ->
                                            abs(c.radius - distFromCenter)
                                        }
                                        val circleId = closestCircle?.id ?: targetCircle

                                        val newLiveNest =
                                            if (action is SwipeActionSerializable.OpenCircleNest && createLiveNestByDefaultWhenCreatingOpenCircleNestPoint) {
                                                action.nestId
                                            } else null

                                        val point = SwipePointSerializable(
                                            id = UUID.randomUUID().toString(),
                                            angleDeg = finalAngle,
                                            action = action,
                                            circleNumber = circleId,
                                            nestId = nestId,
                                            liveNestTargetNestId = newLiveNest
                                        )


                                        applyChange {
                                            addPoint(point)
                                            if (autoSeparatePoints) autoSeparate(
                                                points = points,
                                                nestId = nestId,
                                                circle = circles.find { it.id == circleId },
                                                draggedPoint = point
                                            )
                                        }

                                        // Dequeue: move to the next app
                                        manualPlacementQueue = manualPlacementQueue.drop(1)
                                        return@detectTapGestures
                                    }

                                    // Normal tap mode
                                    var tapped: SwipePointSerializable? = null
                                    var best = Float.MAX_VALUE
                                    var bestPointPos = Offset.Zero

                                    filteredPoints.forEach { p ->
                                        val pointPos = p.computePosition(circles, center)
                                        val dist = hypot(transformedOffset.x - pointPos.x, transformedOffset.y - pointPos.y)

                                        if (dist < best) {
                                            best = dist
                                            bestPointPos = pointPos
                                            tapped = p
                                        }
                                    }

                                    selectedPoint =
                                        if (best <= TOUCH_THRESHOLD_PX)
                                            if (selectedPoint?.id == tapped?.id) {
                                                // Same point tapped -> if circle next, open it, else edit point
                                                if (selectedPoint?.action is SwipeActionSerializable.OpenCircleNest) {
                                                    nestNavigation.goToNest((selectedPoint?.action as SwipeActionSerializable.OpenCircleNest).nestId)
                                                    null
                                                } else {
                                                    showEditDialog = selectedPoint
                                                    tapped
                                                }

                                            } else tapped
                                        else null

                                    selectedPoint?.let {
                                        lastSelectedCircle = it.circleNumber
                                        scope.launch {
                                            selectedPointTempOffset.snapTo(bestPointPos)
                                        }
                                    }
                                }
                            )
                        }
                    }
            )
        }
    }

    AnimatedVisibility(showSubNestSlider) {
        SettingsSlider(
            setting = SwipeMapSettingsStore.subNestDefaultRadius,
            title = "",
            valueRange = 0..50,
            modifier = Modifier
                .height(50.dp)
                .width(150.dp)
                .offset(x = 20.dp, y = 50.dp)
        )
    }


    if (showAddDialog) {
        AddPointDialog(
            onNewNest = ::addNewNest,
            onDismiss = {
                showAddDialog = false
            },
            onMultipleActionsSelected = { actions, autoPlace ->
                val targetCircle = lastSelectedCircle.coerceAtMost(circleNumber - 1)
                val circle = circles.find { it.id == targetCircle }

                if (autoPlace) {
                    // Auto-place all apps evenly on the circle
                    applyChange {
                        for (action in actions) {
                            val newAngle = randomFreeAngle(circle, points) ?: continue

                            val newLiveNest =
                                if (action is SwipeActionSerializable.OpenCircleNest && createLiveNestByDefaultWhenCreatingOpenCircleNestPoint) {
                                    action.nestId
                                } else null

                            val newPoint = SwipePointSerializable(
                                id = UUID.randomUUID().toString(),
                                angleDeg = newAngle,
                                action = action,
                                circleNumber = targetCircle,
                                nestId = nestId,
                                liveNestTargetNestId = newLiveNest
                            )


                            addPoint(newPoint)
                            autoSeparate(points, nestId, circle, newPoint)
                        }
                    }
                } else {
                    // Manual placement: queue actions and let user tap to place each one
                    manualPlacementQueue = actions
                }
                showAddDialog = false
            }
        )
    }

    if (showEditDialog != null) {
        val editPoint = showEditDialog!!

        EditPointSheet(
            point = editPoint,
            onNewNest = ::addNewNest,
            onRenameNest = ::renameNest,
            onDeleteNest = ::deleteNest,
            onDismiss = {
                showEditDialog = null
                appsViewModel.reloadPointIcon(editPoint)
            },
        ) { newPoint ->
            appsViewModel.reloadPointIcon(newPoint)

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


    if (showNestManagementDialog) {
        NestManagementDialog(
            onDismissRequest = { showNestManagementDialog = false },
            onNewNest = ::addNewNest,
            nests = nests,
            onNameChange = ::renameNest,
            onDelete = ::deleteNest,
            onSelect = {
                nestNavigation.goToNest(it.id)
                selectedPoint = null
                showNestManagementDialog = false
            }
        )
    }

    AppPreviewTitle(
        point = selectedPoint,
        topPadding = 100.dp,
        labelSize = appLabelOverlaySize,
        iconSize = appIconOverlaySize,
        showLabel = true,
        showIcon = true
    )

    if (isInManualPlacementMode) {
        val appName = when (val currentAction = manualPlacementQueue.first()) {
            is SwipeActionSerializable.LaunchApp -> {
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
            onNewNest = null,
            onRenameNest = null,
            onDeleteNest = null,
            onDismiss = {
                showEditDefaultPoint = false
            }
        ) {
            scope.launch {
                SwipeSettingsStore.setDefaultPoint(ctx, it)
                reloadIcons()
            }
            showEditDefaultPoint = false
        }
    }

    if (showResetPointsAndNestsDialog) {
        UserValidation(
            title = stringResource(R.string.reset_all_points),
            message = stringResource(R.string.reset_all_points_desc),
            onDismiss = { showResetPointsAndNestsDialog = false }
        ) {
            scope.launch {
                SwipeSettingsStore.resetAll(ctx)
                selectedPoint = null
                showResetPointsAndNestsDialog = false
            }
        }
    }


    /**
     * Debug Infos section
     * Shows various information about the current settings state, may be unreadable when lots of points
     */
    if (settingsDebugInfos) {
        DragonColumnGroup {
            Text("isDragging: $isDragging")
            Text("nests id: $nestId")
            Text("current nests id: ${currentNest.id}")
            Text("nests number: ${nests.size}")
            Text("circle number: $circleNumber")
            Text("currentNest size: ${currentNest.dragDistances.size}")
            Text("circle width incr: $circlesWidthIncrement")
            Text("current dragDistances: ${currentNest.dragDistances}")
            Text("closest hovered point: $closestHoveredTempOffset")
            Text("current nest: $currentNest")

            selectedPoint?.let { Text(it.toString()) }
        }
    }
}

private suspend fun loadNestsList(
    ctx: Context,
    nests: SnapshotStateList<CircleNest>
) {
    val savedNests = SwipeSettingsStore.getNests(ctx)
    nests.clear()
    try {
        nests.addAll(savedNests)
    } catch (e: Exception) {
        logE(SWIPE_TAG, e) { "Error loading nests: $e" }
        ctx.showToast("Error loading swipe points: $e")
    }
}

private suspend fun loadLivePointsList(
    ctx: Context,
    points: SnapshotStateList<SwipePointSerializable>
) {
    val savedPoints = SwipeSettingsStore.getPoints(ctx)
    points.clear()
    try {
        points.addAll(savedPoints)
    } catch (e: NullPointerException) {
        logE(SWIPE_TAG, e) { "NullPointerException loading swipe points" }
        ctx.showToast("NullPointerException loading swipe points: $e")

        // Fallback load them the old way
        try {
            savedPoints.forEach {
                @Suppress("USELESS_ELVIS")
                points.add(
                    it.copy(
                        action = it.action
                            ?: SwipeActionSerializable.OpenDragonLauncherSettings()
                    )
                )
            }
        } catch (e: Exception) {
            logE(SWIPE_TAG, e) { "Fallback loading also failed, clearing all points: $e" }
        }
    } catch (e: Exception) {
        logE(SWIPE_TAG, e) { "Error loading swipe points: $e" }
        ctx.showToast("Error loading swipe points: $e")
    }
}

fun defaultDragDistance(id: Int): Int = when (id) {
    -1 -> 150 // Cancel Zone (below no action activation)
    0 -> 300  // First circle 300
    else -> 300 + 150 * id // others: add 150 each, don't be dumb and go to 10 circles
}
