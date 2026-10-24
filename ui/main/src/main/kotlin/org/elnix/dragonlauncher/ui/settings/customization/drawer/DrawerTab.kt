package org.elnix.dragonlauncher.ui.settings.customization.drawer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.github.elnix90.shared.ui.dragon.components.DragonSettingsGroup
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.navigation.NavigationRoute
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.ui.helpers.settings.RouteItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold

@Composable
fun DrawerTab() {
	val ctx = LocalContext.current
	val scope = rememberCoroutineScope()

	SettingsScaffold(
		title = stringResource(R.string.app_drawer),
		helpText = stringResource(R.string.drawer_tab_text),
		resetText = stringResource(R.string.reset_drawer),
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

		DragonSettingsGroup(R.string.drawer_settings) {
			RouteItem(NavigationRoute.DrawerAppearance)
			RouteItem(NavigationRoute.DrawerBehavior)
			RouteItem(NavigationRoute.DrawerActions)
		}
	}
}
