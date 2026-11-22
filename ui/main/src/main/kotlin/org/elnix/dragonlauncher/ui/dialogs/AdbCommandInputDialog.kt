package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elnix90.shared.theme.AppObjectsColors
import io.github.elnix90.shared.ui.base.components.Spacer
import io.github.elnix90.shared.ui.dragon.components.DragonRow
import io.github.elnix90.shared.ui.dragon.components.DragonSettingsGroup
import io.github.elnix90.shared.ui.dragon.components.ValidateCancelButtons
import io.github.elnix90.shared.ui.dragon.dialogs.CustomAlertDialog
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.i18n.R

@Composable
fun AdbCommandInputDialog(
	onDismiss: () -> Unit,
	showLeaveEmptyNotice: Boolean,
	onActionSelected: (Action.RunAdbCommand) -> Unit
) {
	var commandText by remember { mutableStateOf("") }
	var toast by remember { mutableStateOf(false) }
	var silent by remember { mutableStateOf(false) }

	CustomAlertDialog(
		scroll = false,
		alignment = Alignment.Center,
		modifier = Modifier.padding(40.dp),
		onDismissRequest = onDismiss,
		title = { Text(stringResource(R.string.enter_adb_command)) },
		text = {
			Column {
				DragonSettingsGroup {
					TextField(
						value = commandText,
						onValueChange = { commandText = it },
						singleLine = true,
						label = { Text(stringResource(R.string.adb_command)) },
						colors = AppObjectsColors.outlinedTextFieldColors(),
						shape = CircleShape,
						modifier = Modifier.dragonSettingGroup()
					)

					if (showLeaveEmptyNotice) {
						Text(
							text = stringResource(R.string.adb_command_leave_empty_notice),
							modifier = Modifier.dragonSettingGroup()
						)
					}
				}

				Spacer(10.dp)

				val interactionSource = remember { List(2) { MutableInteractionSource() } }

				DragonSettingsGroup {
					Row(
						modifier =
							Modifier
								.dragonSettingGroup {
									clickable(interactionSource = interactionSource[0]) { toast = !toast }
										.padding(10.dp)
								},
						horizontalArrangement = Arrangement.spacedBy(5.dp)
					) {
						Checkbox(
							checked = toast,
							onCheckedChange = null,
							interactionSource = interactionSource[0]
						)
						Text(stringResource(R.string.show_toast))
					}
					Row(
						modifier =
							Modifier
								.dragonSettingGroup {
									clickable(interactionSource = interactionSource[1]) { silent = !silent }
										.padding(10.dp)
								},
						horizontalArrangement = Arrangement.spacedBy(5.dp)
					) {
						Checkbox(
							checked = silent,
							onCheckedChange = null,
							interactionSource = interactionSource[1]
						)
						Text(stringResource(R.string.silent))
					}
				}
			}
		},
		confirmButton = {
			ValidateCancelButtons(
				cancelText = stringResource(R.string.cancel),
				confirmText = stringResource(R.string.save),
				onCancel = onDismiss
			) {
				onActionSelected(Action.RunAdbCommand(commandText, toast))
				onDismiss()
			}
		}
	)
}
