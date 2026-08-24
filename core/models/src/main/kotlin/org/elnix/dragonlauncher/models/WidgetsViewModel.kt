package org.elnix.dragonlauncher.models

import android.app.Application
import android.appwidget.AppWidgetProviderInfo
import android.util.DisplayMetrics
import androidx.compose.runtime.Stable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.applications.AppRepository
import org.elnix.dragonlauncher.appshortcuts.AppShortcutRepository
import org.elnix.dragonlauncher.base.SettingFlow
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Widget
import org.elnix.dragonlauncher.base.model.serializables.Widget.Companion.WidgetsJson
import org.elnix.dragonlauncher.base.undoredo.UndoRedoManager
import org.elnix.dragonlauncher.base.undoredo.UndoRedoStack
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.settings.stores.array.WidgetsSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore.widgetsCellSizeDp
import javax.inject.Inject
import kotlin.random.Random

@Stable
@HiltViewModel
public class WidgetsViewModel @Inject constructor(
    application: Application,
    private val appsRepository: AppRepository,
    private val shortcutRepository: AppShortcutRepository,
) : AndroidViewModel(application) {


    public val widgets: SettingFlow<List<Widget>> = SettingFlow(emptyList())


    public val dm: DisplayMetrics = application.applicationContext.resources.displayMetrics


    public val cellSizePx: StateFlow<Float> = widgetsCellSizeDp.flow(application.applicationContext).map { it.value * dm.density }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 30 * dm.density
    )

    private val screenWidth = dm.widthPixels.toFloat()
    private val screenHeight = dm.heightPixels.toFloat()
    public val minSize: Float = 1.5f

    init {
        loadWidgets()
        viewModelInitialized()
    }


    private fun snapshotWidgets(): List<Widget> = widgets.value.map { it.copy() }

    public val undoRedo: UndoRedoManager = UndoRedoManager(
        stacks = arrayOf(
            UndoRedoStack(
                snapshot = { snapshotWidgets() },
                restore = { restoreWidgets(it) }
            )
        ),
        scope = viewModelScope
    )


    public fun save() {
        viewModelScope.launch {
            WidgetsSettingsStore.jsonSetting.set(application.applicationContext, WidgetsJson.encode(snapshotWidgets()))
        }
    }

    public fun addWidget(action: Action, info: AppWidgetProviderInfo? = null, nestId: Int) {

        viewModelScope.launch {
            val appWidgetId = if (action is Action.OpenWidget) action.widgetId else null
            val app = Widget(
                id = Random.nextInt(),
                appWidgetId = appWidgetId,
                nestId = nestId,
                action = action
            )

            widgets.value += app

            centerWidget(appId = app.id)
            resetWidgetSize(appId = app.id, info = info)
        }
    }


    public fun removeWidget(id: Int, onDeleteId: (Int) -> Unit) {
        viewModelScope.launch {
            widgets.value = widgets.value.filterNot { it.id == id }
            onDeleteId(id)
        }
    }

    public fun moveWidgetUp(appId: Int) {
        val current = widgets.value
        val index = current.indexOfFirst { it.id == appId }
        if (index <= 0) return

        val moved = current.toMutableList().apply {
            val widget = removeAt(index)
            add(index - 1, widget)
        }
        widgets.value = moved
    }

    public fun moveWidgetDown(appId: Int) {
        val current = widgets.value
        val index = current.indexOfFirst { it.id == appId }
        if (index == -1 || index == current.lastIndex) return

        val moved = current.toMutableList().apply {
            val widget = removeAt(index)
            add(index + 1, widget)
        }
        widgets.value = moved
    }


    public fun centerWidget(appId: Int) {
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


    public fun resetWidgetSize(appId: Int, info: AppWidgetProviderInfo? = null) {
        updateApp(appId) { app ->
            app.copy(
                spanX = calculateSpanX(info?.minWidth?.toFloat()),
                spanY = calculateSpanY(info?.minHeight?.toFloat()),
                angle = 0f
            )
        }
    }

    public fun editWidget(app: Widget) {
        val updated = widgets.value.map { widget ->
            if (widget.id == app.id) app
            else widget
        }

        widgets.value = updated
    }


    public fun restoreWidgets(snapshot: List<Widget>) {
        widgets.value = snapshot.map { it.copy() }
    }

    public fun resetAllWidgets() {
        widgets.value = emptyList()

        viewModelScope.launch {
            WidgetsSettingsStore.resetAll(application.applicationContext)
        }
    }


    private inline fun updateApp(
        appId: Int,
        block: (Widget) -> Widget
    ) {
        undoRedo.applyChange {
            val current = widgets.value

            val updatedList = current.map { app ->
                if (app.id == appId) {
                    block(app)
                } else {
                    app
                }
            }

            widgets.value = updatedList
        }
    }

    private fun loadWidgets() {
        viewModelScope.launch {
            val widgetsJsonString = WidgetsSettingsStore.jsonSetting.get(application.applicationContext)
            widgets.value = WidgetsJson.decode<List<Widget>>(widgetsJsonString, emptyList())
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

    public fun findOne(action: Action.LaunchApp): Flow<org.elnix.dragonlauncher.base.model.models.Application?> = appsRepository.findOne(action)
}
