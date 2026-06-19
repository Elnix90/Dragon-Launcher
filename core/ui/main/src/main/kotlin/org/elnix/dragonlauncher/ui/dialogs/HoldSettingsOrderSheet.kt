package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.DragonJson
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute.Companion.settingsRoutes
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute.Settings.routeResId
import org.elnix.dragonlauncher.enumsui.toggle.BackupSelectStoresButtons
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.logging.HOLD_TAG
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.settings.stores.map.HoldToActivateArcSettingsStore
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.components.VerticalScrollIndicator
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonRow
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonRow
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState


private data class MenuItem(
    val route: NavigationRoute,
    val isSelected: MutableState<Boolean>,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoldSettingsOrderSheet(onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val holdMenuEntries by rememberHoldMenuEntries()
    var menuItems: List<MenuItem> by remember {
        mutableStateOf(emptyList())
    }

    LaunchedEffect(holdMenuEntries) {
        menuItems = buildList {
            settingsRoutes
                .sortedBy { route ->
                    holdMenuEntries.indexOf(route).let {
                        if (it == -1) Int.MAX_VALUE else it
                    }
                }.forEach { route ->
                    add(
                        MenuItem(
                            route = route,
                            isSelected = mutableStateOf(route in holdMenuEntries),
                        )
                    )
                }
        }
    }

    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            menuItems = menuItems.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        }
    )

    DragonModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(true)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        ){
            Text(stringResource(R.string.edit_hold_to_activate_elements))

            MultiSelectConnectedButtonRow(
                entries = BackupSelectStoresButtons.entries,
                enabled = { entry ->
                    val selectedCount = menuItems.count { it.isSelected.value }
                    when (entry) {
                        BackupSelectStoresButtons.DeselectAll -> selectedCount > 0
                        BackupSelectStoresButtons.SelectAll -> selectedCount < settingsRoutes.size
                        BackupSelectStoresButtons.Invert -> true
                    }
                }
            ) {
                when (it) {
                    BackupSelectStoresButtons.DeselectAll -> {
                        menuItems.forEach { item ->
                            item.isSelected.value = false
                        }
                    }

                    BackupSelectStoresButtons.SelectAll -> {
                        menuItems.forEach { item ->
                            item.isSelected.value = true
                        }
                    }

                    BackupSelectStoresButtons.Invert -> {
                        menuItems.forEach { item ->
                            item.isSelected.value = !item.isSelected.value
                        }
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
                        key = entry.route.toString()
                    ) { isDragging ->
                        val scale by animateFloatAsState(if (isDragging) 1.03f else 1f)

                        val isEnabled = entry.route != NavigationRoute.PointsSettings
                        val errorMessage = stringResource(R.string.cant_remove_to_avoid_lock_out)

                        DragonRow(
                            onClick = {
                                if (isEnabled) {
                                    entry.isSelected.value = !isSelected
                                } else {
                                    ctx.showToast(errorMessage)
                                }
                            },
                            modifier = Modifier
                                .scale(scale)
                                .longPressDraggableHandle()
                        ) {
                            Checkbox(
                                checked = isSelected,
                                enabled = isEnabled,
                                onCheckedChange = null
                            )
                            Spacer(5.dp)
                            Text(
                                text = stringResource(routeResId(entry.route)),
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
            // Save in the reordered state, but only selected items
            val saveList = HoldMenuEntriesJson.encode(
                menuItems
                    .filter { it.isSelected.value }
                    .map { it.route }
            )
            logD(HOLD_TAG) { "Saving: $saveList" }

            scope.launch {
                HoldToActivateArcSettingsStore.holdMenuEntries.set(ctx, saveList)
                onDismiss()
            }
        }
    }
}

//private data class MenuItem(
//    val route: NavigationRoute,
//    val isSelected: MutableState<Boolean>,
//)
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun HoldSettingsOrderSheet(onDismiss: () -> Unit) {
//    val ctx = LocalContext.current
//    val scope = rememberCoroutineScope()
//
//    val holdMenuEntries = rememberHoldMenuEntries()
//    var menuItems: List<MenuItem> by remember {
//        mutableStateOf(emptyList())
//    }
//
//    LaunchedEffect(holdMenuEntries) {
//        menuItems = buildList {
//            settingsRoutes.forEach { route ->
//                add(
//                    MenuItem(
//                        route = route,
//                        isSelected = mutableStateOf(route in holdMenuEntries),
//                    )
//                )
//            }
//        }
//    }
//
//    val lazyListState = rememberLazyListState()
//    val reorderState = rememberReorderableLazyListState(
//        lazyListState = lazyListState,
//        onMove = { from, to ->
//            menuItems = menuItems.toMutableList().apply {
//                add(to.index, removeAt(from.index))
//            }
//        }
//    )
//
//    DragonModalBottomSheet(
//        onDismissRequest = onDismiss,
//        sheetState = rememberModalBottomSheetState(true)
//    ) {
//
//        Text(stringResource(R.string.edit_hold_to_activate_elements))
//
//        MultiSelectConnectedButtonRow(
//            entries = BackupSelectStoresButtons.entries,
//            enabled = { entry ->
//                val selectedCount = menuItems.count { it.isSelected.value }
//                when (entry) {
//                    BackupSelectStoresButtons.DeselectAll -> selectedCount > 0
//                    BackupSelectStoresButtons.SelectAll -> selectedCount < settingsRoutes.size
//                    BackupSelectStoresButtons.Invert -> true
//                }
//            }
//        ) {
//            when (it) {
//                BackupSelectStoresButtons.DeselectAll -> {
//                    menuItems.forEach { item ->
//                        item.isSelected.value = false
//                    }
//                }
//
//                BackupSelectStoresButtons.SelectAll -> {
//                    menuItems.forEach { item ->
//                        item.isSelected.value = true
//                    }
//                }
//
//                BackupSelectStoresButtons.Invert -> {
//                    menuItems.forEach { item ->
//                        item.isSelected.value = !item.isSelected.value
//                    }
//                }
//            }
//        }
//
//        Box(
//            modifier = Modifier
//                .heightIn(max = 600.dp)
//                .weight(1f)
//        ) {
//            LazyColumn(state = lazyListState) {
//                items(menuItems, key = { it.route.toString() }) { entry ->
//                    val isSelected by entry.isSelected
//
//                    ReorderableItem(
//                        state = reorderState,
//                        key = entry.route.toString()
//                    ) { isDragging ->
//                        val scale by animateFloatAsState(if (isDragging) 1.03f else 1f)
//
//                        val isEnabled = entry.route != NavigationRoute.PointsSettings
//                        val errorMessage = stringResource(R.string.cant_remove_to_avoid_lock_out)
//
//                        DragonRow(
//                            onClick = {
//                                if (isEnabled) {
//                                    entry.isSelected.value = !isSelected
//                                } else {
//                                    ctx.showToast(errorMessage)
//                                }
//                            },
//                            modifier = Modifier
//                                .scale(scale)
//                                .longPressDraggableHandle()
//                        ) {
//                            Checkbox(
//                                checked = isSelected,
//                                enabled = isEnabled,
//                                onCheckedChange = null
//                            )
//                            Spacer(5.dp)
//                            Text(
//                                stringResource(routeResId(entry.route)),
//                                modifier = Modifier.weight(1f)
//                            )
//                            Icon(
//                                painter = painterResource(R.drawable.drag_handle),
//                                contentDescription = "Drag handle",
//                                modifier = Modifier.draggableHandle()
//                            )
//                        }
//                    }
//                }
//            }
//            VerticalScrollIndicator(lazyListState.canScrollForward)
//        }
//
//        ValidateCancelButtons(
//            onCancel = onDismiss
//        ) {
//            val saveList = HoldMenuEntriesJson.encode(menuItems.filter { it.isSelected.value }.map { it.route })
//            logD(HOLD_TAG) { "Saving: $saveList" }
//
//            scope.launch {
//                HoldToActivateArcSettingsStore.holdMenuEntries.set(ctx, saveList)
//                onDismiss()
//            }
//        }
//    }
//}
//
//


/**
 * Decodes the hold menu entries from the [HoldToActivateArcSettingsStore], and decode them using the [HoldMenuEntriesJson] object
 *
 * Applies a safe modification to the returned list:
 *  - If the decoded value fails, it returns empty list, that'll be interpreted as directly going to the settings root.
 *  - If the list isn't `null`, but contains things, it checks whether if the list contains at least a [NavigationRoute.PointsSettings] element, and if not adds in to the list
 *
 *  In the compose screen, [org.elnix.dragonlauncher.ui.MainAppUi] the list is interpreted and triggers either the popup menu
 *  Since recently, you can no more add a single screen that is not the poins settings, because otherwise it would mean that you are locked out of settings.
 *
 *  @return
 */
@Composable
fun rememberHoldMenuEntries(): State<List<NavigationRoute>> {
    val holdMenuEntriesString by HoldToActivateArcSettingsStore.holdMenuEntries.asState()

    return retain(holdMenuEntriesString) {
        derivedStateOf {
            HoldMenuEntriesJson.decode<List<NavigationRoute>>(holdMenuEntriesString, emptyList())
                .toMutableList()
                .apply {
                    if (!this.any { it is NavigationRoute.PointsSettings }) {
                        add(0, NavigationRoute.PointsSettings())
                    }
                }
        }
    }
}


private object HoldMenuEntriesJson : DragonJson<List<NavigationRoute>>()