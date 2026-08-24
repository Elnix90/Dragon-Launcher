package org.elnix.dragonlauncher.ui.dialogs.importexport

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import io.github.elnix90.core.stores.SettingsStore
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.backupableStores
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

    BaseImportExportDialog(
        title = R.string.select_settings_to_import,
        availableStores = availableStores,
        defaultStores = availableStores, // By default, all stores are selected
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}
