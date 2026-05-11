

package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons

@Composable
fun GoogleLockingWarning(
    onSolution: () -> Unit,
    onDismiss: () -> Unit
) {

    AlertDialog(
        icon = {
            Icon(
                painter = painterResource(R.drawable.warning),
                contentDescription = null,
                tint = Color.Yellow
            )
        },
        onDismissRequest = onDismiss,
        text = {
            Text(stringResource(R.string.google_lockdown_warning))
        },
        confirmButton = {
            ValidateCancelButtons(
                validateText = stringResource(R.string.solution),
                cancelText = stringResource(R.string.dismiss),
                onCancel = onDismiss,
                onConfirm = onSolution
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = DragonShape
    )
}
