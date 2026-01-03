package org.elnix.dragonlauncher.utils.models

import android.annotation.SuppressLint
import android.app.Application
import android.appwidget.AppWidgetProviderInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.data.SwipeActionSerializable
import org.elnix.dragonlauncher.data.helpers.FloatingAppObject
import org.elnix.dragonlauncher.data.stores.FloatingAppsSettingsStore
import kotlin.math.roundToInt
import kotlin.random.Random

class FloatingAppsViewModel(
    application: Application
) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val ctx = application.applicationContext

    private val _floatingApps = MutableStateFlow<List<FloatingAppObject>>(emptyList())
    val floatingApps = _floatingApps.asStateFlow()

    val cellSizePx = 100f

    init {
        loadFloatingApps()
    }

    /* ----------------------------- Public API ----------------------------- */

    fun addFloatingApp(action: SwipeActionSerializable, info: AppWidgetProviderInfo? = null) {

        viewModelScope.launch {
            val app = FloatingAppObject(
                id = Random.nextInt(),
                action = action
            )

            _floatingApps.value += app

            centerFloatingApp(app.id)
            resetFloatingAppSize(app.id, info)

            FloatingAppsSettingsStore.saveFloatingApp(ctx, app)
        }
    }


    fun removeFloatingApp(id: Int, onDeleteId: (Int) -> Unit) {
        viewModelScope.launch {
            _floatingApps.value = _floatingApps.value.filterNot { it.id == id }
            FloatingAppsSettingsStore.deleteFloatingApp(ctx, id)
            onDeleteId(id)
        }
    }



    fun moveFloatingApp(
        appId: Int,
        dxPx: Float,
        dyPx: Float,
        snap: Boolean,
        snapScale: Float = cellSizePx
    ) {
        val screenWidth = ctx.resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = ctx.resources.displayMetrics.heightPixels.toFloat()

        val updated = _floatingApps.value.map { app ->
            if (app.id == appId) {
                var newX = app.x + dxPx / screenWidth
                var newY = app.y + dyPx / screenHeight

                if (snap) {
                    val snapX = snapScale / screenWidth
                    val snapY = snapScale / screenHeight
                    newX = (newX / snapX).roundToInt() * snapX
                    newY = (newY / snapY).roundToInt() * snapY
                }

                app.copy(
                    x = newX,
                    y = newY
                )
            } else app
        }

        _floatingApps.value = updated

        viewModelScope.launch {
            updated.find { it.id == appId }?.let {
                FloatingAppsSettingsStore.saveFloatingApp(ctx, it)
            }
        }
    }



    fun moveFloatingAppUp(appId: Int) {
        val current = _floatingApps.value
        val index = current.indexOfFirst { it.id == appId }
        if (index <= 0) return

        val moved = current.toMutableList().apply {
            val floatingApp = removeAt(index)
            add(index - 1, floatingApp)
        }
        _floatingApps.value = moved

        viewModelScope.launch {
            moved.forEach { FloatingAppsSettingsStore.saveFloatingApp(ctx, it) }
        }
    }

    fun moveFloatingAppDown(appId: Int) {
        val current = _floatingApps.value
        val index = current.indexOfFirst { it.id == appId }
        if (index == -1 || index == current.lastIndex) return

        val moved = current.toMutableList().apply {
            val floatingApp = removeAt(index)
            add(index + 1, floatingApp)
        }
        _floatingApps.value = moved

        viewModelScope.launch {
            moved.forEach { FloatingAppsSettingsStore.saveFloatingApp(ctx, it) }
        }
    }


    fun centerFloatingApp(appId: Int) {
        val screenWidth = ctx.resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = ctx.resources.displayMetrics.heightPixels.toFloat()

        val updated = _floatingApps.value.map { floatingApp ->
            if (floatingApp.id == appId) {
                val floatingAppWidthPx = floatingApp.spanX * cellSizePx
                val floatingAppHeightPx = floatingApp.spanY * cellSizePx

                val centerXPx = (screenWidth - floatingAppWidthPx) / 2f
                val centerYPx = (screenHeight - floatingAppHeightPx) / 2f

                floatingApp.copy(
                    x = centerXPx / screenWidth,
                    y = centerYPx / screenHeight
                )
            } else floatingApp
        }

        _floatingApps.value = updated

        viewModelScope.launch {
            updated.find { it.id == appId }?.let {
                FloatingAppsSettingsStore.saveFloatingApp(ctx, it)
            }
        }
    }


    fun resetFloatingAppSize(appId: Int, info: AppWidgetProviderInfo? = null) {
        val updated = _floatingApps.value.map { floatingApp ->
            if (floatingApp.id == appId) {

                floatingApp.copy(
                    spanX = calculateSpanX(info?.minWidth?.toFloat() ?: 1.5f),
                    spanY = calculateSpanY(info?.minHeight?.toFloat() ?: 1.5f),
                )
            } else floatingApp
        }

        _floatingApps.value = updated

        viewModelScope.launch {
            updated.find { it.id == appId }?.let {
                FloatingAppsSettingsStore.saveFloatingApp(ctx, it)
            }
        }
    }

    /**
     * Resizes a floatingApp while compensating position to maintain visual anchor point.
     * Left/Top resize moves position opposite to drag direction so visual edge stays fixed.
     * Optionally snaps the floatingApp's span to a given scale.
     *
     * @param appId ID of floatingApp to resize
     * @param corner Resize corner/handle being dragged
     * @param dxPx Horizontal drag delta in pixels
     * @param dyPx Vertical drag delta in pixels
     * @param snap If true, snap the floatingApp's width/height to multiples of snapScale
     * @param snapScale Scale in pixels for snapping (default 10px)
     */
    fun resizeFloatingApp(
        appId: Int,
        corner: ResizeCorner,
        dxPx: Float,
        dyPx: Float,
        snap: Boolean,
        snapScale: Float = cellSizePx
    ) {
        val screenWidth = ctx.resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = ctx.resources.displayMetrics.heightPixels.toFloat()

        val updated = _floatingApps.value.map { floatingApp ->
            if (floatingApp.id == appId) {
                val deltaSpanX = dxPx / cellSizePx
                val deltaSpanY = dyPx / cellSizePx
                val deltaPosX = dxPx / screenWidth
                val deltaPosY = dyPx / screenHeight

                var newSpanX = floatingApp.spanX
                var newSpanY = floatingApp.spanY
                var posDeltaX = 0f
                var posDeltaY = 0f

                when (corner) {
                    ResizeCorner.Left -> {
                        newSpanX = (floatingApp.spanX - deltaSpanX).coerceAtLeast(1.5f)
                        posDeltaX = deltaPosX  // Compensate position to keep left edge fixed
                    }
                    ResizeCorner.Right -> {
                        newSpanX = (floatingApp.spanX + deltaSpanX).coerceAtLeast(1.5f)
                        // Right edge extends naturally
                    }
                    ResizeCorner.Top -> {
                        newSpanY = (floatingApp.spanY - deltaSpanY).coerceAtLeast(1.5f)
                        posDeltaY = deltaPosY  // Compensate position to keep top edge fixed
                    }
                    ResizeCorner.Bottom -> {
                        newSpanY = (floatingApp.spanY + deltaSpanY).coerceAtLeast(1.5f)
                        // Bottom edge extends naturally
                    }
                }

                if (snap) {
                    val snapX = snapScale / cellSizePx
                    val snapY = snapScale / cellSizePx
                    newSpanX = (newSpanX / snapX).roundToInt() * snapX
                    newSpanY = (newSpanY / snapY).roundToInt() * snapY
                }

                floatingApp.copy(
                    spanX = newSpanX,
                    spanY = newSpanY,
                    x = floatingApp.x + posDeltaX,
                    y = floatingApp.y + posDeltaY
                )
            } else floatingApp
        }

        _floatingApps.value = updated

        viewModelScope.launch {
            updated.find { it.id == appId }?.let {
                FloatingAppsSettingsStore.saveFloatingApp(ctx, it)
            }
        }
    }


    fun editFloatingApp(app: FloatingAppObject) {
        val updated = _floatingApps.value.map { floatingApp ->
            if (floatingApp.id == app.id) app
            else floatingApp
        }

        _floatingApps.value = updated

        viewModelScope.launch {
            updated.find { it.id == app.id }?.let {
                FloatingAppsSettingsStore.saveFloatingApp(ctx, it)
            }
        }
    }


    enum class ResizeCorner {
        Top, Right, Left, Bottom
    }

    fun resetAllFloatingApps() {
        _floatingApps.value = emptyList()

        viewModelScope.launch {
            FloatingAppsSettingsStore.resetAll(ctx)
        }
    }



    /* ----------------------------- Internal ----------------------------- */

    private fun loadFloatingApps() {
        viewModelScope.launch {
            _floatingApps.value = FloatingAppsSettingsStore.loadFloatingApps(ctx)
        }
    }

    private fun calculateSpanX(minWidthDp: Float): Float {
        val cellWidthDp = 100
        return (minWidthDp / cellWidthDp).coerceAtLeast(1.5f)
    }

    private fun calculateSpanY(minHeightDp: Float): Float {
        val cellHeightDp = 100
        return (minHeightDp / cellHeightDp).coerceAtLeast(1.5f)
    }
}
