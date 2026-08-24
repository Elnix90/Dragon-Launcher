package org.elnix.dragonlauncher.ui.dragon.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.github.elnix90.core.objects.EnumSettingObject
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions
import org.elnix.dragonlauncher.ui.dragon.components.DragonGroupScope
import org.elnix.dragonlauncher.ui.dragon.generic.ActionSelectorRow

@Composable
fun DragonGroupScope.DrawerActionSelector(
    setting: EnumSettingObject<DrawerActions>,
    allowNone: Boolean = false
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val state by setting.asState()

    val actions = DrawerActions.entries
        .filter { it != DrawerActions.Disabled }
        .filter { if (!allowNone) it != DrawerActions.None else true }

    ActionSelectorRow(
        options = actions,
        selected = state,
        label = stringResource(setting.title!!),
        optionLabel = { stringResource(it.resId) },
        toggled = state != DrawerActions.Disabled,
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
