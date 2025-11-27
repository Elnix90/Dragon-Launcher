package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.SwipeActionSerializable

@Composable
fun UrlInputDialog(
    onDismiss: () -> Unit,
    onUrlSelected: (SwipeActionSerializable.OpenUrl) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter URL") },
        text = {
            Column {
                TextField(
                    value = text,
                    onValueChange = {
                        text = it
                        error = false
                    },
                    singleLine = true,
                    label = { Text("https://example.com") }
                )
                if (error) {
                    Text("Invalid URL", color = Color.Red)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val ok = text.startsWith("http://") || text.startsWith("https://")
                if (!ok) {
                    error = true
                    return@Button
                }
                onUrlSelected(SwipeActionSerializable.OpenUrl(text))
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = Color(R.color.surface)
    )
}
