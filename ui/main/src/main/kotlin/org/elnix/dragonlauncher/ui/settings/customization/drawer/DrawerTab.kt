package org.elnix.dragonlauncher.ui.settings.customization.drawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.Companion.notDisabled
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.Companion.notNone
import org.elnix.dragonlauncher.enumsui.toggle.HorizontalAlignment
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.animation.easingSpec
import org.elnix.dragonlauncher.ui.base.modifiers.semiTransparentIfDisabled
import org.elnix.dragonlauncher.ui.dialogs.ToolbarsOrderDialog
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.ResetIcon
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.dragon.settings.DrawerActionSelector
import org.elnix.dragonlauncher.ui.dragon.settings.Setting
import org.elnix.dragonlauncher.ui.helpers.settings.RouteItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.helpers.workspace.AppGrid
import kotlin.time.Duration.Companion.milliseconds


@Composable
fun DrawerTab(drawerViewModel: DrawerViewModel = activityViewModel()) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val apps by drawerViewModel.userApps.collectAsState()

    val leftDrawerAction by DrawerSettingsStore.leftDrawerAction.asState()
    val rightDrawerAction by DrawerSettingsStore.rightDrawerAction.asState()
    val leftDrawerWidth by DrawerSettingsStore.leftDrawerWidth.asState()
    val rightDrawerWidth by DrawerSettingsStore.rightDrawerWidth.asState()

    val autoOpenSingleMatch by DrawerSettingsStore.autoOpenSingleMatch.asState()
    val showRecentlyUsed by DrawerSettingsStore.showRecentlyUsedApps.asState()
    val useCategory by DrawerSettingsStore.useCategory.asState()


    var leftWidth by remember { mutableStateOf(leftDrawerWidth) }
    var rightWidth by remember { mutableStateOf(rightDrawerWidth) }

    LaunchedEffect(leftDrawerWidth, rightDrawerWidth) {
        leftWidth = leftDrawerWidth
        rightWidth = rightDrawerWidth
    }

    var showToolbarsOrderDialog by remember { mutableStateOf(false) }


    // Animate the sroll to the bottom drawer action width when user enables them, otherwise it could be missed
    val scrollState = rememberScrollState()
    var isFirstLaunch by remember { mutableStateOf(true) }
    LaunchedEffect(leftDrawerAction, rightDrawerAction) {
        if (leftDrawerAction.notDisabled || rightDrawerAction.notDisabled) {
            delay(100.milliseconds)
            if (!isFirstLaunch) {
                scrollState.animateScrollBy(
                    1000f,
                    animationSpec = easingSpec()
                )
            } else {
                isFirstLaunch = false
            }
        }
    }

    SettingsScaffold(
        title = stringResource(R.string.app_drawer),
        helpText = stringResource(R.string.drawer_tab_text),
        resetText = stringResource(R.string.reset_drawer),
        scrollState = scrollState,
        onReset = {
            scope.launch {
                DrawerSettingsStore.resetAll(ctx)
            }
        }
    ) {

        DragonSettingsGroup(R.string.workspaces) {
            RouteItem(NavigationRoute.Workspace)
            RouteItem(NavigationRoute.Icons)
        }

        DragonSettingsGroup(R.string.behavior) {
            Setting(DrawerSettingsStore.autoOpenSingleMatch)
            Setting(DrawerSettingsStore.disableAutoLaunchWhenFirstCharIs, enabled = autoOpenSingleMatch)
            Setting(DrawerSettingsStore.searchAllWorkspacesOnlyWhenFirstCharIs)
            Setting(DrawerSettingsStore.autoShowKeyboardOnDrawer)
            Setting(DrawerSettingsStore.autoAskToUnlockProfile)
        }

        DragonSettingsGroup(R.string.drawer_pull_down_settings) {
            Setting(DrawerSettingsStore.pullDownAnimations)
            Setting(DrawerSettingsStore.pullDownWallPaperDim)
            Setting(DrawerSettingsStore.pullDownScaleIn)
        }

        DragonSettingsGroup(R.string.recently_used_apps) {
            Setting(DrawerSettingsStore.showRecentlyUsedApps)

            AnimatedVisibility(showRecentlyUsed) {
                Setting(DrawerSettingsStore.recentlyUsedAppsCount)
            }
        }

        DragonSettingsGroup(R.string.appearance) {
            SettingsItem(
                title = stringResource(R.string.toolbars_order),
                icon = R.drawable._123
            ) { showToolbarsOrderDialog = true }

            Setting(DrawerSettingsStore.showAppIconsInDrawer)
            Setting(DrawerSettingsStore.showAppLabelsInDrawer)
            Setting(DrawerSettingsStore.labelTextColor)

            Setting(DrawerSettingsStore.useCategory)
            Setting(DrawerSettingsStore.showCategoryName, enabled = useCategory)
            Setting(DrawerSettingsStore.categoryGridCells)

            DrawerIconShapePicker()
        }

        DragonSettingsGroup(R.string.grid_settings) {
            Setting(DrawerSettingsStore.gridSize)
            Setting(DrawerSettingsStore.iconSize) { value ->
                if (value == 0.dp) {
                    stringResource(R.string.not_showed)
                } else null
            }
            Setting(DrawerSettingsStore.iconsSpacingHorizontal)
            Setting(DrawerSettingsStore.iconsSpacingVertical)

            val gridSize by DrawerSettingsStore.gridSize.asState()
            val horizontalAlignment by DrawerSettingsStore.horizontalAlignment.asState()

            AnimatedVisibility(gridSize == 1) {
                MultiSelectConnectedButtonRow(
                    entries = HorizontalAlignment.entries,
                    checked = { horizontalAlignment == it }
                ) {
                    scope.launch { DrawerSettingsStore.horizontalAlignment.set(ctx, it) }
                }
            }

            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(MaterialTheme.shapes.large)
                    .border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.large)
            ) {
                AppGrid(
                    apps = apps.shuffled().take(if (gridSize == 1) 3 else gridSize * 2),
                    longPressPopup = false,
                    onClick = null
                )
            }
        }

        DragonSettingsGroup(R.string.drawer_actions) {
            DrawerActionSelector(DrawerSettingsStore.drawerScrollUpAction)
            DrawerActionSelector(DrawerSettingsStore.drawerScrollDownAction)
            DrawerActionSelector(DrawerSettingsStore.tapEmptySpaceAction)
            DrawerActionSelector(DrawerSettingsStore.drawerBackAction)
            DrawerActionSelector(DrawerSettingsStore.drawerEnterAction)
            DrawerActionSelector(DrawerSettingsStore.drawerHomeAction)
        }

        DragonSettingsGroup(R.string.width_actions_settings) {
            DrawerActionSelector(DrawerSettingsStore.leftDrawerAction, allowNone = true)
            DrawerActionSelector(DrawerSettingsStore.rightDrawerAction, allowNone = true)
        }


        AnimatedVisibility(leftDrawerAction.notDisabled || rightDrawerAction.notDisabled) {
            DragonSettingsGroup(
                R.string.drawer_actions_width,
                trailingIcon = {
                    ResetIcon(leftWidth != DrawerSettingsStore.leftDrawerWidth.default || rightWidth != DrawerSettingsStore.rightDrawerWidth.default) {
                        scope.launch {
                            DrawerSettingsStore.leftDrawerWidth.reset(ctx)
                            DrawerSettingsStore.rightDrawerWidth.reset(ctx)
                        }
                    }
                }) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (leftDrawerAction.notDisabled) {

                        Row(
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(leftWidth)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .semiTransparentIfDisabled(leftDrawerAction.notNone),
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
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            DragHandle()
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(rightWidth)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .semiTransparentIfDisabled(rightDrawerAction.notNone),
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
                    value = leftWidth,
                    valueRange = DrawerSettingsStore.leftDrawerWidth.allowedRange,
                    enabled = leftDrawerAction.notDisabled,
                    resetEnabled = leftWidth != DrawerSettingsStore.leftDrawerWidth.default,
                    onReset = {
                        leftWidth = leftDrawerWidth
                        scope.launch {
                            DrawerSettingsStore.leftDrawerWidth.reset(ctx)
                        }
                    },
                    onDragStateChange = { isDragging ->
                        if (!isDragging) {
                            scope.launch {
                                DrawerSettingsStore.leftDrawerWidth.set(ctx, leftWidth)
                            }
                        }
                    }
                ) { leftWidth = it }

                SliderWithLabel(
                    label = stringResource(R.string.right_drawer_width),
                    value = rightWidth,
                    valueRange = DrawerSettingsStore.rightDrawerWidth.allowedRange,
                    enabled = rightDrawerAction.notDisabled,
                    resetEnabled = rightWidth != DrawerSettingsStore.rightDrawerWidth.default,
                    onReset = {
                        rightWidth = rightDrawerWidth
                        scope.launch {
                            DrawerSettingsStore.rightDrawerWidth.reset(ctx)
                        }
                    },
                    onDragStateChange = { isDragging ->
                        if (!isDragging) {
                            scope.launch {
                                DrawerSettingsStore.rightDrawerWidth.set(ctx, rightWidth)
                            }
                        }
                    }
                ) { rightWidth = it }
            }
        }

    }

    if (showToolbarsOrderDialog) {
        ToolbarsOrderDialog { showToolbarsOrderDialog = false }
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
