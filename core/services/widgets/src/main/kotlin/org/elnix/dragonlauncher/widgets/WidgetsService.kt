package org.elnix.dragonlauncher.widgets

import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.util.DisplayMetrics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.SettingFlow
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Widget
import org.elnix.dragonlauncher.base.model.serializables.Widget.Companion.WidgetsJson
import org.elnix.dragonlauncher.base.undoredo.UndoRedoManager
import org.elnix.dragonlauncher.base.undoredo.UndoRedoStack
import org.elnix.dragonlauncher.settings.stores.array.WidgetsSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore.widgetsCellSizeDp
import kotlin.random.Random

public interface WidgetsService {
    public val widgets: SettingFlow<List<Widget>>
    public val dm: DisplayMetrics
    public val cellSizePx: StateFlow<Float>

    public val undoRedo: UndoRedoManager

    public fun save()


    public fun addWidget(action: Action, info: AppWidgetProviderInfo? = null, nestId: Int)

    public fun removeWidget(widget: Widget, onDeleteId: (Int?) -> Unit)

    public fun moveWidgetUp(appId: Int)

    public fun moveWidgetDown(appId: Int)

    public fun centerWidget(appId: Int)

    public fun resetWidgetSize(appId: Int, info: AppWidgetProviderInfo? = null)

    public fun resetAllWidgets()
    public fun updateWidget(
        appId: Int,
        newWidget: (Widget) -> Widget
    )
}


internal class WidgetServiceImpl(
    private val ctx: Context
) : WidgetsService {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())


    override val widgets: SettingFlow<List<Widget>> = SettingFlow(emptyList())


    override val dm: DisplayMetrics = ctx.resources.displayMetrics

    override val cellSizePx: StateFlow<Float> = widgetsCellSizeDp.flow(ctx).map { it.value * dm.density }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = 30 * dm.density
    )

    private val screenWidth = dm.widthPixels.toFloat()
    private val screenHeight = dm.heightPixels.toFloat()

    init {
        loadWidgets()
    }


    private fun snapshotWidgets(): List<Widget> = widgets.value.map { it.copy() }

    override val undoRedo: UndoRedoManager = UndoRedoManager(
        stacks = arrayOf(
            UndoRedoStack(
                snapshot = { snapshotWidgets() },
                restore = { snapshot -> widgets.value = snapshot }
            )
        ),
        scope = scope
    )


    override fun save() {
        scope.launch {
            WidgetsSettingsStore.jsonSetting.set(ctx, WidgetsJson.encode(snapshotWidgets()))
        }
    }


    override fun addWidget(action: Action, info: AppWidgetProviderInfo?, nestId: Int) {

        scope.launch {
            val appWidgetId = if (action is Action.OpenWidget) action.widgetId else null
            val app = Widget(
                id = Random.nextInt(),
                appWidgetId = appWidgetId,
                nestId = nestId,
                action = action
            )

            undoRedo.applyChange {
                widgets.value += app
            }

            centerWidget(appId = app.id)
            resetWidgetSize(appId = app.id, info = info)
        }
    }


    override fun removeWidget(widget: Widget, onDeleteId: (Int?) -> Unit) {
        scope.launch {
            undoRedo.applyChange {
                widgets.value = widgets.value.filterNot { it.id == widget.id }
            }
            onDeleteId(widget.appWidgetId)
        }
    }

    override fun moveWidgetUp(appId: Int) {
        val current = widgets.value
        val index = current.indexOfFirst { it.id == appId }
        if (index <= 0) return

        val moved = current.toMutableList().apply {
            val widget = removeAt(index)
            add(index - 1, widget)
        }
        undoRedo.applyChange {
            widgets.value = moved
        }
    }

    override fun moveWidgetDown(appId: Int) {
        val current = widgets.value
        val index = current.indexOfFirst { it.id == appId }
        if (index == -1 || index == current.lastIndex) return

        val moved = current.toMutableList().apply {
            val widget = removeAt(index)
            add(index + 1, widget)
        }
        undoRedo.applyChange {
            widgets.value = moved
        }
    }


    override fun centerWidget(appId: Int) {
        updateWidget(appId) { app ->
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


    override fun resetWidgetSize(appId: Int, info: AppWidgetProviderInfo?) {
        updateWidget(appId) { app ->
            app.copy(
                spanX = calculateSpanX(info?.minWidth?.toFloat()),
                spanY = calculateSpanY(info?.minHeight?.toFloat()),
                angle = 0f
            )
        }
    }


    override fun resetAllWidgets() {
        undoRedo.applyChange {
            widgets.value = emptyList()
        }

        scope.launch {
            WidgetsSettingsStore.resetAll(ctx)
        }
    }


    override fun updateWidget(
        appId: Int,
        newWidget: (Widget) -> Widget
    ) {
        undoRedo.applyChange {
            val current = widgets.value

            val updatedList = current.map { app ->
                if (app.id == appId) {
                    newWidget(app)
                } else {
                    app
                }
            }

            widgets.value = updatedList
        }
    }

    private fun loadWidgets() {
        scope.launch {
            val widgetsJsonString = WidgetsSettingsStore.jsonSetting.get(ctx)
            widgets.value = WidgetsJson.decode<List<Widget>>(widgetsJsonString, emptyList())
        }
    }

    private fun calculateSpanX(minWidthDp: Float?): Float {
        val cellWidthDp = 100
        return ((minWidthDp ?: Widget.MIN_SIZE) / cellWidthDp).coerceAtLeast(Widget.MIN_SIZE)
    }

    private fun calculateSpanY(minHeightDp: Float?): Float {
        val cellHeightDp = 100
        return ((minHeightDp ?: Widget.MIN_SIZE) / cellHeightDp).coerceAtLeast(Widget.MIN_SIZE)
    }
}