package org.elnix.dragonlauncher.ui.dragon.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.UiConstants
import org.elnix.dragonlauncher.ui.base.withHaptic
import org.elnix.dragonlauncher.ui.dragon.text.AutoResizeableText

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ValidateCancelButtons(
    validateText: String = stringResource(R.string.save),
    cancelText: String = stringResource(R.string.cancel),
    validateEnabled: Boolean = true,
    onCancel: (() -> Unit)? = null,
    onConfirm: () -> Unit
) {

    val interactionSources = remember { List(2) { MutableInteractionSource() } }

    @Suppress("DEPRECATION")
    ButtonGroup(Modifier.fillMaxWidth()) {
        if(onCancel != null) {
            OutlinedButton(
                onClick = withHaptic(HapticFeedbackType.Reject) {
                    onCancel()
                },
                shapes = UiConstants.dragonShapes(),
                modifier = Modifier
                    .weight(1f)
                    .animateWidth(interactionSources[0]),
                interactionSource = interactionSources[0],
                colors = AppObjectsColors.cancelButtonColors(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                AutoResizeableText(
                    text = cancelText,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Button(
            onClick = withHaptic(HapticFeedbackType.Confirm) {
                onConfirm()
            },
            enabled = validateEnabled,
            modifier = Modifier
                .weight(1f)
                .animateWidth(interactionSources[1]),
            interactionSource = interactionSources[1],
            shapes = UiConstants.dragonShapes(),
        ) {
            AutoResizeableText(
                text = validateText,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
