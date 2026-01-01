package org.elnix.dragonlauncher.ui.settings.customization

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoveDown
import androidx.compose.material.icons.filled.MoveUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.MainActivity
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.helpers.WidgetInfo
import org.elnix.dragonlauncher.ui.components.WidgetHostView
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsLazyHeader
import org.elnix.dragonlauncher.utils.models.WidgetsViewModel

@Composable
fun WidgetsTab(
    widgetsViewModel: WidgetsViewModel,
    onBack: () -> Unit,
    onLaunchSystemWidgetPicker: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val widgets by widgetsViewModel.widgets.collectAsState()

    var selected by remember { mutableStateOf<WidgetInfo?>(null) }



    fun removeWidget(widget: WidgetInfo) {
        widgetsViewModel.removeWidget(widget.id) {
            (ctx as MainActivity).deleteWidget(it)
        }

        if (selected == widget) selected = null
    }

    SettingsLazyHeader(
        title = "${stringResource(R.string.widgets)} (${widgets.size})",
        onBack = onBack,
        helpText = stringResource(R.string.widgets_tab_help),
        onReset = {
            scope.launch {
                widgetsViewModel.resetAllWidgets()
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

    Box(Modifier.fillMaxSize()) {


        /* ---------------- Widget canvas ---------------- */

        widgets.forEach { widget ->
            key(widget.id) {
                DraggableWidget(
                    widgetsViewModel = widgetsViewModel,
                    widget = widget,
                    selected = widget.id == selected?.id,
                    onSelect = { selected = widget },
                    onMove = { dx, dy ->
                        widgetsViewModel.offsetWidget(widget.id, dx, dy)
                    },
                    onResize = { corner, dx, dy ->
                        widgetsViewModel.resizeWidget(widget.id, corner, dx, dy)
                    },
                    onRemove = { removeWidget(it) }
                )
            }
        }

        /* ---------------- Bottom controls ---------------- */

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            FloatingActionButton(
                onClick = {
                    selected?.let { removeWidget(it) }
                },
                containerColor = if (selected != null) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null
                )
            }


            FloatingActionButton(
                onClick = onLaunchSystemWidgetPicker,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }

            FloatingActionButton(
                onClick = {
                    if (widgets.isNotEmpty()) {
                        val idx = widgets.indexOfFirst { it == selected }
                        val next = if (idx <= 0) widgets.last() else widgets[idx - 1]
                        selected = next
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.ArrowUpward, contentDescription = null)
            }

            FloatingActionButton(
                onClick = {
                    if (widgets.isNotEmpty()) {
                        val idx = widgets.indexOfFirst { it == selected }
                        val next = if (idx == -1 || idx == widgets.lastIndex)
                            widgets.first()
                        else widgets[idx + 1]
                        selected = next
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.ArrowDownward, contentDescription = null)
            }


            FloatingActionButton(
                onClick = {
                    selected?.let { widgetsViewModel.moveWidgetUp(it.id) }
                },
                containerColor = if (selected != null) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Icon(
                    Icons.Default.MoveDown,
                    contentDescription = null
                )
            }

            FloatingActionButton(
                onClick = {
                    selected?.let { widgetsViewModel.moveWidgetDown(it.id) }
                },
                containerColor = if (selected != null) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Icon(
                    Icons.Default.MoveUp,
                    contentDescription = null
                )
            }
        }
    }
}



/**
 * Creates a resize handle dot with expanded hitbox for better touch targeting.
 * Visual size remains [dotSize] while hitbox is [dotSize] + [hitboxPadding] for easier grabbing.
 *
 * @param corner The resize corner this handle controls
 * @param dotSize Visual size of the dot
 * @param hitboxPadding Extra padding around dot for touch target (default 20.dp)
 * @param modifier Modifier for positioning the handle (align + offset)
 * @param onDrag Callback with (corner, dx, dy) drag deltas
 */
@Composable
private fun ResizeHandle(
    corner: WidgetsViewModel.ResizeCorner,
    dotSize: Dp = 12.dp,
    hitboxPadding: Dp = 20.dp,
    modifier: Modifier = Modifier,
    onDrag: (WidgetsViewModel.ResizeCorner, Float, Float) -> Unit
) {
    val dotColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .size(dotSize + hitboxPadding * 2)
            .pointerInput(corner) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(corner, dragAmount.x, dragAmount.y)
                }
            }
    ) {
        // Visual dot stays centered in larger hitbox
        Box(
            modifier = Modifier
                .size(dotSize)
                .align(Alignment.Center)
                .background(dotColor, CircleShape)
        )
    }
}


