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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.SwipeViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AngleLineObjectsOrderDialog(
    swipeViewModel: SwipeViewModel = activityViewModel(),
    onDismiss: () -> Unit
) {
    val swipeService = swipeViewModel.swipeService
    val angleLineObjects by swipeService.lineObjectOrder.asState()


    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            swipeService.lineObjectOrder.value = angleLineObjects.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        }
    )

    DragonModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        DialogTitle(stringResource(R.string.configure_draw_order)) {
            swipeService.resetAngleLineOrder()
        }

        LazyColumn(
            state = lazyListState,
        ) {

            items(angleLineObjects, key = { it.name }) { item ->

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