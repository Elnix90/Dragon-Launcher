package org.elnix.dragonlauncher.ui.settings.customization

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.serializables.CustomGlow
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape
import org.elnix.dragonlauncher.base.model.serializables.Nest
import org.elnix.dragonlauncher.base.navigaton.ManipulationSystem
import org.elnix.dragonlauncher.base.resolveShape
import org.elnix.dragonlauncher.enumsui.toggle.NestEditTools
import org.elnix.dragonlauncher.enumsui.toggle.NestEditTools.EnterNest
import org.elnix.dragonlauncher.enumsui.toggle.NestEditTools.GoParentNest
import org.elnix.dragonlauncher.enumsui.toggle.NestEditTools.NestManagement
import org.elnix.dragonlauncher.enumsui.toggle.ShapesEditTools
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.px
import org.elnix.dragonlauncher.ktx.rotateBy
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.ktx.snapToRound
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.UiConstants.dragonSettingGroupPaddingValues
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.components.AnimatedFab
import org.elnix.dragonlauncher.ui.base.components.RowWithScrollIndicator
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.components.IntersectionShape
import org.elnix.dragonlauncher.ui.components.ManipulationSystemReset
import org.elnix.dragonlauncher.ui.components.burger.MoreOptions
import org.elnix.dragonlauncher.ui.dialogs.IntersectionShapeManagementDialog
import org.elnix.dragonlauncher.ui.dialogs.NestManagementDialog
import org.elnix.dragonlauncher.ui.dragon.components.DragonDropDownMenu
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonRow
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.dragon.settings.Setting
import org.elnix.dragonlauncher.ui.helpers.DebugZone
import org.elnix.dragonlauncher.ui.helpers.SelfCheckNestPresent
import org.elnix.dragonlauncher.ui.helpers.ShapePreview
import org.elnix.dragonlauncher.ui.helpers.UndoRedoBlock
import org.elnix.dragonlauncher.ui.helpers.customobjects.drawNeonGlowLine
import org.elnix.dragonlauncher.ui.helpers.customobjects.shapeToPath
import org.elnix.dragonlauncher.ui.helpers.detectTransformGestures
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.helpers.swipe.NestOverlay
import org.elnix.dragonlauncher.ui.helpers.swipe.rememberDrawParams


