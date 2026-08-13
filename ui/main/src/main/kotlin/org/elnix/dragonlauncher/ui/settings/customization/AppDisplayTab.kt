package org.elnix.dragonlauncher.ui.settings.customization


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.github.elnix90.runtime.asMutableState
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer.Companion.MainScreenLayerJson
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.settings.stores.map.ColorModesSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.ColorSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.settings.stores.objects.MainScreenLayersSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.components.PointPreviewTitle
import org.elnix.dragonlauncher.ui.composition.LocalMainScreenLayers
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSection
import org.elnix.dragonlauncher.ui.dragon.expandable.rememberExpandableSection
import org.elnix.dragonlauncher.ui.dragon.settings.Setting
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.statusbar.showChargingAnimation

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppDisplayTab(pointsViewModel: PointsViewModel = activityViewModel()) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val mainScreenLayers = LocalMainScreenLayers.current

    val topOverlaySettingsState = rememberExpandableSection(
        title = stringResource(R.string.app_preview_settings),
        description = stringResource(R.string.app_preview_settings_desc)
    )

    SettingsScaffold(
        title = stringResource(R.string.app_display),
        helpText = stringResource(R.string.app_display_desc),
        resetText = stringResource(R.string.reset_app_display_tab),
        onReset = {
            scope.launch {
                ColorSettingsStore.resetAll(ctx)
                ColorModesSettingsStore.resetAll(ctx)
            }
        }
    ) {
        DragonSettingsGroup(R.string.common_settings) {
            Setting(UiSettingsStore.fullScreen)

            val showChargingAnimation by showChargingAnimation()

            SwitchRow(
                title = stringResource(R.string.charging_animation),
                description = stringResource(R.string.charging_animation_desc),
                state = showChargingAnimation
            ) {
                scope.launch {
                    MainScreenLayersSettingsStore.jsonSetting.set(
                        ctx,
                        MainScreenLayerJson.encode(
                            mainScreenLayers.map {
                                if (it is MainScreenLayer.ChargingAnimation) it.copy(enabled = !it.enabled)
                                else it
                            }
                        )
                    )
                }
            }
        }


        DragonSettingsGroup(R.string.dragging_display) {
            Setting(UiSettingsStore.showAppLaunchingPreview)

            val showAppLaunchingPreview by UiSettingsStore.showAppLaunchingPreview.asState()
            AnimatedVisibility(showAppLaunchingPreview) {
                ExpandableSection(topOverlaySettingsState) {
                    Setting(UiSettingsStore.showLaunchingAppLabel)
                    Setting(UiSettingsStore.showLaunchingAppIcon)
                    Setting(UiSettingsStore.appLabelIconOverlayTopPadding)
                    Setting(UiSettingsStore.appLabelOverlaySize)
                    Setting(UiSettingsStore.appIconOverlaySize)
                }
            }

            var showAllActionsOnCurrentNest by UiSettingsStore.showAllPointsInCurrentNest.asMutableState()
            Setting(UiSettingsStore.showAllPointsInCurrentShape) { enabled ->
                if (!enabled) {
                    showAllActionsOnCurrentNest = false
                }
            }

            val showAllActionsOnCurrentCircle by UiSettingsStore.showAllPointsInCurrentShape.asState()
            Setting(UiSettingsStore.showAllPointsInCurrentNest, enabled = showAllActionsOnCurrentCircle)

            Setting(UiSettingsStore.showPointPreviewCenterStartPosition)

            val showAllShapes by UiSettingsStore.showAllShapesInNest.asState()
            var showShape by UiSettingsStore.showCurrentShape.asMutableState()
            Setting(UiSettingsStore.showCurrentShape, enabled = !showAllShapes)
            Setting(UiSettingsStore.showAllShapesInNest) { enabled -> if (enabled) showShape = true }

            Setting(UiSettingsStore.multiplyOrSubtractOpacityInLiveNests)
        }

        DragonSettingsGroup(R.string.depth) {
            Setting(UiSettingsStore.maxNestsDepth)
            Setting(UiSettingsStore.maxLiveNestsDepth)
        }
    }

    val points by pointsViewModel.pointsService.points.collectAsState()
    val randomPoint = remember { points.values.random() }

    val showLaunchingAppLabel by UiSettingsStore.showLaunchingAppLabel.asState()
    val showLaunchingAppIcon by UiSettingsStore.showLaunchingAppIcon.asState()
    val appLabelIconOverlayTopPadding by UiSettingsStore.appLabelIconOverlayTopPadding.asState()

    if (topOverlaySettingsState.isExpanded()) {
        PointPreviewTitle(
            point = randomPoint.copy(customName = "Preview"),
            topPadding = appLabelIconOverlayTopPadding,
            showLabel = showLaunchingAppLabel,
            showIcon = showLaunchingAppIcon
        )
    }
}
