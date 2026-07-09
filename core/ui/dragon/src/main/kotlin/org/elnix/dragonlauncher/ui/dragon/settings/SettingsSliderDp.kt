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
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel

@Composable
public fun Setting(
    setting: DpSettingObject,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    showValue: Boolean = true,
    enabled: Boolean = true,
    allowTextEditValue: Boolean = true,
    customDesc: ((Dp) -> String)? = null,
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val state by setting.asState()

    var tempState by remember { mutableStateOf(state) }

    LaunchedEffect(state) { tempState = state }

    SliderWithLabel(
        modifier = modifier,
        label = stringResource(setting.title!!),
        description = customDesc?.invoke(state) ?: stringResource(setting.description!!),
        value = tempState.value.toInt(),
        valueRange = setting.allowedRange.toIntRange(),
        color = color,
        enabled = enabled,
        allowTextEditValue = allowTextEditValue,
        backgroundColor = backgroundColor,
        showValue = showValue,
        onReset = { scope.launch { setting.reset(ctx) } },
        onDragStateChange = { scope.launch { setting.set(ctx, tempState) } },
        onChange = { tempState = it.dp }
    )
}

public fun ClosedRange<Dp>.toIntRange(): IntRange = IntRange(this.start.value.toInt(), this.endInclusive.value.toInt())