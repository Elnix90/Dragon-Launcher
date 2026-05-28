@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.dragon.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel

@Composable
fun SettingsSlider(
    setting: BaseSettingObject<Int, Int>,
    title: String,
    valueRange: IntRange,
    modifier: Modifier = Modifier,
    description: String? = null,
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

    var tempState by remember { mutableIntStateOf(state) }

    LaunchedEffect(state) { tempState = state }

    SliderWithLabel(
        modifier = modifier,
        label = title,
        description = description,
        value = tempState,
        valueRange = valueRange,
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
        tempState = it
        onChange?.invoke(it)
    }
}
