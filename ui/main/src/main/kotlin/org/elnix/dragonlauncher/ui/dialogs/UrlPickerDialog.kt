package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import io.github.elnix90.shared.theme.AppObjectsColors
import io.github.elnix90.shared.ui.dragon.components.ValidateCancelButtons
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.i18n.R

@Composable
fun UrlInputDialog(
	onDismiss: () -> Unit,
	onUrlSelected: (Action.OpenUrl) -> Unit
) {
	var text by remember { mutableStateOf("https://") }
	var error by remember { mutableStateOf(false) }

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(stringResource(R.string.enter_url)) },
		text = {
			Column {
				TextField(
					value = text,
					onValueChange = {
						text = it
						error = false
					},
					singleLine = true,
					label = { Text("https://example.com") },
					colors =
						AppObjectsColors.outlinedTextFieldColors(
							removeBorder = true,
							backgroundColor = MaterialTheme.colorScheme.surfaceVariant
						)
				)
				if (error) {
					Text(stringResource(R.string.invalid_url), color = Color.Red)
				}
			}
		},
		confirmButton = {
			ValidateCancelButtons(
				cancelText = stringResource(R.string.cancel),
				confirmText = stringResource(R.string.save),
				onCancel = onDismiss
			) {
				val ok = text.startsWith("http://") || text.startsWith("https://")
				if (!ok) {
					error = true
					return@ValidateCancelButtons
				}
				onUrlSelected(Action.OpenUrl(text))
				onDismiss()
			}
		}
	)
}
