package org.elnix.dragonlauncher.ui.settings.customization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.TAG
import org.elnix.dragonlauncher.common.navigaton.NavigationRoute
import org.elnix.dragonlauncher.common.serializables.MainScreenLayer
import org.elnix.dragonlauncher.common.serializables.MainScreenLayerJson
import org.elnix.dragonlauncher.common.serializables.SwipeActionSerializable
import org.elnix.dragonlauncher.common.serializables.SwipePointSerializable.Companion.dummySwipePoint
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.settings.stores.UiSettingsStore
import org.elnix.dragonlauncher.settings.stores.UiSettingsStore.appIconOverlaySize
import org.elnix.dragonlauncher.settings.stores.UiSettingsStore.appLabelIconOverlayTopPadding
import org.elnix.dragonlauncher.settings.stores.UiSettingsStore.appLabelOverlaySize
import org.elnix.dragonlauncher.settings.stores.UiSettingsStore.showLaunchingAppIcon
import org.elnix.dragonlauncher.settings.stores.UiSettingsStore.showLaunchingAppLabel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.components.AppPreviewTitle
import org.elnix.dragonlauncher.ui.composition.LocalDrawerIconsCache
import org.elnix.dragonlauncher.ui.composition.LocalMainScreenLayers
import org.elnix.dragonlauncher.ui.dragon.components.DragonColumnGroup
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSection
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSectionMode
import org.elnix.dragonlauncher.ui.dragon.expandable.rememberExpandableSection
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsSlider
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsSwitchRow
import org.elnix.dragonlauncher.ui.dragon.text.TextDivider
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.statusbar.showChargingAnimation


