package org.elnix.dragonlauncher.ui.dialogs.importexport

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import io.github.elnix90.core.stores.SettingsStore
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.backupableStores
import org.elnix.dragonlauncher.settings.stores.map.BackupSettingsStore
import org.elnix.dragonlauncher.settings.toSettingsStoreList

@Composable
internal fun ExportDialog(
    onDismiss: () -> Unit,
    onConfirm: (selectedStores: Set<SettingsStore<*, *>>) -> Unit
) {
    val defaultStoresStringSet by BackupSettingsStore.backupStores.asState()
    val defaultStores = defaultStoresStringSet.toSettingsStoreList()

    BaseImportExportDialog(
        title = R.string.select_settings_to_export,
        availableStores = backupableStores,
        defaultStores = backupableStores,
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}
