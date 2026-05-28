package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.common.serializables.SwipeAction
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.dragon.components.DragonRow
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons
import org.elnix.dragonlauncher.ui.dragon.dialogs.CustomAlertDialog

@Composable
fun AdbCommandInputDialog(
    onDismiss: () -> Unit,
    showLeaveEmptyNotice: Boolean,
    onActionSelected: (SwipeAction.RunAdbCommand) -> Unit
) {
    var commandText by remember { mutableStateOf("adb ") }
    var toast by remember { mutableStateOf(false) }


    CustomAlertDialog(
        scroll = false,
        alignment = Alignment.Center,
        modifier = Modifier.padding(40.dp),
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.enter_adb_command)) },
        text = {
            Column {
                OutlinedTextField(
                    value = commandText,
                    onValueChange = {
                        commandText = it
                    },
                    singleLine = true,
                    label = { Text(stringResource(R.string.adb_command)) },
                    colors = AppObjectsColors.outlinedTextFieldColors()
                )

                if (showLeaveEmptyNotice) {
                    Text(stringResource(R.string.adb_command_leave_empty_notice))
                }

                DragonRow(
                    onClick = {
                        toast = !toast
                    }
                ) {
                    Checkbox(
                        checked = toast,
                        onCheckedChange = {
                            toast = it
                        }
                    )
                    Text(stringResource(R.string.show_toast))
                }
            }
        },
        confirmButton = {
            ValidateCancelButtons(
                onCancel = onDismiss
            ) {
                onActionSelected(SwipeAction.RunAdbCommand(commandText, toast))
                onDismiss()
            }
        }
    )
}
