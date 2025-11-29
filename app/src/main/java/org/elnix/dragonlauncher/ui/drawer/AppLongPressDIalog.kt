package org.elnix.dragonlauncher.ui.drawer

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun AppLongPressDialog(
    app: AppModel,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
    onSettings: () -> Unit,
    onUninstall: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(app.name) },
        text = {
            Text("Choose an action")
        },
        confirmButton = {},
        dismissButton = {}
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Column {
                TextButton(onClick = { onDismiss(); onOpen() }) { Text("Open") }
                TextButton(onClick = { onDismiss(); onSettings() }) { Text("App Settings") }
                TextButton(onClick = { onDismiss(); onUninstall() }) { Text("Uninstall") }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
