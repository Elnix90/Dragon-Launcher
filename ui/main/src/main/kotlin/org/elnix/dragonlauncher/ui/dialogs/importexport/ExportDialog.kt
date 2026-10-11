package org.elnix.dragonlauncher.ui.dialogs.importexport

import androidx.compose.runtime.Composable
import io.github.elnix90.core.stores.SettingsStore
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.backupableStores

@Composable
internal fun ExportDialog(
	onDismiss: () -> Unit,
	onConfirm: (selectedStores: Set<SettingsStore<*, *>>) -> Unit
) {
	BaseImportExportDialog(
		title = R.string.select_settings_to_export,
		availableStores = backupableStores,
		defaultStores = backupableStores,
		onDismiss = onDismiss,
		onConfirm = onConfirm
	)
}
