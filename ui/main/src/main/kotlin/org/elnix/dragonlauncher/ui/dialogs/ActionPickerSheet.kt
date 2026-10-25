package org.elnix.dragonlauncher.ui.dialogs

import android.content.pm.ShortcutInfo
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elnix90.logging.logD
import io.github.elnix90.runtime.asState
import io.github.elnix90.shared.ui.base.components.Spacer
import io.github.elnix90.shared.ui.dragon.components.DragonModalBottomSheet
import io.github.elnix90.shared.ui.dragon.components.DragonSettingsGroup
import io.github.elnix90.shared.ui.dragon.text.DialogTitle
import org.elnix.dragonlauncher.APP_LAUNCH_TAG
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.models.Application.Companion.toLaunchApp
import org.elnix.dragonlauncher.base.model.models.BluetoothADBCommands
import org.elnix.dragonlauncher.base.model.models.DataADBCommands
import org.elnix.dragonlauncher.base.model.models.WifiADBCommands
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Action.Companion.actionColor
import org.elnix.dragonlauncher.base.model.serializables.Action.LaunchShortcut.Companion.toAction
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.alphaMultiplier
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.settings.stores.map.BehaviorSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.ui.actions.ActionIcon
import org.elnix.dragonlauncher.ui.actions.actionLabel
import org.elnix.dragonlauncher.ui.base.activityViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGridApi::class)
@Composable
fun ActionPickerSheet(
	drawerViewModel: DrawerViewModel = activityViewModel(),
	allowWidgets: Boolean = false,
	onDismiss: () -> Unit,
	onActionSelected: ((Action) -> Unit)? = null,
	onMultipleActionsSelected: ((action: List<Action>) -> Unit)? = null
) {
	require((onActionSelected != null) xor (onMultipleActionsSelected != null)) {
		"You can either use onActionSelected or onMultipleActionsSelected but not both at the same time"
	}

	var showAppPicker by remember { mutableStateOf(false) }
	var showUrlInput by remember { mutableStateOf(false) }
	var showAdbCommandInput by remember { mutableStateOf(false) }
	var showWifiCommandInput by remember { mutableStateOf(false) }
	var showBluetoothCommandInput by remember { mutableStateOf(false) }
	var showDataCommandInput by remember { mutableStateOf(false) }
	var showFilePicker by remember { mutableStateOf(false) }
	var showNestPicker by remember { mutableStateOf(false) }
	var showWorkspacePicker by remember { mutableStateOf(false) }
	var showPinnedShortcutsPicker by remember { mutableStateOf(false) }
	var showActivityPicker by remember { mutableStateOf(false) }
	var showSettingsPagePicker by remember { mutableStateOf(false) }

	val showSpecialActions by DebugSettingsStore.showSpecialActions.asState()
	val promptForShortcuts by BehaviorSettingsStore.promptForShortcutsWhenAddingApp.asState()

	var selectedApp by remember { mutableStateOf<Application?>(null) }
	var shortcutDialogVisible by remember { mutableStateOf(false) }
	var shortcuts by remember { mutableStateOf<List<ShortcutInfo>>(emptyList()) }

	fun onActionPicked(action: Action) {
		if (onMultipleActionsSelected != null) {
			onMultipleActionsSelected(listOf(action))
		} else {
			onActionSelected!!(action)
		}
	}

	DragonModalBottomSheet(
		onDismissRequest = onDismiss
	) {
		DialogTitle(stringResource(R.string.choose_action))

		Spacer(10.dp)

		Column(
			verticalArrangement = Arrangement.spacedBy(5.dp),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Grid(
				config = {
					column(1.fr)
					column(1.fr)
					gap(5.dp)

					repeat(5) {}
				}
			) {
				if (allowWidgets) {
					ActionItem(
						action = Action.OpenWidget.dummy,
						modifier = Modifier.gridItem(columnSpan = 2)
					) { onActionPicked(Action.OpenWidget.dummy) }
				}

				DragonSettingsGroup(
					title = R.string.apps,
					modifier = Modifier.gridItem(rowSpan = 2)
				) {
					ActionItem(Action.LaunchApp.dummy) { showAppPicker = true }
					ActionItem(Action.LaunchActivity.dummy) { showActivityPicker = true }
					ActionItem(Action.LaunchShortcut.dummy) { showPinnedShortcutsPicker = true }
					ActionItem(Action.OpenAppDrawer.dummy) { showWorkspacePicker = true }
				}

				DragonSettingsGroup(R.string.notifications) {
					ActionItem(Action.NotificationShade) { onActionPicked(Action.NotificationShade) }
					ActionItem(Action.ControlPanel) { onActionPicked(Action.ControlPanel) }
				}

				DragonSettingsGroup(R.string.nests) {
					ActionItem(Action.OpenNest.dummy) { showNestPicker = true }
					ActionItem(Action.GoParentNest) { onActionPicked(Action.GoParentNest) }
				}

				DragonSettingsGroup(R.string.miscellaneous) {
					ActionItem(Action.OpenFile.dummy) { showFilePicker = true }
					ActionItem(Action.OpenUrl.dummy) { showUrlInput = true }
					ActionItem(Action.OpenDragonLauncherSettings.dummy) { showSettingsPagePicker = true }
				}

				DragonSettingsGroup(R.string.accessibility) {
					ActionItem(Action.Lock) { onActionPicked(Action.Lock) }
					ActionItem(Action.OpenRecentApps) { onActionPicked(Action.OpenRecentApps) }
				}

				DragonSettingsGroup(R.string.adb) {
					ActionItem(Action.ToggleWifi.dummy) { showWifiCommandInput = true }
					ActionItem(Action.ToggleData.dummy) { showDataCommandInput = true }
					ActionItem(Action.ToggleBluetooth.dummy) { showBluetoothCommandInput = true }
					ActionItem(Action.RunAdbCommand.dummy) { showAdbCommandInput = true }
				}

				if (showSpecialActions) {
					DragonSettingsGroup(R.string.special) {
						ActionItem(Action.ReloadApps) { onActionPicked(Action.ReloadApps) }
						ActionItem(Action.KillLauncher) { onActionPicked(Action.KillLauncher) }
					}
				}
			}
		}
	}

	if (showAppPicker) {
		AppPickerSheet(
			onDismiss = { showAppPicker = false },
			onAppSelected = { app ->

				logD(APP_LAUNCH_TAG) { "Selected App: $app" }

				// Try to query shortcuts, but handle crashes gracefully
				val list =
					if (promptForShortcuts) {
						try {
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
								drawerViewModel.queryAppShortcuts(app.packageName)
							} else {
								emptyList()
							}
						} catch (e: Exception) {
							e.printStackTrace()
							// Some apps (Contacts, Gmail) may throw SecurityException or other errors
							emptyList()
						}
					} else {
						emptyList() // Skip shortcut query if disabled
					}

				if (list.isNotEmpty()) {
					selectedApp = app
					shortcuts = list
					shortcutDialogVisible = true
				} else {
					onActionPicked(app.toLaunchApp())
				}
			},
			onMultipleAppsSelected =
				if (onMultipleActionsSelected != null) {
					{ apps ->
						val actions =
							apps.map {
								Action.LaunchApp(it.packageName, it.profile)
							}
						onMultipleActionsSelected(actions)
						showAppPicker = false
					}
				} else {
					null
				}
		)
	}

	if (showUrlInput) {
		UrlInputDialog(
			onDismiss = { showUrlInput = false },
			onUrlSelected = {
				onActionPicked(it)
				showUrlInput = false
			}
		)
	}

	if (showAdbCommandInput) {
		AdbCommandInputDialog(
			onDismiss = { showAdbCommandInput = false },
			showLeaveEmptyNotice = true,
			onActionSelected = {
				onActionPicked(it)
				showAdbCommandInput = false
			}
		)
	}

	if (showWifiCommandInput) {
		AdbCommandPickerDialog(
			label = "${stringResource(R.string.pick_a)} WIFI ${stringResource(R.string.command)}",
			options = WifiADBCommands.entries,
			selected = { WifiADBCommands.Svc },
			onDismiss = { showWifiCommandInput = false }
		) { command, toast ->
			onActionPicked(Action.ToggleWifi(command, toast))
			showWifiCommandInput = false
		}
	}

	if (showBluetoothCommandInput) {
		AdbCommandPickerDialog(
			label = "${stringResource(R.string.pick_a)} BLUETOOTH ${stringResource(R.string.command)}",
			options = BluetoothADBCommands.entries,
			selected = { BluetoothADBCommands.Cmd },
			onDismiss = { showBluetoothCommandInput = false }
		) { command, toast ->
			onActionPicked(Action.ToggleBluetooth(command, toast))
			showBluetoothCommandInput = false
		}
	}

	if (showDataCommandInput) {
		AdbCommandPickerDialog(
			label = "${stringResource(R.string.pick_a)} DATA ${stringResource(R.string.command)}",
			options = DataADBCommands.entries,
			selected = { DataADBCommands.Svc },
			onDismiss = { showDataCommandInput = false }
		) { command, toast ->
			onActionPicked(Action.ToggleData(command, toast))
			showDataCommandInput = false
		}
	}

	if (showFilePicker) {
		FilePickerDialog(
			onDismiss = { showFilePicker = false },
			onFileSelected = {
				onActionPicked(it)
				showFilePicker = false
			}
		)
	}

	if (shortcutDialogVisible && selectedApp != null) {
		val app = selectedApp!!
		AppShortcutPickerSheet(
			app = app,
			shortcuts = shortcuts,
			onDismiss = { shortcutDialogVisible = false },
			onShortcutSelected = { shortcut ->
				onActionPicked(shortcut.toAction())
				shortcutDialogVisible = false
			},
			onAppSelected = {
				onActionPicked(app.toLaunchApp())
				onDismiss()
			}
		)
	}

	if (showNestPicker) {
		NestManagementSheet(
			onDismissRequest = { showNestPicker = false },
			title = stringResource(R.string.pick_a_nest),
			onSelect = {
				onActionPicked(Action.OpenNest(it.id))
				showNestPicker = false
			}
		)
	}

	if (showSettingsPagePicker) {
		SettingsPagePicker(
			onDismissRequest = { showSettingsPagePicker = false }
		) {
			onActionPicked(Action.OpenDragonLauncherSettings(it))
			showSettingsPagePicker = false
		}
	}

	if (showWorkspacePicker) {
		WorkspacePickerDialog(
			onDismiss = { showWorkspacePicker = false },
			onActionPicked = {
				onActionPicked(it)
				showWorkspacePicker = false
			}
		)
	}

	if (showPinnedShortcutsPicker) {
		PinnedShortcutsPickerSheet(
			onDismiss = { showPinnedShortcutsPicker = false },
			onShortcutSelected = { shortcutAction ->
				onActionPicked(shortcutAction)
				showPinnedShortcutsPicker = false
			}
		)
	}

	if (showActivityPicker) {
		LaunchActivityPickerSheet(
			onDismiss = { showActivityPicker = false },
			onActivitySelected = { activityAction ->
				onActionPicked(activityAction)
				showActivityPicker = false
			}
		)
	}
}

