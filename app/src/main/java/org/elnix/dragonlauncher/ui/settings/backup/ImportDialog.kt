package org.elnix.dragonlauncher.ui.settings.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.data.DataStoreName
import org.elnix.dragonlauncher.utils.colors.AppObjectsColors
import org.json.JSONObject
import java.util.Locale

@Composable
fun ImportSettingsDialog(
    backupJson: JSONObject,
    onDismiss: () -> Unit,
    onConfirm: (selectedStores: List<DataStoreName>) -> Unit
) {
    val allStores = DataStoreName.entries

    // Filter stores that exist in backup JSON
    val availableStores = allStores.filter {
        backupJson.has(it.backupKey) && it.backupKey != null
    }

    val selected = remember(availableStores) {
        mutableStateMapOf<DataStoreName, Boolean>().apply {
            availableStores.forEach { put(it, true) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(availableStores.filter { selected[it] == true })
                },
                colors = AppObjectsColors.buttonColors()
            ) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = AppObjectsColors.cancelButtonColors()
            ) { Text("Cancel") }
        },
        title = { Text("Select settings to import") },
        text = {
            LazyColumn {
                items(availableStores) { store ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .toggleable(
                                value = selected[store] ?: true,
                                onValueChange = { selected[store] = it }
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            store.backupKey!!.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(Locale.ROOT)
                                else it.toString()
                            }
                        )
                        Checkbox(
                            checked = selected[store] ?: true,
                            onCheckedChange = null
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(20.dp)
    )
}
