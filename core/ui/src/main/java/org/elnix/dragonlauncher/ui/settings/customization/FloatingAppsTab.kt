@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.settings.customization

import android.R.attr.x
import android.R.attr.y
import android.annotation.SuppressLint
import android.content.ComponentName
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.FormatClear
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.GridOff
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.MoveDown
import androidx.compose.material.icons.filled.MoveUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.ktx.toDp
import org.elnix.dragonlauncher.common.FloatingAppObject
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.logging.logD
import org.elnix.dragonlauncher.common.serializables.SwipeActionSerializable
import org.elnix.dragonlauncher.common.utils.Constants.Logging.WIDGET_TAG
import org.elnix.dragonlauncher.common.utils.UiConstants.DragonShape
import org.elnix.dragonlauncher.common.utils.WidgetHostProvider
import org.elnix.dragonlauncher.models.FloatingAppsViewModel
import org.elnix.dragonlauncher.settings.stores.DebugSettingsStore
import org.elnix.dragonlauncher.ui.components.FloatingAppsHostView
import org.elnix.dragonlauncher.ui.dialogs.AddPointDialog
import org.elnix.dragonlauncher.ui.dialogs.NestManagementDialog
import org.elnix.dragonlauncher.ui.helpers.CircleIconButton
import org.elnix.dragonlauncher.ui.helpers.UpDownButton
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsLazyHeader
import org.elnix.dragonlauncher.ui.remembers.LocalFloatingAppsViewModel
import org.elnix.dragonlauncher.ui.remembers.LocalShowStatusBar
import org.elnix.dragonlauncher.ui.statusbar.StatusBar
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FloatingAppsTab(
    widgetHostProvider: WidgetHostProvider,
    onBack: () -> Unit,
    onLaunchSystemWidgetPicker: (nestId: Int) -> Unit,
    onResetWidgetSize: (id: Int, widgetId: Int) -> Unit,
    onRemoveWidget: (FloatingAppObject) -> Unit
) {
    val ctx = LocalContext.current
    val showStatusBar = LocalShowStatusBar.current

    val floatingAppsViewModel = LocalFloatingAppsViewModel.current
    val scope = rememberCoroutineScope()

    val widgetsDebugInfos by DebugSettingsStore.widgetsDebugInfo.flow(ctx)
        .collectAsState(initial = false)

    val floatingApps by floatingAppsViewModel.floatingApps.collectAsState()

    val cellSizePx = floatingAppsViewModel.cellSizePx

    var selected by remember { mutableStateOf<FloatingAppObject?>(null) }

    val isSelected = selected != null

    var snapMove by remember { mutableStateOf(true) }
    var snapResize by remember { mutableStateOf(true) }

    fun removeWidget(floatingApp: FloatingAppObject) {
        onRemoveWidget(floatingApp)
        if (selected == floatingApp) selected = null
    }

    val widgetNumber = floatingApps.filter { it.action is SwipeActionSerializable.OpenWidget }.size
    val floatingAppsNumber = floatingApps.filter { it.action is SwipeActionSerializable.LaunchApp }.size

    var showAddDialog by remember { mutableStateOf(false) }
    var showNestPickerDialog by remember { mutableStateOf(false) }
    var nestId by remember { mutableIntStateOf(0) }

    /**
     * Status bar things, copy paste from the getters, do no change that, it's just for displaying
     * the status bar if enabled to preview more easily
     */
    val systemInsets = WindowInsets.systemBars.asPaddingValues()

    val isRealFullscreen = systemInsets.calculateTopPadding() == 0.dp


    /** ───────────────────────────────────────────────────────────────── */


    Column {

        AnimatedVisibility(showStatusBar && isRealFullscreen) {
            StatusBar(null)
        }

        SettingsLazyHeader(
            title = "${stringResource(R.string.widgets_floating_apps)} (${widgetNumber}) (${floatingAppsNumber})",
            onBack = onBack,
            helpText = stringResource(R.string.floating_apps_tab_help),
            onReset = {
                scope.launch {
                    floatingAppsViewModel.resetAllFloatingApps()
                }
            },
            content = {
                Box(
                    Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures {
                                selected = null
                            }
                        }
                )
            }
        )
    }


    /**
     * Draw the grid of snapping that fills the entire screen
     */
    Box(Modifier.fillMaxSize()) {

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


        /* ---------------- Widget canvas ---------------- */

        floatingApps
            .filter { it.nestId == nestId }
            .sortedBy { it.id == selected?.id } // Selected is always displayed first for easier click access
            .forEach { floatingApp ->
                key(floatingApp.id, nestId) {
                    DraggableFloatingApp(
                        floatingAppsViewModel = floatingAppsViewModel,
                        app = floatingApp,
                        selected = floatingApp.id == selected?.id,
                        widgetHostProvider = widgetHostProvider,
                        onSelect = { selected = floatingApp },
                        onRotateEnd = {
                            floatingAppsViewModel.rotateFloatingApp(floatingApp.id, it, true)
                        },
                        onMoveEnd = { x,y ->
                            floatingAppsViewModel.moveFloatingApp(floatingApp.id, x, y, snapMove)
                        },
                        onResize = { corner, dx, dy ->
                            floatingAppsViewModel.resizeFloatingApp(floatingApp.id, corner, dx, dy, false)
                        },
                        onResizeEnd = { corner ->
                            floatingAppsViewModel.resizeFloatingApp(floatingApp.id, corner, 0f, 0f, snapResize)
                        },
                        onRemove = { removeWidget(floatingApp) },
                        onEdit = {
                            floatingAppsViewModel.editFloatingApp(it)
                        }
                    )
                }
            }


        /* ───────────── Bottom controls ───────────── */
        Row(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            UpDownButton(
                upIcon = Icons.Default.Add,
                downIcon = Icons.Default.AccountCircle,
                color = MaterialTheme.colorScheme.primary,
                contentDescriptionUp = stringResource(R.string.add_widget),
                contentDescriptionDown = stringResource(R.string.pick_a_nest),
                padding = 16.dp,
                onClickUp = { showAddDialog = true },
                onClickDown = { showNestPickerDialog = true }
            )

            UpDownButton(
                upIcon = Icons.Default.CenterFocusStrong,
                downIcon = Icons.Default.Restore,
                color = MaterialTheme.colorScheme.primary,
                contentDescriptionUp = stringResource(R.string.center_selected_widget),
                contentDescriptionDown = stringResource(R.string.reset_widget),
                upEnabled = isSelected,
                downEnabled = isSelected,
                padding = 16.dp,
                onClickUp = {
                    selected?.let { floatingAppsViewModel.centerFloatingApp(it.id) }

                },
                onClickDown = {
                    selected?.let {
                        if (it.action is SwipeActionSerializable.OpenWidget) {
                            onResetWidgetSize(it.id, (it.action as SwipeActionSerializable.OpenWidget).widgetId)
                        } else {
                            floatingAppsViewModel.resetFloatingAppSize(it.id)
                        }
                    }
                }
            )

            UpDownButton(
                upIcon = Icons.Default.ArrowUpward,
                downIcon = Icons.Default.ArrowDownward,
                color = MaterialTheme.colorScheme.secondary,
                contentDescriptionUp = stringResource(R.string.select_previous_widget),
                contentDescriptionDown = stringResource(R.string.select_next_widget),
                upEnabled = true,
                downEnabled = true,
                padding = 16.dp,
                onClickUp = {
                    if (floatingApps.isNotEmpty()) {
                        val idx = floatingApps.indexOfFirst { it == selected }
                        val next = if (idx <= 0) floatingApps.last() else floatingApps[idx - 1]
                        selected = next
                    }
                },
                onClickDown = {
                    if (floatingApps.isNotEmpty()) {
                        val idx = floatingApps.indexOfFirst { it == selected }
                        val next =
                            if (idx == -1 || idx == floatingApps.lastIndex) floatingApps.first() else floatingApps[idx + 1]
                        selected = next
                    }
                }
            )

            val upDownEnabled = isSelected && floatingApps.size > 1

            UpDownButton(
                upIcon = Icons.Default.MoveUp,
                downIcon = Icons.Default.MoveDown,
                color = MaterialTheme.colorScheme.tertiary,
                contentDescriptionUp = stringResource(R.string.move_selected_widget_up),
                contentDescriptionDown = stringResource(R.string.move_selected_widget_down),
                upEnabled = upDownEnabled,
                downEnabled = upDownEnabled,
                padding = 16.dp,
                onClickUp = {
                    selected?.let { floatingAppsViewModel.moveFloatingAppDown(it.id) }
                },
                onClickDown = {
                    selected?.let { floatingAppsViewModel.moveFloatingAppUp(it.id) }
                }
            )

            UpDownButton(
                upIcon = if (snapMove) Icons.Default.GridOn else Icons.Default.GridOff,
                downIcon = if (snapResize) Icons.Default.FormatSize else Icons.Default.FormatClear,
                color = MaterialTheme.colorScheme.primary,
                contentDescriptionUp = stringResource(R.string.enable_grid),
                contentDescriptionDown = stringResource(R.string.enable_scale_snap),
                upEnabled = true,
                downEnabled = true,
                padding = 16.dp,
                onClickUp = { snapMove = !snapMove },
                onClickDown = { snapResize = !snapResize }
            )

            // Delete selected widget
            CircleIconButton(
                icon = Icons.Default.Remove,
                contentDescription = stringResource(R.string.delete_widget),
                tint = MaterialTheme.colorScheme.error,
                enabled = isSelected,
                padding = 16.dp
            ) {
                selected?.let { removeWidget(it) }
            }
        }
    }

    if (widgetsDebugInfos) {
        Box(
            modifier = Modifier
                .background(Color.DarkGray.copy(0.5f))
                .padding(5.dp)
        ) {
            Column {
                floatingApps.forEach {
                    Text(it.toString())
                }
            }
        }
    }

    if (showAddDialog) {
        AddPointDialog(
            onDismiss = { showAddDialog = false },
            actions = listOf(
                SwipeActionSerializable.OpenWidget(0, ComponentName("", "")),
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
            canCopyId = false,
            onDelete = null,
            onNewNest = null,
            onNameChange = null
        ) {
            ctx.logD(WIDGET_TAG, it.toString())
            nestId = it.id
            selected = null
            ctx.logD(WIDGET_TAG, nestId.toString())

            showNestPickerDialog = false
        }
    }
}


