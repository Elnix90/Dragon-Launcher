package org.elnix.dragonlauncher.ui.dialogs.importexport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.elnix90.core.stores.SettingsStore
import org.elnix.dragonlauncher.ui.base.components.LazyColumnWithScrollIndicator
import org.elnix.dragonlauncher.ui.base.remember.rememberInteractionSource
import org.elnix.dragonlauncher.ui.dragon.components.DragonGroupScope

@Composable
fun DragonGroupScope.StoreItems(
    snapshotStateMapStores: SnapshotStateMap<SettingsStore<*, *>, Boolean>,
    onAnyAction: () -> Unit = { },
) {
    LazyColumnWithScrollIndicator(
        items = snapshotStateMapStores.toList(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.heightIn(max = 600.dp)
    ) { (store, selected) ->
        StoreItem(selected, store) {
            snapshotStateMapStores[store] = it
            onAnyAction()
        }
    }
}

@Composable
fun DragonGroupScope.StoreItemsNotScrollable(
    snapshotStateMapStores: SnapshotStateMap<SettingsStore<*, *>, Boolean>,
) {
    snapshotStateMapStores.forEach { (store, selected) ->
        StoreItem(selected, store) {
            snapshotStateMapStores[store] = it
        }
    }
}

@Composable
private fun DragonGroupScope.StoreItem(
    selected: Boolean,
    store: SettingsStore<*, *>,
    onToggle: (Boolean) -> Unit
) {
    val interactionSource = rememberInteractionSource()
    Row(
        modifier = Modifier
            .dragonSettingGroup {
                toggleable(
                    interactionSource = interactionSource,
                    value = selected,
                    onValueChange = onToggle
                )
            },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = null,
            interactionSource = interactionSource
        )
        Text(store.name)
    }
}