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
import io.github.elnix90.core.objects.BooleanSettingObject
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.i18n.R
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.dragon.dialogs.UserValidation

@Composable
fun SettingsSwitchRow(
    setting: BooleanSettingObject,
    enabled: Boolean = true,
    needValidationToEnable: Boolean = false,
    needValidationToDisable: Boolean = false,
    confirmText: Int = R.string.are_you_sure,
    onCheck: ((Boolean) -> Unit)? = null
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val state by setting.asState()

    var tempState by remember { mutableStateOf(state) }

    var showConfirmPopup by remember { mutableStateOf<Boolean?>(null) }


    LaunchedEffect(state) {
        // Only sync tempState with setting when no popup is active
        if (showConfirmPopup == null) {
            tempState = state
        }
    }

    fun toggle(state: Boolean) {
        tempState = state
        scope.launch {
            setting.set(ctx, state)
        }
        onCheck?.invoke(state)
    }

    SwitchRow(
        state = tempState,
        title = stringResource(setting.title!!),
        description = stringResource(setting.description!!),
        enabled = enabled
    ) { clicked ->
        when {
            clicked && needValidationToEnable -> showConfirmPopup = true
            !clicked && needValidationToDisable -> showConfirmPopup = false
            else -> toggle(clicked)
        }
    }

    if (showConfirmPopup != null) {
        UserValidation(
            message = stringResource(confirmText),
            onDismiss = { showConfirmPopup = null }
        ) {
            toggle(showConfirmPopup!!)
            showConfirmPopup = null
        }
    }
}
