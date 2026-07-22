@file:Suppress("AssignedValueIsNeverRead", "unused")

package org.elnix.dragonlauncher.ui.dragon.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.github.elnix90.core.objects.FloatSettingObject
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.ui.composition.LocalSettingsPlacementChecker
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel

@Composable
public fun Setting(
    setting: FloatSettingObject,
    modifier: Modifier = Modifier,
    decimals: Int = 2,
    allowTextEditValue: Boolean = true,
    enabled: Boolean = true,
    customDesc: ((Float) -> String)? = null
) {
    // Craches if this setting isn't placed inside a DragonSettingsGroup
    LocalSettingsPlacementChecker.current

    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val state by setting.asState()

    var tempState by remember { mutableFloatStateOf(state) }

    LaunchedEffect(state) { tempState = state }

    SliderWithLabel(
        modifier = modifier,
        label = stringResource(setting.title!!),
        description = customDesc?.invoke(state) ?: stringResource(setting.description!!),
        value = tempState,
        valueRange = setting.allowedRange,
        enabled = enabled,
        decimals = decimals,
        allowTextEditValue = allowTextEditValue,
        resetEnabled = tempState != setting.default,
        onReset = { scope.launch { setting.reset(ctx) } },
        onDragStateChange = { scope.launch { setting.set(ctx, tempState) } }
    ) { tempState = it }
}