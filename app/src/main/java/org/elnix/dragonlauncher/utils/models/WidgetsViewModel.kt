package org.elnix.dragonlauncher.utils.models

import android.annotation.SuppressLint
import android.app.Application
import android.appwidget.AppWidgetManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.data.helpers.WidgetInfo
import org.elnix.dragonlauncher.data.stores.WidgetSettingsStore
import kotlin.math.roundToInt

class WidgetsViewModel(
    application: Application
) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val ctx = application.applicationContext
    private val appWidgetManager = AppWidgetManager.getInstance(ctx)

    private val _widgets = MutableStateFlow<List<WidgetInfo>>(emptyList())
    val widgets: StateFlow<List<WidgetInfo>> = _widgets.asStateFlow()

    val cellSizePx = 100f

    init {
        loadWidgets()
    }

    /* ----------------------------- Public API ----------------------------- */

    /**
     * Adds a widget to the workspace after successful binding and configuration.
     *
     * Expects:
     * - widgetId is already allocated by AppWidgetHost
     * - widget is already bound to provider
     * - configuration (if required) is complete
     */
    fun addWidget(widgetId: Int) {
        viewModelScope.launch {
            val info = appWidgetManager.getAppWidgetInfo(widgetId) ?: run {
                // Widget not bound - clean up ID
                Log.w("WidgetsViewModel", "Cannot find info for widgetId: $widgetId")
                return@launch
            }

            val widget = WidgetInfo(
                id = widgetId,
                provider = info.provider,
                spanX = calculateSpanX(info.minWidth.toFloat()),
                spanY = calculateSpanY(info.minHeight.toFloat()),
                x = 0f,
                y = 0f
            )

            _widgets.value += widget
            WidgetSettingsStore.saveWidget(ctx, widget)

            Log.d("WidgetsViewModel", "Added widget ${widget.id}: ${info.provider}")
        }
    }




    fun removeWidget(widgetId: Int, onDeleteId: (Int) -> Unit) {
        viewModelScope.launch {
            _widgets.value = _widgets.value.filterNot { it.id == widgetId }
            WidgetSettingsStore.deleteWidget(ctx, widgetId)
            onDeleteId(widgetId)
        }
    }



    private fun updateWidgetPosition(
        widgetId: Int,
        dxPx: Float,
        dyPx: Float,
        snap: Boolean,
        snapScale: Float
    ) {
        val screenWidth = ctx.resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = ctx.resources.displayMetrics.heightPixels.toFloat()

        val updated = _widgets.value.map { widget ->
            if (widget.id == widgetId) {
                var newX = widget.x + dxPx / screenWidth
                var newY = widget.y + dyPx / screenHeight

                if (snap) {
                    val snapX = snapScale / screenWidth
                    val snapY = snapScale / screenHeight
                    newX = (newX / snapX).roundToInt() * snapX
                    newY = (newY / snapY).roundToInt() * snapY
                }

                widget.copy(
                    x = newX,
                    y = newY
                )
            } else widget
        }

        _widgets.value = updated

        viewModelScope.launch {
            updated.find { it.id == widgetId }?.let {
                WidgetSettingsStore.saveWidget(ctx, it)
            }
        }
    }


    fun offsetWidget(widgetId: Int, dxPx: Float, dyPx: Float, snap: Boolean, snapScale: Float = cellSizePx) {
        updateWidgetPosition(widgetId, dxPx, dyPx, snap, snapScale)
    }


    fun moveWidgetUp(widgetId: Int) {
        val current = _widgets.value
        val index = current.indexOfFirst { it.id == widgetId }
        if (index <= 0) return

        val moved = current.toMutableList().apply {
            val widget = removeAt(index)
            add(index - 1, widget)
        }
        _widgets.value = moved

        viewModelScope.launch {
            moved.forEach { WidgetSettingsStore.saveWidget(ctx, it) }
        }
    }

    fun moveWidgetDown(widgetId: Int) {
        val current = _widgets.value
        val index = current.indexOfFirst { it.id == widgetId }
        if (index == -1 || index == current.lastIndex) return

        val moved = current.toMutableList().apply {
            val widget = removeAt(index)
            add(index + 1, widget)
        }
        _widgets.value = moved

        viewModelScope.launch {
            moved.forEach { WidgetSettingsStore.saveWidget(ctx, it) }
        }
    }


    fun centerWidget(widgetId: Int) {
        val screenWidth = ctx.resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = ctx.resources.displayMetrics.heightPixels.toFloat()

        val updated = _widgets.value.map { widget ->
            if (widget.id == widgetId) {
                val widgetWidthPx = widget.spanX * cellSizePx
                val widgetHeightPx = widget.spanY * cellSizePx

                val centerXPx = (screenWidth - widgetWidthPx) / 2f
                val centerYPx = (screenHeight - widgetHeightPx) / 2f

                widget.copy(
                    x = centerXPx / screenWidth,
                    y = centerYPx / screenHeight
                )
            } else widget
        }

        _widgets.value = updated

        viewModelScope.launch {
            updated.find { it.id == widgetId }?.let {
                WidgetSettingsStore.saveWidget(ctx, it)
            }
        }
    }


    /**
     * Resizes a widget while compensating position to maintain visual anchor point.
     * Left/Top resize moves position opposite to drag direction so visual edge stays fixed.
     * Optionally snaps the widget's span to a given scale.
     *
     * @param widgetId ID of widget to resize
     * @param corner Resize corner/handle being dragged
     * @param dxPx Horizontal drag delta in pixels
     * @param dyPx Vertical drag delta in pixels
     * @param snap If true, snap the widget's width/height to multiples of snapScale
     * @param snapScale Scale in pixels for snapping (default 10px)
     */
    fun resizeWidget(
        widgetId: Int,
        corner: ResizeCorner,
        dxPx: Float,
        dyPx: Float,
        snap: Boolean,
        snapScale: Float = cellSizePx
    ) {
        val screenWidth = ctx.resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = ctx.resources.displayMetrics.heightPixels.toFloat()

        val updated = _widgets.value.map { widget ->
            if (widget.id == widgetId) {
                val deltaSpanX = dxPx / cellSizePx
                val deltaSpanY = dyPx / cellSizePx
                val deltaPosX = dxPx / screenWidth
                val deltaPosY = dyPx / screenHeight

                var newSpanX = widget.spanX
                var newSpanY = widget.spanY
                var posDeltaX = 0f
                var posDeltaY = 0f

                when (corner) {
                    ResizeCorner.Left -> {
                        newSpanX = (widget.spanX - deltaSpanX).coerceAtLeast(0.5f)
                        posDeltaX = deltaPosX  // Compensate position to keep left edge fixed
                    }
                    ResizeCorner.Right -> {
                        newSpanX = (widget.spanX + deltaSpanX).coerceAtLeast(0.5f)
                        // Right edge extends naturally
                    }
                    ResizeCorner.Top -> {
                        newSpanY = (widget.spanY - deltaSpanY).coerceAtLeast(0.5f)
                        posDeltaY = deltaPosY  // Compensate position to keep top edge fixed
                    }
                    ResizeCorner.Bottom -> {
                        newSpanY = (widget.spanY + deltaSpanY).coerceAtLeast(0.5f)
                        // Bottom edge extends naturally
                    }
                }

                if (snap) {
                    val snapX = snapScale / cellSizePx
                    val snapY = snapScale / cellSizePx
                    newSpanX = (newSpanX / snapX).roundToInt() * snapX
                    newSpanY = (newSpanY / snapY).roundToInt() * snapY
                }

                widget.copy(
                    spanX = newSpanX,
                    spanY = newSpanY,
                    x = widget.x + posDeltaX,
                    y = widget.y + posDeltaY
                )
            } else widget
        }

        _widgets.value = updated

        viewModelScope.launch {
            updated.find { it.id == widgetId }?.let {
                WidgetSettingsStore.saveWidget(ctx, it)
            }
        }
    }

    enum class ResizeCorner {
        Top, Right, Left, Bottom
    }

    fun resetAllWidgets() {
        _widgets.value = emptyList()

        viewModelScope.launch {
            WidgetSettingsStore.resetAll(ctx)
        }
    }



    /* ----------------------------- Internal ----------------------------- */

    private fun loadWidgets() {
        viewModelScope.launch {
            _widgets.value = WidgetSettingsStore.loadWidgets(ctx)
        }
    }

    private fun calculateSpanX(minWidthDp: Float): Float {
        val cellWidthDp = 100
        return (minWidthDp / cellWidthDp).coerceAtLeast(1f)
    }

    private fun calculateSpanY(minHeightDp: Float): Float {
        val cellHeightDp = 100
        return (minHeightDp / cellHeightDp).coerceAtLeast(1f)
    }
}
