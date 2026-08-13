package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elnix90.core.stores.SettingsStore
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.backupableStores
import org.elnix.dragonlauncher.ui.base.components.LazyColumnWithScrollIndicator
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtonsWithLoading
import org.json.JSONObject

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ImportSettingsDialog(
    backupJson: JSONObject,
    onDismiss: () -> Unit,
    onConfirm: (selectedStores: Set<SettingsStore<*, *>>) -> Unit
) {

    /** Filter stores that exist in backup JSON */
    val availableStores = backupableStores.filter { backupJson.has(it.name) }.toSet()

    val selected = remember(availableStores) {
        mutableStateMapOf<SettingsStore<*, *>, Boolean>().apply {
            availableStores.forEach { put(it, true) }
        }
    }

    var hasClickedImport by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            ValidateCancelButtonsWithLoading(
                onCancel = onDismiss,
                hasClickedValidate = hasClickedImport
            ) {
                hasClickedImport = true
                onConfirm(availableStores.filter { selected[it] == true }.toSet())
            }
        },
        title = { Text(stringResource(R.string.select_settings_to_import)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                SelectedActionRow(selected, availableStores.size)
                LazyColumnWithScrollIndicator(
                    items = availableStores.toList(),
                    modifier = Modifier.heightIn(max = 600.dp)
                ) { store ->
                    StoreItem(selected, store)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shape = MaterialTheme.shapes.large
    )
}
