package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.yield
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.dragon.components.ResetIcon
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons


@Composable
fun TextEditorDialog(
    title: @Composable () -> String,
    placeHolder: @Composable () -> String,
    defaultText: String,
    initialText: String,
    onDismiss: () -> Unit,
    onValidate: (String?) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    var textFieldValue by remember { mutableStateOf(TextFieldValue(initialText, TextRange(initialText.length))) }
    LaunchedEffect(Unit) {
        yield()
        focusRequester.requestFocus()
    }

    AlertDialog(
        title = {
            val title = title()
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    painter = painterResource(R.drawable.edit_rounded),
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        onDismissRequest = onDismiss,
        text = {
            TextField(
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
                singleLine = true,
                trailingIcon = {
                    ResetIcon(textFieldValue.text != defaultText) {
                        textFieldValue = TextFieldValue(defaultText, TextRange(defaultText.length))
                    }
                },
                placeholder = {
                    Text(placeHolder())
                },
                colors = AppObjectsColors.outlinedTextFieldColors(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onValidate(textFieldValue.text.trim()) }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape),
                maxLines = 1
            )
        },
        confirmButton = {
            ValidateCancelButtons(
                validateEnabled = textFieldValue.text.trim() != "",
                onCancel = onDismiss
            ) { onValidate(textFieldValue.text.trim()) }
        },
        dismissButton = {},
        modifier = Modifier.focusRequester(focusRequester),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large
    )
}
