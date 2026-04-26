@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.settings.customization

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.elnix.dragonlauncher.base.ktx.toDp
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.serializables.FloatingAppObject
import org.elnix.dragonlauncher.common.serializables.FloatingAppsJson
import org.elnix.dragonlauncher.common.serializables.IconShape
import org.elnix.dragonlauncher.common.serializables.SwipeActionSerializable
import org.elnix.dragonlauncher.common.undoredo.UndoRedoManager
import org.elnix.dragonlauncher.common.utils.Constants.Logging.WIDGET_TAG
import org.elnix.dragonlauncher.enumsui.UndRedoEditTools
import org.elnix.dragonlauncher.enumsui.WidgetsToolsAddNestRemove
import org.elnix.dragonlauncher.enumsui.WidgetsToolsCenterReset
import org.elnix.dragonlauncher.enumsui.WidgetsToolsMoveUpDown
import org.elnix.dragonlauncher.enumsui.WidgetsToolsSnapping
import org.elnix.dragonlauncher.enumsui.WidgetsToolsUpDown
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.models.FloatingAppsViewModel
import org.elnix.dragonlauncher.models.FloatingAppsViewModel.ResizeCorner
import org.elnix.dragonlauncher.settings.stores.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.WidgetsSettingsStore
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.modifiers.settingsGroup
import org.elnix.dragonlauncher.ui.components.FloatingAppsHostView
import org.elnix.dragonlauncher.ui.composition.LocalFloatingAppsViewModel
import org.elnix.dragonlauncher.ui.dialogs.AddPointDialog
import org.elnix.dragonlauncher.ui.dialogs.NestManagementDialog
import org.elnix.dragonlauncher.ui.dialogs.ShapePickerDialog
import org.elnix.dragonlauncher.ui.dragon.components.DragonColumnGroup
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonColumn
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.helpers.SmallShapeRow
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.statusbar.StatusBar
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun FloatingAppsTab(
    onBack: () -> Unit,
    onLaunchSystemWidgetPicker: (nestId: Int) -> Unit,
    onResetWidgetSize: (id: Int, widgetId: Int) -> Unit,
    onRemoveWidget: (FloatingAppObject) -> Unit,
    initialNestId: Int = 0
) {
    val ctx = LocalContext.current

    val floatingAppsViewModel = LocalFloatingAppsViewModel.current
    val cellSizePx by floatingAppsViewModel.cellSizePx.collectAsState()
    val cellSizeDp by floatingAppsViewModel.cellSizeDp.collectAsState()
    val scope = rememberCoroutineScope()

    val widgetsDebugInfos by DebugSettingsStore.widgetsDebugInfo.asState()

    val floatingApps by floatingAppsViewModel.floatingApps.collectAsState()

    var selected by remember { mutableStateOf<FloatingAppObject?>(null) }
    val aWidgetIsSelected = selected != null

    var snapMove by remember { mutableStateOf(true) }
    var snapResize by remember { mutableStateOf(true) }
    var snapRotation by remember { mutableStateOf(true) }

    var showScaleDropdown by remember { mutableStateOf(false) }
    var widgetsScale by remember { mutableFloatStateOf(0.80f) }

    var showAddDialog by remember { mutableStateOf(false) }
    var showNestPickerDialog by remember { mutableStateOf(false) }
    var nestId by remember { mutableIntStateOf(initialNestId) }
    var isPrecisionModeActive by remember { mutableStateOf(false) }


    /* ───────────────────────────────────────────────────────────────── */

    fun snapshotWidgets(): List<FloatingAppObject> = floatingApps.map { it.copy() }


    val undoRedo = remember { UndoRedoManager() }

    LaunchedEffect(Unit) {
        undoRedo.register(
            key = "floatingApps",
            snapshot = { snapshotWidgets() },
            restore = {
                floatingAppsViewModel.restoreFloatingApps(it)
                selected = floatingApps.find { p -> p.id == (selected?.id ?: "") }
            }
        )
    }

    fun save() {
        scope.launch {
            WidgetsSettingsStore.jsonSetting.set(ctx, FloatingAppsJson.encodeFloatingApps(snapshotWidgets()))
        }
    }

    fun applyChange(mutator: () -> Unit) {
        undoRedo.applyChange(mutator)
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


    fun removeWidget(floatingApp: FloatingAppObject) {
        onRemoveWidget(floatingApp)
        if (selected == floatingApp) selected = null
    }


    /**
     * The widgets and the grid, displayed first, to keep access to the buttons
     *
     * The pointerInput is used to disable any widgets on click outside
     */
    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    selected = null
                }
            }
            .scale(widgetsScale)
            .border(1.dp, MaterialTheme.colorScheme.primary, DragonShape)
    ) {

        /**
         * Draw the grid of snapping that fills the entire screen
         */
        if (snapMove) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
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

        floatingApps
            .filter { it.nestId == nestId }
            .forEach { floatingApp ->
                DraggableFloatingApp(
                    floatingAppsViewModel = floatingAppsViewModel,
                    app = floatingApp,
                    snapRotation = { snapRotation },
                    snapMove = { snapMove },
                    snapResize = { snapResize },
                    selected = floatingApp.id == selected?.id,
                    onPrecisionModeChange = { isPrecisionModeActive = it },
                    onSelect = { selected = floatingApp },
                    onEdit = {
                        applyChange {
                            logD(WIDGET_TAG) { "applyChange\nold widget: ${floatingApp.shape}\new one: ${it.shape}" }

                            floatingAppsViewModel.editFloatingApp(it)
                        }
                    }
                )
            }

        AnimatedVisibility(
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
    }


    /**
     * The settings scaffold, with on top the optional status bar
     */
    Column(
        Modifier.fillMaxSize()
    ) {

        StatusBar(null)

        SettingsScaffold(
            title = stringResource(R.string.widgets),
            onBack = onBack,
            helpText = stringResource(R.string.floating_apps_tab_help),
            onReset = {
                scope.launch {
                    applyChange {
                        floatingAppsViewModel.resetAllFloatingApps()
                    }
                }
            },
            otherIcons = arrayOf(
                Triple(
                    {
                        showScaleDropdown = !showScaleDropdown
                    }, Icons.Default.MoreVert,
                    stringResource(R.string.more)
                )
            ),
            bottomContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    MultiSelectConnectedButtonRow(
                        entries = WidgetsToolsSnapping.entries,
                        isChecked = {
                            when (it) {
                                WidgetsToolsSnapping.SnapGrid -> snapMove
                                WidgetsToolsSnapping.SnapResize -> snapResize
                                WidgetsToolsSnapping.SnapRotation -> snapRotation
                            }
                        }
                    ) { entry ->
                        scope.launch {
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
                    }

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

                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MultiSelectConnectedButtonRow(
                        entries = WidgetsToolsAddNestRemove.entries,
                        isChecked = {
                            when (it) {
                                WidgetsToolsAddNestRemove.Add, WidgetsToolsAddNestRemove.Nests -> true
                                WidgetsToolsAddNestRemove.Remove -> aWidgetIsSelected
                            }
                        },
                        isEnabled = {
                            when (it) {
                                WidgetsToolsAddNestRemove.Add, WidgetsToolsAddNestRemove.Nests -> true
                                WidgetsToolsAddNestRemove.Remove -> aWidgetIsSelected
                            }
                        }
                    ) { entry ->
                        scope.launch {
                            when (entry) {
                                WidgetsToolsAddNestRemove.Add -> {
                                    showAddDialog = true
                                }

                                WidgetsToolsAddNestRemove.Nests -> {
                                    showNestPickerDialog = true
                                }

                                WidgetsToolsAddNestRemove.Remove -> {
                                    selected?.let {
                                        applyChange {
                                            removeWidget(it)
                                        }
                                    }
                                }
                            }
                        }
                    }


                    MultiSelectConnectedButtonColumn(
                        entries = WidgetsToolsCenterReset.entries,
                        showLabel = false,
                        isChecked = { true },
                        isEnabled = { aWidgetIsSelected }
                    ) { entry ->
                        scope.launch {
                            when (entry) {
                                WidgetsToolsCenterReset.Center -> {
                                    selected?.let {
                                        applyChange {
                                            floatingAppsViewModel.centerFloatingApp(it.id)
                                        }
                                    }
                                }

                                WidgetsToolsCenterReset.Reset -> {
                                    selected?.let {
                                        applyChange {
                                            if (it.action is SwipeActionSerializable.OpenWidget) {
                                                onResetWidgetSize(it.id, (it.action as SwipeActionSerializable.OpenWidget).widgetId)
                                            } else {
                                                floatingAppsViewModel.resetFloatingAppSize(it.id)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    MultiSelectConnectedButtonColumn(
                        entries = WidgetsToolsUpDown.entries,
                        showLabel = false,
                        isChecked = { true }
                    ) { entry ->
                        scope.launch {
                            when (entry) {
                                WidgetsToolsUpDown.Up -> {
                                    if (floatingApps.isNotEmpty()) {
                                        val idx = floatingApps.indexOfFirst { it == selected }
                                        val next = if (idx <= 0) floatingApps.last() else floatingApps[idx - 1]
                                        selected = next
                                    }
                                }

                                WidgetsToolsUpDown.Down -> {
                                    if (floatingApps.isNotEmpty()) {
                                        val idx = floatingApps.indexOfFirst { it == selected }
                                        val next = if (idx == -1 || idx == floatingApps.lastIndex) floatingApps.first() else floatingApps[idx + 1]
                                        selected = next
                                    }
                                }
                            }
                        }
                    }

                    val upDownEnabled = aWidgetIsSelected && floatingApps.size > 1

                    MultiSelectConnectedButtonColumn(
                        entries = WidgetsToolsMoveUpDown.entries,
                        showLabel = false,
                        isEnabled = { upDownEnabled },
                        isChecked = { upDownEnabled }
                    ) { entry ->
                        scope.launch {
                            when (entry) {
                                WidgetsToolsMoveUpDown.MoveUp -> {
                                    selected?.let {
                                        applyChange {
                                            floatingAppsViewModel.moveFloatingAppDown(it.id)
                                        }
                                    }
                                }

                                WidgetsToolsMoveUpDown.MoveDown -> {
                                    selected?.let {
                                        applyChange {
                                            floatingAppsViewModel.moveFloatingAppUp(it.id)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            content = {
                Column(Modifier.fillMaxSize()) {
                    AnimatedVisibility(showScaleDropdown) {
                        DragonColumnGroup {
                            Text("${stringResource(R.string.widget_number_total)}: ${floatingApps.size}")
                            Text("${stringResource(R.string.widget_number_nest)}: ${floatingApps.count { it.nestId == nestId }}")
                            Text("${stringResource(R.string.current_nest)}: $nestId")

                            HorizontalDivider()

                            SliderWithLabel(
                                label = stringResource(R.string.scale),
                                value = widgetsScale,
                                valueRange = 0.5f..1f,
                                onReset = { widgetsScale = 0.85f }
                            ) { widgetsScale = it }

                            SliderWithLabel(
                                label = stringResource(R.string.cell_size),
                                description = stringResource(R.string.cell_size_help),
                                value = cellSizeDp,
                                valueRange = 1..100,
                                onReset = {
                                    floatingAppsViewModel.updateCellSize(null)
                                },
                            ) {
                                floatingAppsViewModel.updateCellSize(it)
                            }
                        }
                    }
                }
            }
        )
    }


    if (widgetsDebugInfos && floatingApps.isNotEmpty()) {
        DragonColumnGroup {
            floatingApps.forEach {
                Text(it.toString())
            }
        }
    }

    if (showAddDialog) {
        AddPointDialog(
            onDismiss = { showAddDialog = false },
            actions = setOf(
                SwipeActionSerializable.OpenWidget(0, "", ""),
                SwipeActionSerializable.OpenCircleNest(0),
                SwipeActionSerializable.GoParentNest,
                SwipeActionSerializable.LaunchShortcut("", ""),
                SwipeActionSerializable.LaunchApp("", false, 0),
                SwipeActionSerializable.OpenUrl(""),
                SwipeActionSerializable.OpenFile(""),
                SwipeActionSerializable.NotificationShade,
                SwipeActionSerializable.ControlPanel,
                SwipeActionSerializable.OpenAppDrawer(),
                SwipeActionSerializable.Lock,
                SwipeActionSerializable.ReloadApps,
                SwipeActionSerializable.OpenRecentApps,
                SwipeActionSerializable.OpenDragonLauncherSettings()
            ),
            onActionSelected = { action ->
                when (action) {
                    is SwipeActionSerializable.OpenWidget -> onLaunchSystemWidgetPicker(nestId)
                    else -> floatingAppsViewModel.addFloatingApp(action, nestId = nestId)
                }
                showAddDialog = false
            }
        )
    }

    if (showNestPickerDialog) {
        NestManagementDialog(
            onDismissRequest = { showNestPickerDialog = false },
            title = stringResource(R.string.pick_a_nest),
            onDelete = null,
            onNewNest = null,
            onNameChange = null
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
 * @param floatingAppsViewModel Provides `cellSizePx`, `minSize` and screen dimensions.
 * @param app Current immutable widget data used as the source of truth on each commit.
 * @param selected Whether this widget is currently selected, controls handle visibility.
 * @param snapRotation Returns true if rotation should snap to 15° increments.
 * @param snapMove Returns true if position should snap to the cell grid.
 * @param snapResize Returns true if span should snap to whole cell units.
 * @param onPrecisionModeChange Called when the long-press precision mode toggles on or off.
 * @param onSelect Called when the widget is tapped or a drag starts on it.
 * @param onEdit Called at the end of any drag (move, resize, rotate) with the updated [FloatingAppObject].
 */
@Composable
private fun DraggableFloatingApp(
    floatingAppsViewModel: FloatingAppsViewModel,
    app: FloatingAppObject,
    selected: Boolean,

    snapRotation: () -> Boolean,
    snapMove: () -> Boolean,
    snapResize: () -> Boolean,

    onPrecisionModeChange: (Boolean) -> Unit,
    onSelect: () -> Unit,
    onEdit: (FloatingAppObject) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent

    val cellSizePx by floatingAppsViewModel.cellSizePx.collectAsState()
    val minSize = floatingAppsViewModel.minSize
    val dm = floatingAppsViewModel.dm

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


    fun commitChange(newApp: FloatingAppObject? = null) {
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

    fun resizeFloatingApp(corner: ResizeCorner, dxPx: Float, dyPx: Float) {
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
            ResizeCorner.Left -> {
                rawWidgetWidth = (rawWidgetWidth - deltaSpanX).coerceAtLeast(minSize)
                localDeltaX = deltaPosX
            }

            ResizeCorner.Right -> {
                rawWidgetWidth = (rawWidgetWidth + deltaSpanX).coerceAtLeast(minSize)
            }

            ResizeCorner.Top -> {
                rawWidgetHeight = (rawWidgetHeight - deltaSpanY).coerceAtLeast(minSize)
                localDeltaY = deltaPosY
            }

            ResizeCorner.Bottom -> {
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
        FloatingAppsHostView(
            floatingAppObject = app,
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
                                withTimeout(viewConfiguration.longPressTimeoutMillis) {
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
                    .onGloballyPositioned {
                        handleCoordinates = it
                    }
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
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.rotation),
                    tint = MaterialTheme.colorScheme.primary
                )
            }


            // ──────────────────────────────────────────
            // Resize handles - only visible when selected
            // ──────────────────────────────────────────

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
                    .pointerInput(ResizeCorner.Top, app.spanX, app.spanY) {
                        detectDragGestures(
                            onDragEnd = ::commitChange
                        ) { change, dragAmount ->
                            change.consume()
                            resizeFloatingApp(ResizeCorner.Top, 0f, dragAmount.y)
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
                    .pointerInput(ResizeCorner.Bottom, app.spanX, app.spanY) {
                        detectDragGestures(
                            onDragEnd = ::commitChange
                        ) { change, dragAmount ->
                            change.consume()
                            resizeFloatingApp(ResizeCorner.Bottom, 0f, dragAmount.y)
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
                    .pointerInput(ResizeCorner.Left, app.spanX, app.spanY) {
                        detectDragGestures(
                            onDragEnd = ::commitChange
                        ) { change, dragAmount ->
                            change.consume()
                            resizeFloatingApp(ResizeCorner.Left, dragAmount.x, 0f)
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
                    .pointerInput(ResizeCorner.Right, app.spanX, app.spanY) {
                        detectDragGestures(
                            onDragEnd = ::commitChange
                        ) { change, dragAmount ->
                            change.consume()
                            resizeFloatingApp(ResizeCorner.Right, dragAmount.x, 0f)
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
            DragonIconButton(
                onClick = { showEditPopup = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(Color.Transparent),
                imageVector = Icons.Default.Edit,
                contentDescription = stringResource(R.string.edit)
            )
        }


        // ──────────────────────────────────────────
        // Ghost Toggle, to prevent clicks
        // ──────────────────────────────────────────

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

    ShapePickerDialog(
        expanded = { showShapeEditor },
        selected = app.shape ?: IconShape.Square,
        onDismiss = { showShapeEditor = false }
    ) {
        commitChange(app.copy(shape = it))
        showShapeEditor = false
    }
}
