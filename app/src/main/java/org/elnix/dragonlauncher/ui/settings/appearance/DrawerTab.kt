package org.elnix.dragonlauncher.ui.settings.appearance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.helpers.DrawerActions
import org.elnix.dragonlauncher.data.stores.DrawerSettingsStore
import org.elnix.dragonlauncher.data.stores.UiSettingsStore
import org.elnix.dragonlauncher.ui.helpers.GridSizeSlider
import org.elnix.dragonlauncher.ui.helpers.SliderWithLabel
import org.elnix.dragonlauncher.ui.helpers.SwitchRow
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsLazyHeader


@Composable
fun DrawerTab(onBack: () -> Unit) {

    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()


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
        .collectAsState(initial = 75.dp)
    val rightDrawerSize  by DrawerSettingsStore.getRightDrawerWidth(ctx)
        .collectAsState(initial = 75.dp)

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

        item {
            SwitchRow(
                autoLaunchSingleMatch,
                "Auto Launch Single Match",
            ) { scope.launch { DrawerSettingsStore.setAutoLaunchSingleMatch(ctx, it) } }
        }

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
            SwitchRow(
                searchBarBottom,
                "Search bar ${if (searchBarBottom) "Bottom" else "Top"}",
                enabled = false
            ) { scope.launch { DrawerSettingsStore.setSearchBarBottom(ctx, it) } }
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
            GridSizeSlider()
        }
        item {
            SliderWithLabel(
                label = stringResource(R.string.circleR1),
                value = circleR1,
                valueRange = 0f..1000f,
                showValue = false,
                color = circleDataList.find { it.name == "circleR1" }!!.color,
                onReset = { scope.launch { UiSettingsStore.setFirstCircleDragDistance(ctx, 400) } }
            ) {
                scope.launch { UiSettingsStore.setFirstCircleDragDistance(ctx, it) }
            }
        }
    }
}
