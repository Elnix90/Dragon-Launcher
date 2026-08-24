package org.elnix.dragonlauncher.ui.dragon.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.remember.rememberInteractionSource
import org.elnix.dragonlauncher.ui.base.withHaptic
import org.elnix.dragonlauncher.ui.dragon.dialogs.UserValidation


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DragonButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    needConfirm: Boolean = false,
    confirmText: String = stringResource(R.string.are_you_sure),
    isCancel: Boolean = false,
    interactionSource: MutableInteractionSource = rememberInteractionSource(),
    content: @Composable RowScope.() -> Unit,
) {
    var showConfirmPopup by remember { mutableStateOf(false) }


    val colors = if (isCancel) AppObjectsColors.cancelButtonColors() else AppObjectsColors.buttonColors()
    Button(
        modifier = modifier,
        onClick = withHaptic {
            if (needConfirm) showConfirmPopup = true
            else onClick()
        },
        shapes = ButtonDefaults.shapes(),
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
        content = content
    )

    if (showConfirmPopup) {
        UserValidation(
            message = confirmText,
            onDismiss = { showConfirmPopup = false }
        ) {
            onClick()
            showConfirmPopup = false
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DragonGroupScope.DragonButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    needConfirm: Boolean = false,
    confirmText: String = stringResource(R.string.are_you_sure),
    isCancel: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    DragonButton(
        onClick = onClick,
        modifier = Modifier.dragonSettingGroup(enabled = enabled),
        enabled = enabled,
        needConfirm = needConfirm,
        confirmText = confirmText,
        isCancel = isCancel,
        content = content
    )
}
