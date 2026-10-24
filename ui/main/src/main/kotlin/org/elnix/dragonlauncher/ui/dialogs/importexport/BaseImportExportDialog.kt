package org.elnix.dragonlauncher.ui.dialogs.importexport

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import io.github.elnix90.core.stores.SettingsStore
import io.github.elnix90.shared.ui.dragon.components.DragonSettingsGroup
import io.github.elnix90.shared.ui.dragon.components.ValidateCancelButtonsWithLoading
import io.github.elnix90.shared.ui.dragon.dialogs.CustomAlertDialog
import io.github.elnix90.shared.ui.dragon.text.DialogTitle
import org.elnix.dragonlauncher.i18n.R

@Composable
internal fun BaseImportExportDialog(
	title: Int,
	defaultStores: Set<SettingsStore<*, *>>,
	availableStores: Set<SettingsStore<*, *>>,
	onDismiss: () -> Unit,
	onConfirm: (selectedStores: Set<SettingsStore<*, *>>) -> Unit
) {
	val snapshotStateMapStores =
		remember(availableStores, defaultStores) {
			mutableStateMapOf<SettingsStore<*, *>, Boolean>().apply {
				availableStores.forEach { store ->
					put(store, store in defaultStores)
				}
			}
		}

	var hasClickedConfirm by remember { mutableStateOf(false) }

	CustomAlertDialog(
		onDismissRequest = onDismiss,
		confirmButton = {
			ValidateCancelButtonsWithLoading(
				cancelText = stringResource(R.string.cancel),
				confirmText = stringResource(R.string.save),
				onCancel = onDismiss,
				hasClickedValidate = hasClickedConfirm
			) {
				hasClickedConfirm = true
				onConfirm(snapshotStateMapStores.filterValues { it }.keys)
			}
		},
		title = {
			DialogTitle(stringResource(title))
		},
		text = {
			DragonSettingsGroup {
				SelectedActionRow(snapshotStateMapStores)
				StoreItems(snapshotStateMapStores)
			}
		}
	)
}
