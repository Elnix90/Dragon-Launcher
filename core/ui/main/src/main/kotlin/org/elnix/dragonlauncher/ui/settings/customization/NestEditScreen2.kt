package org.elnix.dragonlauncher.ui.settings.customization

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.Constants.Settings.TOUCH_THRESHOLD_PX
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.navigaton.ManipulationSystem
import org.elnix.dragonlauncher.base.resolveShape
import org.elnix.dragonlauncher.base.util.ColorUtils.alphaMultiplier
import org.elnix.dragonlauncher.enumsui.toggle.NestEditTools
import org.elnix.dragonlauncher.enumsui.toggle.NestEditTools.EnterNest
import org.elnix.dragonlauncher.enumsui.toggle.NestEditTools.GoParentNest
import org.elnix.dragonlauncher.enumsui.toggle.NestEditTools.NestManagement
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.distanceTo
import org.elnix.dragonlauncher.ktx.rotateBy
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.points.PointsService
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.components.AnimatedFab
import org.elnix.dragonlauncher.ui.base.components.RowWithScrollIndicator
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.modifiers.selfAlignHorizontally
import org.elnix.dragonlauncher.ui.components.IntersectionShape
import org.elnix.dragonlauncher.ui.components.ManipulationSystemReset
import org.elnix.dragonlauncher.ui.dialogs.IntersectionShapeManagementDialog
import org.elnix.dragonlauncher.ui.dialogs.NestManagementDialog
import org.elnix.dragonlauncher.ui.dragon.components.DragonRow
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.helpers.DebugZone
import org.elnix.dragonlauncher.ui.helpers.SelfCheckNestPresent
import org.elnix.dragonlauncher.ui.helpers.ShapePreview
import org.elnix.dragonlauncher.ui.helpers.UndoRedoBlock
import org.elnix.dragonlauncher.ui.helpers.customobjects.shapeToPath
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.helpers.swipe.NestOverlay
import org.elnix.dragonlauncher.ui.helpers.swipe.rememberDrawParams


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NestEditScreen2(
    pointsViewModel: PointsViewModel = activityViewModel(),
    initialNestId: Int,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val pointsService = pointsViewModel.pointsService

    val snapShapes by UiSettingsStore.snapShapes.asState()
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


    SelfCheckNestPresent()

    val manipulationSystem = remember { ManipulationSystem(center) }
    LaunchedEffect(center) {
        manipulationSystem.center = center
    }

    val offset = manipulationSystem.offset
    val angle = manipulationSystem.angle
    val zoom = manipulationSystem.zoom

    val density = LocalDensity.current
    val paths: SnapshotStateMap<IntersectionShape, Path> = remember(currentNest.intersectionShapes) { mutableStateMapOf() }

    fun addPath(shape: IntersectionShape) {
        paths[shape] = shapeToPath(shape.shape.resolveShape(), shape.getSize(density.density), density)
    }

    LaunchedEffect(Unit) {
        currentNest.intersectionShapes.forEach { shape ->
            addPath(shape)
        }
    }

    fun saveCurrentNest() {
        pointsService.editNest(nestId) { old ->
            old.copy(intersectionShapes = paths.keys)
        }
    }

    val handleBack = {
        saveCurrentNest()
        if (selectedShapeId != null) selectedShapeId = null
        else { onBack() }
    }
    BackHandler(onBack = handleBack)

    val recomposeTrigger by pointsService.recomposeTRigger.asState()

    var rc by remember { mutableIntStateOf(0) }


//    var debugPan by remember { mutableStateOf(Offset.Unspecified) }
//    var debugCentroid by remember { mutableStateOf(Offset.Unspecified) }
//    var debugGestureZoom by remember { mutableFloatStateOf(0f) }
//    var debugGestureRotate by remember { mutableFloatStateOf(0f) }

    SettingsScaffold(
        title = stringResource(R.string.edit_nest, nestId),
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
            }


            // 3. Reset offset/zoom/rotation - undo/redo
            RowWithScrollIndicator(rowsScrollStates[2]) {
                ManipulationSystemReset(manipulationSystem)

                Spacer(12.dp)

                UndoRedoBlock(pointsService.undoRedo)
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
                    onClick = { showShapesManagementDialog = true },
                    icon = R.drawable.edit_rounded,
                    minSize = 70.dp,
                    containerColor = MaterialTheme.colorScheme.secondary
                )

                DragonRow(
                    onClick = { selectedShapeId = null },
                    enabled = !isInDragAroundMode
                ) {
                    AnimatedContent(isInDragAroundMode) {
                        val showShapeEdit = if (it) null else {
                            selectedShapeId?.let { shapeId ->
                                paths.keys.firstOrNull { shape -> shape.id == shapeId }
                            }
                        }

                        if (showShapeEdit == null) {
                            Text(stringResource(R.string.move_around_mode))
                        } else {
                            ShapePreview(
                                iconShape = showShapeEdit.shape,
                                modifier = Modifier.size(40.dp)
                            )

                            Text("ID: ${showShapeEdit.id}")
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
                .background(Color.Blue.alphaMultiplier(0.05f))
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

//            Spacer(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .drawBehind {
//                        glowOverlay(
//                            center = debugPan,
//                            color = Color.Blue,
//                            radius = 50f
//                        )
//
//                        glowOverlay(
//                            center = debugCentroid,
//                            color = Color.Red,
//                            radius = 50f
//                        )
//                    }
//            )

//            DragonColumnGroup(modifier = Modifier.selfAlignHorizontally(Alignment.End)) {
//                Text("Rotation: $debugGestureRotate")
//                Text("Zoom: $debugGestureZoom")
//                Text("Internal shapes:${paths.keys}")
//            }

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
            key(nests.size, currentNest, recomposeTrigger) {
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
                        nest = currentNest.copy(intersectionShapes = emptySet()),
                        depth = Int.MAX_VALUE,
                        preventBgErasing = true,
                        showConfiguratorDecorations = true,
                        forceShowAllActionsInCurrentNest = true,
                        hideSelectedPoint = true
                    )
                    val drawParams = rememberDrawParams(
                        preventBgErasing = true,
                        showConfiguratorDecorations = false,
                        forceShowAllActionsInCurrentNest = true,
                        allowShowPointCenter = false,
                        hideSelectedPoint = false
                    )

                    Canvas(Modifier.fillMaxSize()) {
                        repeat(2) { pass ->
                            paths.forEach { (shape, path) ->
                                this.IntersectionShape(
                                    path = path,
                                    shape = shape,
                                    center = center,
                                    drawParams = drawParams,
                                    erase = pass == 0
                                )
                            }
                        }
                    }
                }
            }
            Text(text = rc.toString(), modifier = Modifier.selfAlignHorizontally(Alignment.End))

            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit, isInDragAroundMode, nestId) {
                        detectTransformGestures(true) { centroid, pan, gestureZoom, gestureRotate ->
                            rc++
                            if (isInDragAroundMode) {

                                val oldScale = zoom.value
                                val newScale = zoom.value * gestureZoom
//                                val effectiveNewScale = if (snapShapes) newScale.snapToRound(SNAP_THRESHOLD) else newScale

                                val newAngle = angle.value + gestureRotate
//                                val effectiveNewAngle = if (snapShapes) newAngle.snapToRound(SNAP_THRESHOLD) else newAngle

                                val newOffset =
                                    (offset.value + centroid / oldScale).rotateBy(gestureRotate) - (centroid / newScale + pan / oldScale)
//                                val effectiveNewOffset = if (snapShapes) newOffset.snapToRound(SNAP_THRESHOLD) else newOffset

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
                                val newAngle = shape.angle + gestureRotate

//                                val newOffset =
//                                    (shape.offset + centroid / oldScale).rotateBy(gestureRotate) - (centroid / newScale + pan / oldScale)
//                                (shape.offset + centroid / oldScale).rotateBy(gestureRotate) - (centroid / newScale + pan / oldScale)

//                                val newOffset =
//                                    (shape.offset + centroid / oldScale).rotateBy(gestureRotate) - (centroid / newScale) + pan / oldScale




//                                val effectiveNewOffset = if (snapShapes) {
//                                    newOffset.copy(
//                                        x = newOffset.x.fastRoundToInt().toFloat(),
//                                        y = newOffset.y.fastRoundToInt().toFloat()
//                                    )
//                                } else newOffset

                                val newShape = shape.copy(
//                                    offset = newOffset,
                                    scale = newScale,
                                    angle = newAngle.fastRoundToInt()
                                )
                                paths -= shape
                                addPath(newShape)
                            }
                        }
                    }
            )
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
            shapes = paths.keys,
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


