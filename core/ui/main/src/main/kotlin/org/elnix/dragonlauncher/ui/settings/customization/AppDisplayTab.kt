@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.settings.customization


import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayerJson
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.settings.stores.map.ColorModesSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.ColorSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.UiConstants.dragonSettingGroupPaddingValues
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.components.PointPreviewTitle
import org.elnix.dragonlauncher.ui.composition.LocalMainScreenLayers
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSection
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSectionMode
import org.elnix.dragonlauncher.ui.dragon.expandable.rememberExpandableSection
import org.elnix.dragonlauncher.ui.dragon.settings.Setting
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.statusbar.showChargingAnimation

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
public fun AppDisplayTab(
    onBack: (() -> Unit),
    pointsViewModel: PointsViewModel = activityViewModel()
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val showLaunchingAppLabel by UiSettingsStore.showLaunchingAppLabel.asState()
    val showLaunchingAppIcon by UiSettingsStore.showPreviewPoint.asState()
    val appLabelIconOverlayTopPadding by UiSettingsStore.appLabelIconOverlayTopPadding.asState()
    val showAllActionsOnCurrentCircle by UiSettingsStore.showAllActionsOnCurrentShape.asState()

    val mainScreenLayers = LocalMainScreenLayers.current

    val topOverlaySettingsState = rememberExpandableSection(stringResource(R.string.app_preview_settings), mode = ExpandableSectionMode.Expandable)

    SettingsScaffold(
        title = stringResource(R.string.app_display),
        onBack = onBack,
        helpText = stringResource(R.string.app_display_desc),
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
                    UiSettingsStore.mainScreenLayers.set(
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

        ExpandableSection(topOverlaySettingsState) {
            Setting(UiSettingsStore.showLaunchingAppLabel)
            Setting(UiSettingsStore.showPreviewPoint)
            Setting(
                setting = UiSettingsStore.appLabelIconOverlayTopPadding,
                color = MaterialTheme.colorScheme.primary
            )
            Setting(
                setting = UiSettingsStore.appLabelOverlaySize,
                color = MaterialTheme.colorScheme.primary
            )
            Setting(
                setting = UiSettingsStore.appIconOverlaySize,
                color = MaterialTheme.colorScheme.primary
            )
        }

        DragonSettingsGroup(
            title = R.string.dragging_display,
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Setting(UiSettingsStore.showAppLaunchingPreview)
            Setting(UiSettingsStore.showAllActionsOnCurrentShape)

            Setting(UiSettingsStore.showAllActionsOnCurrentShape) {
                if (!it) {
                    scope.launch {
                        UiSettingsStore.showAllActionsOnCurrentNest.set(ctx, false)
                    }
                }
            }

            Setting(UiSettingsStore.showAllActionsOnCurrentNest, enabled = showAllActionsOnCurrentCircle)
            Setting(UiSettingsStore.showPointPreviewCenterStartPosition)
            Setting(UiSettingsStore.linePreviewSnapToAction)

            val snap by UiSettingsStore.linePreviewSnapToAction.asState()
            Setting(UiSettingsStore.animationWhenSnapping, enabled = snap)

            Setting(UiSettingsStore.multiplyOrSubtractOpacityInLiveNests)
        }

        DragonSettingsGroup(
            title = R.string.depth,
            contentPadding = dragonSettingGroupPaddingValues
        ) {
            Setting(UiSettingsStore.maxNestsDepth)
            Setting(UiSettingsStore.maxLiveNestsDepth)
        }
    }


    val pointsService = pointsViewModel.pointsService
    val points by pointsService.points.asState()
    val randomPoint = remember { points.random() }

    if (topOverlaySettingsState.isExpanded()) {
        PointPreviewTitle(
            point = randomPoint.copy(customName = "Preview"),
            topPadding = appLabelIconOverlayTopPadding.dp,
            showLabel = showLaunchingAppLabel,
            showIcon = showLaunchingAppIcon
        )
    }
}
