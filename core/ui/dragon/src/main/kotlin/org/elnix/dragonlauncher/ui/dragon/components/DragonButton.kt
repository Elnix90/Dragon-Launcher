package org.elnix.dragonlauncher.ui.dragon.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupScope
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
import org.elnix.dragonlauncher.ui.base.withHaptic
import org.elnix.dragonlauncher.ui.dragon.dialogs.UserValidation


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
public fun DragonButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    needConfirm: Boolean = false,
    confirmText: String = stringResource(R.string.are_you_sure),
    colors: ButtonColors = AppObjectsColors.buttonColors(),
    content: @Composable RowScope.() -> Unit,
) {
    var showConfirmPopup by remember { mutableStateOf(false) }


    Button(
        modifier = modifier,
        onClick = withHaptic {
            if (needConfirm) showConfirmPopup = true
            else onClick()
        },
        shapes = ButtonDefaults.shapes(),
        enabled = enabled,
        colors = colors,
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
public fun ButtonGroupScope.DragonButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    needValidation: Boolean = false,
    confirmText: String = stringResource(R.string.are_you_sure),
    colors: ButtonColors = AppObjectsColors.buttonColors(),
    content: @Composable RowScope.() -> Unit,
) {
    var showConfirmPopup by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }

    Button(
        modifier = modifier
            .weight(1f)
            .animateWidth(interactionSource),
        onClick = withHaptic {
            if (needValidation) showConfirmPopup = true
            else onClick()
        },
        shapes = ButtonDefaults.shapes(),
        enabled = enabled,
        colors = colors,
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