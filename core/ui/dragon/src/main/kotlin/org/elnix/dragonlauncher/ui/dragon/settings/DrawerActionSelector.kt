package org.elnix.dragonlauncher.ui.dragon.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.github.elnix90.core.objects.EnumSettingObject
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.ui.dragon.generic.ActionSelectorRow

@Composable
fun DrawerActionSelector(
    settingObject: EnumSettingObject<DrawerActions>,
    allowNone: Boolean = false
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val state by settingObject.asState()

    var tempState by remember { mutableStateOf(state) }

    LaunchedEffect(state) { tempState = state }

    val stateNotDisabled = tempState != DrawerActions.Disabled

    val actions = DrawerActions.entries
        .filter { it != DrawerActions.Disabled }
        .filter { if (!allowNone) it != DrawerActions.None else true }

    ActionSelectorRow(
        options = actions,
        selected = tempState,
        label = stringResource(settingObject.title!!),
        optionLabel = { stringResource(it.resId) },
        toggled = stateNotDisabled
    ) {
        tempState = it ?: DrawerActions.Disabled
        scope.launch {
            settingObject.set(ctx, tempState)
        }
    }
}
