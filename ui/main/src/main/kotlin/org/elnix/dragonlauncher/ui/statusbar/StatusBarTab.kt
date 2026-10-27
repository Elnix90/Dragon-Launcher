package org.elnix.dragonlauncher.ui.statusbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.elnix90.annotations.SettingScreen
import io.github.elnix90.runtime.asStateNull
import io.github.elnix90.shared.ui.base.components.Spacer
import io.github.elnix90.shared.ui.dragon.components.DragonSettingsGroup
import io.github.elnix90.shared.ui.dragon.components.SwitchRow
import io.github.elnix90.ui.Setting
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.navigation.NavigationRoute
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.stores.array.StatusBarJsonSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.StatusBarSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.dragon.settings.Setting
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold

@SettingScreen(NavigationRoute.StatusBar::class)
@Composable
fun StatusBarTab() {
	val ctx = LocalContext.current
	val scope = rememberCoroutineScope()
	var showStatusBar by showStatusBar()

	val fullscreen by UiSettingsStore.fullScreen.asStateNull()

	SettingsScaffold(
		title = stringResource(R.string.status_bar),
		helpText = stringResource(R.string.status_bar_tab_text),
		resetText = stringResource(R.string.reset_status_bar),
		onReset = {
			scope.launch {
				StatusBarSettingsStore.resetAll(ctx)
				StatusBarJsonSettingsStore.resetAll(ctx)
				showStatusBar = false
			}
		},
		topContent = {
			if (showStatusBar) {
				Spacer()
			}
		}
	) {
		DragonSettingsGroup(R.string.show_status_bar) {
			SwitchRow(
				title = R.string.show_status_bar,
				description = R.string.show_status_bar_desc,
				state = showStatusBar
			) { showStatusBar = it }
		}

		if (fullscreen == false) {
			Text(
				text = stringResource(R.string.status_bar_warning),
				style = MaterialTheme.typography.bodyMediumEmphasized,
				textAlign = TextAlign.Center
			)
		}

		AnimatedVisibility(showStatusBar) {
			Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
				EditStatusBar()

				DragonSettingsGroup(R.string.color) {
					Setting(StatusBarSettingsStore.barBackgroundColor)
					Setting(StatusBarSettingsStore.barTextColor)
				}

				DragonSettingsGroup(R.string.padding) {
					Setting(StatusBarSettingsStore.leftPadding)
					Setting(StatusBarSettingsStore.rightPadding)
					Setting(StatusBarSettingsStore.topPadding)
					Setting(StatusBarSettingsStore.bottomPadding)
				}
			}
		}
	}
	StatusBar(null)
}
