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
import io.github.elnix90.runtime.asStateNull
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer.ChargingAnimation
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer.Companion.MainScreenLayerJson
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer.Companion.copyWithEnabled
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer.Companion.defaultMainScreenLayers
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer.Companion.enabled
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer.Companion.label
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer.CustomDim
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer.DragOverlay
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer.HoldToActivate
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer.Widgets
import org.elnix.dragonlauncher.base.model.serializables.StatusBar
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.stores.objects.MainScreenLayersSettingsStore
import org.elnix.dragonlauncher.ui.dragon.components.DragonColumnGroup
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
public fun MainScreeLayersTab() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val order by rememberMainScreenLayerOrder()
    var objects by remember(order) { mutableStateOf(order) }

    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            objects = objects.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        }
    )

    fun save() {
        scope.launch {
            val encoded = MainScreenLayerJson.encode(objects)
            MainScreenLayersSettingsStore.jsonSetting.set(ctx, encoded)
        }
    }

    SettingsScaffold(
        title = stringResource(R.string.main_screen_layers),
        helpText = stringResource(R.string.main_screen_layers_help),
        onReset = {
            scope.launch {
                MainScreenLayersSettingsStore.jsonSetting.reset(ctx)
            }
        },
        lasyListState = lazyListState,
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
                            .longPressDraggableHandle(onDragStopped = ::save),
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

                            if (item is CustomDim) {
                                AnimatedVisibility(item.enabled) {
                                    var tempShowAfter by remember { mutableIntStateOf(item.showAfterMs) }
                                    var tempDimAmount by remember { mutableFloatStateOf(item.dimAmount) }

                                    DragonColumnGroup {
                                        SliderWithLabel(
                                            value = tempShowAfter,
                                            valueRange = 0..5000,
                                            label = stringResource(R.string.show_after),
                                            description = stringResource(R.string.show_after_help),
                                            resetEnabled = tempShowAfter != CustomDim.defaultShowAfterMs,
                                            onReset = {
                                                objects = objects.map {
                                                    if (it is CustomDim) it.copy(showAfterMs = CustomDim.defaultShowAfterMs) else it
                                                }
                                                save()
                                            },
                                            onDragStateChange = { isDragging ->
                                                if (!isDragging) {
                                                    objects = objects.map {
                                                        if (it is CustomDim) it.copy(showAfterMs = tempShowAfter) else it
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
                                            resetEnabled = tempDimAmount != CustomDim.defaultDimAmount,
                                            onReset = {
                                                objects = objects.map {
                                                    if (it is CustomDim) it.copy(dimAmount = CustomDim.defaultDimAmount) else it
                                                }
                                                save()
                                            },
                                            onDragStateChange = { isDragging ->
                                                if (!isDragging) {
                                                    objects = objects.map {
                                                        if (it is CustomDim) it.copy(dimAmount = tempDimAmount) else it
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
public fun rememberMainScreenLayerOrder(): MutableState<List<MainScreenLayer>> {
    val orderString by MainScreenLayersSettingsStore.jsonSetting.asStateNull()

    return remember(orderString) {
        val decoded = MainScreenLayerJson
            .decode<List<MainScreenLayer>>(orderString)
            ?.takeIf { layers ->
                val expectedTypes = setOf(
                    ChargingAnimation::class,
                    StatusBar::class,
                    Widgets::class,
                    CustomDim::class,
                    DragOverlay::class,
                    HoldToActivate::class
                )
                layers.map { it::class }.toSet() == expectedTypes
            }
            ?: defaultMainScreenLayers

        mutableStateOf(decoded)
    }
}