@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
public fun NestEditScreen2(
    pointsViewModel: PointsViewModel = activityViewModel(),
    initialNestId: Int,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val pointsService = pointsViewModel.pointsService

    val snapShapesOffset by UiSettingsStore.snapShapesOffset.asState()
    val snapShapesScale by UiSettingsStore.snapShapesScale.asState()
    val snapShapeAngle by UiSettingsStore.snapShapeAngle.asState()

    val cellSizeDp by UiSettingsStore.widgetsCellSizeDp.asState()
    val cellSizePx = cellSizeDp.px // TODO
    var showMoreSheet by remember { mutableStateOf(false) }

    val nests by pointsService.nests.asState()
    val rowsScrollStates = List(3) { rememberScrollState() }

    var showShapesManagementDialog by remember { mutableStateOf(false) }
    var showNestManagementDialog by remember { mutableStateOf(false) }
    var center by remember { mutableStateOf(Offset.Zero) }

    var selectedShapeId: Int? by remember { mutableStateOf(null) }
    val isInDragAroundMode: Boolean = selectedShapeId == null

    val nestNavigation = pointsViewModel.nestsNavigationService
    LaunchedEffect(Unit) {
        nestNavigation.goToNest(initialNestId)
    }

    val nestId by pointsViewModel.currentNestId.collectAsState()
    val currentNest = remember(nestId) {
        pointsService.findNestByIdOrNull(nestId) ?: run {
            // The nest isn't found in the list, create a new one with this id
            pointsService.addNest()
            ctx.showToast("Saved missing nest!")
            onBack()
            null
        }
    } ?: return

    var tempCancelZone by remember { mutableIntStateOf(currentNest.cancelZone) }
    var tempCustomName by remember { mutableStateOf(currentNest.name ?: "") }

    SelfCheckNestPresent()

    val manipulationSystem = remember { ManipulationSystem(center) }
    LaunchedEffect(center) {
        manipulationSystem.center = center
    }

    val offset = manipulationSystem.offset
    val angle = manipulationSystem.angle
    val zoom = manipulationSystem.zoom

    val density = LocalDensity.current
    val paths: SnapshotStateMap<IntersectionShape, Path> = remember { mutableStateMapOf() }
    val shapes: Set<IntersectionShape> = paths.keys

    fun addPath(shape: IntersectionShape) {
        paths[shape] = shapeToPath(shape.shape.resolveShape(), shape.getSize(density.density), density)
    }

    LaunchedEffect(currentNest.intersectionShapes) {
        paths.clear()
        currentNest.intersectionShapes.forEach { shape ->
            addPath(shape)
        }
    }

    val snapOffsetThreshold = 30.dp.px

    fun IntersectionShape.snap(): IntersectionShape = this.copy(
        offset = if (snapShapesOffset) this.offset.snapToRound(Offset.Zero, snapOffsetThreshold) else this.offset,
        scale = if (snapShapesScale) this.scale.snapToRound(1f, 0.1f) else this.scale,
        angle = if (snapShapeAngle) this.angle.snapToRound(0f, 20f) else this.angle
    )

    fun saveCurrentNest() {
        pointsService.editNest(nestId) { old ->
            old.copy(
                intersectionShapes = shapes.mapTo(mutableSetOf()) { shape -> shape.snap() }
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
//    TODO("Mark center of the nest with some graphical stuff for users")

    val recomposeTrigger by pointsService.recomposeTrigger.asState()
    val drawParams = rememberDrawParams(
        preventBgErasing = true,
        showConfiguratorDecorations = false,
        forceShowAllActionsInCurrentNest = true,
        allowShowPointCenter = false,
        hideSelectedPoint = false,
        showCancelZone = true,
        hideShapes = false
    )

    SettingsScaffold(
        title = stringResource(R.string.edit_nest_arg, nestId),
        onBack = handleBack,
        helpText = "Nest",
        onReset = {
            pointsService.resetNest(nestId)
            onBack()
        },
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
                    scope.launch {
                        when (it) {
                            ShapesEditTools.SnapOffset -> UiSettingsStore.snapShapesOffset.set(ctx, !snapShapesOffset)
                            ShapesEditTools.SnapScale -> UiSettingsStore.snapShapesScale.set(ctx, !snapShapesScale)
                            ShapesEditTools.SnapAngle -> UiSettingsStore.snapShapeAngle.set(ctx, !snapShapeAngle)
                        }
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
                                shapes.firstOrNull { shape -> shape.id == shapeId }
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
                            val filteredShapes = shapes.filter { it.id != selectedShapeId }
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
            /**
             * Main Canva, draws the circles, and sub nests by recursivity.
             *
             * Uses [graphicsLayer] to apply transformation of [offset], [zoom] and [angle] and provide an easy way to navigate in the canvas
             *
             * - If the user drags a point, I draw it in the offset of where the finger is.
             * - If the user has hovered a point for more than 500ms, a radial circle overlay spawns and indicates
             *   that it can release to merge the 2 points
             * - If the selected point is a live nest, it is drawn in transparency on top of it.
             *   **Only if the nest isn't a OpenCircleNest that points to the same nest action**
             */
            key(nests.size, currentNest, recomposeTrigger, tempCancelZone) {
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

                    Canvas(Modifier.fillMaxSize()) {
                        centerOfNest(center)
                        repeat(2) { pass ->
                            paths.forEach { (shape, path) ->
                                val selected = shape.id == selectedShapeId
                                this.IntersectionShape(
                                    path = path,
                                    shape = shape.snap().copy(glow = if (selected) CustomGlow(color = primaryColor, radius = 30f) else shape.glow),
                                    center = center,
                                    drawParams = drawParams,
                                    erase = pass == 0
                                )
                            }
                        }
                    }

                    NestOverlay(
                        center = center,
                        nest = currentNest.copy(
                            intersectionShapes = shapes,
                            cancelZone = tempCancelZone
                        ),
                        depth = Int.MAX_VALUE,
                        preventBgErasing = true,
                        showConfiguratorDecorations = true,
                        forceShowAllActionsInCurrentNest = true,
                        hideSelectedPoint = true,
                        showCancelZone = true,
                        hideShapes = true
                    )
                }
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit, isInDragAroundMode, nestId) {
                        detectTransformGestures(
                            panZoomLock = true,
                            onGestureEnd = {
                                if (!isInDragAroundMode) {
                                    saveCurrentNest()
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
                                val shape = shapes.firstOrNull { it.id == shapeId } ?: return@detectTransformGestures

                                val oldScale = shape.scale
                                val newScale = oldScale * gestureZoom
                                val newAngle = shape.angle + gestureRotate

                                val canvasCentroid = manipulationSystem.normalize(manipulationSystem.transform(centroid))

                                // Same thing as above but there's no need to apply the offset (and in fact it'll break the whole thing)
                                // because the pan if the amount of drag
                                val canvasPan = (pan / zoom.value).rotateBy(-angle.value)

                                // Compute the new offset that keeps the gesture centroid
                                // visually fixed during rotation and scaling.
                                //
                                // Derivation:
                                //   C  = center + O + R(θ) * d       (pre-gesture)
                                //   C' = center + N + R(θ+Δθ) * d'  (post-gesture)
                                // where:
                                //   C  = canvas centroid, O = old offset, θ = old angle,
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
                                paths -= shape
                                addPath(newShape)
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
                    text = stringResource(R.string.shapes_number, paths.size),
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
                TextField(
                    value = tempCustomName,
                    onValueChange = {
                        tempCustomName = it

                        pointsService.editNest(nestId) { nest ->
                            nest.copy(name = it)
                        }
                    },
                    placeholder = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.edit_rounded),
                                contentDescription = stringResource(R.string.custom_name)
                            )
                            Text(
                                text = stringResource(R.string.custom_name),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = AppObjectsColors.outlinedTextFieldColors(removeBorder = true),
                    singleLine = true,
                    modifier = Modifier
                        .clip(CircleShape)
                        .fillMaxWidth()
                )

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
                nestNavigation.goToNest(it.id)
                showNestManagementDialog = false
            }
        )
    }

    if (showShapesManagementDialog) {
        IntersectionShapeManagementDialog(
            shapes = shapes,
            onSelectShape = { newShape ->
                selectedShapeId = newShape
                showShapesManagementDialog = false
            },
            onSave = { newShapes ->
                paths.clear()
                newShapes.forEach { shape ->
                    addPath(shape)
                }
            }
        ) { showShapesManagementDialog = false }
    }

    DebugZone(DebugSettingsStore.nestDebugInfo) {
        Text("Current nest: $nestId")
        Text("${paths.size} shapes inside this nest")
        Text("Selected shape: $selectedShapeId")
    }
}


///**
// * Holds an Offset and provides helper functions and value to manage its values and variants inside settings screens that allows objects manipulation
// */
//class TransformedOffset(
//    private val manipulationSystem: ManipulationSystem,
//    private val pointsService: PointsService,
//    private val nestId: Int,
//    /**
//     * Original offset, in normal screen coordinates
//     * It will be transformed to give the actual useful values
//     */
//    val offset: Offset
//) {
//
//    /**
//     * Transformed offset, represents the coordinated in space of the [offset] after undoing the
//     * transformations of [angle], [zoom], and [offset] that are only for visual in the settings screen
//     */
//    public val transformedOffset: Offset by lazy {
//        manipulationSystem.transform(this.offset)
//    }
//
//    /**
//     * Represents the offset of the point, if you do not account for both the [angle], [zoom], and [offset] transformations and the [center]
//     * in the middle ot the screen.
//     *
//     * ### **It's the offset you want to save into the points property**
//     * as it can be interpreted by the [PointsService] and be
//     * converted back to screen coordinates.
//     */
//    public val normalizedOffset: Offset by lazy {
//        manipulationSystem.normalize(this.transformedOffset)
//    }
//
//    /**
//     * Computes the closest point relative to this [transformedOffset].
//     * @see PointsService.computeClosest
//     */
//    public val bestP: Point? by lazy {
//        pointsService.computeClosest(this.normalizedOffset, nestId)
//    }
//
//    private val distance: Float by lazy {
//        val betsPOffset = this.bestP?.let { pointsService.computePointOffset(it) } ?: return@lazy Float.MAX_VALUE
//        betsPOffset distanceTo this.normalizedOffset
//    }
//
//    /**
//     * Whether the distance to the closest point is inferior to an arbitrary [TOUCH_THRESHOLD_PX].
//     *
//     * TODO Make this threshold dependent on the [zoom]
//     */
//    public val distanceSmallEnough: Boolean by lazy { distance <= TOUCH_THRESHOLD_PX }
//
//
//    /** Executes [block] if [distanceSmallEnough] */
//    public inline infix fun ifDistanceIsSmallEnough(block: () -> Point?): Point? = if (distanceSmallEnough) block() else null
//
//    override fun toString(): String =
//        "TR(\n" +
//                "   offset = ${this.offset}\n" +
//                "   transformedOffset = $transformedOffset\n" +
//                "   normalizedOffset = $normalizedOffset\n" +
//                "   bestP = $bestP\n" +
//                "   distance = $distance${if (!distanceSmallEnough) " (Too Far!)" else ""}\n" +
//                ")"
//}


private val lineSize = 30.dp
public fun DrawScope.centerOfNest(center: Offset) {
    val linePx = lineSize.toPx()

    val horizontalStart = Offset(center.x - linePx, center.y)
    val horizontalEnd = Offset(center.x + linePx, center.y)

    val verticalStart = Offset(center.x, center.y - linePx)
    val verticalEnd = Offset(center.x, center.y + linePx)

    drawNeonGlowLine(
        start = horizontalStart,
        end = horizontalEnd,
        color = Color.Red,
        lineStrokeWidth = 1f,
        erase = false,
        glow = CustomGlow(5f)
    )

    drawNeonGlowLine(
        start = verticalStart,
        end = verticalEnd,
        color = Color.Red,
        lineStrokeWidth = 1f,
        erase = false,
        glow = CustomGlow(5f)
    )
}