/**
 * Holds an Offset and provides helper functions and value to manage its values and variants inside settings screens that allows objects manipulation
 */
class TransformedOffset(
    private val manipulationSystem: ManipulationSystem,
    private val pointsService: PointsService,
    private val nestId: Int,
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
        manipulationSystem.transform(this.offset)
    }

    /**
     * Represents the offset of the point, if you do not account for both the [angle], [zoom], and [offset] transformations and the [center]
     * in the middle ot the screen.
     *
     * ### **It's the offset you want to save into the points property**
     * as it can be interpreted by the [PointsService] and be
     * converted back to screen coordinates.
     */
    public val normalizedOffset: Offset by lazy {
        manipulationSystem.normalize(this.transformedOffset)
    }

    /**
     * Computes the closest point relative to this [transformedOffset].
     * @see PointsService.computeClosest
     */
    public val bestP: Point? by lazy {
        pointsService.computeClosest(this.normalizedOffset, nestId)
    }

    private val distance: Float by lazy {
        val betsPOffset = this.bestP?.let { pointsService.computePointOffset(it) } ?: return@lazy Float.MAX_VALUE
        betsPOffset distanceTo this.normalizedOffset
    }

    /**
     * Whether the distance to the closest point is inferior to an arbitrary [TOUCH_THRESHOLD_PX].
     *
     * TODO Make this threshold dependent on the [zoom]
     */
    public val distanceSmallEnough: Boolean by lazy { distance <= TOUCH_THRESHOLD_PX }


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

