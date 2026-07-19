package org.elnix.dragonlauncher.ui.settings.customization

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elnix90.logging.logWtf
import io.github.elnix90.runtime.asMutableState
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape.Companion.highlightedIfSelected
import org.elnix.dragonlauncher.base.model.serializables.Nest
import org.elnix.dragonlauncher.base.navigaton.ManipulationSystem
import org.elnix.dragonlauncher.base.resolveShape
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.enumsui.toggle.NestEditTools
import org.elnix.dragonlauncher.enumsui.toggle.NestEditTools.EnterNest
import org.elnix.dragonlauncher.enumsui.toggle.NestEditTools.GoParentNest
import org.elnix.dragonlauncher.enumsui.toggle.NestEditTools.NestManagement
import org.elnix.dragonlauncher.enumsui.toggle.ShapesEditTools
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.px
import org.elnix.dragonlauncher.ktx.rotateBy
import org.elnix.dragonlauncher.ktx.snapToRound
import org.elnix.dragonlauncher.ktx.toPath
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.UiConstants.dragonSettingGroupPaddingValues
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asMutableState
import org.elnix.dragonlauncher.ui.base.components.AnimatedFab
import org.elnix.dragonlauncher.ui.base.components.RowWithScrollIndicator
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.components.IntersectionShape
import org.elnix.dragonlauncher.ui.components.ManipulationSystemReset
import org.elnix.dragonlauncher.ui.components.NestNameEditor
import org.elnix.dragonlauncher.ui.components.burger.MoreOptions
import org.elnix.dragonlauncher.ui.dialogs.IntersectionShapeManagementDialog
import org.elnix.dragonlauncher.ui.dialogs.NestManagementDialog
import org.elnix.dragonlauncher.ui.dragon.components.DragonDropDownMenu
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonRow
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.dragon.settings.Setting
import org.elnix.dragonlauncher.ui.helpers.DebugZone
import org.elnix.dragonlauncher.ui.helpers.ShapePreview
import org.elnix.dragonlauncher.ui.helpers.UndoRedoBlock
import org.elnix.dragonlauncher.ui.helpers.detectTransformGestures
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.helpers.swipe.NestOverlay
import org.elnix.dragonlauncher.ui.helpers.swipe.centerOfNest


