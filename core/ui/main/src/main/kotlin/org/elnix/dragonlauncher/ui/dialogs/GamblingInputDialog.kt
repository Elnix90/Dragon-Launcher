package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons

@Composable
public fun GamblingInputDialog(
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val ctx = LocalContext.current
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.gamble_apps)) },
        text = {
            Column {
                TextField(
                    value = text,
                    onValueChange = {
                        text = it
                    },
                    singleLine = true,
                    label = { Text(stringResource(R.string.how_many_question)) },
                    colors = AppObjectsColors.outlinedTextFieldColors(
                        removeBorder = true,
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    )
                )
            }
        },
        confirmButton = {
            ValidateCancelButtons(
                validateText = stringResource(R.string.ok),
                onCancel = onDismiss
            ) {
                val number = try {
                    text.toInt()
                } catch (_: Exception) {
                    ctx.showToast("Wrong number format")
                    0
                }

                onSelect(number)
                onDismiss()
            }

        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
