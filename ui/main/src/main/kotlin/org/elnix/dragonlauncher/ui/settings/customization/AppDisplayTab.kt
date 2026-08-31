package org.elnix.dragonlauncher.ui.settings.customization

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.github.elnix90.runtime.asMutableState
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.settings.stores.map.ColorModesSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.ColorSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.components.PointPreviewTitle
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.dragon.settings.Setting
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.statusbar.showChargingAnimation

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppDisplayTab(pointsViewModel: PointsViewModel = activityViewModel()) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var showPreview by remember { mutableStateOf(false) }

    SettingsScaffold(
        title = stringResource(R.string.app_display),
        helpText = stringResource(R.string.app_display_desc),
        resetText = stringResource(R.string.reset_app_display_tab),
        onReset = {
            showPreview = false
            scope.launch {
                ColorSettingsStore.resetAll(ctx)
                ColorModesSettingsStore.resetAll(ctx)
            }
        }
    ) {
        DragonSettingsGroup(R.string.common_settings) {
            Setting(UiSettingsStore.fullScreen)

            var showChargingAnimation by showChargingAnimation()

            SwitchRow(
                title = R.string.charging_animation,
                description = R.string.charging_animation_desc,
                icon = R.drawable.battery_charging,
                state = showChargingAnimation
            ) { showChargingAnimation = it }
        }

        DragonSettingsGroup(R.string.app_preview_settings) {
            SwitchRow(
                state = showPreview,
                title = R.string.show_app_launch_preview,
                description = R.string.app_preview_settings_desc,
                icon = R.drawable.visibility
            ) { showPreview = it }

            Setting(UiSettingsStore.showLaunchingAppLabel)
            Setting(UiSettingsStore.showLaunchingAppIcon)
            Setting(UiSettingsStore.appLabelIconOverlayTopPadding)
            Setting(UiSettingsStore.appLabelOverlaySize)
            Setting(UiSettingsStore.appIconOverlaySize)
        }

        DragonSettingsGroup(R.string.dragging_display) {
            var showAllActionsOnCurrentNest by UiSettingsStore.showAllPointsInCurrentNest.asMutableState()
            Setting(UiSettingsStore.showAllPointsInCurrentShape) { enabled ->
                if (!enabled) showAllActionsOnCurrentNest = false
            }

            val showAllActionsOnCurrentCircle by UiSettingsStore.showAllPointsInCurrentShape.asState()
            Setting(UiSettingsStore.showAllPointsInCurrentNest, enabled = showAllActionsOnCurrentCircle)

            Setting(UiSettingsStore.showPointPreviewCenterStartPosition)

            val showAllShapes by UiSettingsStore.showAllShapesInNest.asState()
            Setting(UiSettingsStore.showCurrentShape, enabled = !showAllShapes)

            var showShape by UiSettingsStore.showCurrentShape.asMutableState()
            Setting(UiSettingsStore.showAllShapesInNest) { enabled ->
                if (enabled) showShape = true
            }

            Setting(UiSettingsStore.multiplyOrSubtractOpacityInLiveNests)
        }

        DragonSettingsGroup(R.string.depth) {
            Setting(UiSettingsStore.maxNestsDepth)
            Setting(UiSettingsStore.maxLiveNestsDepth)
        }
    }

    val points by pointsViewModel.pointsService.points.collectAsState()
    val randomPoint = remember(showPreview) { points.values.random() }

    val showLaunchingAppLabel by UiSettingsStore.showLaunchingAppLabel.asState()
    val showLaunchingAppIcon by UiSettingsStore.showLaunchingAppIcon.asState()
    val appLabelIconOverlayTopPadding by UiSettingsStore.appLabelIconOverlayTopPadding.asState()

    if (showPreview) {
        PointPreviewTitle(
            point = randomPoint,
            topPadding = appLabelIconOverlayTopPadding,
            showLabel = showLaunchingAppLabel,
            showIcon = showLaunchingAppIcon
        )
    }
}
