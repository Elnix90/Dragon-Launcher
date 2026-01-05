package org.elnix.dragonlauncher.ui.settings.customization

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.SwipeActionSerializable
import org.elnix.dragonlauncher.data.stores.UiSettingsStore
import org.elnix.dragonlauncher.ui.SETTINGS
import org.elnix.dragonlauncher.ui.components.AppPreviewTitle
import org.elnix.dragonlauncher.ui.helpers.SliderWithLabel
import org.elnix.dragonlauncher.ui.helpers.SwitchRow
import org.elnix.dragonlauncher.ui.helpers.TextDivider
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsLazyHeader
import org.elnix.dragonlauncher.ui.theme.LocalExtraColors


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

    val showAllActionsOnCurrentCircle by UiSettingsStore.getShowAllActionsOnCurrentCircle(ctx)
        .collectAsState(initial = false)

    val showActionIconBorder by UiSettingsStore.getShowActionIconBorder(ctx)
        .collectAsState(initial = false)

    val appLabelIconOverlayTopPadding by UiSettingsStore.getAppLabelIconOverlayTopPadding(ctx)
        .collectAsState(initial = 30)
    var isDraggingAppLabelIcon by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (isDraggingAppLabelIcon) 1f else 0f,
        animationSpec = tween(150)
    )
    val offsetY by animateDpAsState(
        targetValue = if (isDraggingAppLabelIcon) 0.dp else (-20).dp,
        animationSpec = tween(150)
    )
    val extraColors = LocalExtraColors.current

    val icons = emptyMap<String, ImageBitmap>()


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

        item {
            SettingsItem(
                title = stringResource(R.string.icon_pack),
                icon = Icons.Default.Palette
            ) { navController.navigate(SETTINGS.ICON_PACK) }
        }


        item {
            SettingsItem(
                title = stringResource(R.string.status_bar),
                icon = Icons.Default.SignalCellular4Bar
            ) { navController.navigate(SETTINGS.STATUS_BAR) }
        }

        item {
            SettingsItem(
                title = stringResource(R.string.theme_selector),
                icon = Icons.Default.Style
            ) { navController.navigate(SETTINGS.THEME) }
        }

        item {
            SettingsItem(
                title = stringResource(R.string.widgets_floating_apps),
                icon = Icons.Default.Widgets
            ) { navController.navigate(SETTINGS.FLOATING_APPS) }
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
                stringResource(R.string.show_launching_app_label),
            ) { scope.launch { UiSettingsStore.setShowLaunchingAppLabel(ctx, it) } }
        }

        item {
            SwitchRow(
                showLaunchingAppIcon,
                stringResource(R.string.show_launching_app_icon),
            ) { scope.launch { UiSettingsStore.setShowLaunchingAppIcon(ctx, it) } }
        }

        item {
            SliderWithLabel(
                label = stringResource(R.string.app_label_icon_overlay_top_padding),
                value = appLabelIconOverlayTopPadding,
                showValue = true,
                valueRange = 0f..360f,
                color = MaterialTheme.colorScheme.primary,
                onReset = { scope.launch { UiSettingsStore.setAppLabelIconOverlayTopPadding(ctx, 30) } },
                onChange = {
                    scope.launch { UiSettingsStore.setAppLabelIconOverlayTopPadding(ctx, it) }
                },
                onDragStateChange = {
                    isDraggingAppLabelIcon = it
                }
            )
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
                label = stringResource(R.string.min_dist_to_activate_action),
                value = minAngleFromAPointToActivateIt,
                showValue = true,
                valueRange = 0f..360f,
                color = MaterialTheme.colorScheme.primary,
                onReset = { scope.launch { UiSettingsStore.setMinAngleFromAPointToActivateIt(ctx, 30) } }
            ) {
                scope.launch { UiSettingsStore.setMinAngleFromAPointToActivateIt(ctx, it) }
            }
        }

        item {
            SwitchRow(
                state = showAllActionsOnCurrentCircle,
                text = stringResource(R.string.show_all_actions_on_current_circle),
            ) { scope.launch { UiSettingsStore.setShowAllActionsOnCurrentCircle(ctx, it) } }
        }

        item {
            SwitchRow(
                state = showActionIconBorder,
                text = stringResource(R.string.show_actions_icon_border),
            ) { scope.launch { UiSettingsStore.setShowActionIconBorder(ctx, it) } }
        }
    }

    if (isDraggingAppLabelIcon) {
        AppPreviewTitle(
            offsetY = offsetY,
            alpha = alpha,
            icons = icons,
            currentAction = SwipeActionSerializable.OpenDragonLauncherSettings,
            extraColors = extraColors,
            label = stringResource(R.string.dragon_launcher_settings),
            topPadding = appLabelIconOverlayTopPadding.dp,
            showLabel = showLaunchingAppLabel,
            showIcon = showLaunchingAppIcon
        )
    }
}
