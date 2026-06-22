package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import io.github.elnix90.core.stores.SettingsStore
import org.elnix.dragonlauncher.enumsui.toggle.BackupSelectStoresButtons
import org.elnix.dragonlauncher.enumsui.toggle.BackupSelectStoresButtons.DeselectAll
import org.elnix.dragonlauncher.enumsui.toggle.BackupSelectStoresButtons.Invert
import org.elnix.dragonlauncher.enumsui.toggle.BackupSelectStoresButtons.SelectAll
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.backupableStores
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.base.components.LazyColumnWithScrollIndicator
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonRow

@Composable
fun ExportSettingsDialog(
    onDismiss: () -> Unit,
    title: Int = R.string.select_settings_to_export,
    availableStores: Set<SettingsStore<*, *>> = backupableStores,
    defaultStores: Set<SettingsStore<*, *>> = backupableStores,
    onConfirm: (selectedStores: Set<SettingsStore<*, *>>) -> Unit
) {

    val selected = remember(availableStores, defaultStores) {
        mutableStateMapOf<SettingsStore<*, *>, Boolean>().apply {
            availableStores.forEach { store ->
                put(store, store in defaultStores)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            ValidateCancelButtons(
                onCancel = onDismiss
            ) {
                onConfirm(selected.filterValues { it }.keys)
            }
        },
        title = { Text(stringResource(title)) },
        text = {
            SelectedActionRow(selected, availableStores.size)

            LazyColumnWithScrollIndicator(
                items = availableStores.toList(),
                modifier = Modifier.heightIn(max = 600.dp)
            ) { store ->
                StoreItem(selected, store)
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
    onAnyAction: (() -> Unit)? = null
) {
    MultiSelectConnectedButtonRow(
        entries = BackupSelectStoresButtons.entries,
        enabled = { entry ->
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
                onAnyAction?.invoke()
            }

            SelectAll -> {
                selected.forEach { (store, _) ->
                    selected[store] = true
                }
                onAnyAction?.invoke()
            }

            Invert -> {
                selected.forEach { (store, isSelected) ->
                    selected[store] = !isSelected
                }
                onAnyAction?.invoke()
            }
        }
    }
}


@Composable
fun StoreItem(
    selected: SnapshotStateMap<SettingsStore<*,*>, Boolean>,
    settingsStore: SettingsStore<*, *>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(DragonShape)
            .padding(vertical = 4.dp)
            .toggleable(
                value = selected[settingsStore] ?: true,
            ) { selected[settingsStore] = it },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(settingsStore.name)
        Checkbox(
            checked = selected[settingsStore] ?: true,
            onCheckedChange = null
        )
    }
}