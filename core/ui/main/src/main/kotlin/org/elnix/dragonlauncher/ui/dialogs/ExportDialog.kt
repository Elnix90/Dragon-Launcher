package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.enumsui.toggle.BackupSelectStoresButtons
import org.elnix.dragonlauncher.enumsui.toggle.BackupSelectStoresButtons.DeselectAll
import org.elnix.dragonlauncher.enumsui.toggle.BackupSelectStoresButtons.Invert
import org.elnix.dragonlauncher.enumsui.toggle.BackupSelectStoresButtons.SelectAll
import org.elnix.dragonlauncher.settings.bases.DatastoreProvider
import org.elnix.dragonlauncher.settings.backupableStores
import org.elnix.dragonlauncher.settings.bases.BaseSettingsStore
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonRow

@Composable
fun ExportSettingsDialog(
    onDismiss: () -> Unit,
    availableStores: Map<DatastoreProvider, BaseSettingsStore<*, *>> = backupableStores,
    defaultStores: Map<DatastoreProvider, BaseSettingsStore<*, *>> = backupableStores,
    onConfirm: (selectedStores: Map<DatastoreProvider, BaseSettingsStore<*, *>>) -> Unit
) {

    val selected = remember(availableStores) {
        mutableStateMapOf<DatastoreProvider, Boolean>().apply {
            availableStores.forEach { put(it.key, it.value in defaultStores.values) }
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
        title = { Text(stringResource(R.string.select_settings_to_export)) },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 600.dp)
            ) {
                item {
                    SelectedActionRow(selected, availableStores.size) { }
                }

                items(availableStores.entries.toList()) { entry ->
                    StoreItem(selected, entry.key, entry.value)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shape = DragonShape
    )
}

@Composable
fun <T> SelectedActionRow(
    selected: SnapshotStateMap<T, Boolean>,
    totalNumber: Int,
    onAnyAction: () -> Unit
) {
    MultiSelectConnectedButtonRow(
        entries = BackupSelectStoresButtons.entries,
        isEnabled = { entry ->
            val selectedCount = selected.map { it.value }.count { it }
            when (entry) {
                DeselectAll -> selectedCount > 0
                SelectAll -> selectedCount < totalNumber
                Invert -> true
            }
        }
    ) {
        when (it) {
            DeselectAll -> {
                selected.forEach { (store, _) ->
                    selected[store] = false
                }
                onAnyAction()
            }

            SelectAll -> {
                selected.forEach { (store, _) ->
                    selected[store] = true
                }
                onAnyAction()
            }

            Invert -> {
                selected.forEach { (store, isSelected) ->
                    selected[store] = !isSelected
                }
                onAnyAction()
            }
        }
    }
}


@Composable
fun StoreItem(
    selected: SnapshotStateMap<DatastoreProvider, Boolean>,
    dataStoreName: DatastoreProvider,
    settingsStore: BaseSettingsStore<*, *>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(DragonShape)
            .padding(vertical = 4.dp)
            .toggleable(
                value = selected[dataStoreName] ?: true,
            ) { selected[dataStoreName] = it },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(settingsStore.name)
        Checkbox(
            checked = selected[dataStoreName] ?: true,
            onCheckedChange = null
        )
    }
}