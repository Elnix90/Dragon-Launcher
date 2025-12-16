package org.elnix.dragonlauncher.ui.settings.appearance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.helpers.DrawerActions
import org.elnix.dragonlauncher.data.helpers.drawerActionIcon
import org.elnix.dragonlauncher.data.helpers.drawerActionsLabel
import org.elnix.dragonlauncher.data.stores.DrawerSettingsStore
import org.elnix.dragonlauncher.ui.helpers.ActionSelectorRow
import org.elnix.dragonlauncher.ui.helpers.GridSizeSlider
import org.elnix.dragonlauncher.ui.helpers.SwitchRow
import org.elnix.dragonlauncher.ui.helpers.TextDivider
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsLazyHeader
import org.elnix.dragonlauncher.utils.models.AppDrawerViewModel


@Composable
fun DrawerTab(
    appsViewModel: AppDrawerViewModel,
    onBack: () -> Unit
) {

    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val apps by appsViewModel.userApps.collectAsState(initial = emptyList())
    val icons by appsViewModel.icons.collectAsState()

    val autoLaunchSingleMatch by DrawerSettingsStore.getAutoLaunchSingleMatch(ctx)
        .collectAsState(initial = true)

    val showAppIconsInDrawer by DrawerSettingsStore.getShowAppIconsInDrawer(ctx)
        .collectAsState(initial = true)

    val showAppLabelsInDrawer by DrawerSettingsStore.getShowAppLabelsInDrawer(ctx)
        .collectAsState(initial = true)

    val autoShowKeyboardOnDrawer by DrawerSettingsStore.getAutoShowKeyboardOnDrawer(ctx)
        .collectAsState(initial = false)

    val clickEmptySpaceToRaiseKeyboard by DrawerSettingsStore.getClickEmptySpaceToRaiseKeyboard(ctx)
        .collectAsState(initial = false)


    val searchBarBottom by DrawerSettingsStore.getSearchBarBottom(ctx)
        .collectAsState(initial = true)


    val leftDrawerAction by DrawerSettingsStore.getLeftDrawerAction(ctx)
        .collectAsState(initial = DrawerActions.TOGGLE_KB)

    val rightDrawerAction by DrawerSettingsStore.getRightDrawerAction(ctx)
        .collectAsState(initial = DrawerActions.CLOSE)

    val leftDrawerWidth by DrawerSettingsStore.getLeftDrawerWidth(ctx)
        .collectAsState(initial = 0.1f)
    val rightDrawerWidth  by DrawerSettingsStore.getRightDrawerWidth(ctx)
        .collectAsState(initial = 0.1f)

    var totalWidthPx by remember { mutableFloatStateOf(0f) }

    var localLeft by remember { mutableFloatStateOf(leftDrawerWidth) }
    var localRight by remember { mutableFloatStateOf(rightDrawerWidth) }

    val leftActionNotNone = leftDrawerAction != DrawerActions.NONE
    val rightActionNotNone = rightDrawerAction != DrawerActions.NONE

    val leftActionNotDisabled = leftDrawerAction != DrawerActions.DISABLED
    val rightActionNotDisabled = rightDrawerAction != DrawerActions.DISABLED

    SettingsLazyHeader(
        title = stringResource(R.string.app_drawer),
        onBack = onBack,
        helpText = stringResource(R.string.drawer_tab_text),
        onReset = {
            scope.launch {
                DrawerSettingsStore.resetAll(ctx)
            }
        }
    ) {

        item { TextDivider(stringResource(R.string.behavior)) }

        item {
            SwitchRow(
                autoLaunchSingleMatch,
                "Auto Launch Single Match",
            ) { scope.launch { DrawerSettingsStore.setAutoLaunchSingleMatch(ctx, it) } }
        }

        item {
            SwitchRow(
                autoShowKeyboardOnDrawer,
                "Auto Show Keyboard on Drawer",
            ) { scope.launch { DrawerSettingsStore.setAutoShowKeyboardOnDrawer(ctx, it) } }
        }

        item {
            SwitchRow(
                clickEmptySpaceToRaiseKeyboard,
                "Tap Empty Space to Raise Keyboard",
            ) { scope.launch { DrawerSettingsStore.setClickEmptySpaceToRaiseKeyboard(ctx, it) } }
        }

        item {
            SwitchRow(
                searchBarBottom,
                "Search bar ${if (searchBarBottom) "Bottom" else "Top"}",
                enabled = false
            ) { scope.launch { DrawerSettingsStore.setSearchBarBottom(ctx, it) } }
        }

        item { TextDivider(stringResource(R.string.appearance)) }


        item {
            SwitchRow(
                showAppIconsInDrawer,
                "Show App Icons in Drawer",
            ) { scope.launch { DrawerSettingsStore.setShowAppIconsInDrawer(ctx, it) } }
        }

        item {
            SwitchRow(
                showAppLabelsInDrawer,
                "Show App Labels in Drawer",
            ) { scope.launch { DrawerSettingsStore.setShowAppLabelsInDrawer(ctx, it) } }
        }

        item {
            GridSizeSlider(
                apps = apps,
                icons = icons,
                showIcons = showAppIconsInDrawer,
                showLabels = showAppLabelsInDrawer
            )
        }


        item { TextDivider(stringResource(R.string.drawer_actions)) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = stringResource(R.string.drawer_actions_width),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.width(15.dp))

                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = stringResource(R.string.reset),
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .clickable {
                            localLeft = 0.1f
                            localRight = 0.1f
                            scope.launch{
                                DrawerSettingsStore.setLeftDrawerWidth(ctx, 0.1f)
                                DrawerSettingsStore.setRightDrawerWidth(ctx, 0.1f)
                            }
                        }
                )
            }
        }

        item {
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
                    // LEFT PANEL -----------------------------------------------------------
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(localLeft)
                            .background(MaterialTheme.colorScheme.primary.copy(if (leftActionNotNone) 1f else 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (leftActionNotNone) {
                            Icon(
                                imageVector = drawerActionIcon(leftDrawerAction),
                                contentDescription = stringResource(R.string.left_drawer_action),
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    // DRAG HANDLE LEFT -----------------------------------------------------
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(6.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(if (rightActionNotNone) 1f else 0.5f))
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDrag = { change, drag ->
                                        change.consume()
                                        if (totalWidthPx > 0f) {
                                            val deltaPercent = drag.x / totalWidthPx
                                            localLeft = (localLeft + deltaPercent).coerceIn(0f, 1f)
                                        }
                                    },
                                    onDragEnd = {
                                        scope.launch {
                                            DrawerSettingsStore.setLeftDrawerWidth(ctx, localLeft)
                                        }
                                    }
                                )
                            }
                    )
                }

                Spacer(Modifier.weight(1f))

                if (rightActionNotDisabled) {

                    // DRAG HANDLE RIGHT ----------------------------------------------------
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(6.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(if (rightActionNotNone) 1f else 0.5f))
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDrag = { change, drag ->
                                        change.consume()
                                        if (totalWidthPx > 0f) {
                                            val deltaPercent = -drag.x / totalWidthPx // reversed
                                            localRight =
                                                (localRight + deltaPercent).coerceIn(0f, 1f)
                                        }
                                    },
                                    onDragEnd = {
                                        scope.launch {
                                            DrawerSettingsStore.setRightDrawerWidth(ctx, localRight)
                                        }
                                    }
                                )
                            }
                    )

                    // RIGHT PANEL ----------------------------------------------------------
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(localRight)
                            .background(MaterialTheme.colorScheme.primary.copy(if (rightActionNotNone) 1f else 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (rightActionNotNone) {
                            Icon(
                                imageVector = drawerActionIcon(rightDrawerAction),
                                contentDescription = stringResource(R.string.right_drawer_action),
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }

        item {
            ActionSelectorRow(
                options = DrawerActions.entries.filter { it != DrawerActions.DISABLED },
                selected = leftDrawerAction,
                label = stringResource(R.string.left_drawer_action),
                optionLabel = { drawerActionsLabel(ctx, it) },
                onToggle = {
                    scope.launch {
                        if (leftActionNotDisabled) {
                            DrawerSettingsStore.setLeftDrawerAction(ctx, DrawerActions.DISABLED)
                        } else {
                            DrawerSettingsStore.setLeftDrawerAction(ctx, DrawerActions.TOGGLE_KB)
                        }
                    }
                },
                toggled = leftDrawerAction != DrawerActions.DISABLED
            ) {
                scope.launch { DrawerSettingsStore.setLeftDrawerAction(ctx, it) }
            }
        }

        item {
            ActionSelectorRow(
                options = DrawerActions.entries.filter { it != DrawerActions.DISABLED },
                selected = rightDrawerAction,
                label = stringResource(R.string.right_drawer_action),
                optionLabel = { drawerActionsLabel(ctx, it) },
                onToggle = {
                    scope.launch {
                        if (rightActionNotDisabled) {
                            DrawerSettingsStore.setRightDrawerAction(ctx, DrawerActions.DISABLED)
                        } else {
                            DrawerSettingsStore.setRightDrawerAction(ctx, DrawerActions.CLOSE)
                        }
                    }
                },
                toggled = rightDrawerAction != DrawerActions.DISABLED
            ) {
                scope.launch { DrawerSettingsStore.setRightDrawerAction(ctx, it) }
            }
        }
    }
}
