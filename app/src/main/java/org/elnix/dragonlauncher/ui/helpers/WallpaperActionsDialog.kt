package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.data.helpers.WallpaperTarget

@Composable
fun WallpaperActionsDialog(
    onApply: (WallpaperTarget) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Apply wallpaper to")
        },
        text = {
            Text("Choose where to apply the wallpaper")
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onApply(WallpaperTarget.HOME)
                    onDismiss()
                }
            ) {
                Text("Home screen")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onApply(WallpaperTarget.LOCK)
                    onDismiss()
                }
            ) {
                Text("Lock screen")
            }

            TextButton(
                onClick = {
                    onApply(WallpaperTarget.BOTH)
                    onDismiss()
                }
            ) {
                Text("Both")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(20.dp)
    )
}
