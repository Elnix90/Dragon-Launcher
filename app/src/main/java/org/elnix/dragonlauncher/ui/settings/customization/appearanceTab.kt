package org.elnix.dragonlauncher.ui.settings.customization

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.SwipeActionSerializable
import org.elnix.dragonlauncher.data.dummySwipePoint
import org.elnix.dragonlauncher.data.stores.UiSettingsStore
import org.elnix.dragonlauncher.ui.SETTINGS
import org.elnix.dragonlauncher.ui.components.AppPreviewTitle
import org.elnix.dragonlauncher.ui.helpers.SliderWithLabel
import org.elnix.dragonlauncher.ui.helpers.SwitchRow
import org.elnix.dragonlauncher.ui.helpers.TextDivider
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsLazyHeader
import org.elnix.dragonlauncher.utils.colors.adjustBrightness
import org.elnix.dragonlauncher.utils.models.AppsViewModel


@Composable
fun AppearanceTab(
    appsViewModel: AppsViewModel,
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

    val appLabelIconOverlayTopPadding by UiSettingsStore.getAppLabelIconOverlayTopPadding(ctx)
        .collectAsState(initial = 30)
    val appLabelOverlaySize by UiSettingsStore.getAppLabelOverlaySize(ctx)
        .collectAsState(initial = 18)
    val appIconOverlaySize by UiSettingsStore.getAppIconOverlaySize(ctx)
        .collectAsState(initial = 22)

    var isDraggingAppPreviewOverlays by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (isDraggingAppPreviewOverlays) 1f else 0f,
        animationSpec = tween(150)
    )
    val offsetY by animateDpAsState(
        targetValue = if (isDraggingAppPreviewOverlays) 0.dp else (-20).dp,
        animationSpec = tween(150)
    )

    val icons by appsViewModel.pointIcons.collectAsState()


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
                stringResource(R.string.rgb_loading_settings),
            ) { scope.launch { UiSettingsStore.setRGBLoading(ctx, it) } }

        }

        item {
            SwitchRow(
                rgbLine,
                stringResource(R.string.rgb_line_selector),
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
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.adjustBrightness(0.7f))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.adjustBrightness(0.2f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(8.dp)
            ) {
                SliderWithLabel(
                    label = stringResource(R.string.app_label_icon_overlay_top_padding),
                    value = appLabelIconOverlayTopPadding,
                    showValue = true,
                    valueRange = 0..360,
                    color = MaterialTheme.colorScheme.primary,
                    onReset = {
                        scope.launch {
                            UiSettingsStore.setAppLabelIconOverlayTopPadding(
                                ctx,
                                18
                            )
                        }
                    },
                    onChange = {
                        scope.launch { UiSettingsStore.setAppLabelIconOverlayTopPadding(ctx, it) }
                    },
                    onDragStateChange = {
                        isDraggingAppPreviewOverlays = it
                    }
                )

                SliderWithLabel(
                    label = stringResource(R.string.app_label_size),
                    value = appLabelOverlaySize,
                    showValue = true,
                    valueRange = 0..70,
                    color = MaterialTheme.colorScheme.primary,
                    onReset = {
                        scope.launch {
                            UiSettingsStore.setAppLabelOverlaySize(
                                ctx,
                                18
                            )
                        }
                    },
                    onChange = {
                        scope.launch { UiSettingsStore.setAppLabelOverlaySize(ctx, it) }
                    },
                    onDragStateChange = {
                        isDraggingAppPreviewOverlays = it
                    }
                )

                SliderWithLabel(
                    label = stringResource(R.string.app_icon_size),
                    value = appIconOverlaySize,
                    showValue = true,
                    valueRange = 0..70,
                    color = MaterialTheme.colorScheme.primary,
                    onReset = {
                        scope.launch {
                            UiSettingsStore.setAppIconOverlaySize(
                                ctx,
                                22
                            )
                        }
                    },
                    onChange = {
                        scope.launch { UiSettingsStore.setAppIconOverlaySize(ctx, it) }
                    },
                    onDragStateChange = {
                        isDraggingAppPreviewOverlays = it
                    }
                )
            }
        }

        item {
            SwitchRow(
                showAppLaunchPreview,
                stringResource(R.string.show_app_launch_preview),
            ) { scope.launch { UiSettingsStore.setShowAppLaunchPreview(ctx, it) } }
        }

        item {
            SwitchRow(
                showAppCirclePreview,
                stringResource(R.string.show_app_circle_preview),
            ) { scope.launch { UiSettingsStore.setShowCirclePreview(ctx, it) } }
        }

        item {
            SwitchRow(
                showAppLinePreview,
                stringResource(R.string.show_app_line_preview),
            ) { scope.launch { UiSettingsStore.setShowLinePreview(ctx, it) } }
        }

        item {
            SwitchRow(
                showAppAnglePreview,
                stringResource(
                    R.string.show_app_angle_preview,
                    if (!showAppAnglePreview) stringResource(R.string.do_you_hate_it) else ""
                ),
            ) { scope.launch { UiSettingsStore.setShowAnglePreview(ctx, it) } }
        }

        item {
            SwitchRow(
                showAppPreviewIconCenterStartPosition,
                stringResource(R.string.show_app_icon_angle_preview_in_center_of_start_dragging_position),
            ) { scope.launch { UiSettingsStore.setShowAppPreviewIconCenterStartPosition(ctx, it) } }
        }

        item {
            SwitchRow(
                linePreviewSnapToAction,
                stringResource(R.string.line_preview_snap_to_action),
            ) { scope.launch { UiSettingsStore.setLinePreviewSnapToAction(ctx, it) } }
        }


        item {
            SliderWithLabel(
                label = stringResource(R.string.min_dist_to_activate_action),
                value = minAngleFromAPointToActivateIt,
                showValue = true,
                valueRange = 0..360,
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
    }

    if (isDraggingAppPreviewOverlays) {
        AppPreviewTitle(
            offsetY = offsetY,
            alpha = alpha,
            pointIcons = icons,
            point = dummySwipePoint(SwipeActionSerializable.OpenDragonLauncherSettings).copy(
                customName = "Preview",
                id = icons.keys.random() // Kinda funny so I'll keep it :)
            ),
            topPadding = appLabelIconOverlayTopPadding.dp,
            labelSize = appLabelOverlaySize,
            iconSize = appIconOverlaySize,
            showLabel = showLaunchingAppLabel,
            showIcon = showLaunchingAppIcon
        )
    }
}
