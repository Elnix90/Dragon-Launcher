package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.compose.runtime.Stable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.elnix90.logging.logE
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.ANGLE_LINE_TAG
import org.elnix.dragonlauncher.base.SettingFlow
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
import org.elnix.dragonlauncher.ktx.isNotBlankJson
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.settings.stores.array.MainScreenLayersSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.AngleLineSettingsStore
import org.elnix.dragonlauncher.settings.stores.objects.AngleObjectSettingStore
import org.elnix.dragonlauncher.settings.stores.objects.EndObjectSettingStore
import org.elnix.dragonlauncher.settings.stores.objects.HoldToActivateObject
import org.elnix.dragonlauncher.settings.stores.objects.LineObjectSettingStore
import org.elnix.dragonlauncher.settings.stores.objects.StartObjectSettingStore
import javax.inject.Inject

@Stable
@HiltViewModel
public class SwipeViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    public val lineObject: SettingFlow<CustomObject> = SettingFlow(defaultLineCustomObject)
    public val angleObject: SettingFlow<CustomObject> = SettingFlow(defaultAngleCustomObject)
    public val startObject: SettingFlow<CustomObject> = SettingFlow(defaultStartCustomObject)
    public val endObject: SettingFlow<CustomObject> = SettingFlow(defaultEndCustomObject)

    public val holdObject: SettingFlow<CustomObject> = SettingFlow(defaultHoldCustomObject)

    public val lineObjectOrder: SettingFlow<List<AngleLineObjects>> = SettingFlow(AngleLineObjects.entries)

    public val mainScreenLayerOrder: SettingFlow<List<MainScreenLayer>> = SettingFlow(defaultMainScreenLayers)

    init {
        loadMainScreenLayers()
        loadAngleLineObjects()
        loadHoldObject()
        loadAngleLineOrder()

        viewModelInitialized()
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
}