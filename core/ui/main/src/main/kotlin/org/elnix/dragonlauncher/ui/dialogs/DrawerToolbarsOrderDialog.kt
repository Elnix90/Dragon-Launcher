package org.elnix.dragonlauncher.ui.dialogs

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults.elevatedCardElevation
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.messyfolder.Constants
import org.elnix.dragonlauncher.enumsui.toggle.DrawerToolbar
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.settings.stores.DrawerSettingsStore
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons
import org.elnix.dragonlauncher.ui.dragon.text.TextDividerOld
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@SuppressLint("MutableCollectionMutableState")
@Composable
fun DrawerToolbarsOrderDialog(
    onDismiss: () -> Unit,
    onSelect: (List<DrawerToolbar>) -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val showSearchBar by DrawerSettingsStore.showSearchBar.asState()
    val showRecentlyUsedApps by DrawerSettingsStore.showRecentlyUsedApps.asState()

    val selectedToolbarItemsStringSet by DrawerSettingsStore.toolbarsOrder.asState()
    val selectedToolbarItems by remember {
        derivedStateOf {
            try {
                selectedToolbarItemsStringSet.split(',').map {
                    DrawerToolbar.valueOf(it)
                }
            } catch (e: Exception) {
                logE(Constants.Logging.DRAWER_TAG, e) { "Unable to decode drawerToolbars order, using default value" }
                DrawerToolbar.entries
            }
        }
    }


    var toolbarItems by remember { mutableStateOf(selectedToolbarItems.toMutableList()) }

    LaunchedEffect(toolbarItems) {
        if (toolbarItems.size != 3) {
            // Something went wrong, reset
            toolbarItems = DrawerToolbar.entries.toMutableList()
        }
    }


    LaunchedEffect(selectedToolbarItems) {
        toolbarItems = selectedToolbarItems.toMutableList()
    }


    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            toolbarItems = toolbarItems.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        }
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        confirmButton = {

            ValidateCancelButtons(
                onCancel = onDismiss
            ) {
                onSelect(toolbarItems)
            }
        },
        title = { Text(stringResource(R.string.choose_action)) },
        text = {
            LazyColumn(
                state = lazyListState,
            ) {
                items(toolbarItems, key = { it.name }) { item ->

                    ReorderableItem(state = reorderState, key = item.name) { isDragging ->
                        val scale by animateFloatAsState(if (isDragging) 1.03f else 1f)
                        val elevation by animateDpAsState(if (isDragging) 16.dp else 0.dp)

                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .scale(scale)
                                .longPressDraggableHandle(),
                            elevation = elevatedCardElevation(elevation),
                            colors = AppObjectsColors.cardColors(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {

                                if (item == DrawerToolbar.Spacer) {
                                    TextDividerOld(
                                        text = stringResource(item.resId),
                                        thickness = 5.dp,
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    val checked = if (item == DrawerToolbar.RecentlyUsed) {
                                        showRecentlyUsedApps
                                    } else {
                                        showSearchBar
                                    }

                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = {
                                            scope.launch {
                                                if (item == DrawerToolbar.RecentlyUsed) {
                                                    DrawerSettingsStore.showRecentlyUsedApps.set(ctx, !showRecentlyUsedApps)
                                                } else {
                                                    DrawerSettingsStore.showSearchBar.set(ctx, !showSearchBar)
                                                }
                                            }
                                        }
                                    )

                                    Icon(
                                        painter = painterResource(item.iconEnabled),
                                        contentDescription = null
                                    )

                                    Text(
                                        text = stringResource(item.resId),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Icon(
                                    painter = painterResource(R.drawable.drag_handle),
                                    contentDescription = "Drag handle",
                                    modifier = Modifier.draggableHandle(),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}