/**
 * Handles all widget interactions: drag to move, resize handles, tap/long-press.
 * Resize handles provide visual-only resize feedback by compensating position changes internally.
 *
 * @param floatingAppsViewModel ViewModel for widget state management
 * @param app Current widget data
 * @param selected True if this widget is currently selected
 * @param onSelect Callback when widget is tapped/selected
 * @param onMove Callback for position drag deltas (dx, dy in pixels)
 * @param onResize Callback for resize drag (corner, dx, dy in pixels)
 * @param onRemove Callback for long-press removal
 */
@SuppressLint("LocalContextResourcesRead")
@Composable
private fun DraggableFloatingApp(
    floatingAppsViewModel: FloatingAppsViewModel,
    app: FloatingAppObject,
    selected: Boolean,
    widgetHostProvider: WidgetHostProvider,
    onSelect: () -> Unit,
    onRotateEnd: (Float) -> Unit,
    onMoveEnd: (Float, Float) -> Unit,
    onResize: (FloatingAppsViewModel.ResizeCorner, Float, Float) -> Unit,
    onResizeEnd: (FloatingAppsViewModel.ResizeCorner) -> Unit,
    onRemove: () -> Unit,
    onEdit: (FloatingAppObject) -> Unit
) {
    val ctx = LocalContext.current
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent

    val cellSizePx = floatingAppsViewModel.cellSizePx

    val dm = ctx.resources.displayMetrics

    val widthPixels = dm.widthPixels
    val heightPixels = dm.heightPixels

    var x by remember { mutableFloatStateOf(app.x) }
    var y by remember { mutableFloatStateOf(app.y) }

    val width = app.spanX * cellSizePx
    val height = app.spanY * cellSizePx


    var widgetCenter by remember { mutableStateOf(Offset.Zero) }
    var handleCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var widgetAngle by remember { mutableFloatStateOf(app.angle) }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (x * widthPixels).toInt(),
                    y = (y * heightPixels).toInt()
                )
            }
            .size(
                width = width.toDp,
                height = height.toDp
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
            cellSizePx = cellSizePx,
            widgetHostProvider = widgetHostProvider
        ) { }


        // Main interaction overlay (move + tap)
        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(app.id) {
                    detectTapGestures(
                        onPress = { onSelect() },
                        onLongPress = { onRemove() }
                    )
                }
                .pointerInput(app.id) {
                    detectDragGestures(
                        onDragStart = { onSelect() },
                        onDrag = { change, dragAmount ->

                            val angleRad = Math.toRadians(app.angle.toDouble())

                            val cos = cos(angleRad)
                            val sin = sin(angleRad)

                            x = (dragAmount.x * cos - dragAmount.y * sin).toInt()
                            y = (dragAmount.x * sin + dragAmount.y * cos).toInt()

                            change.consume()
                        },
                        onDragEnd = { onMoveEnd(x, y) }
                    )
                }
        )

        if (selected) {

            // Rotate drag handle
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-40).dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
                    .onGloballyPositioned {
                        handleCoordinates = it
                    }
                    .pointerInput(app.id) {

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

                                dragStartWidgetAngle = widgetAngle
                            },

                            onDragEnd = {
                                dragStartFingerAngle = null
                                onRotateEnd(widgetAngle)
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

                                widgetAngle = dragStartWidgetAngle + delta
                            }

                            change.consume()
                        }
                    }
            )


            // ------------------------------------------
            // Resize handles - only visible when selected
            // ------------------------------------------

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
                    .pointerInput(FloatingAppsViewModel.ResizeCorner.Top) {
                        detectDragGestures(
                            onDragEnd = {
                                onResizeEnd(FloatingAppsViewModel.ResizeCorner.Top)
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            onResize(FloatingAppsViewModel.ResizeCorner.Top, 0f, dragAmount.y)
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
                    .pointerInput(FloatingAppsViewModel.ResizeCorner.Bottom) {
                        detectDragGestures(
                            onDragEnd = {
                                onResizeEnd(FloatingAppsViewModel.ResizeCorner.Bottom)
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            onResize(FloatingAppsViewModel.ResizeCorner.Bottom, 0f, dragAmount.y)
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
                    .pointerInput(FloatingAppsViewModel.ResizeCorner.Left) {
                        detectDragGestures(
                            onDragEnd = {
                                onResizeEnd(FloatingAppsViewModel.ResizeCorner.Left)
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            onResize(FloatingAppsViewModel.ResizeCorner.Left, dragAmount.x, 0f)
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
                    .pointerInput(FloatingAppsViewModel.ResizeCorner.Right) {
                        detectDragGestures(
                            onDragEnd = {
                                onResizeEnd(FloatingAppsViewModel.ResizeCorner.Right)
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            onResize(FloatingAppsViewModel.ResizeCorner.Right, dragAmount.x, 0f)
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
        }
    }

    // ------------------------------------------
    // Ghost Toggle, to prevent clicks
    // ------------------------------------------


    // If close to top
    val offsetY = if (app.y < 0.05f) y + height.toInt()
    else y - 200

    if (selected) {
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = x,
                        y = offsetY
                    )
                }
        ) {
            Column {

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = app.ghosted == true,
                        onCheckedChange = {
                            onEdit(app.copy(ghosted = it))
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
                            onEdit(app.copy(foreground = it))
                        }
                    )

                    Text(
                        text = stringResource(R.string.foreground),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
