package org.elnix.dragonlauncher.ui.settings.customization

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.stores.UiSettingsStore
import org.elnix.dragonlauncher.ui.SETTINGS
import org.elnix.dragonlauncher.ui.helpers.SliderWithLabel
import org.elnix.dragonlauncher.ui.helpers.SwitchRow
import org.elnix.dragonlauncher.ui.helpers.TextDivider
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsLazyHeader


@Composable
fun AppearanceTab(
    navController: NavController,
    onBack: () -> Unit
) {

    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val fullscreenApp by UiSettingsStore.getFullscreen(ctx)
        .collectAsState(initial = false)

    val rgbLoading by UiSettingsStore.getRGBLoading(ctx)
        .collectAsState(initial = true)

    val rgbLine by UiSettingsStore.getRGBLine(ctx)
        .collectAsState(initial = true)

    val showLaunchingAppLabel by UiSettingsStore.getShowLaunchingAppLabel(ctx)
        .collectAsState(initial = true)

    val showLaunchingAppIcon by UiSettingsStore.getShowLaunchingAppIcon(ctx)
        .collectAsState(initial = true)

    val showAppLaunchPreview by UiSettingsStore.getShowAppLaunchPreview(ctx)
        .collectAsState(initial = true)

    val showAppCirclePreview by UiSettingsStore.getShowCirclePreview(ctx)
        .collectAsState(initial = true)

    val showAppLinePreview by UiSettingsStore.getShowLinePreview(ctx)
        .collectAsState(initial = true)

    val showAppAnglePreview by UiSettingsStore.getShowAnglePreview(ctx)
        .collectAsState(initial = true)

    val showAppPreviewIconCenterStartPosition by UiSettingsStore.getShowAppPreviewIconCenterStartPosition(ctx)
        .collectAsState(initial = false)

    val linePreviewSnapToAction by UiSettingsStore.getLinePreviewSnapToAction(ctx)
        .collectAsState(initial = false)

    val minAngleFromAPointToActivateIt by UiSettingsStore.getMinAngleFromAPointToActivateIt(ctx)
        .collectAsState(initial = 0)


    SettingsLazyHeader(
        title = stringResource(R.string.appearance),
        onBack = onBack,
        helpText = stringResource(R.string.appearance_tab_text),
        onReset = {
            scope.launch {
                UiSettingsStore.resetAll(ctx)
            }
        }
    ) {

        item {
            SettingsItem(
                title = stringResource(R.string.color_selector),
                icon = Icons.Default.ColorLens
            ) { navController.navigate(SETTINGS.COLORS) }
        }

        item {
            SettingsItem(
                title = stringResource(R.string.wallpaper),
                icon = Icons.Default.Wallpaper
            ) { navController.navigate(SETTINGS.WALLPAPER) }
        }

        item { TextDivider(stringResource(R.string.app_display)) }


        item {
            SwitchRow(
                fullscreenApp,
                stringResource(R.string.fullscreen_app),
            ) {
                scope.launch { UiSettingsStore.setFullscreen(ctx, it) }
            }
        }

        item {
            SwitchRow(
                rgbLoading,
                "RGB loading settings",
            ) { scope.launch { UiSettingsStore.setRGBLoading(ctx, it) } }

        }

        item {
            SwitchRow(
                rgbLine,
                "RGB line selector",
            ) { scope.launch { UiSettingsStore.setRGBLine(ctx, it) } }
        }

        item {
            SwitchRow(
                showLaunchingAppLabel,
                "Show App label",
            ) { scope.launch { UiSettingsStore.setShowLaunchingAppLabel(ctx, it) } }
        }

        item {
            SwitchRow(
                showLaunchingAppIcon,
                "Show App icon",
            ) { scope.launch { UiSettingsStore.setShowLaunchingAppIcon(ctx, it) } }
        }

        item {
            SwitchRow(
                showAppLaunchPreview,
                "Show App launch preview",
            ) { scope.launch { UiSettingsStore.setShowAppLaunchPreview(ctx, it) } }
        }

        item {
            SwitchRow(
                showAppCirclePreview,
                "Show App circle preview",
            ) { scope.launch { UiSettingsStore.setShowCirclePreview(ctx, it) } }
        }

        item {
            SwitchRow(
                showAppLinePreview,
                "Show App line preview",
            ) { scope.launch { UiSettingsStore.setShowLinePreview(ctx, it) } }
        }

        item {
            SwitchRow(
                showAppAnglePreview,
                "Show App Angle preview. ${if (!showAppAnglePreview) " Do you hate it?" else ""}",
            ) { scope.launch { UiSettingsStore.setShowAnglePreview(ctx, it) } }
        }

        item {
            SwitchRow(
                showAppPreviewIconCenterStartPosition,
                "Show App Icon angle preview in center of start dragging position",
            ) { scope.launch { UiSettingsStore.setShowAppPreviewIconCenterStartPosition(ctx, it) } }
        }

        item {
            SwitchRow(
                linePreviewSnapToAction,
                "Line Preview snap to action",
            ) { scope.launch { UiSettingsStore.setLinePreviewSnapToAction(ctx, it) } }
        }

        item {
            SliderWithLabel(
                label = "Min Distance to activate action (0 for infinite)",
                value = minAngleFromAPointToActivateIt,
                showValue = true,
                valueRange = 0f..360f,
                color = MaterialTheme.colorScheme.primary,
                onReset = { scope.launch { UiSettingsStore.setMinAngleFromAPointToActivateIt(ctx, 30) } }
            ) {
                scope.launch { UiSettingsStore.setMinAngleFromAPointToActivateIt(ctx, it) }
            }
        }
    }
}
