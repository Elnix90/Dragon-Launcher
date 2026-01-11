@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.settings.customization

import android.annotation.SuppressLint
import android.content.ComponentName
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.MainActivity
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.SwipeActionSerializable
import org.elnix.dragonlauncher.data.helpers.FloatingAppObject
import org.elnix.dragonlauncher.data.stores.DebugSettingsStore
import org.elnix.dragonlauncher.data.stores.StatusBarSettingsStore
import org.elnix.dragonlauncher.ui.components.FloatingAppsHostView
import org.elnix.dragonlauncher.ui.components.dialogs.AddPointDialog
import org.elnix.dragonlauncher.ui.helpers.CircleIconButton
import org.elnix.dragonlauncher.ui.helpers.UpDownButton
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsLazyHeader
import org.elnix.dragonlauncher.ui.statusbar.StatusBar
import org.elnix.dragonlauncher.utils.models.AppsViewModel
import org.elnix.dragonlauncher.utils.models.FloatingAppsViewModel

@Composable
fun FloatingAppsTab(
    appsViewModel: AppsViewModel,
    floatingAppsViewModel: FloatingAppsViewModel,
    onBack: () -> Unit,
    onLaunchSystemWidgetPicker: () -> Unit,
    onResetWidgetSize: (id: Int, widgetId: Int) -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val icons by appsViewModel.icons.collectAsState()

    val widgetsDebugInfos by DebugSettingsStore.getWidgetsDebugInfos(ctx)
        .collectAsState(initial = false)


    val floatingApps by floatingAppsViewModel.floatingApps.collectAsState()

    val cellSizePx = floatingAppsViewModel.cellSizePx

    var selected by remember { mutableStateOf<FloatingAppObject?>(null) }

    val isSelected = selected != null

    var snapMove by remember { mutableStateOf(false) }
    var snapResize by remember { mutableStateOf(false) }

    fun removeWidget(floatingApp: FloatingAppObject) {
        floatingAppsViewModel.removeFloatingApp(floatingApp.id) {
            (ctx as MainActivity).deleteWidget(it)
        }

        if (selected == floatingApp) selected = null
    }

    val widgetNumber = floatingApps.filter { it.action is SwipeActionSerializable.OpenWidget }.size
    val floatingAppsNumber = floatingApps.filter { it.action is SwipeActionSerializable.LaunchApp }.size

    var showAddDialog by remember { mutableStateOf(false) }


    /** ───────────────────────────────────────────────────────────────────────────────────────────
     * Status bar things, copy paste from the getters, do no change that, it's just for displaying
     * the status bar if enabled to preview more easily
    ──────────────────────────────────────────────────────────────────────────────────────────────*/
    val systemInsets = WindowInsets.systemBars.asPaddingValues()

    val isRealFullscreen = systemInsets.calculateTopPadding() == 0.dp

    val showStatusBar by StatusBarSettingsStore.getShowStatusBar(ctx)
        .collectAsState(initial = false)

    /** ───────────────────────────────────────────────────────────────── */


    Column{

        if (showStatusBar && isRealFullscreen) {
            StatusBar(
                onDateAction = {},
                onClockAction = {}
            )
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

        floatingApps.forEach { floatingApp ->
            key(floatingApp.id) {
                DraggableFloatingApp(
                    floatingAppsViewModel = floatingAppsViewModel,
                    app = floatingApp,
                    icons = icons,
                    selected = floatingApp.id == selected?.id,
                    onSelect = { selected = floatingApp },
                    onMove = { dx, dy ->
                        floatingAppsViewModel.moveFloatingApp(floatingApp.id, dx, dy, false)
                    },
                    onMoveEnd = {
                        floatingAppsViewModel.moveFloatingApp(floatingApp.id, 0f, 0f, snapMove)
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

        /* ---------------- Bottom controls ---------------- */


        Row(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Open Add floating app dialog
            CircleIconButton(
                icon = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_widget),
                color = MaterialTheme.colorScheme.primary,
                padding = 16.dp
            ) {
               showAddDialog = true
            }


            // Delete selected widget
            CircleIconButton(
                icon = Icons.Default.Remove,
                contentDescription = stringResource(R.string.delete_widget),
                color = MaterialTheme.colorScheme.error,
                enabled = isSelected,
                padding = 16.dp
            ) {
                selected?.let { removeWidget(it) }
            }

//            // Center selected widget
//            CircleIconButton(
//                icon = Icons.Default.CenterFocusStrong,
//                contentDescription = stringResource(R.string.center_selected_floating_app),
//                color = MaterialTheme.colorScheme.secondary,
//                padding = 16.dp
//            ) {
//            }

            UpDownButton(
                upIcon = Icons.Default.CenterFocusStrong,
                downIcon = Icons.Default.Restore,
                color = MaterialTheme.colorScheme.primary,
                upEnabled = isSelected,
                downEnabled = isSelected,
                upClickable = isSelected,
                downClickable = isSelected,
                padding = 16.dp,
                onClickUp = {
                    selected?.let { floatingAppsViewModel.centerFloatingApp(it.id) }

                },
                onClickDown = {
                    selected?.let {
                        if (it.action is SwipeActionSerializable.OpenWidget) {
                            onResetWidgetSize(it.id, it.action.widgetId)
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
                upEnabled = true,
                downEnabled = true,
                upClickable = true,
                downClickable = true,
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
                        val next = if (idx == -1 || idx == floatingApps.lastIndex) floatingApps.first() else floatingApps[idx + 1]
                        selected = next
                    }
                }
            )

            val upDownEnabled = isSelected && floatingApps.size > 1

            UpDownButton(
                upIcon = Icons.Default.MoveUp,
                downIcon = Icons.Default.MoveDown,
                color = MaterialTheme.colorScheme.tertiary,
                upEnabled = upDownEnabled,
                downEnabled = upDownEnabled,
                upClickable = upDownEnabled,
                downClickable = upDownEnabled,
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
                upEnabled = true,
                downEnabled = true,
                upClickable = true,
                downClickable = true,
                padding = 16.dp,
                onClickUp = { snapMove = !snapMove },
                onClickDown = { snapResize = !snapResize }
            )
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
            appsViewModel = appsViewModel,
            onDismiss = { showAddDialog = false },
            actions =listOf(
                SwipeActionSerializable.OpenWidget(0, ComponentName("", "")),
                SwipeActionSerializable.OpenCircleNest(0),
                SwipeActionSerializable.GoParentNest,
                SwipeActionSerializable.LaunchApp(""),
                SwipeActionSerializable.OpenUrl(""),
                SwipeActionSerializable.OpenFile(""),
                SwipeActionSerializable.NotificationShade,
                SwipeActionSerializable.ControlPanel,
                SwipeActionSerializable.OpenAppDrawer,
                SwipeActionSerializable.Lock,
                SwipeActionSerializable.ReloadApps,
                SwipeActionSerializable.OpenRecentApps,
                SwipeActionSerializable.OpenDragonLauncherSettings
            ),
        ) { action ->
            when (action) {
                is SwipeActionSerializable.OpenWidget -> onLaunchSystemWidgetPicker()
                else -> floatingAppsViewModel.addFloatingApp(action)
            }
            showAddDialog = false
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
    icons: Map<String, ImageBitmap>,
    selected: Boolean,
    onSelect: () -> Unit,
    onMove: (Float, Float) -> Unit,
    onMoveEnd: () -> Unit,
    onResize: (FloatingAppsViewModel.ResizeCorner, Float, Float) -> Unit,
    onResizeEnd: (FloatingAppsViewModel.ResizeCorner) -> Unit,
    onRemove: () -> Unit,
    onEdit: (FloatingAppObject) -> Unit
) {
    val ctx = LocalContext.current
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val shape = RoundedCornerShape(12.dp)

    val cellSizePx = floatingAppsViewModel.cellSizePx


    val density = LocalDensity.current


    val dm = ctx.resources.displayMetrics

    val widthPixels = dm.widthPixels
    val heightPixels = dm.heightPixels

    val x = (app.x * widthPixels).toInt()
    val y = (app.y * heightPixels).toInt()

    val width =  app.spanX * cellSizePx
    val height = app.spanY * cellSizePx

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = x,
                    y = y
                )
            }
            .size(
                width = with(density) { width.toDp() },
                height = with(density) { height.toDp() }
            )
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = borderColor,
                shape = shape
            )
    ) {

        // Widget / App content (touch blocked during editing)
        FloatingAppsHostView(
            floatingAppObject = app,
            blockTouches = true,
            icons = icons,
            cellSizePx = cellSizePx
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
                            change.consume()
                            onMove(dragAmount.x, dragAmount.y)
                        },
                        onDragEnd = {
                            onMoveEnd()
                        }
                    )
                }
        )

        if (selected) {

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
