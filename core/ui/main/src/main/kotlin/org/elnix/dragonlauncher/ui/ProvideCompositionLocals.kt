package org.elnix.dragonlauncher.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.elnix.dragonlauncher.common.serializables.ColorSerializer
import org.elnix.dragonlauncher.common.serializables.StatusBarJson
import org.elnix.dragonlauncher.common.serializables.SwipePointSerializable.Companion.defaultSwipePointsValues
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.ANGLE_LINE_TAG
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.ICONS_TAG
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.STATUS_BAR_TAG
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.logging.logV
import org.elnix.dragonlauncher.logging.logW
import org.elnix.dragonlauncher.settings.stores.AngleLineSettingsStore
import org.elnix.dragonlauncher.settings.stores.BehaviorSettingsStore
import org.elnix.dragonlauncher.settings.stores.DrawerSettingsStore
import org.elnix.dragonlauncher.settings.stores.HoldToActivateArcSettingsStore
import org.elnix.dragonlauncher.settings.stores.StatusBarJsonSettingsStore
import org.elnix.dragonlauncher.settings.stores.SwipeSettingsStore
import org.elnix.dragonlauncher.settings.stores.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.UiConstants
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.compositionslocals.LocalAppItemSettings
import org.elnix.dragonlauncher.ui.base.compositionslocals.LocalDisableHapticFeedbackGlobally
import org.elnix.dragonlauncher.ui.base.compositionslocals.rememberAppItemSettings
import org.elnix.dragonlauncher.ui.composition.LocalAngleLineObject
import org.elnix.dragonlauncher.ui.composition.LocalAppsViewModel
import org.elnix.dragonlauncher.ui.composition.LocalDefaultPoint
import org.elnix.dragonlauncher.ui.composition.LocalDrawerIconsCache
import org.elnix.dragonlauncher.ui.composition.LocalEndLineObject
import org.elnix.dragonlauncher.ui.composition.LocalHoldCustomObject
import org.elnix.dragonlauncher.ui.composition.LocalIconShape
import org.elnix.dragonlauncher.ui.composition.LocalLineObject
import org.elnix.dragonlauncher.ui.composition.LocalMainScreenLayers
import org.elnix.dragonlauncher.ui.composition.LocalNests
import org.elnix.dragonlauncher.ui.composition.LocalPointIconsCache
import org.elnix.dragonlauncher.ui.composition.LocalPoints
import org.elnix.dragonlauncher.ui.composition.LocalShowLabelsInAddPointDialog
import org.elnix.dragonlauncher.ui.composition.LocalStartLineObject
import org.elnix.dragonlauncher.ui.composition.LocalStatusBarElements
import org.elnix.dragonlauncher.ui.dialogs.rememberMainScreenLayerOrder
import org.elnix.dragonlauncher.ui.remembers.rememberDecodedObject

@Composable
fun ProvideGlobalCompositionLocals(content: @Composable () -> Unit) {
    val appsViewModel = LocalAppsViewModel.current
    val ctx = LocalContext.current

    val disableHapticFeedbackGlobally by BehaviorSettingsStore.disableHapticFeedbackGlobally.asState()


    val nests by SwipeSettingsStore.getNestsFlow(ctx).collectAsState(initial = emptyList())
    val defaultPoint by SwipeSettingsStore.getDefaultPointFlow(ctx)
        .collectAsState(defaultSwipePointsValues)

    val points by SwipeSettingsStore.getPointsFlow(ctx).collectAsState(emptyList())
    val pointsIconCache = appsViewModel.pointsIconsCache
    LaunchedEffect(points.size) {
        logD(ICONS_TAG) { "Updating icons cache size to ${points.size}"}
        pointsIconCache.updateMaxCacheSize(points.size)
    }

    val drawerIconCache = appsViewModel.drawerIconCache

    val elementsJson by StatusBarJsonSettingsStore.jsonSetting.asState()

    val elements by remember(elementsJson) {

        derivedStateOf {
            StatusBarJson.decodeStatusBarElements(elementsJson).also {
                logV(STATUS_BAR_TAG) { "Element: $elementsJson, decoded: $it" }
            }
        }
    }

    val iconsShape by DrawerSettingsStore.iconsShape.asState()

    // Used internally by the app view model
    // Caches the icon shape inside to avoid having to pass the shape through each call of a reload icon
    // Crashes if shape not defined, but as it is passed soon enough, this should be ok (never saw any crash tough)
    LaunchedEffect(iconsShape) {
        appsViewModel.cacheIconShape(iconsShape)
    }

    /* ───────────── Decodes the angle lines things ───────────── */

    val json = Json {
        serializersModule = SerializersModule {
            contextual(Color::class, ColorSerializer)
        }
    }

    val lineJson by AngleLineSettingsStore.lineJson.asState()
    val lineObject = rememberDecodedObject(
        jsonString = lineJson,
        default = UiConstants.defaultLineCustomObject,
        json = json
    ) {
        logW(ANGLE_LINE_TAG) { "Error decoding lineObject" }
    }

    val angleLineJson by AngleLineSettingsStore.angleLineJson.asState()
    val angleLineObject = rememberDecodedObject(
        jsonString = angleLineJson,
        default = UiConstants.defaultAngleCustomObject,
        json = json
    ) {
        logW(ANGLE_LINE_TAG) { "Error decoding angleLineObject" }
    }

    val startLineJson by AngleLineSettingsStore.startLineJson.asState()
    val startLineObject = rememberDecodedObject(
        jsonString = startLineJson,
        default = UiConstants.defaultStartCustomObject,
        json = json
    ) {
        logW(ANGLE_LINE_TAG) { "Error decoding startLineObject" }
    }

    val endLineJson by AngleLineSettingsStore.endLineJson.asState()
    val endLineObject = rememberDecodedObject(
        jsonString = endLineJson,
        default = UiConstants.defaultEndCustomObject,
        json = json
    ) {
        logW(ANGLE_LINE_TAG) { "Error decoding endLineObject" }
    }

    val holdCustomObjectJson by HoldToActivateArcSettingsStore.holdToActivateArcCustomObject.asState()
    val holdCustomObject = rememberDecodedObject(
        jsonString = holdCustomObjectJson,
        default = UiConstants.defaultHoldCustomObject,
        json = json
    ) {
        logW(ANGLE_LINE_TAG) { "Error decoding endLineObject" }
    }

    val layersOrder by rememberMainScreenLayerOrder()

    val showTooltipsOnAddPointDialog by UiSettingsStore.showTooltipsOnAddPointDialog.asState()


    /**
     * Main Composition local provider, I just for everything I can here to avoid having to import them everywhere
     * I know that I should carefully review what global locals I add, but until now it worked to I'll keep it that way until I notice lag
     */
    CompositionLocalProvider(
        LocalDefaultPoint provides defaultPoint,

        LocalDrawerIconsCache provides drawerIconCache,
        LocalPointIconsCache provides pointsIconCache,

        LocalIconShape provides iconsShape,
        LocalPoints provides points,
        LocalNests provides nests,
        LocalStatusBarElements provides elements,

        LocalLineObject provides lineObject,

        LocalAngleLineObject provides angleLineObject,
        LocalStartLineObject provides startLineObject,
        LocalEndLineObject provides endLineObject,
        LocalHoldCustomObject provides holdCustomObject,

        LocalMainScreenLayers provides layersOrder,
        LocalShowLabelsInAddPointDialog provides showTooltipsOnAddPointDialog,

        LocalDisableHapticFeedbackGlobally provides disableHapticFeedbackGlobally,

        LocalAppItemSettings provides rememberAppItemSettings()
    ) {
        content()
    }
}