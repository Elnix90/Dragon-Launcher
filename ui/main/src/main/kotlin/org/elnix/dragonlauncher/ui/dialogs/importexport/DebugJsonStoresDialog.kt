package org.elnix.dragonlauncher.ui.dialogs.importexport

import androidx.compose.runtime.Composable
import io.github.elnix90.core.stores.SettingsStore
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.AllStores

@Composable
internal fun DebugJsonStoresDialog(
    defaultStores: Set<SettingsStore<*, *>>,
    onDismiss: () -> Unit,
    onConfirm: (selectedStores: Set<SettingsStore<*, *>>) -> Unit
) {
    BaseImportExportDialog(
        title = R.string.select_stores,
        availableStores = AllStores,
        defaultStores = defaultStores,
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}
