package org.elnix.dragonlauncher.ui.dialogs.importexport

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import io.github.elnix90.core.stores.SettingsStore
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtonsWithLoading
import org.elnix.dragonlauncher.ui.dragon.dialogs.CustomAlertDialog
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle

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
