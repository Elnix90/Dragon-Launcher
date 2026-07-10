package org.elnix.dragonlauncher.ui.settings.workspace

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.Workspace
import org.elnix.dragonlauncher.enumsui.toggle.WorkspaceAction
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.modifiers.shapedClickable
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import sh.calvin.reorderable.ReorderableCollectionItemScope

@Composable
public fun ReorderableCollectionItemScope.WorkspaceRow(
    workspace: Workspace,
    isDragging: Boolean = false,
    onClick: () -> Unit,
    onCheck: (Boolean) -> Unit,
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

    Card(
        colors = AppObjectsColors.cardColors(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(elevation.value),
        modifier = Modifier
            .scale(scale.value)
            .shapedClickable(onClick = onClick)
    ) {
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
