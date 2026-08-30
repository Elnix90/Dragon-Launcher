package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextAlign
import org.elnix.dragonlauncher.base.model.serializables.WorkspaceType
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons
import org.elnix.dragonlauncher.ui.dragon.generic.ActionSelectorRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrEditWorkspaceDialog(
    visible: Boolean,
    title: String,
    name: String,
    type: WorkspaceType?,
    onNameChange: (String) -> Unit,
    onConfirm: (WorkspaceType) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    var selectedType by remember { mutableStateOf(type ?: WorkspaceType.Custom) }

    DragonModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = title,
            modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center
        )

        DragonSettingsGroup {
            TextField(
                value = name,
                onValueChange = onNameChange,
                singleLine = true,
                placeholder = {
                    Text(stringResource(R.string.workspace_name))
                },
                modifier = Modifier.dragonSettingGroup()
            )

            ActionSelectorRow(
                options = WorkspaceType.entries,
                selected = selectedType,
                switchEnabled = false,
                label = stringResource(R.string.workspace_type),
                resetEnabled = selectedType != WorkspaceType.Custom,
                onReset = {
                    selectedType = WorkspaceType.Custom
                }
            ) {
                selectedType = it!!
            }
        }

        ValidateCancelButtons(
            onCancel = onDismiss,
            onConfirm = {
                onConfirm(selectedType)
            }
        )
    }
}
