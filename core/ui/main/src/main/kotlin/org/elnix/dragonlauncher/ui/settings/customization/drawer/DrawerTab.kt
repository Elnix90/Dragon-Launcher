@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.settings.customization.drawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute
import org.elnix.dragonlauncher.base.util.ColorUtils.semiTransparentIfDisabled
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.Companion.notDisabled
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.Companion.notNone
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.modifiers.settingsGroupHorizontalPadding
import org.elnix.dragonlauncher.ui.dialogs.DrawerToolbarsOrderDialog
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSection
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSectionMode
import org.elnix.dragonlauncher.ui.dragon.expandable.rememberExpandableSection
import org.elnix.dragonlauncher.ui.dragon.settings.DrawerActionSelector
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsSlider
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsSwitchRow
import org.elnix.dragonlauncher.ui.dragon.settings.toIntRange
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold


@Composable
public fun DrawerTab(
    onBack: () -> Unit,
    onNavigate: (NavigationRoute) -> Unit,
    drawerViewModel: DrawerViewModel = activityViewModel()
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val apps by drawerViewModel.userApps.collectAsState()

    val leftDrawerAction by DrawerSettingsStore.leftDrawerAction.asState()
    val rightDrawerAction by DrawerSettingsStore.rightDrawerAction.asState()
    val leftDrawerWidth by DrawerSettingsStore.leftDrawerWidth.asState()
    val rightDrawerWidth by DrawerSettingsStore.rightDrawerWidth.asState()

    val drawerCategorySettingsState = rememberExpandableSection(stringResource(R.string.category_settings))
    val drawerNormalSettingsState = rememberExpandableSection(stringResource(R.string.grid_settings))
    val actionsSettingsState = rememberExpandableSection(
        title = stringResource(R.string.action_settings),
        mode = ExpandableSectionMode.ModalSheet(true)
    )

    val autoLaunchSingleMatch by DrawerSettingsStore.autoOpenSingleMatch.asState()
    val showRecentlyUsed by DrawerSettingsStore.showRecentlyUsedApps.asState()
    val useCategory by DrawerSettingsStore.useCategory.asState()


    var leftWidth by remember { mutableStateOf(leftDrawerWidth) }
    var rightWidth by remember { mutableStateOf(rightDrawerWidth) }

    LaunchedEffect(leftDrawerWidth, rightDrawerWidth) {
        leftWidth = leftDrawerWidth
        rightWidth = rightDrawerWidth
    }

    var showToolbarsOrderDialog by remember { mutableStateOf(false) }



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

        DragonSettingsGroup(R.string.workspaces) {
            SettingsItem(
                title = stringResource(R.string.workspaces),
                icon = R.drawable.workspaces
            ) { onNavigate(NavigationRoute.Workspace) }

            SettingsItem(
                title = stringResource(R.string.icon_pack),
                icon = R.drawable.palette
            ) { onNavigate(NavigationRoute.IconPack) }

        }

        DragonSettingsGroup(R.string.behavior) {
            SettingsSwitchRow(DrawerSettingsStore.autoOpenSingleMatch)

            AnimatedVisibility(autoLaunchSingleMatch) {
                SettingsSwitchRow(DrawerSettingsStore.disableAutoLaunchOnSpaceFirstChar)
            }

            SettingsSwitchRow(DrawerSettingsStore.autoShowKeyboardOnDrawer)
        }

        DragonSettingsGroup(R.string.drawer_pull_down_settings) {
            SettingsSwitchRow(DrawerSettingsStore.pullDownAnimations)
            SettingsSwitchRow(DrawerSettingsStore.pullDownWallPaperDim)
            SettingsSwitchRow(DrawerSettingsStore.pullDownScaleIn)
        }



        DragonSettingsGroup(R.string.recently_used_apps) {
            SettingsSwitchRow(DrawerSettingsStore.showRecentlyUsedApps)

            AnimatedVisibility(showRecentlyUsed) {
                SettingsSlider(
                    setting = DrawerSettingsStore.recentlyUsedAppsCount,
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

            SettingsSwitchRow(DrawerSettingsStore.showAppIconsInDrawer)

            SettingsSwitchRow(DrawerSettingsStore.showAppLabelInDrawer)

            ExpandableSection(drawerCategorySettingsState) {
                SettingsSwitchRow(DrawerSettingsStore.useCategory)
                SettingsSwitchRow(DrawerSettingsStore.showCategoryName, enabled = useCategory)
                SettingsSlider(DrawerSettingsStore.categoryGridCells)
            }

            ExpandableSection(drawerNormalSettingsState) {
                SettingsSlider(DrawerSettingsStore.iconSize)
                SettingsSlider(DrawerSettingsStore.iconsSpacingHorizontal)
                SettingsSlider(DrawerSettingsStore.iconsSpacingVertical)
            }

            DrawerIconShapePicker()
        }

        GridSizeSlider(apps)

        DragonSettingsGroup(R.string.drawer_actions) {

            ExpandableSection(actionsSettingsState) {
                DrawerActionSelector(DrawerSettingsStore.leftDrawerAction, allowNone = true)
                DrawerActionSelector(DrawerSettingsStore.rightDrawerAction, allowNone = true)
                DrawerActionSelector(DrawerSettingsStore.scrollUpDrawerAction)
                DrawerActionSelector(DrawerSettingsStore.scrollDownDrawerAction)
                DrawerActionSelector(DrawerSettingsStore.tapEmptySpaceAction)
                DrawerActionSelector(DrawerSettingsStore.backDrawerAction)
                DrawerActionSelector(DrawerSettingsStore.drawerEnterAction)
                DrawerActionSelector(DrawerSettingsStore.drawerHomeAction)
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

            AnimatedVisibility(leftDrawerAction.notNone || rightDrawerAction.notNone) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {

                        if (leftDrawerAction.notDisabled) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(leftWidth)
                                        .background(MaterialTheme.colorScheme.primary.semiTransparentIfDisabled(leftDrawerAction.notNone)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (leftDrawerAction.notNone) {
                                        Icon(
                                            painter = painterResource(leftDrawerAction.iconEnabled),
                                            contentDescription = stringResource(R.string.left_drawer_action),
                                            tint = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                                DragHandle()
                            }
                        }

                        if (rightDrawerAction.notDisabled) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                            ) {
                                DragHandle()
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(rightWidth)
                                        .background(MaterialTheme.colorScheme.primary.semiTransparentIfDisabled(rightDrawerAction.notNone)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (rightDrawerAction.notNone) {
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

                    SliderWithLabel(
                        label = stringResource(DrawerSettingsStore.leftDrawerWidth.title!!),
                        value = leftWidth.value.toInt(),
                        modifier = Modifier.settingsGroupHorizontalPadding(),
                        valueRange = DrawerSettingsStore.leftDrawerWidth.allowedRange.toIntRange(),
                        onDragStateChange = { isDragging ->
                            if (!isDragging) {
                                scope.launch {
                                    DrawerSettingsStore.leftDrawerWidth.set(ctx, leftWidth)
                                }
                            }
                        }
                    ) {
                        leftWidth = it.dp
                    }

                    SliderWithLabel(
                        label = stringResource(R.string.right_drawer_width),
                        value = rightWidth.value.toInt(),
                        modifier = Modifier.settingsGroupHorizontalPadding(),
                        valueRange = DrawerSettingsStore.rightDrawerWidth.allowedRange.toIntRange(),
                        onDragStateChange = { isDragging ->
                            if (!isDragging) {
                                scope.launch {
                                    DrawerSettingsStore.rightDrawerWidth.set(ctx, rightWidth)
                                }
                            }
                        }
                    ) {
                        rightWidth = it.dp
                    }
                }
            }
        }
    }

    if (showToolbarsOrderDialog) {
        DrawerToolbarsOrderDialog { showToolbarsOrderDialog = false }
    }
}

@Composable
private fun DragHandle() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(6.dp)
            .background(MaterialTheme.colorScheme.outline)
    )
}
