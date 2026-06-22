@file:Suppress("AssignedValueIsNeverRead", "unused")

package org.elnix.dragonlauncher.ui.dragon.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.github.elnix90.core.objects.FloatSettingObject
import kotlinx.coroutines.launch
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel

@Composable
fun SettingsSlider(
    setting: FloatSettingObject,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    showValue: Boolean = true,
    decimals: Int = 2,
    allowTextEditValue: Boolean = true,
    enabled: Boolean = true,
    onReset: (() -> Unit)? = null,
    onDragStateChange: ((Boolean) -> Unit)? = null,
    onChange: ((Float) -> Unit)? = null
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val state by setting.asState()

    var tempState by remember { mutableFloatStateOf(state) }

    LaunchedEffect(state) { tempState = state }

    SliderWithLabel(
        modifier = modifier,
        label = stringResource(setting.title!!),
        description = stringResource(setting.description!!),
        value = tempState,
        valueRange = setting.allowedRange,
        color = color,
        enabled = enabled,
        backgroundColor = backgroundColor,
        showValue = showValue,
        decimals = decimals,
        allowTextEditValue = allowTextEditValue,
        onReset = {
            scope.launch { setting.reset(ctx) }
            onReset?.invoke()
        },
        onDragStateChange = {
            scope.launch { setting.set(ctx, tempState) }
            onDragStateChange?.invoke(it)
        }
    ) {
        tempState = it
        onChange?.invoke(it)
    }
}