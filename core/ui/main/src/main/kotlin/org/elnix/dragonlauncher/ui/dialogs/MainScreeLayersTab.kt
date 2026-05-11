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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults.elevatedCardElevation
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import org.elnix.dragonlauncher.common.messyfolder.isNotBlankJson
import org.elnix.dragonlauncher.common.serializables.MainScreenLayer
import org.elnix.dragonlauncher.common.serializables.MainScreenLayerJson
import org.elnix.dragonlauncher.common.serializables.copyWithEnabled
import org.elnix.dragonlauncher.common.serializables.defaultMainScreenLayers
import org.elnix.dragonlauncher.common.serializables.enabled
import org.elnix.dragonlauncher.common.serializables.label
import org.elnix.dragonlauncher.settings.stores.UiSettingsStore
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.dragon.components.DragonColumnGroup
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun MainScreeLayersTab(
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val order by rememberMainScreenLayerOrder()

    var objects by remember { mutableStateOf(order) }
    LaunchedEffect(order) { objects = order }

    fun save() {
        scope.launch {
            val encoded = MainScreenLayerJson.encode(objects)
            UiSettingsStore.mainScreenLayers.set(ctx, encoded)
        }
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

    SettingsScaffold(
        title = stringResource(R.string.main_screen_layers),
        onBack = onBack,
        helpText = stringResource(R.string.main_screen_layers_help),
        onReset = {
            scope.launch {
                UiSettingsStore.mainScreenLayers.reset(ctx)
            }
        },
        listState = lazyListState,
        resetTitle = stringResource(R.string.main_screen_layers_reset_title),
        resetText = stringResource(R.string.main_screen_layers_reset),
        lazyContent = {

            items(objects, key = { it.toString() }) { item ->

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
                            .longPressDraggableHandle(
                                onDragStopped = ::save
                            ),
                        elevation = elevatedCardElevation(elevation),
                        colors = AppObjectsColors.cardColors(),
                        shape = RoundedCornerShape(12.dp)
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
                                        objects = objects.map {
                                            if (it == item) it.copyWithEnabled(!it.enabled) else it
                                        }
                                        save()
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
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.draggableHandle(
                                        onDragStopped = ::save
                                    )
                                )
                            }

                            if (item is MainScreenLayer.CustomDim) {
                                AnimatedVisibility(item.enabled) {
                                    var tempShowAfter by remember { mutableIntStateOf(item.showAfter) }
                                    var tempDimAmount by remember { mutableFloatStateOf(item.dimAmount) }

                                    DragonColumnGroup {
                                        SliderWithLabel(
                                            value = tempShowAfter,
                                            valueRange = 0..5000,
                                            label = stringResource(R.string.show_after),
                                            description = stringResource(R.string.show_after_help),
                                            onReset = {
                                                objects = objects.map {
                                                    if (it is MainScreenLayer.CustomDim) it.copy(showAfter = 1000) else it
                                                }
                                                save()
                                            },
                                            onDragStateChange = { isDragging ->
                                                if (!isDragging) {
                                                    objects = objects.map {
                                                        if (it is MainScreenLayer.CustomDim) it.copy(showAfter = tempShowAfter) else it
                                                    }
                                                    save()
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
                                            onReset = {
                                                objects = objects.map {
                                                    if (it is MainScreenLayer.CustomDim) it.copy(dimAmount = 0.5f) else it
                                                }
                                                save()
                                            },
                                            onDragStateChange = { isDragging ->
                                                if (!isDragging) {
                                                    objects = objects.map {
                                                        if (it is MainScreenLayer.CustomDim) it.copy(dimAmount = tempDimAmount) else it
                                                    }
                                                    save()
                                                }
                                            }
                                        ) { newValue ->
                                            tempDimAmount = newValue
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun rememberMainScreenLayerOrder(): MutableState<List<MainScreenLayer>> {
    val orderString by UiSettingsStore.mainScreenLayers.asState()

    return remember(orderString) {
        val decoded =
            orderString
                .takeIf { it.isNotBlankJson }
                ?.let {
                    MainScreenLayerJson.decode(orderString)
                        .takeIf { it.size == 6 } // Ensure they have been saved
                } ?: defaultMainScreenLayers

        mutableStateOf(decoded)
    }
}