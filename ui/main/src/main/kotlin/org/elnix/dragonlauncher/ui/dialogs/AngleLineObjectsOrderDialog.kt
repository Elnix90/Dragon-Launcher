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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardDefaults.elevatedCardElevation
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elnix90.logging.ANGLE_LINE_TAG
import io.github.elnix90.logging.logE
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.base.model.models.AngleLineObjects
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.stores.map.AngleLineSettingsStore
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun AngleLineObjectsOrderDialog(
    order: List<AngleLineObjects>,
    onChange: (newOrder: List<AngleLineObjects>) -> Unit,
    onDismiss: () -> Unit
) {
    var objects by remember(order) { mutableStateOf(order) }
    LaunchedEffect(objects) {
        onChange(objects)
    }


    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            objects = objects.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        }
    )

    DragonModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        DialogTitle(stringResource(R.string.configure_draw_order)) {
            objects = AngleLineObjects.entries
        }

        LazyColumn(
            state = lazyListState,
        ) {

            items(objects, key = { it.name }) { item ->

                ReorderableItem(
                    state = reorderState,
                    key = item.name
                ) { isDragging ->

                    val scale by animateFloatAsState(if (isDragging) 1.03f else 1f)
                    val elevation by animateDpAsState(if (isDragging) 16.dp else 0.dp)

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .scale(scale)
                            .draggableHandle()
                            .longPressDraggableHandle(),
                        elevation = elevatedCardElevation(elevation),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
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
                                painter = painterResource(R.drawable.drag_handle),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
public fun rememberLineObjectsOrder(): MutableState<List<AngleLineObjects>> {
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
