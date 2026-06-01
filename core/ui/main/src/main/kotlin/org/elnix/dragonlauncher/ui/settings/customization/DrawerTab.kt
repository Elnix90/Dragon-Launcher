@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.settings.customization

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.util.ColorUtils.semiTransparentIfDisabled
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.base.Constants.Logging.DRAWER_TAG
import org.elnix.dragonlauncher.base.Constants.Logging.SHAPES_TAG
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.models.AppsViewModel
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.ui.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.modifiers.settingsGroupHorizontalPadding
import org.elnix.dragonlauncher.ui.dialogs.DrawerToolbarsOrderDialog
import org.elnix.dragonlauncher.ui.dialogs.ShapePickerDialog
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSection
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSectionMode
import org.elnix.dragonlauncher.ui.dragon.expandable.rememberExpandableSection
import org.elnix.dragonlauncher.ui.dragon.settings.DrawerActionSelector
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsSlider
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsSwitchRow
import org.elnix.dragonlauncher.ui.helpers.GridSizeSlider
import org.elnix.dragonlauncher.ui.helpers.ShapeRow
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold


@Composable
fun DrawerTab(
    onBack: () -> Unit,
    appsViewModel: AppsViewModel = activityViewModel()
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val apps by appsViewModel.userApps.collectAsState(initial = emptyList())

    val leftDrawerAction by DrawerSettingsStore.leftDrawerAction.asState()

    val rightDrawerAction by DrawerSettingsStore.rightDrawerAction.asState()

    val leftDrawerWidth by DrawerSettingsStore.leftDrawerWidth.asState()
    val rightDrawerWidth by DrawerSettingsStore.rightDrawerWidth.asState()

    val iconsShape by DrawerSettingsStore.iconsShape.asState()

    val drawerCategorySettingsState = rememberExpandableSection(stringResource(R.string.category_settings))
    val drawerNormalSettingsState = rememberExpandableSection(stringResource(R.string.grid_settings))
    val actionsSettingsState = rememberExpandableSection(
        title = stringResource(R.string.action_settings),
        mode = ExpandableSectionMode.ModalSheet(true)
    )


    val autoLaunchSingleMatch by DrawerSettingsStore.autoOpenSingleMatch.asState()
    val showRecentlyUsed by DrawerSettingsStore.showRecentlyUsedApps.asState()
    val useCategory by DrawerSettingsStore.useCategory.asState()

    var totalWidthPx by remember { mutableFloatStateOf(0f) }

    var leftWeight by remember { mutableFloatStateOf(leftDrawerWidth) }
    var rightWeight by remember { mutableFloatStateOf(rightDrawerWidth) }

    var showToolbarsOrderDialog by remember { mutableStateOf(false) }

    LaunchedEffect(leftDrawerWidth, rightDrawerWidth) {
        leftWeight = leftDrawerWidth
        rightWeight = rightDrawerWidth
    }

    val leftActionNotNone = leftDrawerAction != DrawerActions.NONE
    val rightActionNotNone = rightDrawerAction != DrawerActions.NONE

    val leftActionNotDisabled = leftDrawerAction != DrawerActions.DISABLED
    val rightActionNotDisabled = rightDrawerAction != DrawerActions.DISABLED

    var showShapePickerDialog by remember { mutableStateOf(false) }

    SettingsScaffold(
        title = stringResource(R.string.app_drawer),
        onBack = onBack,
        helpText = stringResource(R.string.drawer_tab_text),
        onReset = {
            scope.launch {
                DrawerSettingsStore.resetAll(ctx)
            }
        }
    ) {

        DragonSettingsGroup(R.string.behavior) {
            SettingsSwitchRow(
                setting = DrawerSettingsStore.autoOpenSingleMatch,
                title = stringResource(R.string.auto_launch_single_match),
                description = stringResource(R.string.auto_launch_single_match_desc),
            )

            AnimatedVisibility(autoLaunchSingleMatch) {
                SettingsSwitchRow(
                    setting = DrawerSettingsStore.disableAutoLaunchOnSpaceFirstChar,
                    title = stringResource(R.string.disable_auto_launch_on_space_first_char),
                    description = stringResource(R.string.disable_auto_launch_on_space_first_char_desc),
                )
            }

            SettingsSwitchRow(
                setting = DrawerSettingsStore.autoShowKeyboardOnDrawer,
                title = stringResource(R.string.auto_show_keyboard),
                description = stringResource(R.string.auto_show_keyboard_desc),
            )
        }

        DragonSettingsGroup(R.string.drawer_pull_down_settings) {
            SettingsSwitchRow(
                setting = DrawerSettingsStore.pullDownAnimations,
                title = stringResource(R.string.pull_down_animations),
                description = stringResource(R.string.pull_down_animations_desc)
            )

            SettingsSwitchRow(
                setting = DrawerSettingsStore.pullDownWallPaperDimFade,
                title = stringResource(R.string.pull_down_wallpaper_dim),
                description = stringResource(R.string.pull_down_wallpaper_dim_desc)
            )

            SettingsSwitchRow(
                setting = DrawerSettingsStore.pullDownScaleIn,
                title = stringResource(R.string.pull_down_scale_in),
                description = stringResource(R.string.pull_down_scale_in_desc)
            )

//                SettingsSwitchRow(
//                    setting = DrawerSettingsStore.pullDownIconFade,
//                    enabled = false,
//                    title = stringResource(R.string.pull_down_icon_fade),
//                    description = stringResource(R.string.pull_down_icon_fade_desc)
//                )
        }



        DragonSettingsGroup(R.string.recently_used_apps) {
            SettingsSwitchRow(
                setting = DrawerSettingsStore.showRecentlyUsedApps,
                title = stringResource(R.string.show_recently_used_apps),
                description = stringResource(R.string.show_recently_used_apps_desc),
            )

            AnimatedVisibility(showRecentlyUsed) {
                SettingsSlider(
                    setting = DrawerSettingsStore.recentlyUsedAppsCount,
                    title = stringResource(R.string.recently_used_apps_count),
                    valueRange = 1..20,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .settingsGroupHorizontalPadding()
                        .padding(bottom = 12.dp)
                )
            }
        }

        DragonSettingsGroup(R.string.appearance) {
            SettingsItem(
                title = stringResource(R.string.toolbars_order),
                icon = R.drawable._123
            ) { showToolbarsOrderDialog = true }

            SettingsSwitchRow(
                setting = DrawerSettingsStore.showAppIconsInDrawer,
                title = stringResource(R.string.show_app_icons_in_drawer),
                description = stringResource(R.string.show_app_icons_in_drawer_desc)
            )

            SettingsSwitchRow(
                setting = DrawerSettingsStore.showAppLabelInDrawer,
                title = stringResource(R.string.show_app_labels_in_drawer),
                description = stringResource(R.string.show_app_labels_in_drawer_desc)
            )

            ExpandableSection(drawerCategorySettingsState) {
                SettingsSwitchRow(
                    setting = DrawerSettingsStore.useCategory,
                    title = stringResource(R.string.use_categories),
                    description = stringResource(R.string.use_categories_desc)
                )

                SettingsSwitchRow(
                    setting = DrawerSettingsStore.showCategoryName,
                    title = stringResource(R.string.show_category_name),
                    description = stringResource(R.string.show_category_name_desc),
                    enabled = useCategory,
                )

                SettingsSlider(
                    setting = DrawerSettingsStore.categoryGridWidth,
                    title = stringResource(R.string.category_grid_width),
                    valueRange = 1..4
                )

                SettingsSlider(
                    setting = DrawerSettingsStore.categoryGridCells,
                    title = stringResource(R.string.category_cells),
                    description = stringResource(R.string.category_cells),
                    valueRange = 2..5
                )
            }

            ExpandableSection(drawerNormalSettingsState) {
                SettingsSlider(
                    setting = DrawerSettingsStore.maxIconSize,
                    description = stringResource(R.string.max_icon_size_desc),
                    title = stringResource(R.string.max_icon_size),
                    valueRange = 0..200
                )

                SettingsSlider(
                    setting = DrawerSettingsStore.iconsSpacingHorizontal,
                    title = stringResource(R.string.icons_spacing_horizontal),
                    description = stringResource(R.string.icons_spacing_horizontal_desc),
                    valueRange = 0..50
                )

                SettingsSlider(
                    setting = DrawerSettingsStore.iconsSpacingVertical,
                    title = stringResource(R.string.icons_spacing_vertical),
                    description = stringResource(R.string.icons_spacing_vertical_desc),
                    valueRange = 0..50
                )
            }

            ShapeRow(
                selected = iconsShape,
                onReset = { scope.launch { DrawerSettingsStore.iconsShape.reset(ctx) } }
            ) { showShapePickerDialog = true }
        }

        GridSizeSlider(apps)

        DragonSettingsGroup(R.string.drawer_actions) {

            ExpandableSection(actionsSettingsState) {
                DrawerActionSelector(
                    settingObject = DrawerSettingsStore.leftDrawerAction,
                    label = stringResource(R.string.left_drawer_action),
                    allowNone = true
                )

                DrawerActionSelector(
                    settingObject = DrawerSettingsStore.rightDrawerAction,
                    label = stringResource(R.string.right_drawer_action),
                    allowNone = true
                )

                DrawerActionSelector(
                    settingObject = DrawerSettingsStore.scrollUpDrawerAction,
                    label = stringResource(R.string.scroll_up_action),
                )

                DrawerActionSelector(
                    settingObject = DrawerSettingsStore.scrollDownDrawerAction,
                    label = stringResource(R.string.scroll_down_action),
                )

                DrawerActionSelector(
                    settingObject = DrawerSettingsStore.tapEmptySpaceAction,
                    label = stringResource(R.string.tap_empty_space_action),
                )

                DrawerActionSelector(
                    settingObject = DrawerSettingsStore.backDrawerAction,
                    label = stringResource(R.string.back_action),
                )

                DrawerActionSelector(
                    settingObject = DrawerSettingsStore.drawerEnterAction,
                    label = stringResource(R.string.drawer_enter_key_action),
                )

                DrawerActionSelector(
                    settingObject = DrawerSettingsStore.drawerHomeAction,
                    label = stringResource(R.string.drawer_home_action),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .settingsGroupHorizontalPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(stringResource(R.string.drawer_actions_width))
                DragonIconButton(
                    icon = R.drawable.reset,
                    contentDescription = stringResource(R.string.reset)
                ) {
                    scope.launch {
                        DrawerSettingsStore.leftDrawerWidth.reset(ctx)
                        DrawerSettingsStore.rightDrawerWidth.reset(ctx)
                    }
                }
            }

            if (leftActionNotNone || rightActionNotNone) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .onGloballyPositioned {
                            totalWidthPx = it.size.width.toFloat()
                        },
                    horizontalArrangement = Arrangement.Center
                ) {

                    if (leftActionNotDisabled) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(leftWeight.coerceIn(0.001f, 1f))
                                .background(MaterialTheme.colorScheme.primary.semiTransparentIfDisabled(leftActionNotNone)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (leftActionNotNone) {
                                Icon(
                                    painter = painterResource(leftDrawerAction.iconEnabled),
                                    contentDescription = stringResource(R.string.left_drawer_action),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                        DragHandle(
                            onDrag = { dx ->
                                if (totalWidthPx > 0f) {
                                    leftWeight = (leftWeight + dx / totalWidthPx).coerceIn(0.001f, 1f)
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    DrawerSettingsStore.leftDrawerWidth.set(ctx, leftWeight)
                                }
                            }
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    if (rightActionNotDisabled) {
                        DragHandle(
                            onDrag = { dx ->
                                if (totalWidthPx > 0f) {
                                    rightWeight = (rightWeight - dx / totalWidthPx).coerceIn(0.001f, 1f)
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    DrawerSettingsStore.rightDrawerWidth.set(ctx, rightWeight)
                                }
                            }
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(rightWeight.coerceIn(0.001f, 1f))
                                .background(MaterialTheme.colorScheme.primary.semiTransparentIfDisabled(rightActionNotNone)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (rightActionNotNone) {
                                Icon(
                                    painter = painterResource(rightDrawerAction.iconEnabled),
                                    contentDescription = stringResource(R.string.right_drawer_action),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showShapePickerDialog) {
        ShapePickerDialog(
            selected = iconsShape,
            onDismiss = { showShapePickerDialog = false }
        ) {
            logD(SHAPES_TAG) { "Picked: $it" }
            scope.launch {
                DrawerSettingsStore.iconsShape.set(ctx, it)
            }
        }
    }


    if (showToolbarsOrderDialog) {
        DrawerToolbarsOrderDialog(
            onDismiss = { showToolbarsOrderDialog = false }
        ) { newList ->

            logD(DRAWER_TAG) { "Saving newList: $newList, encoded = ${newList.joinToString(",") { it.toString() }}" }

            scope.launch {
                DrawerSettingsStore.toolbarsOrder.set(ctx, newList.joinToString(",") { it.toString() })
            }

            showToolbarsOrderDialog = false
        }
    }
}

@Composable
private fun DragHandle(
    onDrag: (dx: Float) -> Unit,
    onDragEnd: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(6.dp)
            .background(MaterialTheme.colorScheme.outline)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, drag ->
                        change.consume()
                        onDrag(drag.x)
                    },
                    onDragEnd = onDragEnd
                )
            }
    )
}
