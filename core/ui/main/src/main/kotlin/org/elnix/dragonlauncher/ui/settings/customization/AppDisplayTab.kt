@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.settings.customization


import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayerJson
import org.elnix.dragonlauncher.base.model.serializables.Point.Companion.dummySwipePoint
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.settings.stores.map.ColorModesSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.ColorSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.components.AppPreviewTitle
import org.elnix.dragonlauncher.ui.composition.LocalMainScreenLayers
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSection
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSectionMode
import org.elnix.dragonlauncher.ui.dragon.expandable.rememberExpandableSection
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsSlider
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsSwitchRow
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.statusbar.showChargingAnimation

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppDisplayTab(
    onBack: (() -> Unit),
    drawerViewModel: DrawerViewModel = activityViewModel()
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val showLaunchingAppLabel by UiSettingsStore.showLaunchingAppLabel.asState()
    val showLaunchingAppIcon by UiSettingsStore.showLaunchingAppIcon.asState()
    val appLabelIconOverlayTopPadding by UiSettingsStore.appLabelIconOverlayTopPadding.asState()
    val appLabelOverlaySize by UiSettingsStore.appLabelOverlaySize.asState()
    val showAllActionsOnCurrentCircle by UiSettingsStore.showAllActionsOnCurrentCircle.asState()

    val mainScreenLayers = LocalMainScreenLayers.current

    val topOverlaySettingsState = rememberExpandableSection(stringResource(R.string.app_preview_settings), mode = ExpandableSectionMode.Expandable)

    var demoIcon by remember(topOverlaySettingsState.isExpanded()) {
        mutableStateOf(drawerViewModel.iconsService.getRandomAppIcon())
    }

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
            SettingsSwitchRow(UiSettingsStore.fullScreen)

            SwitchRow(
                title = stringResource(R.string.charging_animation),
                description = stringResource(R.string.charging_animation_desc),
                state = showChargingAnimation()
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

            SettingsSwitchRow(UiSettingsStore.showLaunchingAppLabel)
            SettingsSwitchRow(UiSettingsStore.showLaunchingAppIcon)
            SettingsSlider(
                setting = UiSettingsStore.appLabelIconOverlayTopPadding,
                color = MaterialTheme.colorScheme.primary
            )
            SettingsSlider(
                setting = UiSettingsStore.appLabelOverlaySize,
                color = MaterialTheme.colorScheme.primary
            )
            SettingsSlider(
                setting = UiSettingsStore.appIconOverlaySize,
                color = MaterialTheme.colorScheme.primary
            )
        }

        DragonSettingsGroup(
            title = R.string.dragging_display,
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            SettingsSwitchRow(UiSettingsStore.showAppLaunchingPreview)
            SettingsSwitchRow(UiSettingsStore.showCirclePreview)

            SettingsSwitchRow(UiSettingsStore.showAllActionsOnCurrentCircle) {
                if (!it) {
                    scope.launch {
                        UiSettingsStore.showAllActionsOnCurrentNest.set(ctx, false)
                    }
                }
            }

            SettingsSwitchRow(UiSettingsStore.showAllActionsOnCurrentNest, enabled = showAllActionsOnCurrentCircle)
            SettingsSwitchRow(UiSettingsStore.showAppPreviewIconCenterStartPosition)
            SettingsSwitchRow(UiSettingsStore.rgbLine)
            SettingsSwitchRow(UiSettingsStore.linePreviewSnapToAction)
            SettingsSwitchRow(UiSettingsStore.multiplyOrSubtractOpacityInLiveNests)
        }

        DragonSettingsGroup(
            title = R.string.depth,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            SettingsSlider(UiSettingsStore.maxNestsDepth)
            SettingsSlider(UiSettingsStore.maxLiveNestsDepth)
        }
    }


    if (topOverlaySettingsState.isExpanded()) {
        AppPreviewTitle(
            point = dummySwipePoint(Action.OpenRecentApps).copy(
                customName = "Preview",
                id = demoIcon?.cacheKey ?: ""
            ),
            topPadding = appLabelIconOverlayTopPadding.dp,
            labelSize = appLabelOverlaySize,
            showLabel = showLaunchingAppLabel,
            showIcon = showLaunchingAppIcon
        )
    }
}
