package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import org.elnix.dragonlauncher.i18n.R

import org.elnix.dragonlauncher.theme.AppObjectsColors

@Composable
fun UrlInputDialog(
    onDismiss: () -> Unit,
    onUrlSelected: (Action.OpenUrl) -> Unit
) {
    var text by remember { mutableStateOf("https://") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.enter_url)) },
        text = {
            Column {
                TextField(
                    value = text,
                    onValueChange = {
                        text = it
                        error = false
                    },
                    singleLine = true,
                    label = { Text("https://example.com") },
                    colors = AppObjectsColors.outlinedTextFieldColors(
                        removeBorder = true,
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                if (error) {
                    Text(stringResource(R.string.invalid_url), color = Color.Red)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ok = text.startsWith("http://") || text.startsWith("https://")
                    if (!ok) {
                        @Suppress("AssignedValueIsNeverRead")
                        error = true
                        return@Button
                    }
                    onUrlSelected(Action.OpenUrl(text))
                    onDismiss()
                },
                colors = AppObjectsColors.buttonColors()
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = AppObjectsColors.cancelButtonColors()
            ) { Text(stringResource(R.string.cancel)) }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
