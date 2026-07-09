package org.elnix.dragonlauncher.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import io.github.elnix90.logging.ICONS_TAG
import io.github.elnix90.logging.logD
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.base.model.serializables.StatusBar
import org.elnix.dragonlauncher.base.model.serializables.StatusBarJson
import org.elnix.dragonlauncher.models.IconsViewModel
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.settings.stores.array.StatusBarJsonSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.BehaviorSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.compositionslocals.LocalDisableHapticFeedbackGlobally
import org.elnix.dragonlauncher.ui.base.compositionslocals.ProvideCurrentTime
import org.elnix.dragonlauncher.ui.composition.LocalAngleLineObject
import org.elnix.dragonlauncher.ui.composition.LocalEndLineObject
import org.elnix.dragonlauncher.ui.composition.LocalHoldCustomObject
import org.elnix.dragonlauncher.ui.composition.LocalLineObject
import org.elnix.dragonlauncher.ui.composition.LocalMainScreenLayers
import org.elnix.dragonlauncher.ui.composition.LocalNestDebugOverlay
import org.elnix.dragonlauncher.ui.composition.LocalShowLabelsInAddPointDialog
import org.elnix.dragonlauncher.ui.composition.LocalStartLineObject
import org.elnix.dragonlauncher.ui.composition.LocalStatusBarElements
import org.elnix.dragonlauncher.ui.dialogs.rememberMainScreenLayerOrder
import org.elnix.dragonlauncher.ui.remembers.CustomObjectJson.rememberAngleLineObjects
import org.elnix.dragonlauncher.ui.remembers.CustomObjectJson.rememberHoldCustomObject

@Composable
public fun ProvideGlobalCompositionLocals(
    iconsViewModel: IconsViewModel = activityViewModel(),
    pointsViewModel: PointsViewModel = activityViewModel(),
    content: @Composable () -> Unit
) {
    val pointsService = pointsViewModel.pointsService
    val points by pointsService.points.asState()
    LaunchedEffect(points.size) { // TODO pu this into the points viewmodel
        logD(ICONS_TAG) { "Updating icons cache size to ${points.size}" }
        iconsViewModel.updateMaxCacheSize(points.size)
    }


    val disableHapticFeedbackGlobally by BehaviorSettingsStore.disableHapticFeedbackGlobally.asState()
    val elementsJson by StatusBarJsonSettingsStore.jsonSetting.asState()

    val elements by remember(elementsJson) {
        derivedStateOf {
            StatusBarJson.decode<List<StatusBar>>(elementsJson, emptyList())
        }
    }



    val lineObjects = rememberAngleLineObjects()
    val holdCustomObject by rememberHoldCustomObject()
    val layersOrder by rememberMainScreenLayerOrder()

    val showTooltipsOnAddPointDialog by UiSettingsStore.showTooltipsOnAddPointDialog.asState()

    val nestDebugOverlay by DebugSettingsStore.nestDebugOverlay.asState()


    /**
     * Main Composition local provider, I just for everything I can here to avoid having to import them everywhere
     * I know that I should carefully review what global locals I add, but until now it worked to I'll keep it that way until I notice lag
     */
    CompositionLocalProvider(

        LocalStatusBarElements provides elements,

        LocalLineObject provides lineObjects.line,

        LocalAngleLineObject provides lineObjects.angleLine,
        LocalStartLineObject provides lineObjects.startLine,
        LocalEndLineObject provides lineObjects.endLine,
        LocalHoldCustomObject provides holdCustomObject,

        LocalMainScreenLayers provides layersOrder,
        LocalShowLabelsInAddPointDialog provides showTooltipsOnAddPointDialog,

        LocalDisableHapticFeedbackGlobally provides disableHapticFeedbackGlobally,
        LocalNestDebugOverlay provides nestDebugOverlay
    ) {
        ProvideCurrentTime {
            content()
        }
    }
}