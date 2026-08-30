package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.elnix90.logging.logE
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.ANGLE_LINE_TAG
import org.elnix.dragonlauncher.base.Constants.Settings.DOUBLE_CLICK_ACTION_DELAY
import org.elnix.dragonlauncher.base.SettingFlow
import org.elnix.dragonlauncher.base.model.DragonJson
import org.elnix.dragonlauncher.base.model.json
import org.elnix.dragonlauncher.base.model.models.AngleLineObjects
import org.elnix.dragonlauncher.base.model.serializables.CustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultAngleCustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultEndCustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultHoldCustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultLineCustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultStartCustomObject
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer.Companion.MainScreenLayerJson
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer.Companion.defaultMainScreenLayers
import org.elnix.dragonlauncher.base.navigation.NavigationRoute
import org.elnix.dragonlauncher.ktx.isNotBlankJson
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.settings.stores.array.MainScreenLayersSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.AngleLineSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.BehaviorSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.HoldToActivateArcSettingsStore
import org.elnix.dragonlauncher.settings.stores.objects.AngleObjectSettingStore
import org.elnix.dragonlauncher.settings.stores.objects.EndObjectSettingStore
import org.elnix.dragonlauncher.settings.stores.objects.HoldToActivateObject
import org.elnix.dragonlauncher.settings.stores.objects.LineObjectSettingStore
import org.elnix.dragonlauncher.settings.stores.objects.StartObjectSettingStore
import org.elnix.dragonlauncher.widgets.WidgetsService
import javax.inject.Inject

