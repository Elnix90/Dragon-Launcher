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
import io.github.elnix90.runtime.asStateNull
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer.Companion.MainScreenLayerJson
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer.Companion.copyWithEnabled
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer.Companion.defaultMainScreenLayers
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer.Companion.enabled
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer.Companion.label
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.stores.array.MainScreenLayersSettingsStore
import org.elnix.dragonlauncher.ui.compositionslocals.LocalNavigator
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun MainScreeLayersTab() {
    val ctx = LocalContext.current
    val navigator = LocalNavigator.current
    val scope = rememberCoroutineScope()

    val order by rememberMainScreenLayerOrder()
    var objects by remember { mutableStateOf(order) }

    LaunchedEffect(order) {
        objects = order
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
        helpText = stringResource(R.string.main_screen_layers_help),
        onReset = {
            scope.launch {
                MainScreenLayersSettingsStore.jsonSetting.reset(ctx)
            }
        },
        onBack = {
            scope.launch {
                val encoded = MainScreenLayerJson.encode(objects)
                MainScreenLayersSettingsStore.jsonSetting.set(ctx, encoded)
                navigator.onBack()
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
                                        objects = objects.map {
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
                                                    objects = objects.map {
                                                        if (it is MainScreenLayer.CustomDim) it.copy(showAfterMs = MainScreenLayer.CustomDim.defaultShowAfterMs) else it
                                                    }
                                                },
                                                onDragStateChange = { isDragging ->
                                                    if (!isDragging) {
                                                        objects = objects.map {
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
                                                    objects = objects.map {
                                                        if (it is MainScreenLayer.CustomDim) it.copy(dimAmount = MainScreenLayer.CustomDim.defaultDimAmount) else it
                                                    }
                                                },
                                                onDragStateChange = { isDragging ->
                                                    if (!isDragging) {
                                                        objects = objects.map {
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
                                                    objects = objects.map {
                                                        if (it is MainScreenLayer.DragOverlay) it.copy(lineBeforeNests = MainScreenLayer.DragOverlay.defaultLineBeforeNests) else it
                                                    }
                                                }
                                            ) { newValue ->
                                                objects = objects.map {
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

@Composable
fun rememberMainScreenLayerOrder(): MutableState<List<MainScreenLayer>> {
    val orderString by MainScreenLayersSettingsStore.jsonSetting.asStateNull()

    return remember(orderString) {
        val decoded = orderString
            ?.let { MainScreenLayerJson.decode<List<MainScreenLayer>>(it) }
            ?.takeIf { layers ->
                val expectedTypes = setOf(
                    MainScreenLayer.ChargingAnimation::class,
                    MainScreenLayer.StatusBar::class, // Fuuuuuuuuuuck it imported the wrong status bar class
                    MainScreenLayer.Widgets::class,
                    MainScreenLayer.CustomDim::class,
                    MainScreenLayer.DragOverlay::class,
                    MainScreenLayer.HoldToActivate::class
                )

                layers.map { it::class }.toSet() == expectedTypes
            }
            ?: defaultMainScreenLayers

        mutableStateOf(decoded)
    }
}