@Composable
private fun ActionItem(
	action: Action,
	modifier: Modifier = Modifier,
	onSelected: () -> Unit
) {
	val extraColors = LocalExtraColors.current

	val name =
		when (action) {
			is Action.LaunchApp -> stringResource(R.string.open_app)
			is Action.LaunchShortcut -> stringResource(R.string.pinned_shortcuts)
			is Action.LaunchActivity -> stringResource(R.string.launch_activity)
			is Action.OpenUrl -> stringResource(R.string.open_url)
			is Action.RunAdbCommand -> stringResource(R.string.run_adb_command)
			is Action.OpenFile -> stringResource(R.string.open_file)
			is Action.OpenNest -> stringResource(R.string.open_nest)
			is Action.OpenDragonLauncherSettings -> stringResource(R.string.dragon_launcher_settings)
			else -> actionLabel(action)
		}

	val color = action.actionColor(extraColors)
	Row(
		modifier =
			modifier
				.fillMaxWidth()
				.clip(MaterialTheme.shapes.large)
				.background(color.alphaMultiplier(0.5f))
				.clickable(onClick = onSelected)
				.padding(12.dp),
		horizontalArrangement = Arrangement.spacedBy(12.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		ActionIcon(
			action = action,
			size = 30.dp
		)

		Text(
			text = name,
			maxLines = 2,
			modifier = Modifier.basicMarquee(3),
			style = MaterialTheme.typography.titleMediumEmphasized
		)
	}
}
