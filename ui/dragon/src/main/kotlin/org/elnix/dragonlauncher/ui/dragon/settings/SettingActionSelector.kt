package org.elnix.dragonlauncher.ui.dragon.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.github.elnix90.core.objects.EnumSettingObject
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.ui.dragon.components.DragonGroupScope
import org.elnix.dragonlauncher.ui.dragon.generic.ActionSelectorRow

@Composable
inline fun <reified T : Enum<T>> DragonGroupScope.Setting(
    setting: EnumSettingObject<T>,
    enabled: Boolean = true
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val state: T by setting.asState()

    // If this works, well, I'm a genius
    val actions: List<T> = T::class.java.enumConstants!!.toList()

    ActionSelectorRow(
        options = actions,
        selected = state,
        label = stringResource(setting.title!!),
        optionLabel = { it.name },
        enabled = enabled,
        resetEnabled = state != setting.default,
        onReset = {
            scope.launch {
                setting.reset(ctx)
            }
        }
    ) {
        scope.launch {
            setting.set(ctx, it)
        }
    }
}
