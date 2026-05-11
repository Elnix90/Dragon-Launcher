package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.HOLD_TAG
import org.elnix.dragonlauncher.common.navigaton.NavigationRoute
import org.elnix.dragonlauncher.common.navigaton.NavigationRoute.AdvancedSettings.routeResId
import org.elnix.dragonlauncher.common.navigaton.NavigationRoute.Companion.settingsRoutes
import org.elnix.dragonlauncher.enumsui.toggle.BackupSelectStoresButtons
import org.elnix.dragonlauncher.enumsui.toggle.BackupSelectStoresButtons.DeselectAll
import org.elnix.dragonlauncher.enumsui.toggle.BackupSelectStoresButtons.Invert
import org.elnix.dragonlauncher.enumsui.toggle.BackupSelectStoresButtons.SelectAll
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.settings.stores.HoldToActivateArcSettingsStore
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.components.VerticalScrollIndicator
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonRow
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonRow
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

private data class MenuItem(
    val route: NavigationRoute,
    var isSelected: MutableState<Boolean>,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoldSettingsOrderSheet(onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()


    val holdMenuEntries = rememberHoldMenuEntries()
    val menuItems: SnapshotStateList<MenuItem> = remember { mutableStateListOf() }

    LaunchedEffect(holdMenuEntries) {
        menuItems.clear()
        settingsRoutes.forEach { route ->
            menuItems.add(
                MenuItem(
                    route = route,
                    isSelected = mutableStateOf(route in holdMenuEntries),
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

    DragonModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(true)
    ) {

        Text(stringResource(R.string.edit_hold_to_activate_elements))

        MultiSelectConnectedButtonRow(
            entries = BackupSelectStoresButtons.entries,
            isEnabled = { entry ->
                val selectedCount = menuItems.count { it.isSelected.value }
                when (entry) {
                    DeselectAll -> selectedCount > 0
                    SelectAll -> selectedCount < settingsRoutes.size
                    Invert -> true
                }
            }
        ) {
            when (it) {
                DeselectAll -> {
                    menuItems.forEach { item ->
                        item.isSelected.value = false
                    }
                }

                SelectAll -> {
                    menuItems.forEach { item ->
                        item.isSelected.value = true
                    }
                }

                Invert -> {
                    menuItems.forEach { item ->
                        item.isSelected.value = !item.isSelected.value
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .heightIn(max = 600.dp)
                .weight(1f)
        ) {
            LazyColumn(state = lazyListState) {
                items(menuItems, key = { it.route.toString() }) { entry ->
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
                                painter = painterResource(R.drawable.drag_handle),
                                contentDescription = "Drag handle",
                                modifier = Modifier.draggableHandle()
                            )
                        }
                    }
                }
            }
            VerticalScrollIndicator(lazyListState.canScrollForward)
        }

        ValidateCancelButtons(
            onCancel = onDismiss
        ) {

            val saveList = HoldMenuEntriesJson.encode(menuItems.filter { it.isSelected.value }.map { it.route })
            logD(HOLD_TAG) { "Saving: $saveList" }

            scope.launch {
                HoldToActivateArcSettingsStore.holdMenuEntries.set(ctx, saveList)
                onDismiss()
            }
        }
    }
}


@Composable
fun rememberHoldMenuEntries(): List<NavigationRoute> {
    val holdMenuEntriesString by HoldToActivateArcSettingsStore.holdMenuEntries.asState()

    return remember(holdMenuEntriesString) {
        HoldMenuEntriesJson.decode(holdMenuEntriesString)
    }
}

object HoldMenuEntriesJson {
    private val jsonConfig = Json {
        explicitNulls = false
        ignoreUnknownKeys = true
    }

    fun decode(json: String): List<NavigationRoute> {
        return try {
            jsonConfig
                .decodeFromString<List<NavigationRoute>>(json)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun encode(list: List<NavigationRoute>): String {
        return jsonConfig.encodeToString(list)
    }
}