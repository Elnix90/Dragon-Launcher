package org.elnix.dragonlauncher.ui.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.R

private data class DialogEntry(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val backgroundColor: androidx.compose.ui.graphics.Color,
    val iconTint: androidx.compose.ui.graphics.Color,
    val onClick: () -> Unit
)

@Composable
fun AppLongPressDialog(
    app: AppModel,
    // Normal
    onOpen: (() -> Unit)? = null,
    onSettings: (() -> Unit)? = null,
    onUninstall: (() -> Unit)? = null,
    // Workspace
    onRemoveFromWorkspace: (() -> Unit)? = null,
    onAddToWorkspace: (() -> Unit)? = null,
    onRenameApp: (() -> Unit)? = null,
    onChangeAppIcon: (() -> Unit)? = null,
    onResetAppIcon: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {

    val entries = buildList {
        onOpen?.let {
            add(
                DialogEntry(
                    label = stringResource(R.string.open),
                    icon = Icons.AutoMirrored.Filled.OpenInNew,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = { onDismiss(); it() }
                )
            )
        }

        onSettings?.let {
            add(
                DialogEntry(
                    label = stringResource(R.string.app_info),
                    icon = Icons.Default.Settings,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f),
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = { onDismiss(); it() }
                )
            )
        }

        onUninstall?.let {
            add(
                DialogEntry(
                    label = stringResource(R.string.uninstall),
                    icon = Icons.Default.Delete,
                    backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                    iconTint = MaterialTheme.colorScheme.error,
                    onClick = { onDismiss(); it() }
                )
            )
        }

        onRenameApp?.let {
            add(
                DialogEntry(
                    label = stringResource(R.string.rename_app),
                    icon = Icons.Default.Edit,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f),
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = { onDismiss(); it() }
                )
            )
        }

        onChangeAppIcon?.let {
            add(
                DialogEntry(
                    label = stringResource(R.string.change_app_icon),
                    icon = Icons.Default.Image,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f),
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = { onDismiss(); it() }
                )
            )
        }

        onResetAppIcon?.let {
            add(
                DialogEntry(
                    label = stringResource(R.string.reset_app_icon),
                    icon = Icons.Default.Restore,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f),
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = { onDismiss(); it() }
                )
            )
        }

        onAddToWorkspace?.let {
            add(
                DialogEntry(
                    label = stringResource(R.string.add_to_workspace),
                    icon = Icons.Default.Add,
                    backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = { onDismiss(); it() }
                )
            )
        }

        onRemoveFromWorkspace?.let {
            add(
                DialogEntry(
                    label = stringResource(R.string.remove_from_workspace),
                    icon = Icons.Default.Close,
                    backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                    iconTint = MaterialTheme.colorScheme.error,
                    onClick = { onDismiss(); it() }
                )
            )
        }
    }

    AlertDialog(
        title = { Text(app.name) },
        onDismissRequest = onDismiss,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                entries.forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(entry.backgroundColor)
                            .clickable { entry.onClick() }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = entry.icon,
                            contentDescription = entry.label,
                            modifier = Modifier.size(24.dp),
                            tint = entry.iconTint
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = entry.label,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {},
        containerColor = MaterialTheme.colorScheme.surface
    )
}
