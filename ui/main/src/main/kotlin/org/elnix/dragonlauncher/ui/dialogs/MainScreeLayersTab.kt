package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardDefaults.elevatedCardElevation
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer.Companion.copyWithEnabled
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer.Companion.label
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.SwipeViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.compositionslocals.LocalNavigator
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun MainScreeLayersTab(
    swipeViewModel: SwipeViewModel = activityViewModel()
) {
    val navigator = LocalNavigator.current

    val order by swipeViewModel.mainScreenLayerOrder.asState()

    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            swipeViewModel.mainScreenLayerOrder.value = order.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        }
    )

    SettingsScaffold(
        title = stringResource(R.string.main_screen_layers),
        helpText = stringResource(R.string.main_screen_layers_help),
        onReset = {
            swipeViewModel.resetMainScreenLayers()
        },
        onBack = {
            swipeViewModel.saveMainScreenLayers()
            navigator.onBack()
        },
        lasyListState = lazyListState,
        resetTitle = stringResource(R.string.main_screen_layers_reset_title),
        resetText = stringResource(R.string.main_screen_layers_reset),
        lazyContent = {
            items(order, key = { it.toString() }) { item ->

                ReorderableItem(
                    state = reorderState,
                    key = item.toString()
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
                            .scale(scale)
                            .longPressDraggableHandle(),
                        elevation = elevatedCardElevation(elevation),
                        shape = CardDefaults.shape
                    ) {

                        Column(
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 12.dp,
                                    vertical = 10.dp
                                )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    onCheckedChange = {
                                        swipeViewModel.mainScreenLayerOrder.value = order.map {
                                            if (it == item) it.copyWithEnabled(!it.enabled) else it
                                        }
                                    },
                                    checked = item.enabled
                                )

                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )

                                Icon(
                                    painter = painterResource(R.drawable.drag_handle),
                                    contentDescription = null,
                                    modifier = Modifier.draggableHandle()
                                )
                            }


                            @Suppress("UnusedExpression")
                            when (item) {
                                is MainScreenLayer.CustomDim -> {
                                    AnimatedVisibility(item.enabled) {
                                        var tempShowAfter by remember { mutableIntStateOf(item.showAfterMs) }
                                        var tempDimAmount by remember { mutableFloatStateOf(item.dimAmount) }

                                        DragonSettingsGroup {
                                            SliderWithLabel(
                                                value = tempShowAfter,
                                                valueRange = 0..5000,
                                                label = stringResource(R.string.show_after),
                                                description = stringResource(R.string.show_after_help),
                                                resetEnabled = tempShowAfter != MainScreenLayer.CustomDim.defaultShowAfterMs,
                                                onReset = {
                                                    swipeViewModel.mainScreenLayerOrder.value = order.map {
                                                        if (it is MainScreenLayer.CustomDim) it.copy(showAfterMs = MainScreenLayer.CustomDim.defaultShowAfterMs) else it
                                                    }
                                                },
                                                onDragStateChange = { isDragging ->
                                                    if (!isDragging) {
                                                        swipeViewModel.mainScreenLayerOrder.value = order.map {
                                                            if (it is MainScreenLayer.CustomDim) it.copy(showAfterMs = tempShowAfter) else it
                                                        }
                                                    }
                                                }
                                            ) { newValue ->
                                                tempShowAfter = newValue
                                            }

                                            SliderWithLabel(
                                                value = tempDimAmount,
                                                valueRange = 0f..1f,
                                                label = stringResource(R.string.dim_amount),
                                                description = stringResource(R.string.dim_amount_help),
                                                resetEnabled = tempDimAmount != MainScreenLayer.CustomDim.defaultDimAmount,
                                                onReset = {
                                                    swipeViewModel.mainScreenLayerOrder.value = order.map {
                                                        if (it is MainScreenLayer.CustomDim) it.copy(dimAmount = MainScreenLayer.CustomDim.defaultDimAmount) else it
                                                    }
                                                },
                                                onDragStateChange = { isDragging ->
                                                    if (!isDragging) {
                                                        swipeViewModel.mainScreenLayerOrder.value = order.map {
                                                            if (it is MainScreenLayer.CustomDim) it.copy(dimAmount = tempDimAmount) else it
                                                        }
                                                    }
                                                }
                                            ) { newValue ->
                                                tempDimAmount = newValue
                                            }
                                        }
                                    }
                                }

                                is MainScreenLayer.DragOverlay -> {
                                    AnimatedVisibility(item.enabled) {
                                        @Suppress("SimplifyBooleanWithConstants")
                                        DragonSettingsGroup {
                                            SwitchRow(
                                                state = item.lineBeforeNests,
                                                title = R.string.line_before_nests,
                                                resetEnabled = item.lineBeforeNests != MainScreenLayer.DragOverlay.defaultLineBeforeNests,
                                                onReset = {
                                                    swipeViewModel.mainScreenLayerOrder.value = order.map {
                                                        if (it is MainScreenLayer.DragOverlay) it.copy(lineBeforeNests = MainScreenLayer.DragOverlay.defaultLineBeforeNests) else it
                                                    }
                                                }
                                            ) { newValue ->
                                                swipeViewModel.mainScreenLayerOrder.value = order.map {
                                                    if (it is MainScreenLayer.DragOverlay) it.copy(lineBeforeNests = newValue) else it
                                                }
                                            }
                                        }
                                    }
                                }

                                else -> null
                            }
                        }
                    }
                }
            }
        }
    )
}
