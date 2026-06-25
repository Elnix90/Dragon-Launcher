@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.settings.customization

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elnix90.logging.WIDGET_TAG
import io.github.elnix90.logging.logD
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.elnix.dragonlauncher.base.model.models.ResizeSide
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.base.model.serializables.Widget
import org.elnix.dragonlauncher.common.circles.rotateBy
import org.elnix.dragonlauncher.enumsui.toggle.MoveAroundTools
import org.elnix.dragonlauncher.enumsui.toggle.MoveAroundTools.Center
import org.elnix.dragonlauncher.enumsui.toggle.MoveAroundTools.ResetRotation
import org.elnix.dragonlauncher.enumsui.toggle.MoveAroundTools.ResetZoom
import org.elnix.dragonlauncher.enumsui.toggle.WidgetsToolsAddNestRemove
import org.elnix.dragonlauncher.enumsui.toggle.WidgetsToolsCenterReset
import org.elnix.dragonlauncher.enumsui.toggle.WidgetsToolsMoveUpDown
import org.elnix.dragonlauncher.enumsui.toggle.WidgetsToolsSnapping
import org.elnix.dragonlauncher.enumsui.toggle.WidgetsToolsUpDown
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.toDp
import org.elnix.dragonlauncher.models.WidgetsViewModel
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.RowWithScrollIndicator
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.modifiers.conditional
import org.elnix.dragonlauncher.ui.base.modifiers.settingsGroup
import org.elnix.dragonlauncher.ui.components.WidgetHostView
import org.elnix.dragonlauncher.ui.dialogs.AddPointDialog
import org.elnix.dragonlauncher.ui.dialogs.NestManagementDialog
import org.elnix.dragonlauncher.ui.dialogs.ShapePickerDialog
import org.elnix.dragonlauncher.ui.dragon.components.DragonColumnGroup
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonColumn
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsSlider
import org.elnix.dragonlauncher.ui.helpers.SmallShapeRow
import org.elnix.dragonlauncher.ui.helpers.UndoRedoBlock
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.statusbar.StatusBar
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetsTab(
    onBack: () -> Unit,
    widgetsViewModel: WidgetsViewModel = activityViewModel(),
    onLaunchSystemWidgetPicker: (nestId: Int) -> Unit,
    onResetWidgetSize: (id: Int, widgetId: Int) -> Unit,
    onRemoveWidget: (Widget) -> Unit,
    initialNestId: Int = 0
) {
    val cellSizeDp by UiSettingsStore.cellSizeDp.asState()
    val cellSizePx = cellSizeDp * LocalDensity.current.density
    val widgets by widgetsViewModel.widgets.collectAsState()
    val scope = rememberCoroutineScope()

    val widgetsDebugInfos by DebugSettingsStore.widgetsDebugInfo.asState()

    var selected by remember { mutableStateOf<Widget?>(null) }
    val aWidgetIsSelected = selected != null

    var snapMove by remember { mutableStateOf(true) }
    var snapResize by remember { mutableStateOf(true) }
    var snapRotation by remember { mutableStateOf(true) }

    var showMoreSheet by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showNestPickerDialog by remember { mutableStateOf(false) }
    var nestId by remember { mutableIntStateOf(initialNestId) }
    var isPrecisionModeActive by remember { mutableStateOf(false) }


    fun removeWidget(widget: Widget) {
        onRemoveWidget(widget)
        if (selected == widget) selected = null
    }

    val handleBack = {
        if (selected != null) {
            selected = null
        } else {
            onBack()
        }
    }
    BackHandler(onBack = handleBack)

    val rowsScrollStates = List(2) { rememberScrollState() }

    val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val zoom = remember { Animatable(1f) }
    val angle = remember { Animatable(0f) }

    SettingsScaffold(
        title = stringResource(R.string.widgets),
        onBack = handleBack,
        helpText = stringResource(R.string.widgets_tab_help),
        onReset = { widgetsViewModel.resetAllWidgets() },
        applyPadding = false,
        scrollableContent = false,
        otherIcons = arrayOf(
            Triple(
                { showMoreSheet = !showMoreSheet },
                R.drawable.more_vert,
                stringResource(R.string.more)
            )
        ),
        bottomContent = {
            RowWithScrollIndicator(rowsScrollStates[0]) {
                MultiSelectConnectedButtonRow(
                    entries = WidgetsToolsSnapping.entries,
                    checked = {
                        when (it) {
                            WidgetsToolsSnapping.SnapGrid -> snapMove
                            WidgetsToolsSnapping.SnapResize -> snapResize
                            WidgetsToolsSnapping.SnapRotation -> snapRotation
                        }
                    }
                ) { entry ->
                    when (entry) {
                        WidgetsToolsSnapping.SnapGrid -> {
                            snapMove = !snapMove
                        }

                        WidgetsToolsSnapping.SnapResize -> {
                            snapResize = !snapResize
                        }

                        WidgetsToolsSnapping.SnapRotation -> {
                            snapRotation = !snapRotation
                        }
                    }

                }

                Spacer(12.dp)
                UndoRedoBlock(widgetsViewModel.undoRedo)
            }


            RowWithScrollIndicator(rowsScrollStates[1]) {
                MultiSelectConnectedButtonRow(
                    entries = WidgetsToolsAddNestRemove.entries,
                    checked = {
                        when (it) {
                            WidgetsToolsAddNestRemove.Add, WidgetsToolsAddNestRemove.Nests -> true
                            WidgetsToolsAddNestRemove.Remove -> aWidgetIsSelected
                        }
                    },
                    enabled = {
                        when (it) {
                            WidgetsToolsAddNestRemove.Add, WidgetsToolsAddNestRemove.Nests -> true
                            WidgetsToolsAddNestRemove.Remove -> aWidgetIsSelected
                        }
                    }
                ) { entry ->
                    when (entry) {
                        WidgetsToolsAddNestRemove.Add -> {
                            showAddDialog = true
                        }

                        WidgetsToolsAddNestRemove.Nests -> {
                            showNestPickerDialog = true
                        }

                        WidgetsToolsAddNestRemove.Remove -> {
                            selected?.let { removeWidget(it) }
                        }
                    }
                }

                Spacer(12.dp)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MultiSelectConnectedButtonColumn(
                        entries = WidgetsToolsCenterReset.entries,
                        showLabel = false,
                        checked = { true },
                        enabled = { aWidgetIsSelected }
                    ) { entry ->
                        when (entry) {
                            WidgetsToolsCenterReset.Center -> {
                                selected?.let {
                                    widgetsViewModel.centerWidget(it.id)
                                }
                            }

                            WidgetsToolsCenterReset.Reset -> {
                                selected?.let {
                                    if (it.action is Action.OpenWidget) {
                                        onResetWidgetSize(it.id, (it.action as Action.OpenWidget).widgetId)
                                    } else {
                                        widgetsViewModel.resetWidgetSize(it.id)
                                    }
                                }
                            }
                        }
                    }

                    MultiSelectConnectedButtonColumn(
                        entries = WidgetsToolsUpDown.entries,
                        showLabel = false,
                        checked = { true }
                    ) { entry ->
                        when (entry) {
                            WidgetsToolsUpDown.Up -> {
                                if (widgets.isNotEmpty()) {
                                    val idx = widgets.indexOfFirst { it == selected }
                                    val next = if (idx <= 0) widgets.last() else widgets[idx - 1]
                                    selected = next
                                }
                            }

                            WidgetsToolsUpDown.Down -> {
                                if (widgets.isNotEmpty()) {
                                    val idx = widgets.indexOfFirst { it == selected }
                                    val next = if (idx == -1 || idx == widgets.lastIndex) widgets.first() else widgets[idx + 1]
                                    selected = next
                                }
                            }
                        }
                    }

                    val upDownEnabled = aWidgetIsSelected && widgets.size > 1

                    MultiSelectConnectedButtonColumn(
                        entries = WidgetsToolsMoveUpDown.entries,
                        showLabel = false,
                        enabled = { upDownEnabled },
                        checked = { upDownEnabled }
                    ) { entry ->
                        when (entry) {
                            WidgetsToolsMoveUpDown.MoveUp -> {
                                selected?.let {
                                    widgetsViewModel.moveWidgetDown(it.id)

                                }
                            }

                            WidgetsToolsMoveUpDown.MoveDown -> {
                                selected?.let {
                                    widgetsViewModel.moveWidgetUp(it.id)
                                }
                            }
                        }
                    }
                }
            }
        }
    ) {

        Box(modifier = Modifier.fillMaxSize()) {
            /**
             * The widgets and the grid, displayed first, to keep access to the buttons
             * The pointerInput is used to disable any widgets on click outside
             */
            Box(
                Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .pointerInput(Unit) {
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
                    }
                    .graphicsLayer {
                        translationX = -offset.value.x * zoom.value
                        translationY = -offset.value.y * zoom.value
                        scaleX = zoom.value
                        scaleY = zoom.value
                        rotationZ = angle.value
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
            ) {
                /**
                 * Draw the grid of snapping that fills the entire screen
                 */
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, MaterialTheme.colorScheme.primary, DragonShape)
                        .conditional(snapMove) {
                            drawWithCache {
                                onDrawBehind {
                                    val lineWidth = 1f
                                    val color = Color.White.copy(alpha = 0.25f)

                                    // Vertical lines
                                    var x = 0f
                                    while (x <= size.width) {
                                        drawLine(
                                            color = color,
                                            start = Offset(x, 0f),
                                            end = Offset(x, size.height),
                                            strokeWidth = lineWidth
                                        )
                                        x += cellSizePx
                                    }

                                    // Horizontal lines
                                    var y = 0f
                                    while (y <= size.height) {
                                        drawLine(
                                            color = color,
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = lineWidth
                                        )
                                        y += cellSizePx
                                    }
                                }
                            }
                        }
                )

                widgets
                    .filter { it.nestId == nestId }
                    .forEach { widget ->
                        DraggableWidget(
                            widgetsViewModel = widgetsViewModel,
                            app = widget,
                            snapRotation = { snapRotation },
                            snapMove = { snapMove },
                            snapResize = { snapResize },
                            selected = widget.id == selected?.id,
                            onPrecisionModeChange = { isPrecisionModeActive = it },
                            onSelect = { selected = widget },
                            onEdit = { widgetsViewModel.editWidget(it) }
                        )
                    }
            }

            this@SettingsScaffold.AnimatedVisibility(
                visible = isPrecisionModeActive,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 32.dp),
                enter = fadeIn() + slideInVertically { -it },
                exit = fadeOut() + slideOutVertically { -it }
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = CircleShape
                ) {
                    Text(
                        text = stringResource(R.string.precision_mode_active),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (widgetsDebugInfos && widgets.isNotEmpty()) {
                DragonColumnGroup {
                    widgets.forEach {
                        Text(it.toString())
                    }
                }
            }
        }
    }

    StatusBar(null)

    if (showAddDialog) {
        AddPointDialog(
            onDismiss = { showAddDialog = false },
            actions = Action.defaultChoosableActions.toMutableList().apply {
                add(0, Action.OpenWidget.dummy)
            },
            onActionSelected = { action ->
                when (action) {
                    is Action.OpenWidget -> onLaunchSystemWidgetPicker(nestId)
                    else -> widgetsViewModel.addWidget(action, nestId = nestId)
                }
                showAddDialog = false
            }
        )
    }


    if (showMoreSheet) {
        DragonModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
        ) {
            Text("${stringResource(R.string.widget_number_total)}: ${widgets.size}")
            Text("${stringResource(R.string.widget_number_nest)}: ${widgets.count { it.nestId == nestId }}")
            Text("${stringResource(R.string.current_nest)}: $nestId")

            HorizontalDivider()

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

            SettingsSlider(UiSettingsStore.cellSizeDp)
        }
    }

    if (showNestPickerDialog) {
        NestManagementDialog(
            onDismissRequest = { showNestPickerDialog = false },
            title = stringResource(R.string.pick_a_nest)
        ) {
            logD(WIDGET_TAG) { it.toString() }
            nestId = it.id
            selected = null
            logD(WIDGET_TAG) { nestId.toString() }

            showNestPickerDialog = false
        }
    }
}


