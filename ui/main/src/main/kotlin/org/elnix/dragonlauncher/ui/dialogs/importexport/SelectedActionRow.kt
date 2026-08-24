package org.elnix.dragonlauncher.ui.dialogs.importexport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.elnix.dragonlauncher.enumsui.toggle.BackupSelectStoresButtons
import org.elnix.dragonlauncher.ui.dragon.components.DragonGroupScope
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonRow

@Composable
fun <T> DragonGroupScope.SelectedActionRow(
    snapshotStateMapStores: SnapshotStateMap<T, Boolean>,
) {
    val totalNumber = snapshotStateMapStores.size
    val selectedCount = snapshotStateMapStores.count { it.value }

    Row(
        modifier = Modifier.dragonSettingGroup(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MultiSelectConnectedButtonRow(
            entries = BackupSelectStoresButtons.entries,
            enabled = { entry ->
                when (entry) {
                    BackupSelectStoresButtons.DeselectAll -> selectedCount > 0
                    BackupSelectStoresButtons.SelectAll -> selectedCount < totalNumber
                    BackupSelectStoresButtons.Invert -> true
                }
            },
        ) {
            when (it) {
                BackupSelectStoresButtons.DeselectAll -> {
                    snapshotStateMapStores.forEach { (store, _) ->
                        snapshotStateMapStores[store] = false
                    }
                }

                BackupSelectStoresButtons.SelectAll -> {
                    snapshotStateMapStores.forEach { (store, _) ->
                        snapshotStateMapStores[store] = true
                    }
                }

                BackupSelectStoresButtons.Invert -> {
                    snapshotStateMapStores.forEach { (store, isSelected) ->
                        snapshotStateMapStores[store] = !isSelected
                    }
                }
            }
        }
    }
}