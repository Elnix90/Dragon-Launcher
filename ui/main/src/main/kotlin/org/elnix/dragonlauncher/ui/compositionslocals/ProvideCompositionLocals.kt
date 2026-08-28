package org.elnix.dragonlauncher.ui.compositionslocals

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.text.rememberTextMeasurer
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.base.model.serializables.StatusBar
import org.elnix.dragonlauncher.base.model.serializables.StatusBarJson
import org.elnix.dragonlauncher.settings.stores.array.StatusBarJsonSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.BehaviorSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.ColorModesSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.ui.base.compositionlocals.LocalDisableHapticFeedbackGlobally
import org.elnix.dragonlauncher.ui.composition.LocalColorPickerMode
import org.elnix.dragonlauncher.ui.composition.LocalNestDebugOverlay
import org.elnix.dragonlauncher.ui.composition.LocalStatusBarElements
import org.elnix.dragonlauncher.ui.composition.LocalTextMeasurer

@Composable
fun ProvideGlobalCompositionLocals(
    content: @Composable () -> Unit
) {
    val disableHapticFeedbackGlobally by BehaviorSettingsStore.disableHapticFeedbackGlobally.asState()
    val elementsJson by StatusBarJsonSettingsStore.jsonSetting.asState()

    val elements by remember(elementsJson) {
        derivedStateOf {
            StatusBarJson.decode<List<StatusBar>>(elementsJson, emptyList())
        }
    }

    val nestDebugOverlay by DebugSettingsStore.nestDebugOverlay.asState()

    val colorPickerMode by ColorModesSettingsStore.colorPickerMode.asState()


    /**
     * Main Composition local provider, I just for everything I can here to avoid having to import them everywhere
     * I know that I should carefully review what global locals I add, but until now it worked to I'll keep it that way until I notice lag
     */
    CompositionLocalProvider(
        LocalTextMeasurer provides rememberTextMeasurer(),

        LocalStatusBarElements provides elements,
        LocalDisableHapticFeedbackGlobally provides disableHapticFeedbackGlobally,
        LocalNestDebugOverlay provides nestDebugOverlay,
        LocalColorPickerMode provides colorPickerMode
    ) {
        ProvideCurrentTime {
            ProvideDrawerSettings {
                content()
            }
        }
    }
}