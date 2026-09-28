package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.permissions.PermissionGroup
import org.elnix.dragonlauncher.permissions.permissionsManager

@Composable
fun AppUsagePermissionDialog(onDismiss: () -> Unit) {
	val ctx = LocalContext.current

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(stringResource(R.string.usage_permission_required)) },
		text = { Text(stringResource(R.string.usage_permission_description)) },
		confirmButton = {
			TextButton(
				onClick = {
					ctx.permissionsManager.requestPermission(PermissionGroup.UsageStat)
					onDismiss()
				}
			) {
				Text(stringResource(R.string.open_settings))
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text(stringResource(R.string.cancel))
			}
		}
	)
}

@Composable
fun OverlayPermissionDialog(onDismiss: () -> Unit) {
	val ctx = LocalContext.current

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(stringResource(R.string.overlay_permission_required)) },
		text = { Text(stringResource(R.string.overlay_permission_description)) },
		confirmButton = {
			TextButton(
				onClick = {
					ctx.permissionsManager.requestPermission(PermissionGroup.DisplayOverOtherApps)
					onDismiss()
				}
			) {
				Text(stringResource(R.string.open_settings))
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text(stringResource(R.string.cancel))
			}
		}
	)
}