/**
 * A fully interactive, self-contained widget overlay that handles all real-time manipulation:
 * drag to move, corner handles to resize, a rotation handle, and tap/long-press for selection
 * and precision mode.
 *
 * Position, size and angle are tracked locally as normalized/span state and only committed
 * to the parent via [onEdit] at drag end, keeping I/O overhead minimal.
 * Snap variants are provided as lambdas so the caller can toggle them reactively without
 * restarting pointer inputs.
 *
 * Position compensation on resize and move is angle-aware: deltas are rotated through the
 * widget's current angle so handles behave correctly at any rotation.
 *
 * @param widgetsViewModel Provides `cellSizePx`, `minSize` and screen dimensions.
 * @param app Current immutable widget data used as the source of truth on each commit.
 * @param selected Whether this widget is currently selected, controls handle visibility.
 * @param snapRotation Returns true if rotation should snap to 15° increments.
 * @param snapMove Returns true if position should snap to the cell grid.
 * @param snapResize Returns true if span should snap to whole cell units.
 * @param onPrecisionModeChange Called when the long-press precision mode toggles on or off.
 * @param onSelect Called when the widget is tapped or a drag starts on it.
 * @param onEdit Called at the end of any drag (move, resize, rotate) with the updated [Widget].
 */
@Composable
private fun DraggableWidget(
    widgetsViewModel: WidgetsViewModel,
    app: Widget,
    selected: Boolean,

    snapRotation: () -> Boolean,
    snapMove: () -> Boolean,
    snapResize: () -> Boolean,

    onPrecisionModeChange: (Boolean) -> Unit,
    onSelect: () -> Unit,
    onEdit: (Widget) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent

    val cellSizePx by widgetsViewModel.cellSizePx.collectAsState()
    val minSize = widgetsViewModel.minSize
    val dm = widgetsViewModel.dm

    val widthPixels = dm.widthPixels
    val heightPixels = dm.heightPixels

    val snapScaleX = cellSizePx / widthPixels
    val snapScaleY = cellSizePx / heightPixels

    var widgetCenter by remember(selected) { mutableStateOf(Offset.Zero) }
    var handleCoordinates by remember(selected) { mutableStateOf<LayoutCoordinates?>(null) }

    var widgetAngle by remember(app.angle) { mutableFloatStateOf(app.angle) }

    var widgetX by remember(app.x) { mutableFloatStateOf(app.x) }
    var widgetY by remember(app.y) { mutableFloatStateOf(app.y) }
    var rawWidgetX by remember(app.x) { mutableFloatStateOf(app.x) }
    var rawWidgetY by remember(app.y) { mutableFloatStateOf(app.y) }

    var widgetWidth by remember(app.spanX) { mutableFloatStateOf(app.spanX) }
    var widgetHeight by remember(app.spanY) { mutableFloatStateOf(app.spanY) }
    var rawWidgetWidth by remember(app.spanX) { mutableFloatStateOf(app.spanX) }
    var rawWidgetHeight by remember(app.spanY) { mutableFloatStateOf(app.spanY) }


    var isPrecisionMode by remember { mutableStateOf(false) }
    var showEditPopup by remember { mutableStateOf(false) }
    var showShapeEditor by remember { mutableStateOf(false) }

    LaunchedEffect(isPrecisionMode) {
        onPrecisionModeChange(isPrecisionMode)
    }


    fun commitChange(newApp: Widget? = null) {
        onEdit(
            newApp ?: app.copy(
                spanX = widgetWidth,
                spanY = widgetHeight,
                x = widgetX,
                y = widgetY,
                angle = widgetAngle
            )
        )
    }

    fun resizeWidget(corner: ResizeSide, dxPx: Float, dyPx: Float) {
        val deltaSpanX = dxPx / cellSizePx
        val deltaSpanY = dyPx / cellSizePx
        val deltaPosX = dxPx / widthPixels
        val deltaPosY = dyPx / heightPixels

        val angleRad = Math.toRadians(widgetAngle.toDouble())
        val cos = cos(angleRad).toFloat()
        val sin = sin(angleRad).toFloat()

        var localDeltaX = 0f
        var localDeltaY = 0f


        when (corner) {
            ResizeSide.Left -> {
                rawWidgetWidth = (rawWidgetWidth - deltaSpanX).coerceAtLeast(minSize)
                localDeltaX = deltaPosX
            }

            ResizeSide.Right -> {
                rawWidgetWidth = (rawWidgetWidth + deltaSpanX).coerceAtLeast(minSize)
            }

            ResizeSide.Top -> {
                rawWidgetHeight = (rawWidgetHeight - deltaSpanY).coerceAtLeast(minSize)
                localDeltaY = deltaPosY
            }

            ResizeSide.Bottom -> {
                rawWidgetHeight = (rawWidgetHeight + deltaSpanY).coerceAtLeast(minSize)
            }
        }

        val worldDeltaX = (localDeltaX * cos - localDeltaY * sin)
        val worldDeltaY = (localDeltaX * sin + localDeltaY * cos)

        widgetX += worldDeltaX
        widgetY += worldDeltaY

        widgetWidth = if (snapResize()) {
            rawWidgetWidth.roundToInt().toFloat().coerceAtLeast(minSize)
        } else rawWidgetWidth

        widgetHeight = if (snapResize()) {
            rawWidgetHeight.roundToInt().toFloat().coerceAtLeast(minSize)
        } else rawWidgetHeight
    }


    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (widgetX * widthPixels).toInt(),
                    y = (widgetY * heightPixels).toInt()
                )
            }
            .size(
                width = (widgetWidth * cellSizePx).toDp,
                height = (widgetHeight * cellSizePx).toDp
            )
            // Used to compute the widget position for rotation computing
            .onGloballyPositioned { coordinates ->
                val rect = coordinates.boundsInRoot()
                widgetCenter = Offset(
                    rect.left + rect.width / 2f,
                    rect.top + rect.height / 2f
                )
            }
            .graphicsLayer {
                rotationZ = widgetAngle
                transformOrigin = TransformOrigin.Center
                clip = false
            }
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = borderColor,
                shape = DragonShape
            )
    ) {

        // Widget / App content (touch blocked during editing)
        WidgetHostView(
            widget = app,
            blockTouches = true,
            cellSizePx = cellSizePx
        ) { }


        // Main interaction overlay (move + tap)
        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(app.id) {
                    detectTapGestures(
                        onPress = {
                            isPrecisionMode = false
                            onSelect()
                            try {
                                withTimeout(viewConfiguration.longPressTimeoutMillis.milliseconds) {
                                    tryAwaitRelease()
                                }
                            } catch (_: TimeoutCancellationException) {
                                isPrecisionMode = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                    )
                }
                .pointerInput(app.id, app.angle, app.x, app.y) {
                    detectDragGestures(
                        onDragStart = {
                            onSelect()
                            rawWidgetX = widgetX
                            rawWidgetY = widgetY
                        },
                        onDrag = { change, dragAmount ->

                            val angleRad = Math.toRadians(widgetAngle.toDouble())

                            val cos = cos(angleRad)
                            val sin = sin(angleRad)

                            val amountX = if (isPrecisionMode) dragAmount.x / 2f else dragAmount.x
                            val amountY = if (isPrecisionMode) dragAmount.y / 2f else dragAmount.y

                            val worldDx = (amountX * cos - amountY * sin).toFloat()
                            val worldDy = (amountX * sin + amountY * cos).toFloat()


                            rawWidgetX += worldDx / widthPixels
                            rawWidgetY += worldDy / heightPixels

                            val isSnapMove = snapMove() && !isPrecisionMode

                            widgetX = if (isSnapMove) {
                                (rawWidgetX / snapScaleX).roundToInt() * snapScaleX
                            } else rawWidgetX

                            widgetY = if (isSnapMove) {
                                (rawWidgetY / snapScaleY).roundToInt() * snapScaleY
                            } else rawWidgetY

                            change.consume()
                        },
                        onDragEnd = {
                            commitChange()
                            isPrecisionMode = false
                        },
                        onDragCancel = {
                            isPrecisionMode = false
                        }
                    )
                }
        )

        if (selected) {

            // Rotate drag handle
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-50).dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .onGloballyPositioned { handleCoordinates = it }
                    .pointerInput(app.id, app.angle) {

                        var dragStartFingerAngle: Float? = null
                        var dragStartWidgetAngle = 0f

                        detectDragGestures(

                            onDragStart = { offset ->

                                val rootPos = handleCoordinates
                                    ?.localToRoot(offset)
                                    ?: return@detectDragGestures

                                dragStartFingerAngle = Math.toDegrees(
                                    atan2(
                                        (rootPos.y - widgetCenter.y).toDouble(),
                                        (rootPos.x - widgetCenter.x).toDouble()
                                    )
                                ).toFloat()

                                // Initialize here to prevent the widget rotated to do one billion rotations a second
                                dragStartWidgetAngle = widgetAngle
                            },

                            onDragEnd = {
                                dragStartFingerAngle = null
                                commitChange()
                            },

                            onDragCancel = {
                                dragStartFingerAngle = null
                            }

                        ) { change, _ ->

                            val rootPos = handleCoordinates
                                ?.localToRoot(change.position)
                                ?: return@detectDragGestures

                            val currentFingerAngle = Math.toDegrees(
                                atan2(
                                    (rootPos.y - widgetCenter.y).toDouble(),
                                    (rootPos.x - widgetCenter.x).toDouble()
                                )
                            ).toFloat()

                            dragStartFingerAngle?.let { startAngle ->

                                var delta = currentFingerAngle - startAngle

                                if (delta > 180f) delta -= 360f
                                if (delta < -180f) delta += 360f

                                val newAngle = dragStartWidgetAngle + delta

                                widgetAngle = if (snapRotation()) {
                                    (newAngle / 15f).roundToInt() * 15f
                                } else newAngle
                            }

                            change.consume()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.refresh),
                    contentDescription = stringResource(R.string.rotation),
                    tint = MaterialTheme.colorScheme.primary
                )
            }


            // Resize handles - only visible when selected

            val dotSize = 12.dp
            val hitboxPadding = 20.dp

            // Top handle
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = -((dotSize.value / 2 + hitboxPadding.value).dp))
                    .size(dotSize + hitboxPadding * 2)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .pointerInput(ResizeSide.Top, app.spanX, app.spanY) {
                        detectDragGestures(
                            onDragEnd = ::commitChange
                        ) { change, dragAmount ->
                            change.consume()
                            resizeWidget(ResizeSide.Top, 0f, dragAmount.y)
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .align(Alignment.Center)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }

            // Bottom handle
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = ((dotSize.value / 2 + hitboxPadding.value).dp))
                    .size(dotSize + hitboxPadding * 2)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .pointerInput(ResizeSide.Bottom, app.spanX, app.spanY) {
                        detectDragGestures(
                            onDragEnd = ::commitChange
                        ) { change, dragAmount ->
                            change.consume()
                            resizeWidget(ResizeSide.Bottom, 0f, dragAmount.y)
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .align(Alignment.Center)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }

            // Left handle
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = -((dotSize.value / 2 + hitboxPadding.value).dp))
                    .size(dotSize + hitboxPadding * 2)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .pointerInput(ResizeSide.Left, app.spanX, app.spanY) {
                        detectDragGestures(
                            onDragEnd = ::commitChange
                        ) { change, dragAmount ->
                            change.consume()
                            resizeWidget(ResizeSide.Left, dragAmount.x, 0f)
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .align(Alignment.Center)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }

            // Right handle
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = ((dotSize.value / 2 + hitboxPadding.value).dp))
                    .size(dotSize + hitboxPadding * 2)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .pointerInput(ResizeSide.Right, app.spanX, app.spanY) {
                        detectDragGestures(
                            onDragEnd = ::commitChange
                        ) { change, dragAmount ->
                            change.consume()
                            resizeWidget(ResizeSide.Right, dragAmount.x, 0f)
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .align(Alignment.Center)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }


            // Edit button
            Box {
                DragonIconButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(Color.Transparent),
                    icon = R.drawable.edit_rounded,
                    contentDescription = stringResource(R.string.edit)
                ) { showEditPopup = true }

                DropdownMenu(
                    expanded = showEditPopup,
                    onDismissRequest = { showEditPopup = false },
                    containerColor = Color.Transparent,
                    shadowElevation = 0.dp,
                    tonalElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.settingsGroup()
                    ) {

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = app.ghosted == true,
                                onCheckedChange = {
                                    commitChange(app.copy(ghosted = it))
                                }
                            )

                            Text(
                                text = stringResource(R.string.ghosted),
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 12.sp
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = app.foreground == true,
                                onCheckedChange = {
                                    commitChange(app.copy(foreground = it))
                                }
                            )

                            Text(
                                text = stringResource(R.string.foreground),
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 12.sp
                            )
                        }

                        SmallShapeRow(
                            selected = app.shape ?: IconShape.Square,
                            onReset = {
                                commitChange(app.copy(shape = null))
                            }
                        ) { showShapeEditor = true }
                    }
                }
            }
        }
    }

    if (showShapeEditor) {
        ShapePickerDialog(
            selected = app.shape ?: IconShape.Square,
            onDismiss = { showShapeEditor = false }
        ) {
            commitChange(app.copy(shape = it))
            showShapeEditor = false
        }
    }
}
