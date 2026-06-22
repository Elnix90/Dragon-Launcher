@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.dragon.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.elnix90.core.objects.DpSettingObject
import kotlinx.coroutines.launch
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel

@Composable
fun SettingsSlider(
    setting: DpSettingObject,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    showValue: Boolean = true,
    enabled: Boolean = true,
    allowTextEditValue: Boolean = true,
    onReset: (() -> Unit)? = null,
    onDragStateChange: ((Boolean) -> Unit)? = null,
    onChange: ((Int) -> Unit)? = null,
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val state by setting.asState()

    var tempState by remember { mutableStateOf(state) }

    LaunchedEffect(state) { tempState = state }

    SliderWithLabel(
        modifier = modifier,
        label = stringResource(setting.title!!),
        description = stringResource(setting.description!!),
        value = tempState.value.toInt(),
        valueRange = setting.allowedRange.toIntRange(),
        color = color,
        enabled = enabled,
        allowTextEditValue = allowTextEditValue,
        backgroundColor = backgroundColor,
        showValue = showValue,
        onReset = {
            scope.launch { setting.reset(ctx) }
            onReset?.invoke()
        },
        onDragStateChange = {
            scope.launch { setting.set(ctx, tempState) }
            onDragStateChange?.invoke(it)
        }
    ) {
        tempState = it.dp
        onChange?.invoke(it)
    }
}

fun ClosedRange<Dp>.toIntRange(): IntRange = IntRange(this.start.value.toInt(), this.endInclusive.value.toInt())