package org.elnix.dragonlauncher.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.stores.DrawerSettingsStore
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

    val searchBarBottom by DrawerSettingsStore.getSearchBarBottom(ctx)
        .collectAsState(initial = true)


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
                searchBarBottom,
                "Search bar ${if (searchBarBottom) "Bottom" else "Top"}",
            ) { scope.launch { DrawerSettingsStore.setSearchBarBottom(ctx, it) } }
        }
    }
}
