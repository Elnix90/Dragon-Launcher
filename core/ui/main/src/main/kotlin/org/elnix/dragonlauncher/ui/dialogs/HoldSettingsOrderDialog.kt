package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.navigaton.routeResId
import org.elnix.dragonlauncher.common.navigaton.settingsRoutes
import org.elnix.dragonlauncher.common.utils.Constants.Logging.HOLD_TAG
import org.elnix.dragonlauncher.enumsui.BackupSelectStoresButtons
import org.elnix.dragonlauncher.enumsui.BackupSelectStoresButtons.DESELECT_ALL
import org.elnix.dragonlauncher.enumsui.BackupSelectStoresButtons.INVERT
import org.elnix.dragonlauncher.enumsui.BackupSelectStoresButtons.SELECT_ALL
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.settings.stores.HoldToActivateArcSettingsStore
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.components.sheets.DismissableBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonRow
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonRow
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

private data class MenuItem(
    val route: String,
    var isSelected: MutableState<Boolean>,
)

@Composable
fun HoldSettingsOrderDialog(
    visible: () -> Boolean,
    onDismiss: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()


    val holdMenuEntriesString by HoldToActivateArcSettingsStore.holdMenuEntries.asState()
    val menuItems: SnapshotStateList<MenuItem> = remember { mutableStateListOf() }

    LaunchedEffect(holdMenuEntriesString) {
        menuItems.clear()
        settingsRoutes.forEach { route ->
            menuItems.add(
                MenuItem(
                    route = route,
                    isSelected = mutableStateOf(route in holdMenuEntriesString),
                )
            )
        }
    }

    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            menuItems.apply {
                add(to.index, removeAt(from.index))
            }
        }
    )

    DismissableBottomSheet(
        onDismissRequest = onDismiss,
        expanded = visible()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 5.dp)
        ) {
            Text(stringResource(R.string.edit_hold_to_activate_elements))

            MultiSelectConnectedButtonRow(
                entries = BackupSelectStoresButtons.entries,
                isEnabled = { entry ->
                    val selectedCount = menuItems.count { it.isSelected.value }
                    when (entry) {
                        DESELECT_ALL -> selectedCount > 0
                        SELECT_ALL -> selectedCount < settingsRoutes.size
                        INVERT -> true
                    }
                }
            ) {
                when (it) {
                    DESELECT_ALL -> {
                        menuItems.forEach { item ->
                            item.isSelected.value = false
                        }
                    }

                    SELECT_ALL -> {
                        menuItems.forEach { item ->
                            item.isSelected.value = true
                        }
                    }

                    INVERT -> {
                        menuItems.forEach { item ->
                            item.isSelected.value = !item.isSelected.value
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.heightIn(max = 600.dp),
                state = lazyListState
            ) {
                items(menuItems, key = { it.route }) { entry ->
                    val isSelected by entry.isSelected

                    ReorderableItem(
                        state = reorderState,
                        key = entry.route
                    ) { isDragging ->
                        val scale by animateFloatAsState(if (isDragging) 1.03f else 1f)

                        DragonRow(
                            onClick = {
                                entry.isSelected.value = !isSelected
                            },
                            modifier = Modifier
                                .scale(scale)
                                .longPressDraggableHandle()
                        ) {
                            Checkbox(checked = isSelected, onCheckedChange = null)
                            Spacer(Modifier.width(5.dp))

                            Text(
                                stringResource(routeResId(entry.route)),
                                modifier = Modifier.weight(1f)
                            )

                            Icon(
                                imageVector = Icons.Default.DragHandle,
                                contentDescription = "Drag handle",
                                modifier = Modifier.draggableHandle()
                            )
                        }
                    }
                }
            }

            ValidateCancelButtons(
                onCancel = onDismiss
            ) {

                val saveList = menuItems.filter { it.isSelected.value }.map { it.route }
                logD(HOLD_TAG) { "Saving: $saveList" }

                scope.launch {
                    HoldToActivateArcSettingsStore.holdMenuEntries.set(ctx, saveList)
                    onDismiss()
                }
            }
        }
    }
}
