package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.SwipeActionSerializable

@Composable
fun AddPointDialog(
    onDismiss: () -> Unit,
    onActionSelected: (SwipeActionSerializable) -> Unit
) {
    var showAppPicker by remember { mutableStateOf(false) }
    var showUrlInput by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text("Choose action") },
        text = {
            Column {
                Button(onClick = { showAppPicker = true }) {
                    Text("Open App")
                }

                Button(onClick = {
                    onActionSelected(SwipeActionSerializable.OpenAppDrawer)
                }) { Text("Open App Drawer") }

                Button(onClick = {
                    onActionSelected(SwipeActionSerializable.NotificationShade)
                }) { Text("Notification Shade") }

                Button(onClick = {
                    onActionSelected(SwipeActionSerializable.ControlPanel)
                }) { Text("Control Panel") }

                Button(onClick = { showUrlInput = true }) {
                    Text("Open URL")
                }
            }
        },
        containerColor = Color(R.color.surface)
    )

    if (showAppPicker) {
        AppPickerDialog(
            onDismiss = { showAppPicker = false },
            onAppSelected = {
                onActionSelected(it)
                showAppPicker = false
            }
        )
    }

    if (showUrlInput) {
        UrlInputDialog(
            onDismiss = { showUrlInput = false },
            onUrlSelected = {
                onActionSelected(it)
                showUrlInput = false
            }
        )
    }
}
