package org.elnix.dragonlauncher.ui.dialogs

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.Constants.Settings.MAX_ITEMS_ALLOWED
import org.elnix.dragonlauncher.base.model.enumsui.toggle.BackupSelectStoresButtons
import org.elnix.dragonlauncher.base.navigation.NavigationRoute
import org.elnix.dragonlauncher.base.navigation.NavigationRoute.Companion.settingsRoutes
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.models.SwipeViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.components.VerticalScrollIndicator
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonRow
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

private data class MenuItem(
    val route: NavigationRoute,
    val isSelected: MutableState<Boolean>
)

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoldSettingsOrderSheet(
    swipeViewModel: SwipeViewModel = activityViewModel(),
    onDismiss: () -> Unit
) {
    val ctx = LocalContext.current
    val swipeService = swipeViewModel.swipeService

    val holdMenuEntries by swipeService.holdMenuEntriesString.asState()

    // I don't want to put this in the viewmodel as it might be a lot of boilerplate, so I fall back to retain API
    var menuItems: List<MenuItem> by retain { mutableStateOf(emptyList()) }

    LaunchedEffect(holdMenuEntries) {
        menuItems =
            buildList {
                settingsRoutes
                    .sortedBy { route ->
                        holdMenuEntries.indexOf(route).let {
                            if (it == -1) Int.MAX_VALUE else it
                        }
                    }.forEach { route ->
                        add(
                            MenuItem(
                                route = route,
                                isSelected = mutableStateOf(route in holdMenuEntries)
                            )
                        )
                    }
            }
    }

    val lazyListState = rememberLazyListState()
    val reorderState =
        rememberReorderableLazyListState(
            lazyListState = lazyListState,
            onMove = { from, to ->
                menuItems =
                    menuItems.toMutableList().apply {
                        add(to.index, removeAt(from.index))
                    }
            }
        )

    DragonModalBottomSheet(
        onDismissRequest = {
            // Save in the reordered state, but only selected items
            swipeService.holdMenuEntriesString.value =
                menuItems
                    .filter { it.isSelected.value }
                    .map { it.route }

            swipeService.saveHoldMenuEntries()
            onDismiss()
        },
        skipPartiallyExpanded = true
    ) {
        DialogTitle(stringResource(R.string.edit_hold_to_activate_elements))
        val selectedCount = menuItems.count { it.isSelected.value }

        MultiSelectConnectedButtonRow(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            entries = BackupSelectStoresButtons.entries,
            enabled = { entry ->
                when (entry) {
                    BackupSelectStoresButtons.DeselectAll -> selectedCount > 0
                    BackupSelectStoresButtons.SelectAll -> selectedCount < settingsRoutes.size
                    BackupSelectStoresButtons.Invert -> true
                }
            }
        ) { button ->
            when (button) {
                BackupSelectStoresButtons.DeselectAll -> {
                    menuItems.filter { it.route !is NavigationRoute.PointsSettings }.forEach { item ->
                        item.isSelected.value = false
                    }
                }

                BackupSelectStoresButtons.SelectAll -> {
                    menuItems.filter { it.route !is NavigationRoute.PointsSettings }.forEach { item ->
                        item.isSelected.value = true
                    }
                }

                BackupSelectStoresButtons.Invert -> {
                    menuItems.filter { it.route !is NavigationRoute.PointsSettings }.forEach { item ->
                        item.isSelected.value = !item.isSelected.value
                    }
                }
            }
        }

        Box(modifier = Modifier.heightIn(max = 600.dp)) {
            LazyColumn(
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                items(menuItems, key = { it.route.toString() }) { entry ->
                    val isSelected by entry.isSelected

                    ReorderableItem(
                        state = reorderState,
                        key = entry.route.toString()
                    ) { isDragging ->
                        val scale by animateFloatAsState(if (isDragging) 1.03f else 1f)

                        val isEnabled = entry.route != NavigationRoute.PointsSettings

                        DragonRow(
                            onClick = {
                                when {
                                    !isEnabled -> ctx.showToast(ctx.getString(R.string.cant_remove_to_avoid_lock_out))
                                    selectedCount >= MAX_ITEMS_ALLOWED && !entry.isSelected.value ->
                                        ctx.showToast(
                                            ctx.getString(R.string.cannot_add_more_than_x, MAX_ITEMS_ALLOWED)
                                        )
                                    else -> entry.isSelected.value = !isSelected
                                }
                            },
                            modifier =
                                Modifier
                                    .scale(scale)
                                    .longPressDraggableHandle()
                        ) {
                            Checkbox(
                                checked = isSelected,
                                enabled = isEnabled,
                                onCheckedChange = null
                            )
                            Spacer(15.dp)
                            Icon(
                                painter = painterResource(entry.route.icon),
                                contentDescription = "Entry icon"
                            )
                            Spacer(5.dp)
                            Text(
                                text = stringResource(entry.route.resId),
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
            VerticalScrollIndicator(lazyListState.canScrollBackward, true)
            VerticalScrollIndicator(lazyListState.canScrollForward)
        }
    }
}