@Composable
fun AppearanceTab(
    onNavigate: (NavigationRoute) -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val icons = LocalDrawerIconsCache.current
    val scope = rememberCoroutineScope()

    // Top overlay things
    val showLaunchingAppLabel by showLaunchingAppLabel.asState()
    val showLaunchingAppIcon by showLaunchingAppIcon.asState()
    val appLabelIconOverlayTopPadding by appLabelIconOverlayTopPadding.asState()
    val appLabelOverlaySize by appLabelOverlaySize.asState()
    val appIconOverlaySize by appIconOverlaySize.asState()
    val showAllActionsOnCurrentCircle by UiSettingsStore.showAllActionsOnCurrentCircle.asState()


    val mainScreenLayers = LocalMainScreenLayers.current

    val topOverlaySettingsState = rememberExpandableSection(stringResource(R.string.app_preview_settings), mode = ExpandableSectionMode.Expandable)
    val draggingDisplayState = rememberExpandableSection(stringResource(R.string.dragging_display))

    var demoIcon by remember(topOverlaySettingsState.isExpanded()) {
        mutableStateOf(icons.getRandom())
    }

    SettingsScaffold(
        title = stringResource(R.string.appearance),
        onBack = onBack,
        helpText = stringResource(R.string.appearance_tab_text),
        onReset = {
            scope.launch {
                UiSettingsStore.resetAll(ctx)
            }
        }
    ) {
        SettingsItem(
            title = stringResource(R.string.color_selector),
            icon = R.drawable.palette
        ) {
            onNavigate(NavigationRoute.Colors)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            SettingsItem(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.wallpaper),
                icon = R.drawable.wallpaper
            ) {
                onNavigate(NavigationRoute.Wallpaper)
            }

            SettingsItem(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.widgets),
                icon = R.drawable.widgets
            ) {
                onNavigate(NavigationRoute.Widgets())
            }
        }

        SettingsItem(
            title = stringResource(R.string.icon_pack),
            icon = R.drawable.palette
        ) {
            onNavigate(NavigationRoute.IconPack)
        }

        SettingsItem(
            title = stringResource(R.string.status_bar),
            icon = R.drawable.android_cell_5
        ) {
            onNavigate(NavigationRoute.StatusBar)
        }

        SettingsItem(
            title = stringResource(R.string.theme_selector),
            icon = R.drawable.style
        ) {
            onNavigate(NavigationRoute.Theme)
        }

        SettingsItem(
            title = stringResource(R.string.font_selector),
            description = stringResource(R.string.font_selector_desc),
            icon = R.drawable.text_fields_alt
        ) {
            onNavigate(NavigationRoute.Fonts)
        }

        SettingsItem(
            title = stringResource(R.string.angle_line),
            icon = R.drawable.polyline
        ) {
            onNavigate(NavigationRoute.AngleLineEdit)
        }

        SettingsItem(
            title = stringResource(R.string.hold_settings),
            icon = R.drawable.shape_line
        ) {
            onNavigate(NavigationRoute.HoldToActivateArc)
        }

        SettingsItem(
            title = stringResource(R.string.main_screen_layers),
            icon = R.drawable.layers
        ) {
            onNavigate(NavigationRoute.MainScreenLayers)
        }
        TextDivider(stringResource(R.string.app_display))



        SettingsSwitchRow(
            setting = UiSettingsStore.fullScreen,
            title = stringResource(R.string.fullscreen_app),
            description = stringResource(R.string.fullscreen_description)
        )

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

        ExpandableSection(topOverlaySettingsState) {

            SettingsSwitchRow(
                setting = UiSettingsStore.showLaunchingAppLabel,
                title = stringResource(R.string.show_launching_app_label),
                description = stringResource(R.string.show_launching_app_label_description)
            )

            SettingsSwitchRow(
                setting = UiSettingsStore.showLaunchingAppIcon,
                title = stringResource(R.string.show_launching_app_icon),
                description = stringResource(R.string.show_launching_app_icon_description)
            )

            SettingsSlider(
                setting = UiSettingsStore.appLabelIconOverlayTopPadding,
                title = stringResource(R.string.app_label_icon_overlay_top_padding),
                valueRange = 0..1000,
                color = MaterialTheme.colorScheme.primary
            )

            SettingsSlider(
                setting = UiSettingsStore.appLabelOverlaySize,
                title = stringResource(R.string.app_label_overlay_size),
                valueRange = 0..100,
                color = MaterialTheme.colorScheme.primary
            )

            SettingsSlider(
                setting = UiSettingsStore.appIconOverlaySize,
                title = stringResource(R.string.app_icon_overlay_size),
                valueRange = 0..400,
                color = MaterialTheme.colorScheme.primary
            )
        }

        ExpandableSection(draggingDisplayState) {
            SettingsSwitchRow(
                setting = UiSettingsStore.showAppLaunchingPreview,
                title = stringResource(R.string.show_app_launch_preview),
                description = stringResource(R.string.show_app_launch_preview_description)
            )

            SettingsSwitchRow(
                setting = UiSettingsStore.showCirclePreview,
                title = stringResource(R.string.show_app_circle_preview),
                description = stringResource(R.string.show_app_circle_preview_description)
            )

            SettingsSwitchRow(
                setting = UiSettingsStore.showAllActionsOnCurrentCircle,
                title = stringResource(R.string.show_all_actions_on_current_circle),
                description = stringResource(R.string.show_all_actions_on_current_circle_description)
            ) {
                if (!it) {
                    scope.launch {
                        UiSettingsStore.showAllActionsOnCurrentNest.set(ctx, false)
                    }
                }
            }

            SettingsSwitchRow(
                setting = UiSettingsStore.showAllActionsOnCurrentNest,
                enabled = showAllActionsOnCurrentCircle,
                title = stringResource(R.string.show_all_actions_on_current_nest),
                description = stringResource(R.string.show_all_actions_on_current_nest_desc)
            )

            SettingsSwitchRow(
                setting = UiSettingsStore.showAppPreviewIconCenterStartPosition,
                title = stringResource(R.string.show_app_icon_start_drag_position),
                description = stringResource(R.string.show_app_icon_start_drag_position_description)
            )

            /* If the line is rgb (computed via the angle) or uses the line color from settings */
            SettingsSwitchRow(
                setting = UiSettingsStore.rgbLine,
                title = stringResource(R.string.rgb_line_selector),
                description = stringResource(R.string.rgb_line_selector_description)
            )

            SettingsSwitchRow(
                setting = UiSettingsStore.linePreviewSnapToAction,
                title = stringResource(R.string.line_preview_snap_to_action),
                description = stringResource(R.string.line_preview_snap_to_action_description)
            )

            SettingsSwitchRow(
                setting = UiSettingsStore.multiplyOrSubtractOpacityInLiveNests,
                title = stringResource(R.string.multiply_or_subtract_opacity_in_live_nests),
                description = stringResource(R.string.multiply_or_subtract_opacity_in_live_nests_desc)
            )
        }

        DragonColumnGroup {
            SettingsSlider(
                setting = UiSettingsStore.maxNestsDepth,
                title = stringResource(R.string.depth),
                description = stringResource(R.string.depth_desc),
                valueRange = 1..10
            )

            SettingsSlider(
                setting = UiSettingsStore.maxLiveNestsDepth,
                title = stringResource(R.string.live_nest_depth),
                description = stringResource(R.string.live_nests_depth_desc),
                valueRange = 1..10
            )
        }
    }

    if (topOverlaySettingsState.isExpanded()) {
        logD(TAG) { "App preview shown " }
        AppPreviewTitle(
            point = dummySwipePoint(SwipeActionSerializable.OpenRecentApps).copy(
                customName = "Preview",
                id = demoIcon?.cacheKey ?: ""
            ),
            topPadding = appLabelIconOverlayTopPadding.dp,
            labelSize = appLabelOverlaySize,
            iconSize = appIconOverlaySize,
            showLabel = showLaunchingAppLabel,
            showIcon = showLaunchingAppIcon
        )
    }
}
