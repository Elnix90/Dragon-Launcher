package org.elnix.dragonlauncher.ui.settings.customization.drawer

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.ui.compositionslocals.LocalDrawerSettings
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.settings.Setting
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold

@Composable
fun DrawerBehaviorTab() {
	val drawerSettings = LocalDrawerSettings.current
	val autoOpenSingleMatch = drawerSettings.autoOpenSingleMatch

	SettingsScaffold(
		title = stringResource(R.string.drawer_behavior),
		helpText = stringResource(R.string.drawer_behavior_help),
		resetText = null,
		onReset = null
	) {
		DragonSettingsGroup {
			Setting(DrawerSettingsStore.autoOpenSingleMatch)
			Setting(DrawerSettingsStore.disableAutoLaunchWhenFirstCharIs, enabled = autoOpenSingleMatch, singleChar = true)
			Setting(DrawerSettingsStore.searchAllWorkspacesOnlyWhenFirstCharIs, singleChar = true)
			Setting(DrawerSettingsStore.autoShowKeyboardOnDrawer)
			Setting(DrawerSettingsStore.autoAskToUnlockProfile)
			Setting(DrawerSettingsStore.imePadding)
		}

		DragonSettingsGroup(R.string.drawer_pull_down_settings) {
			Setting(DrawerSettingsStore.pullDownAnimations)
			Setting(DrawerSettingsStore.pullDownWallPaperDim)
			Setting(DrawerSettingsStore.pullDownScaleIn)
		}
	}
}
