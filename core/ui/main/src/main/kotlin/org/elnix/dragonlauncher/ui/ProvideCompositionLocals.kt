package org.elnix.dragonlauncher.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import org.elnix.dragonlauncher.base.model.serializables.StatusBar
import org.elnix.dragonlauncher.base.model.serializables.StatusBarJson
import org.elnix.dragonlauncher.logging.ICONS_TAG
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.models.PointViewModel
import org.elnix.dragonlauncher.settings.stores.array.StatusBarJsonSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.BehaviorSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.compositionslocals.LocalAppItemSettings
import org.elnix.dragonlauncher.ui.base.compositionslocals.LocalDisableHapticFeedbackGlobally
import org.elnix.dragonlauncher.ui.base.compositionslocals.ProvideCurrentTime
import org.elnix.dragonlauncher.ui.base.compositionslocals.rememberAppItemSettings
import org.elnix.dragonlauncher.ui.composition.LocalAngleLineObject
import org.elnix.dragonlauncher.ui.composition.LocalEndLineObject
import org.elnix.dragonlauncher.ui.composition.LocalGridSize
import org.elnix.dragonlauncher.ui.composition.LocalHoldCustomObject
import org.elnix.dragonlauncher.ui.composition.LocalIconShape
import org.elnix.dragonlauncher.ui.composition.LocalLineObject
import org.elnix.dragonlauncher.ui.composition.LocalMainScreenLayers
import org.elnix.dragonlauncher.ui.composition.LocalShowLabelsInAddPointDialog
import org.elnix.dragonlauncher.ui.composition.LocalStartLineObject
import org.elnix.dragonlauncher.ui.composition.LocalStatusBarElements
import org.elnix.dragonlauncher.ui.dialogs.rememberMainScreenLayerOrder
import org.elnix.dragonlauncher.ui.remembers.CustomObjectJson.rememberAngleLineObjects
import org.elnix.dragonlauncher.ui.remembers.CustomObjectJson.rememberHoldCustomObject

@Composable
fun ProvideGlobalCompositionLocals(
    drawerViewModel: DrawerViewModel = activityViewModel(),
    pointsViewModel: PointViewModel = activityViewModel(),
    content: @Composable () -> Unit
) {
    val points by pointsViewModel.points.collectAsState()

    val disableHapticFeedbackGlobally by BehaviorSettingsStore.disableHapticFeedbackGlobally.asState()

    val pointsIconCache = drawerViewModel.pointsIconsCache

    LaunchedEffect(points.size) {
        logD(ICONS_TAG) { "Updating icons cache size to ${points.size}" }
        pointsIconCache.updateMaxCacheSize(points.size)
    }


    val elementsJson by StatusBarJsonSettingsStore.jsonSetting.asState()

    val elements by remember(elementsJson) {
        derivedStateOf {
            StatusBarJson.decode<List<StatusBar>>(elementsJson) ?: emptyList()
        }
    }


    val gridSize by drawerViewModel.gridSize.asState()

    val lineObjects = rememberAngleLineObjects()
    val holdCustomObject = rememberHoldCustomObject()
    val layersOrder by rememberMainScreenLayerOrder()

    val showTooltipsOnAddPointDialog by UiSettingsStore.showTooltipsOnAddPointDialog.asState()

    val iconShape by drawerViewModel.iconShape.asState()

    /**
     * Main Composition local provider, I just for everything I can here to avoid having to import them everywhere
     * I know that I should carefully review what global locals I add, but until now it worked to I'll keep it that way until I notice lag
     */
    CompositionLocalProvider(

        LocalIconShape provides iconShape,
        LocalGridSize provides gridSize,
        LocalStatusBarElements provides elements,

        LocalLineObject provides lineObjects.line,

        LocalAngleLineObject provides lineObjects.angleLine,
        LocalStartLineObject provides lineObjects.startLine,
        LocalEndLineObject provides lineObjects.endLine,
        LocalHoldCustomObject provides holdCustomObject,

        LocalMainScreenLayers provides layersOrder,
        LocalShowLabelsInAddPointDialog provides showTooltipsOnAddPointDialog,

        LocalDisableHapticFeedbackGlobally provides disableHapticFeedbackGlobally,

        LocalAppItemSettings provides rememberAppItemSettings()
    ) {
        ProvideCurrentTime {
            content()
        }
    }
}