/**
 * Handles all widget interactions: drag to move, resize handles, tap/long-press.
 * Resize handles provide visual-only resize feedback by compensating position changes internally.
 *
 * @param widgetsViewModel ViewModel for widget state management
 * @param widget Current widget data
 * @param selected True if this widget is currently selected
 * @param onSelect Callback when widget is tapped/selected
 * @param onMove Callback for position drag deltas (dx, dy in pixels)
 * @param onResize Callback for resize drag (corner, dx, dy in pixels)
 * @param onRemove Callback for long-press removal
 */
@SuppressLint("LocalContextResourcesRead")
@Composable
private fun DraggableWidget(
    widgetsViewModel: WidgetsViewModel,
    widget: WidgetInfo,
    selected: Boolean,
    onSelect: () -> Unit,
    onMove: (Float, Float) -> Unit,
    onResize: (WidgetsViewModel.ResizeCorner, Float, Float) -> Unit,
    onRemove: (WidgetInfo) -> Unit
) {
    val ctx = LocalContext.current
    val dm = ctx.resources.displayMetrics
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (widget.x * dm.widthPixels).toInt(),
                    y = (widget.y * dm.heightPixels).toInt()
                )
            }
            .size(
                width = (widget.spanX * 100f).dp,
                height = (widget.spanY * 100f).dp
            )
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = borderColor,
                shape = shape
            )
            .padding(4.dp)
    ) {
        // Widget content (touch blocked during editing)
        WidgetHostView(
            widgetInfo = widget,
            blockTouches = true
        )

        // Main interaction overlay (move + tap)
        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(widget.id) {
                    detectTapGestures(
                        onPress = { onSelect() },
                        onLongPress = { onRemove(widget) }
                    )
                }
                .pointerInput(widget.id) {
                    detectDragGestures(
                        onDragStart = { onSelect() },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            onMove(dragAmount.x, dragAmount.y)
                        }
                    )
                }
        )

        // Resize handles - only visible when selected
        if (selected) {
            val dotSize = 12.dp
            val hitboxPadding = 20.dp
            val totalHitbox = (dotSize + hitboxPadding * 2).value  // Use .value for math

            // Top handle - perfect top center alignment
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = -((dotSize.value / 2 + hitboxPadding.value).dp))
                    .size(dotSize + hitboxPadding * 2)
                    .background(Color.Transparent)
                    .pointerInput(WidgetsViewModel.ResizeCorner.Top) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onResize(WidgetsViewModel.ResizeCorner.Top, 0f, dragAmount.y)
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

            // Bottom handle - perfect bottom center alignment
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = ((dotSize.value / 2 + hitboxPadding.value).dp))
                    .size(dotSize + hitboxPadding * 2)
                    .background(Color.Transparent)
                    .pointerInput(WidgetsViewModel.ResizeCorner.Bottom) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onResize(WidgetsViewModel.ResizeCorner.Bottom, 0f, dragAmount.y)
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

            // Left handle - perfect left center alignment
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = -((dotSize.value / 2 + hitboxPadding.value).dp))
                    .size(dotSize + hitboxPadding * 2)
                    .background(Color.Transparent)
                    .pointerInput(WidgetsViewModel.ResizeCorner.Left) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onResize(WidgetsViewModel.ResizeCorner.Left, dragAmount.x, 0f)
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

            // Right handle - perfect right center alignment
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = ((dotSize.value / 2 + hitboxPadding.value).dp))
                    .size(dotSize + hitboxPadding * 2)
                    .background(Color.Transparent)
                    .pointerInput(WidgetsViewModel.ResizeCorner.Right) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onResize(WidgetsViewModel.ResizeCorner.Right, dragAmount.x, 0f)
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
}