@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
public fun NestEditScreen(
    pointsViewModel: PointsViewModel = activityViewModel(),
    initialNestId: Int,
    onBack: () -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val pointsService = pointsViewModel.pointsService
    val points by pointsService.points.collectAsState()

    val nestNavigation = pointsViewModel.nestsNavigationService
    LaunchedEffect(Unit) {
        nestNavigation.goToNest(initialNestId)
    }

    val nestId by pointsViewModel.nestsNavigationService.currentNestId.collectAsState()
    val currentNest = pointsService.findNestById(nestId)


    var snapShapesOffset by UiSettingsStore.snapShapesOffset.asMutableState()
    var snapShapesScale by UiSettingsStore.snapShapesScale.asMutableState()
    var snapShapeAngle by UiSettingsStore.snapShapeAngle.asMutableState()
    val snapOffsetThreshold = 30.dp.px

    fun IntersectionShape.snap(): IntersectionShape = this.copy(
        offset = if (snapShapesOffset) this.offset.snapToRound(Offset.Zero, snapOffsetThreshold) else this.offset,
        scale = if (snapShapesScale) this.scale.snapToRound(1f, 0.5f) else this.scale,
        angle = if (snapShapeAngle) this.angle.snapToRound(0f, 20f) else this.angle
    )

    val cellSizeDp by UiSettingsStore.widgetsCellSizeDp.asState()
    val cellSizePx = cellSizeDp.px // TODO
    var showMoreSheet by remember { mutableStateOf(false) }

    val rowsScrollStates = List(3) { rememberScrollState() }

    var showShapesManagementDialog by remember { mutableStateOf(false) }
    var showNestManagementDialog by remember { mutableStateOf(false) }
    var center by remember { mutableStateOf(Offset.Zero) }

    var selectedShapeId: Int? by remember { mutableStateOf(null) }
    val isInDragAroundMode: Boolean = selectedShapeId == null

    var tempCancelZone by remember { mutableIntStateOf(currentNest.cancelZone) }

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


    var witnessShape: IntersectionShape? by remember { mutableStateOf(null) }
    var netOffsetChange by remember { mutableStateOf(Offset.Zero) }

    fun saveCurrentNest() {
        pointsService.updateNest(
            nestId = nestId,
            shapeId = selectedShapeId,
            netOffsetChange = netOffsetChange
        ) { old ->
            old.copy(
                intersectionShapes = paths.keys.mapTo(mutableSetOf()) { it.snap() }
            )
        }
    }

    val handleBack = {
        if (selectedShapeId != null) selectedShapeId = null
        else {
            saveCurrentNest()
            onBack()
        }
    }
    BackHandler(onBack = handleBack)


//    TODO("Make snapping tools global (not only with the center of the nest")

    var recomposeTrigger by pointsService.recomposeTrigger.asMutableState()

    val manipulationSystem = remember { ManipulationSystem(center) }
    LaunchedEffect(center) {
        manipulationSystem.center = center
    }

    val offset: Animatable<Offset, AnimationVector2D> = manipulationSystem.offset
    val angle: Animatable<Float, AnimationVector1D> = manipulationSystem.angle
    val zoom: Animatable<Float, AnimationVector1D> = manipulationSystem.zoom


    SettingsScaffold(
        title = stringResource(R.string.edit_nest_arg, nestId),
        onBack = handleBack,
        helpText = "Nest",
        onReset = { pointsService.resetNest(nestId) },
        resetText = stringResource(R.string.reset_nest_desc),
        resetTitle = stringResource(R.string.reset_nest),
        horizontalPadding = 0.dp,
        scrollableContent = false,
        moreOptions = { dismiss ->
            listOf(
                MoreOptions(
                    text = { stringResource(R.string.show_more_sheet) },
                    onClick = {
                        showMoreSheet = !showMoreSheet
                        dismiss()
                    },
                    icon = R.drawable.add_circle,
                )
            )
        },
        bottomContent = {
            RowWithScrollIndicator(rowsScrollStates[0]) {
                val canGoback = nestId != 0
                MultiSelectConnectedButtonRow(
                    entries = NestEditTools.entries.filterNot { it == EnterNest },
                    enabled = {
                        when (it) {
                            NestManagement -> true
                            GoParentNest -> canGoback
                            EnterNest -> error("Shouldn't happen")
                        }
                    },
                    checked = {
                        when (it) {
                            NestManagement -> true
                            GoParentNest -> canGoback
                            EnterNest -> error("Shouldn't happen")
                        }
                    }
                ) { entry ->
                    when (entry) {
                        NestManagement -> {
                            showNestManagementDialog = true
                        }

                        GoParentNest -> {
                            nestNavigation.goBack()
                            pointsService.deselectAll()
                        }

                        EnterNest -> error("Shouldn't happen")
                    }
                }

                Spacer(12.dp)

                MultiSelectConnectedButtonRow(
                    entries = ShapesEditTools.entries,
                    checked = {
                        when (it) {
                            ShapesEditTools.SnapOffset -> snapShapesOffset
                            ShapesEditTools.SnapScale -> snapShapesScale
                            ShapesEditTools.SnapAngle -> snapShapeAngle
                        }
                    }
                ) {
                    when (it) {
                        ShapesEditTools.SnapOffset -> snapShapesOffset = !snapShapesOffset
                        ShapesEditTools.SnapScale -> snapShapesScale = !snapShapesScale
                        ShapesEditTools.SnapAngle -> snapShapeAngle = !snapShapeAngle
                    }
                }
            }

            RowWithScrollIndicator(rowsScrollStates[2]) {
                ManipulationSystemReset(manipulationSystem)

                Spacer(12.dp)

                UndoRedoBlock(pointsService.undoRedo)
            }


            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {

                AnimatedFab(
                    onClick = { showShapesManagementDialog = true },
                    icon = R.drawable.edit_rounded,
                    minSize = 70.dp,
                    containerColor = MaterialTheme.colorScheme.secondary
                )

                var showDropDownMenu by remember { mutableStateOf(false) }

                Box {
                    AnimatedContent(isInDragAroundMode) {
                        val selectedShape = if (it) null else {
                            selectedShapeId?.let { shapeId ->
                                paths.keys.firstOrNull { shape -> shape.id == shapeId }
                            }
                        }

                        if (selectedShape == null) {
                            DragonRow(onClick = { showDropDownMenu = true }) {
                                Text(stringResource(R.string.move_around_mode))
                                Spacer(5.dp)
                                Icon(
                                    painter = painterResource(R.drawable.drag_indicator),
                                    contentDescription = stringResource(R.string.drag_handle)
                                )
                            }
                        } else {
                            DragonRow(onClick = { showDropDownMenu = true }) {
                                ShapePreview(
                                    iconShape = selectedShape.shape,
                                    modifier = Modifier.size(30.dp)
                                )
                                Spacer(5.dp)
                                Text("ID: ${selectedShape.id}")
                                Spacer(5.dp)
                                Icon(
                                    painter = painterResource(R.drawable.drag_indicator),
                                    contentDescription = stringResource(R.string.drag_handle)
                                )
                            }
                        }
                    }

                    DragonDropDownMenu(
                        expanded = showDropDownMenu,
                        onDismissRequest = { showDropDownMenu = false }
                    ) {
                        DropdownMenuGroup(
                            shapes = MenuDefaults.groupShapes()
                        ) {
                            val filteredShapes = paths.keys.filter { it.id != selectedShapeId }
                            filteredShapes.forEachIndexed { idx, shape ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = shape.id.toString(),
                                            modifier = Modifier.weight(1f)
                                        )
                                    },
                                    leadingIcon = {
                                        ShapePreview(
                                            iconShape = shape.shape,
                                            modifier = Modifier.size(30.dp),
                                        )
                                    },
                                    onClick = {
                                        selectedShapeId = shape.id
                                        showDropDownMenu = false
                                    },
                                    shape = when (idx) {
                                        0 -> MenuDefaults.leadingItemShape
                                        filteredShapes.size if selectedShapeId != null -> MenuDefaults.trailingItemShape
                                        else -> MenuDefaults.middleItemShape
                                    }
                                )
                            }

                            if (selectedShapeId != null) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.move_around_mode)) },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.close),
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        selectedShapeId = null
                                        showDropDownMenu = false
                                    },
                                    shape = MenuDefaults.trailingItemShape
                                )
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
            key(currentNest, recomposeTrigger, tempCancelZone) {
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
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val extraColors = LocalExtraColors.current

                    Canvas(Modifier.fillMaxSize()) {
                        paths.forEach { (shape, path) ->
                            val selected = shape.id == selectedShapeId
                            this.IntersectionShape(
                                path = path,
                                shape = shape.snap().highlightedIfSelected(selected, primaryColor),
                                center = center,
                                shapesColor = extraColors.shapes,
                                erase = false,
                                eraseColor = null
                            )
                        }
                    }

                    NestOverlay(
                        center = center,
                        nest = currentNest.copy(
                            intersectionShapes = paths.keys.mapTo(mutableSetOf()) { it.snap() },
                            cancelZone = tempCancelZone
                        ),
                        depth = Int.MAX_VALUE,
                        eraseColor = MaterialTheme.colorScheme.background,
                        pointSettingsDisplay = true,
                        showCancelZone = true,
                        hideShapes = true
                    )

                    Canvas(Modifier.fillMaxSize()) {
                        centerOfNest(center)
                    }
                }
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit, isInDragAroundMode, nestId) {
                        detectTransformGestures(
                            panZoomLock = true,
                            onGestureStart = {
                                if (!isInDragAroundMode) {
                                    netOffsetChange = Offset.Zero
                                    witnessShape = paths.keys.find { it.id == selectedShapeId }?.snap()
                                }
                            },
                            onGestureEnd = { totalPanChange: Offset, totalZoomChange: Float, totalRotationChange: Float ->
                                if (!isInDragAroundMode) {
                                    witnessShape = null

                                    logWtf { "pan: $totalPanChange (dist = ${totalPanChange.getDistanceSquared()}\nzoom: $totalZoomChange, rotation: $totalRotationChange\n " }
                                    if ((totalPanChange.getDistanceSquared() > 0f) || totalZoomChange != 0f || totalRotationChange != 0f) {
                                        saveCurrentNest()
                                    }
                                }
                            }
                        ) { centroid, pan, gestureZoom, gestureRotate ->
                            if (isInDragAroundMode) {

                                val oldScale = zoom.value
                                val newScale = zoom.value * gestureZoom
                                val newAngle = angle.value + gestureRotate

                                val newOffset =
                                    (offset.value + centroid / oldScale).rotateBy(gestureRotate) - (centroid / newScale + pan / oldScale)

                                // For natural zooming and rotating, the centroid of the gesture should
                                // be the fixed point where zooming and rotating occurs.
                                // We compute where the centroid was (in the pre-transformed coordinate
                                // space), and then compute where it will be after this delta.
                                // We then compute what the new offset should be to keep the centroid
                                // visually stationary for rotating and zooming, and also apply the pan.
                                scope.launch {
                                    offset.snapTo(newOffset)
                                    zoom.snapTo(newScale)
                                    angle.snapTo(newAngle)
                                }
                            } else {
                                val shapeId = selectedShapeId ?: return@detectTransformGestures
                                val shape = paths.keys.firstOrNull { it.id == shapeId } ?: return@detectTransformGestures

                                val oldScale = shape.scale
                                val newScale = oldScale * gestureZoom
                                val newAngle = (shape.angle + gestureRotate) % 360

                                val canvasCentroid = manipulationSystem.normalize(manipulationSystem.transform(centroid))

                                // Same thing as above but there's no need to apply the offset (and in fact it'll break the whole thing)
                                // because the pan is the amount of drag
                                val canvasPan = (pan / zoom.value).rotateBy(-angle.value)

                                // Compute the new offset that keeps the gesture centroid
                                // visually fixed during rotation and scaling.
                                //
                                // Derivation:
                                //   C  = center + O + R(θ) * d       (pre-gesture)
                                //   C' = center + N + R(θ+Δθ) * d'  (post-gesture)
                                // where:
                                //   C  = canvas centroid,
                                //   O = old offset
                                //   θ = old angle
                                //   d  = local point offset from shape center
                                //   N  = new offset, Δθ = rotation delta,
                                //   d' = d * (newScale / oldScale) (path scales linearly)
                                //
                                // Solving for N with C' = C + pan:
                                //   N = (C - center) + pan
                                //       - R(Δθ) * (C - center - O) * (newScale / oldScale)
                                val newOffset =
                                    canvasPan + canvasCentroid -
                                            (canvasCentroid - shape.offset).rotateBy(gestureRotate) *
                                            (newScale / oldScale)

                                val newShape = shape.copy(
                                    offset = newOffset,
                                    scale = newScale,
                                    angle = newAngle
                                )

                                val newSnappedShape = newShape.snap()

                                netOffsetChange = newSnappedShape.offset - witnessShape!!.offset

                                paths -= shape
                                addPath(newShape)

                                points
                                    .filter { (_, point) -> point.nestId == nestId && point.shapeId == shapeId }
                                    .forEach { (_, point) ->

                                        val pointChanged = point.copy(offset = point.offset + netOffsetChange)
                                        point.pos = pointsService.computePointOffsetRealTime(pointChanged, newSnappedShape)
                                    }
                            }
                        }
                    }
            )
        }
    }

    if (showMoreSheet) {


        DragonModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
        ) {
            DragonSettingsGroup(
                title = R.string.nest_info,
                contentPadding = dragonSettingGroupPaddingValues

            ) {
                Text(
                    text = stringResource(R.string.shapes_number, currentNest.intersectionShapes.size),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = stringResource(R.string.current_nest, nestId),
                    modifier = Modifier.fillMaxWidth()
                )

                Setting(UiSettingsStore.nestsCellSizeDp)
            }

            DragonSettingsGroup(
                title = R.string.nest_edition,
                contentPadding = dragonSettingGroupPaddingValues
            ) {
                NestNameEditor(currentNest, pointsService, Modifier.fillMaxWidth())

                val showAllPointsInCurrentShape by UiSettingsStore.showAllPointsInCurrentShape.asState()
                SwitchRow(
                    state = currentNest.showAllPointsInCurrentShape ?: showAllPointsInCurrentShape,
                    title = stringResource(R.string.show_all_actions_on_current_shape),
                    description = stringResource(R.string.show_all_actions_on_current_shape_desc),
                    onReset = {
                        pointsService.editNest(nestId) { old ->
                            old.copy(showAllPointsInCurrentShape = null)
                        }
                    }
                ) { value ->
                    pointsService.editNest(nestId) { old ->
                        old.copy(showAllPointsInCurrentShape = value)
                    }
                }

                val showAllPointsInCurrentNest by UiSettingsStore.showAllPointsInCurrentNest.asState()
                SwitchRow(
                    state = currentNest.showAllPointsInCurrentNest ?: showAllPointsInCurrentNest,
                    title = stringResource(R.string.show_all_actions_in_current_nest),
                    description = stringResource(R.string.show_all_actions_in_current_nest_desc),
                    onReset = {
                        pointsService.editNest(nestId) { old ->
                            old.copy(showAllPointsInCurrentNest = null)
                        }
                    }
                ) { value ->
                    pointsService.editNest(nestId) { old ->
                        old.copy(showAllPointsInCurrentNest = value)
                    }
                }

                val showCurrentShape by UiSettingsStore.showCurrentShape.asState()
                SwitchRow(
                    state = currentNest.showCurrentShape ?: showCurrentShape,
                    title = stringResource(R.string.show_shape),
                    description = stringResource(R.string.show_shape_desc),
                    onReset = {
                        pointsService.editNest(nestId) { old ->
                            old.copy(showCurrentShape = null)
                        }
                    }
                ) { value ->
                    pointsService.editNest(nestId) { old ->
                        old.copy(showCurrentShape = value)
                    }
                }

                val showAllShapesInNest by UiSettingsStore.showAllShapesInNest.asState()

                SwitchRow(
                    state = currentNest.showAllShapes ?: showAllShapesInNest,
                    title = stringResource(R.string.show_all_shapes),
                    description = stringResource(R.string.show_all_shapes_desc),
                    onReset = {
                        pointsService.editNest(nestId) { old ->
                            old.copy(showAllShapes = null)
                        }
                    }
                ) { value ->
                    pointsService.editNest(nestId) { old ->
                        old.copy(showAllShapes = value)
                    }
                }

                SliderWithLabel(
                    label = stringResource(R.string.cancel_zone),
                    description = stringResource(R.string.cancel_zone_desc),
                    value = tempCancelZone,
                    valueRange = 0..300,
                    onReset = { tempCancelZone = Nest.defaultCancelZone },
                    onDragStateChange = { isDragging ->
                        if (!isDragging) {
                            pointsService.editNest(nestId) { old ->
                                old.copy(cancelZone = tempCancelZone)
                            }
                        }
                    }
                ) { newValue -> tempCancelZone = newValue }
            }
        }
    }


    if (showNestManagementDialog) {
        NestManagementDialog(
            onDismissRequest = { showNestManagementDialog = false },
            onSelect = {
                saveCurrentNest()
                selectedShapeId = null
                nestNavigation.goToNest(it.id)
                showNestManagementDialog = false
            }
        )
    }

    if (showShapesManagementDialog) {
        IntersectionShapeManagementDialog(
            shapes = paths.keys,
            onSelectShape = { newShape ->
                selectedShapeId = newShape
            },
            onSave = { newShapes ->
                pointsService.editNest(nestId) { old ->
                    old.copy(intersectionShapes = newShapes)
                }
                // Reset the selected shape. if you only added new ones, it'll resolve to the same as before, but if you removed the current selected one, it'll deselect cause it won't find it
                selectedShapeId = newShapes.find { it.id == selectedShapeId }?.id
            }
        ) {
            recomposeTrigger++
            showShapesManagementDialog = false
        }
    }

    DebugZone(DebugSettingsStore.nestDebugInfo) {
        Text("Paths size: ${paths.size}")
        Text("RecomposeTrigger: $recomposeTrigger")
    }
}