package org.elnix.dragonlauncher.ui.settings.workspace

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.utils.colors.AppObjectsColors

@Composable
fun CreateOrRenameWorkspaceDialog(
    visible: Boolean,
    title: String,
    name: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onConfirm() },
                colors = AppObjectsColors.buttonColors()
            ) {
                Text(
                    text = stringResource(android.R.string.ok),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss(); },
                colors = AppObjectsColors.cancelButtonColors()
            ) {
                Text(
                    stringResource(android.R.string.cancel),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        title = { Text(title) },
        text = {
            TextField(
                value = name,
                onValueChange = onNameChange,
                singleLine = true,
                colors = AppObjectsColors.outlinedTextFieldColors(backgroundColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(20.dp)
    )
}
