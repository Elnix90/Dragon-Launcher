package org.elnix.dragonlauncher.ui.settings.debug

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold

@Composable
fun PermissionsTab(
	viewModel: PermissionsViewModel
) {
	SettingsScaffold(
		title = stringResource(R.string.permissions),
		helpText = null,
		onReset = null,
		resetText = null
	) {
		DragonSettingsGroup {
			viewModel.states.forEach { (group, state) ->
				val granted by state.collectAsState()

				SwitchRow(
					state = granted,
					title = group.title,
					description = group.description,
					icon = group.icon
				) {
					// These are granted in system settings only, so the row cannot
					// toggle anything itself. Both directions send the user to the
					// screen where the grant actually lives.
					viewModel.requestPermission(group)
				}
			}
		}
	}
}
