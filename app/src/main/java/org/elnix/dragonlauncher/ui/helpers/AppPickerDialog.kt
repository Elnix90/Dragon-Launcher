package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.SwipeActionSerializable

@Composable
fun AppPickerDialog(
    onDismiss: () -> Unit,
    onAppSelected: (SwipeActionSerializable.LaunchApp) -> Unit
) {
    val ctx = LocalContext.current

    // Load installed apps
    val apps = remember {
        val pm = ctx.packageManager
        pm.getInstalledApplications(0)
            .filter {
                pm.getLaunchIntentForPackage(it.packageName) != null
            }
            .sortedBy { pm.getApplicationLabel(it).toString() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select App") },
        text = {
            LazyColumn {
                items(apps) { app ->
                    val pm = ctx.packageManager
                    val label = pm.getApplicationLabel(app).toString()

                    Button(onClick = {
                        onAppSelected(SwipeActionSerializable.LaunchApp(app.packageName))
                        onDismiss()
                    }) {
                        Text(label)
                    }

                    Spacer(Modifier.height(6.dp))
                }
            }
        },
        confirmButton = {},
        containerColor = MaterialTheme.colorScheme.surface
    )
}
