package org.elnix.dragonlauncher.ui.settings.workspace

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.serializables.Workspace
import org.elnix.dragonlauncher.common.serializables.WorkspaceType
import org.elnix.dragonlauncher.enumsui.toggle.WorkspaceAction
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import sh.calvin.reorderable.ReorderableCollectionItemScope

@Composable
fun ReorderableCollectionItemScope.WorkspaceRow(
    workspace: Workspace,
    isDragging: Boolean = false,
    onClick: () -> Unit,
    onCheck: (Boolean) -> Unit,
    showSamsungSettingsIcon: Boolean = false,
    onSamsungSettingsClick: (() -> Unit)? = null,
    onAction: (WorkspaceAction) -> Unit,
    onDragEnd: () -> Unit
) {
    val enabled = workspace.enabled
    val elevation = animateDpAsState(
        targetValue = if (isDragging) 8.dp else 0.dp
    )

    val scale = animateFloatAsState(
        targetValue = if (isDragging) 1.05f else 1f
    )

    val isPrivateWorkspace = workspace.type == WorkspaceType.PRIVATE

    Card(
        colors = AppObjectsColors.cardColors(),
        shape = DragonShape,
        elevation = CardDefaults.cardElevation(elevation.value),
        modifier = Modifier
            .scale(scale.value)
            .clickable {
                if (!isPrivateWorkspace) {
                    onClick()
                }
            }
    ) {
        if (isPrivateWorkspace) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.lock),
                        contentDescription = stringResource(R.string.private_space_title),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = workspace.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )

                    Switch(
                        checked = enabled,
                        onCheckedChange = onCheck,
                        colors = AppObjectsColors.switchColors()
                    )

                    if (showSamsungSettingsIcon && onSamsungSettingsClick != null) {
                        DragonIconButton(
                            onClick = onSamsungSettingsClick,
                            icon = R.drawable.settings,
                            contentDescription = stringResource(R.string.samsung_secure_folder_settings)
                        )
                    }

                    Icon(
                        painter = painterResource(R.drawable.drag_handle),
                        contentDescription = stringResource(R.string.drag_handle),
                        tint = if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.draggableHandle(onDragStopped = onDragEnd)
                    )
                }

                Text(
                    text = stringResource(R.string.private_space_managed_by_android),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = stringResource(R.string.private_space_manage_instructions),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = enabled,
                    onCheckedChange = { onCheck(it) },
                    colors = AppObjectsColors.checkboxColors()
                )

                Text(
                    text = workspace.name,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WorkspaceAction.entries.forEach { action ->
                        DragonIconButton(
                            icon = action.iconEnabled,
                            contentDescription = action.resId
                        ) { onAction(action) }
                    }
                }

                Icon(
                    painter = painterResource(R.drawable.drag_handle),
                    contentDescription = stringResource(R.string.drag_handle),
                    tint = if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.draggableHandle(onDragStopped = onDragEnd)
                )
            }
        }
    }
}
