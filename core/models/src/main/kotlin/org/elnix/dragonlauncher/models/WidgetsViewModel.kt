package org.elnix.dragonlauncher.models

import android.annotation.SuppressLint
import android.app.Application
import android.appwidget.AppWidgetProviderInfo
import android.util.DisplayMetrics
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Widget
import org.elnix.dragonlauncher.base.model.serializables.Widget.Companion.WidgetsJson
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.settings.stores.array.WidgetsSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class WidgetsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val ctx = application.applicationContext

    private val _floatingApps = MutableStateFlow<List<Widget>>(emptyList())
    val floatingApps = _floatingApps.asStateFlow()


    val dm: DisplayMetrics = ctx.resources.displayMetrics
    private val _cellSizeDp = MutableStateFlow(30)
    val cellSizeDp: StateFlow<Int> = _cellSizeDp.asStateFlow()
    val cellSizePx: StateFlow<Float> = _cellSizeDp.map { it * dm.density }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = _cellSizeDp.value * dm.density
    )
    private val screenWidth = dm.widthPixels.toFloat()
    private val screenHeight = dm.heightPixels.toFloat()
    val minSize = 1.5f

    init {
        loadFloatingApps()

        viewModelScope.launch {
            _cellSizeDp.value = UiSettingsStore.cellSizeDp.get(ctx)
        }
        viewModelInitialized()
    }

    fun addFloatingApp(action: Action, info: AppWidgetProviderInfo? = null, nestId: Int) {

        viewModelScope.launch {
            val appWidgetId = if (action is Action.OpenWidget) action.widgetId else null
            val app = Widget(
                id = Random.nextInt(),
                appWidgetId = appWidgetId,
                nestId = nestId,
                action = action
            )

            _floatingApps.value += app

            centerFloatingApp(appId = app.id)
            resetFloatingAppSize(appId = app.id, info = info)
        }
    }


    fun removeFloatingApp(id: Int, onDeleteId: (Int) -> Unit) {
        viewModelScope.launch {
            _floatingApps.value = _floatingApps.value.filterNot { it.id == id }
            onDeleteId(id)
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
    }


    fun centerFloatingApp(appId: Int) {

        updateApp(appId) { app ->
            val floatingAppWidthPx = app.spanX * cellSizePx.value
            val floatingAppHeightPx = app.spanY * cellSizePx.value

            val centerXPx = (screenWidth - floatingAppWidthPx) / 2f
            val centerYPx = (screenHeight - floatingAppHeightPx) / 2f

            app.copy(
                x = centerXPx / screenWidth,
                y = centerYPx / screenHeight
            )
        }
    }


    fun resetFloatingAppSize(appId: Int, info: AppWidgetProviderInfo? = null) {
        updateApp(appId) { app ->
            app.copy(
                spanX = calculateSpanX(info?.minWidth?.toFloat()),
                spanY = calculateSpanY(info?.minHeight?.toFloat()),
                angle = 0f
            )
        }
    }


    fun editFloatingApp(app: Widget) {
        val updated = _floatingApps.value.map { floatingApp ->
            if (floatingApp.id == app.id) app
            else floatingApp
        }

        _floatingApps.value = updated
    }


    enum class ResizeCorner {
        Top, Right, Left, Bottom
    }


    fun restoreFloatingApps(snapshot: List<Widget>) {
        _floatingApps.value = snapshot.map { it.copy() }
    }

    fun resetAllFloatingApps() {
        _floatingApps.value = emptyList()

        viewModelScope.launch {
            WidgetsSettingsStore.resetAll(ctx)
        }
    }

    fun updateCellSize(newCellSize: Int?) {
        newCellSize?.let {
            _cellSizeDp.value = newCellSize.coerceAtLeast(1)
        } ?: run {
            _cellSizeDp.value = 30
        }

        viewModelScope.launch {
            UiSettingsStore.cellSizeDp.set(ctx, newCellSize)
        }
    }


    /* ───────────────────────────── Internal ───────────────────────────── */

    private fun updateApp(
        appId: Int,
        block: (Widget) -> Widget
    ) {
        val current = _floatingApps.value

        val updatedList = current.map { app ->
            if (app.id == appId) {
                block(app)
            } else {
                app
            }
        }

        _floatingApps.value = updatedList
    }

    private fun loadFloatingApps() {
        viewModelScope.launch {
            val floatingAppsJsonString = WidgetsSettingsStore.jsonSetting.get(ctx)
            _floatingApps.value = WidgetsJson.decode<List<Widget>>(floatingAppsJsonString) ?: emptyList()
        }
    }

    private fun calculateSpanX(minWidthDp: Float?): Float {
        val cellWidthDp = 100
        return ((minWidthDp ?: minSize) / cellWidthDp).coerceAtLeast(minSize)
    }

    private fun calculateSpanY(minHeightDp: Float?): Float {
        val cellHeightDp = 100
        return ((minHeightDp ?: minSize) / cellHeightDp).coerceAtLeast(minSize)
    }
}
