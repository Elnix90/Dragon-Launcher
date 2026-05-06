package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults.elevatedCardElevation
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.ANGLE_LINE_TAG
import org.elnix.dragonlauncher.enumsui.other.AngleLineObjects
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.settings.stores.AngleLineSettingsStore
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun AngleLineObjectsOrderDialog(
    onDismiss: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val order by rememberLineObjectsOrder()

    var objects by remember { mutableStateOf(order) }
    LaunchedEffect(order) { objects = order }

    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            objects = objects.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            ValidateCancelButtons(
                onCancel = onDismiss,
                onConfirm = {
                    scope.launch {
                        AngleLineSettingsStore.angleLineObjectsOrder.set(
                            ctx,
                            objects.joinToString(",") { it.name }
                        )
                    }
                    onDismiss()
                }
            )
        },
        title = { Text(stringResource(R.string.configure_draw_order)) },
        text = {
            LazyColumn(
                state = lazyListState,
            ) {

                items(objects, key = { it.name }) { item ->

                    ReorderableItem(
                        state = reorderState,
                        key = item.name
                    ) { isDragging ->

                        val scale by animateFloatAsState(
                            if (isDragging) 1.03f else 1f
                        )

                        val elevation by animateDpAsState(
                            if (isDragging) 16.dp else 0.dp
                        )


                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .scale(scale)
                                .draggableHandle()
                                .longPressDraggableHandle(),
                            elevation = elevatedCardElevation(elevation),
                            colors = AppObjectsColors.cardColors(),
                            shape = RoundedCornerShape(12.dp)
                        ) {

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = 12.dp,
                                        vertical = 10.dp
                                    )
                            ) {

                                Text(
                                    text = stringResource(item.resId),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )

                                Icon(
                                    imageVector = Icons.Default.DragHandle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}


@Composable fun rememberLineObjectsOrder(): MutableState<List<AngleLineObjects>> {
    val orderString by AngleLineSettingsStore.angleLineObjectsOrder.asState()

    return remember(orderString) {
        val decoded: List<AngleLineObjects> =
            try {
                orderString
                    .takeIf { it.isNotEmpty() }
                    ?.split(",")
                    ?.map { AngleLineObjects.valueOf(it) }
            } catch (e: Exception) {
                logE(ANGLE_LINE_TAG, e) { "Failed to decode angle line objects order, using default value" }
                null
            } ?: AngleLineObjects.entries.toList()

        mutableStateOf(decoded)
    }
}
