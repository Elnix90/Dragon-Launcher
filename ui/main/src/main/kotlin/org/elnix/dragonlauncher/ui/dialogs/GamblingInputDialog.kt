package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle

@Composable
public fun GamblingInputDialog(
    onSelect: (number: Int, snapToShapes: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val ctx = LocalContext.current

    var text by remember { mutableStateOf("") }
    var snapToShapes by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { DialogTitle(stringResource(R.string.gamble_apps)) },
        text = {
            Column {
                TextField(
                    value = text,
                    onValueChange = {
                        text = it
                    },
                    singleLine = true,
                    label = { Text(stringResource(R.string.how_many_question)) },
                    colors = AppObjectsColors.outlinedTextFieldColors(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    shape = CircleShape,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(5.dp)

                SwitchRow(
                    state = snapToShapes,
                    title = stringResource(R.string.snap_points),
                    modifier = Modifier.clip(MaterialTheme.shapes.large)
                ) { snapToShapes = it }
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

                onSelect(number, snapToShapes)
                onDismiss()
            }

        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
