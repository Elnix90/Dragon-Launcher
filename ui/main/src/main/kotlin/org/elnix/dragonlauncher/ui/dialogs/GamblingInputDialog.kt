package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import io.github.elnix90.runtime.asState
import io.github.elnix90.shared.theme.AppObjectsColors
import io.github.elnix90.shared.ui.dragon.components.DragonSettingsGroup
import io.github.elnix90.shared.ui.dragon.components.SwitchRow
import io.github.elnix90.shared.ui.dragon.components.ValidateCancelButtons
import io.github.elnix90.shared.ui.dragon.text.DialogTitle
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore

@Composable
fun GamblingInputDialog(
	onSelect: (number: Int, snapToShapes: Boolean) -> Unit,
	initialSnap: Boolean,
	onDismiss: () -> Unit
) {
	val ctx = LocalContext.current
	val allowFreePoints by UiSettingsStore.allowFreePoints.asState()

	var text by remember { mutableStateOf("") }
	var snapToShapes by remember { mutableStateOf(initialSnap) }

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { DialogTitle(stringResource(R.string.gamble_apps)) },
		text = {
			DragonSettingsGroup {
				TextField(
					value = text,
					onValueChange = {
						text = it
					},
					singleLine = true,
					label = { Text(stringResource(R.string.how_many_question)) },
					colors = AppObjectsColors.outlinedTextFieldColors(),
					keyboardOptions =
						KeyboardOptions(
							keyboardType = KeyboardType.Decimal,
							imeAction = ImeAction.Done
						),
					shape = CircleShape,
					modifier = Modifier.fillMaxWidth()
				)

				// Only show this when user has explicitly selected to allow free points
				AnimatedVisibility(allowFreePoints) {
					SwitchRow(
						state = snapToShapes,
						title = R.string.snap_points
					) { snapToShapes = it }
				}
			}
		},
		confirmButton = {
			ValidateCancelButtons(
				cancelText = stringResource(R.string.cancel),
				confirmText = stringResource(R.string.ok),
				onCancel = onDismiss
			) {
				val number =
					try {
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
