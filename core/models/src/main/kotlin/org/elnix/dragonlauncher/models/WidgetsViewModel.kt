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
import org.elnix.dragonlauncher.base.undoredo.UndoRedoManager
import org.elnix.dragonlauncher.base.undoredo.UndoRedoStack
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.settings.stores.array.WidgetsSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore.cellSizeDp
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class WidgetsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val ctx = application.applicationContext

    private val _widgets = MutableStateFlow<List<Widget>>(emptyList())
    val widgets = _widgets.asStateFlow()


    val dm: DisplayMetrics = ctx.resources.displayMetrics


    val cellSizePx: StateFlow<Float> = cellSizeDp.flow(ctx).map { it * dm.density }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 30 * dm.density
    )

    private val screenWidth = dm.widthPixels.toFloat()
    private val screenHeight = dm.heightPixels.toFloat()
    val minSize = 1.5f

    init {
        loadWidgets()
        viewModelInitialized()
    }


    private fun snapshotWidgets(): List<Widget> = _widgets.value.map { it.copy() }

    val undoRedo = UndoRedoManager(
        arrayOf(
            UndoRedoStack(
                snapshot = { snapshotWidgets() },
                restore = {
                    restoreWidgets(it)
//                        selected = widgets.find { p -> p.id == (selected?.id ?: "") }
                }
            )
        )
    )


    fun save() {
        viewModelScope.launch {
            WidgetsSettingsStore.jsonSetting.set(ctx, WidgetsJson.encode(snapshotWidgets()))
        }
    }

    fun addWidget(action: Action, info: AppWidgetProviderInfo? = null, nestId: Int) {

        viewModelScope.launch {
            val appWidgetId = if (action is Action.OpenWidget) action.widgetId else null
            val app = Widget(
                id = Random.nextInt(),
                appWidgetId = appWidgetId,
                nestId = nestId,
                action = action
            )

            _widgets.value += app

            centerWidget(appId = app.id)
            resetWidgetSize(appId = app.id, info = info)
        }
    }


    fun removeWidget(id: Int, onDeleteId: (Int) -> Unit) {
        viewModelScope.launch {
            _widgets.value = _widgets.value.filterNot { it.id == id }
            onDeleteId(id)
        }
    }

    fun moveWidgetUp(appId: Int) {
        val current = _widgets.value
        val index = current.indexOfFirst { it.id == appId }
        if (index <= 0) return

        val moved = current.toMutableList().apply {
            val widget = removeAt(index)
            add(index - 1, widget)
        }
        _widgets.value = moved
    }

    fun moveWidgetDown(appId: Int) {
        val current = _widgets.value
        val index = current.indexOfFirst { it.id == appId }
        if (index == -1 || index == current.lastIndex) return

        val moved = current.toMutableList().apply {
            val widget = removeAt(index)
            add(index + 1, widget)
        }
        _widgets.value = moved
    }


    fun centerWidget(appId: Int) {
        updateApp(appId) { app ->
            val widgetWidthPx = app.spanX * cellSizePx.value
            val widgetHeightPx = app.spanY * cellSizePx.value

            val centerXPx = (screenWidth - widgetWidthPx) / 2f
            val centerYPx = (screenHeight - widgetHeightPx) / 2f

            app.copy(
                x = centerXPx / screenWidth,
                y = centerYPx / screenHeight
            )
        }
    }


    fun resetWidgetSize(appId: Int, info: AppWidgetProviderInfo? = null) {
        updateApp(appId) { app ->
            app.copy(
                spanX = calculateSpanX(info?.minWidth?.toFloat()),
                spanY = calculateSpanY(info?.minHeight?.toFloat()),
                angle = 0f
            )
        }
    }

    fun editWidget(app: Widget) {
        val updated = _widgets.value.map { widget ->
            if (widget.id == app.id) app
            else widget
        }

        _widgets.value = updated
    }


    fun restoreWidgets(snapshot: List<Widget>) {
        _widgets.value = snapshot.map { it.copy() }
    }

    fun resetAllWidgets() {
        _widgets.value = emptyList()

        viewModelScope.launch {
            WidgetsSettingsStore.resetAll(ctx)
        }
    }


    private inline fun updateApp(
        appId: Int,
        block: (Widget) -> Widget
    ) {
        undoRedo.applyChange {
            val current = _widgets.value

            val updatedList = current.map { app ->
                if (app.id == appId) {
                    block(app)
                } else {
                    app
                }
            }

            _widgets.value = updatedList
        }
    }

    private fun loadWidgets() {
        viewModelScope.launch {
            val widgetsJsonString = WidgetsSettingsStore.jsonSetting.get(ctx)
            _widgets.value = WidgetsJson.decode<List<Widget>>(widgetsJsonString) ?: emptyList()
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
