package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.backupableStores
import org.elnix.dragonlauncher.settings.bases.stores.SettingsStore
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.base.components.LazyColumnWithScrollIndicator
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons
import org.json.JSONObject

@Composable
fun ImportSettingsDialog(
    backupJson: JSONObject,
    onDismiss: () -> Unit,
    onConfirm: (selectedStores: Map<DataStoreName, SettingsStore<*, *>>) -> Unit
) {

    // Filter stores that exist in backup JSON
    val availableStores = backupableStores.filter {
        backupJson.has(it.key.name) ||
                backupJson.has("actions") // Old actions store, for legacy support
    }

    val selected = remember(availableStores) {
        mutableStateMapOf<DataStoreName, Boolean>().apply {
            availableStores.forEach { put(it.key, true) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            ValidateCancelButtons(
                onCancel = onDismiss
            ) {
                onConfirm(availableStores.filter { selected[it.key] == true })
            }
        },
        title = { Text(stringResource(R.string.select_settings_to_import)) },
        text = {
            SelectedActionRow(selected, availableStores.size)
            LazyColumnWithScrollIndicator(
                items = availableStores.entries.toList(),
                modifier = Modifier.heightIn(max = 600.dp)
            ) { entry ->
                StoreItem(selected, entry.key, entry.value)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shape = DragonShape
    )
}
