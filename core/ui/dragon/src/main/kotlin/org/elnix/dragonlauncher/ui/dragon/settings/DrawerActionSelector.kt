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
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions
import org.elnix.dragonlauncher.settings.bases.BaseSettingObject
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.dragon.generic.ActionSelectorRow

@Composable
fun DrawerActionSelector(
    settingObject: BaseSettingObject<DrawerActions, String>,
    label: String,
    allowNone: Boolean = false
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val state by settingObject.asState()

    var tempState by remember { mutableStateOf(state) }

    LaunchedEffect(state) { tempState = state }

    val stateNotDisabled = tempState != DrawerActions.DISABLED

    val actions = DrawerActions.entries
        .filter { it != DrawerActions.DISABLED }
        .filter { if (!allowNone) it != DrawerActions.NONE else true }

    ActionSelectorRow(
        options = actions,
        selected = tempState,
        label = label,
        optionLabel = { stringResource(it.resId) },
        toggled = stateNotDisabled
    ) {
        tempState = it ?: DrawerActions.DISABLED
        scope.launch {
            settingObject.set(ctx, tempState)
        }
    }
}
