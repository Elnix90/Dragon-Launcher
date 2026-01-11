package org.elnix.dragonlauncher.ui.settings.customization

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.stores.BehaviorSettingsStore
import org.elnix.dragonlauncher.data.stores.UiSettingsStore
import org.elnix.dragonlauncher.ui.helpers.CustomActionSelector
import org.elnix.dragonlauncher.ui.helpers.SliderWithLabel
import org.elnix.dragonlauncher.ui.helpers.SwitchRow
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsLazyHeader
import org.elnix.dragonlauncher.utils.models.AppsViewModel


@Composable
fun BehaviorTab(
    appsViewModel: AppsViewModel,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val backAction by BehaviorSettingsStore.getBackAction(ctx).collectAsState(initial = null)
    val doubleClickAction by BehaviorSettingsStore.getDoubleClickAction(ctx).collectAsState(initial = null)
    val keepScreenOn by BehaviorSettingsStore.getKeepScreenOn(ctx).collectAsState(initial = false)
    val leftPadding by BehaviorSettingsStore.getLeftPadding(ctx).collectAsState(initial = 0)
    val rightPadding by BehaviorSettingsStore.getRightPadding(ctx).collectAsState(initial = 0)
    val upPadding by BehaviorSettingsStore.getUpPadding(ctx).collectAsState(initial = 0)
    val downPadding by BehaviorSettingsStore.getDownPadding(ctx).collectAsState(initial = 0)

    val isDragging = remember { mutableStateOf(false) }

    SettingsLazyHeader(
        title = stringResource(R.string.behavior),
        onBack = onBack,
        helpText = stringResource(R.string.behavior_help),
        onReset = {
            scope.launch {
                UiSettingsStore.resetAll(ctx)
            }
        }
    ) {
        item {
            SwitchRow(
                keepScreenOn,
                stringResource(R.string.keep_screen_on),
            ) {
                scope.launch { BehaviorSettingsStore.setKeepScreenOn(ctx, it) }
            }
        }

        item {
            CustomActionSelector(
                appsViewModel = appsViewModel,
                currentAction = backAction,
                label = stringResource(R.string.back_action),
                onToggle = {
                    scope.launch {
                        BehaviorSettingsStore.setBackAction(ctx, null)
                    }
                }
            ) {
                scope.launch {
                    BehaviorSettingsStore.setBackAction(ctx, it)
                }
            }
        }

        item {
            CustomActionSelector(
                appsViewModel = appsViewModel,
                currentAction = doubleClickAction,
                label = stringResource(R.string.double_click_action),
                onToggle = {
                    scope.launch {
                        BehaviorSettingsStore.setDoubleClickAction(ctx, null)
                    }
                }
            ) {
                scope.launch {
                    BehaviorSettingsStore.setDoubleClickAction(ctx, it)
                }
            }
        }

        item {
            SliderWithLabel(
                label = stringResource(R.string.left_padding),
                value = leftPadding,
                valueRange = 0..300,
                color = MaterialTheme.colorScheme.primary,
                showValue = true,
                onReset = {
                    scope.launch {
                        BehaviorSettingsStore.setLeftPadding(ctx, 60)
                    }
                },
                onChange = {
                    scope.launch {
                        BehaviorSettingsStore.setLeftPadding(ctx, it)
                    }
                },
                onDragStateChange = { dragging ->
                    isDragging.value = dragging
                }
            )
        }

        item {
            SliderWithLabel(
                label = stringResource(R.string.right_padding),
                value = rightPadding,
                valueRange = 0..300,
                color = MaterialTheme.colorScheme.primary,
                showValue = true,
                onReset = {
                    scope.launch {
                        BehaviorSettingsStore.setRightPadding(ctx, 60)
                    }
                },
                onChange = {
                    scope.launch {
                        BehaviorSettingsStore.setRightPadding(ctx, it)
                    }
                },
                onDragStateChange = { dragging ->
                    isDragging.value = dragging
                }
            )
        }

        item {
            SliderWithLabel(
                label = stringResource(R.string.up_padding),
                value = upPadding,
                valueRange = 0..300,
                color = MaterialTheme.colorScheme.primary,
                showValue = true,
                onReset = {
                    scope.launch {
                        BehaviorSettingsStore.setUpPadding(ctx, 80)
                    }
                },
                onChange = {
                    scope.launch {
                        BehaviorSettingsStore.setUpPadding(ctx, it)
                    }
                },
                onDragStateChange = { dragging ->
                    isDragging.value = dragging
                }
            )
        }

        item {
            SliderWithLabel(
                label = stringResource(R.string.down_padding),
                value = downPadding,
                valueRange = 0..300,
                color = MaterialTheme.colorScheme.primary,
                showValue = true,
                onReset = {
                    scope.launch {
                        BehaviorSettingsStore.setDownPadding(ctx, 100)
                    }
                },
                onChange = {
                    scope.launch {
                        BehaviorSettingsStore.setDownPadding(ctx, it)
                    }
                },
                onDragStateChange = { dragging ->
                    isDragging.value = dragging
                }
            )
        }
    }


    if (isDragging.value){
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                color = Color(0x55FF0000),
                topLeft = Offset(
                    leftPadding.toFloat(),
                    upPadding.toFloat()
                ),
                size = Size(
                    size.width - leftPadding - rightPadding.toFloat(),
                    size.height - upPadding - downPadding.toFloat()
                )
            )
        }
    }
}