@Stable
@HiltViewModel
public class SwipeViewModel @Inject constructor(
    application: Application,
    private val widgetsService: WidgetsService
) : AndroidViewModel(application) {
    public val lineObject: SettingFlow<CustomObject> = SettingFlow(defaultLineCustomObject)
    public val angleObject: SettingFlow<CustomObject> = SettingFlow(defaultAngleCustomObject)
    public val startObject: SettingFlow<CustomObject> = SettingFlow(defaultStartCustomObject)
    public val endObject: SettingFlow<CustomObject> = SettingFlow(defaultEndCustomObject)

    public val holdObject: SettingFlow<CustomObject> = SettingFlow(defaultHoldCustomObject)
    public val holdMenuEntriesString: SettingFlow<List<NavigationRoute>> = SettingFlow(emptyList())

    public val lineObjectOrder: SettingFlow<List<AngleLineObjects>> = SettingFlow(AngleLineObjects.entries)
    public val mainScreenLayerOrder: SettingFlow<List<MainScreenLayer>> = SettingFlow(defaultMainScreenLayers)


    public val start: SettingFlow<Offset?> = SettingFlow(null)
    public val current: SettingFlow<Offset?> = SettingFlow(null)
    private var lastClickTime = 0L


    public fun clearAfterLaunch() {
        start.value = null
        current.value = null
        lastClickTime = 0L
    }


    private var leftPadding = 0
    private var rightPadding = 0
    private var topPadding = 0
    private var bottomPadding = 0


    private val _doubleClicActionChannel = Channel<Unit>(Channel.CONFLATED)
    public val doubleClicActionChannel: Flow<Unit> = _doubleClicActionChannel.receiveAsFlow()

    public suspend fun PointerInputScope.mainDragGesture() {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)

                val down = event.changes.firstOrNull { it.changedToDown() } ?: continue
                val pos = down.position

                val allowed = pos.isInsideActiveZone(
                    size = size,
                    left = leftPadding,
                    right = rightPadding,
                    top = topPadding,
                    bottom = bottomPadding
                )

                if (!allowed) {
                    continue
                }

                if (pos.isInsideForegroundWidget()) {
                    // Let widget handle scroll - do NOT consume or process
                    continue
                }

                start.value = down.position
                current.value = down.position

                val pointerId = down.id

                val currentTime = System.currentTimeMillis()
                val diff = currentTime - lastClickTime
                if (diff < DOUBLE_CLICK_ACTION_DELAY) {
                    clearAfterLaunch()
                    _doubleClicActionChannel.trySend(Unit)
                    continue
                }

                lastClickTime = currentTime

                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val change = event.changes.firstOrNull { it.id == pointerId }

                    if (change != null) {
                        if (change.pressed) {
                            change.consume()
                            current.value = change.position
                        } else {
                            start.value = null
                            current.value = null
                            break
                        }
                    } else {
                        start.value = null
                        current.value = null
                        break
                    }
                }
            }
        }
    }

    init {
        loadMainScreenLayers()
        loadAngleLineObjects()
        loadHoldObject()
        loadAngleLineOrder()
        loadHoldMenuEntries()

        viewModelInitialized()

        viewModelScope.launch {
            launch {
                BehaviorSettingsStore.leftPadding.flow(application).collectLatest { leftPadding = it }
            }
            launch {
                BehaviorSettingsStore.rightPadding.flow(application).collectLatest { rightPadding = it }
            }
            launch {
                BehaviorSettingsStore.topPadding.flow(application).collectLatest { topPadding = it }
            }
            launch {
                BehaviorSettingsStore.bottomPadding.flow(application).collectLatest { bottomPadding = it }
            }
        }
    }

    private fun loadMainScreenLayers() {
        viewModelScope.launch {
            val mainScreenLayerString = MainScreenLayersSettingsStore.jsonSetting.getOrNull(application)
            mainScreenLayerOrder.value = mainScreenLayerString?.let { MainScreenLayerJson.decode<List<MainScreenLayer>>(it) }
                ?.takeIf { layers ->
                    val expectedTypes = setOf(
                        MainScreenLayer.ChargingAnimation::class,
                        MainScreenLayer.StatusBar::class,
                        MainScreenLayer.Widgets::class,
                        MainScreenLayer.CustomDim::class,
                        MainScreenLayer.DragOverlay::class,
                        MainScreenLayer.HoldToActivate::class
                    )

                    layers.map { it::class }.toSet() == expectedTypes
                }
                ?: defaultMainScreenLayers

        }
    }

    private fun loadAngleLineObjects() {
        viewModelScope.launch {
            val lineJsonString = LineObjectSettingStore.jsonSetting.get(application)
            lineObject.value = loadCustomObject(lineJsonString, defaultLineCustomObject)

            val angleJsonString = AngleObjectSettingStore.jsonSetting.get(application)
            angleObject.value = loadCustomObject(angleJsonString, defaultAngleCustomObject)

            val startJsonString = StartObjectSettingStore.jsonSetting.get(application)
            startObject.value = loadCustomObject(startJsonString, defaultStartCustomObject)

            val endJsonString = EndObjectSettingStore.jsonSetting.get(application)
            endObject.value = loadCustomObject(endJsonString, defaultEndCustomObject)
        }
    }

    private fun loadHoldObject() {
        viewModelScope.launch {
            val holdJsonString = HoldToActivateObject.jsonSetting.get(application)
            holdObject.value = loadCustomObject(holdJsonString, defaultHoldCustomObject)
        }
    }


    private fun loadAngleLineOrder() {
        viewModelScope.launch {
            val orderString = AngleLineSettingsStore.angleLineObjectsOrder.get(application)

            lineObjectOrder.value = try {
                orderString
                    .takeIf { it.isNotEmpty() }
                    ?.split(",")
                    ?.map { AngleLineObjects.valueOf(it) }
            } catch (e: Exception) {
                logE(ANGLE_LINE_TAG, e) { "Failed to decode angle line objects order, using default value" }
                null
            } ?: AngleLineObjects.entries
        }
    }

    public fun saveLineObjects() {
        viewModelScope.launch {
            val lineJsonString = json.encodeToString(lineObject.value)
            LineObjectSettingStore.jsonSetting.set(application, lineJsonString)

            val angleJsonString = json.encodeToString(angleObject.value)
            AngleObjectSettingStore.jsonSetting.set(application, angleJsonString)

            val startJsonString = json.encodeToString(startObject.value)
            StartObjectSettingStore.jsonSetting.set(application, startJsonString)

            val endJsonString = json.encodeToString(endObject.value)
            EndObjectSettingStore.jsonSetting.set(application, endJsonString)
        }
    }

    public fun resetLineObjects() {
        viewModelScope.launch {
            lineObject.value = defaultLineCustomObject
            LineObjectSettingStore.jsonSetting.reset(application)

            angleObject.value = defaultAngleCustomObject
            AngleObjectSettingStore.jsonSetting.reset(application)

            startObject.value = defaultStartCustomObject
            StartObjectSettingStore.jsonSetting.reset(application)

            endObject.value = defaultEndCustomObject
            EndObjectSettingStore.jsonSetting.reset(application)
        }
    }

    public fun saveHoldObject() {
        viewModelScope.launch {
            val holdJsonString = json.encodeToString(holdObject.value)
            HoldToActivateObject.jsonSetting.set(application, holdJsonString)
        }
    }

    public fun resetHoldObject() {
        viewModelScope.launch {
            holdObject.value = defaultHoldCustomObject
            HoldToActivateObject.jsonSetting.reset(application)
        }
    }

    public fun saveOrder() {
        viewModelScope.launch {
            val orderString = lineObjectOrder.value.joinToString(",")
            AngleLineSettingsStore.angleLineObjectsOrder.set(application, orderString)
        }
    }

    public fun resetOrder() {
        viewModelScope.launch {
            lineObjectOrder.value = AngleLineObjects.entries
            AngleLineSettingsStore.angleLineObjectsOrder.reset(application)
        }
    }


    public fun saveMainScreenLayers() {
        viewModelScope.launch {
            val mainScreenLayersString = MainScreenLayerJson.encode(mainScreenLayerOrder.value)
            MainScreenLayersSettingsStore.jsonSetting.set(application, mainScreenLayersString)
        }
    }

    public fun resetMainScreenLayers() {
        viewModelScope.launch {
            mainScreenLayerOrder.value = defaultMainScreenLayers
            MainScreenLayersSettingsStore.jsonSetting.reset(application)
        }
    }


    private fun loadHoldMenuEntries() {
        viewModelScope.launch {
            val holdMenuString = HoldToActivateArcSettingsStore.holdMenuEntriesJson.get(application)
            val decoded = HoldMenuEntriesJson.decode<List<NavigationRoute>>(holdMenuString, emptyList())
            holdMenuEntriesString.value = decoded
        }
    }

    public fun saveHoldMenuEntries() {
        viewModelScope.launch {
            val encoded = HoldMenuEntriesJson.encode<List<NavigationRoute>>(holdMenuEntriesString.value)
            HoldToActivateArcSettingsStore.holdMenuEntriesJson.set(application, encoded)
        }
    }

    public fun resetHoldMenuEntries() {
        viewModelScope.launch {
            holdMenuEntriesString.value = emptyList()
            HoldToActivateArcSettingsStore.holdMenuEntriesJson.reset(application)
        }
    }

    /**
     *  Kept here for compatibility, I don't need it actually, but I'm afraid of what's could happen if I remove it
     */
    private object HoldMenuEntriesJson : DragonJson<List<NavigationRoute>>()


    private inline fun <reified T> loadCustomObject(
        jsonString: String,
        default: T,
        crossinline onError: (Exception) -> Unit = {}
    ): T {
        return if (jsonString.isNotBlankJson) {
            try {
                json.decodeFromString<T>(jsonString)
            } catch (e: Exception) {
                onError(e)
                default
            }
        } else {
            default
        }
    }


    /**
     * Determines whether a pointer position lies within the allowed interaction zone.
     *
     * The active zone is defined as the rectangular area of the screen obtained by
     * excluding padding margins from each edge. Any position inside this rectangle
     * is considered valid for gesture handling.
     *
     * @receiver [Offset] Pointer position in screen coordinates.
     * @param size Full size of the available surface.
     * @param left Excluded distance from the left edge.
     * @param right Excluded distance from the right edge.
     * @param top Excluded distance from the top edge.
     * @param bottom Excluded distance from the bottom edge.
     *
     * @return `true` if the position is inside the active zone, `false` otherwise.
     */
    @Suppress("NOTHING_TO_INLINE")
    public inline fun Offset.isInsideActiveZone(
        size: IntSize,
        left: Int,
        right: Int,
        top: Int,
        bottom: Int
    ): Boolean = x >= left &&
            x <= size.width - right &&
            y >= top &&
            y <= size.height - bottom


    /**
     * Checks if pointer position is inside any foreground widget bounds.
     */
    private fun Offset.isInsideForegroundWidget(
    ): Boolean = widgetsService.widgets.value.any { widget ->
        if (widget.foreground == false) return@any false

        val dm = widgetsService.dm
        val left = widget.x * dm.widthPixels
        val top = widget.y * dm.heightPixels

        val width = widget.spanX * widgetsService.cellSizePx.value
        val height = widget.spanY * widgetsService.cellSizePx.value

        val right = left + width
        val bottom = top + height

        (x in left..right) && (y in top..bottom)
    }
}