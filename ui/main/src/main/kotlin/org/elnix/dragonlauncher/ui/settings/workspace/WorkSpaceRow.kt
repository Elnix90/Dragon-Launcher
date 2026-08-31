package org.elnix.dragonlauncher.ui.settings.workspace

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.enumsui.toggle.WorkspaceAction
import org.elnix.dragonlauncher.base.model.serializables.Workspace
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import sh.calvin.reorderable.ReorderableCollectionItemScope

@Composable
fun ReorderableCollectionItemScope.WorkspaceRow(
    workspace: Workspace,
    isDragging: Boolean = false,
    onClick: () -> Unit,
    onCheck: (Boolean) -> Unit,
    onAction: (WorkspaceAction) -> Unit,
    onDragEnd: () -> Unit
) {
    val scale by animateFloatAsState(if (isDragging) 1.03f else 1f)
    val elevation by animateDpAsState(if (isDragging) 16.dp else 0.dp)

    Card(
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(elevation),
        modifier =
            Modifier
                .scale(scale)
                .longPressDraggableHandle(onDragStopped = onDragEnd)
                .clickable(onClick = onClick)
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = workspace.enabled,
                onCheckedChange = { onCheck(it) },
                colors = AppObjectsColors.checkboxColors()
            )

            Text(
                text = workspace.id,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DragonIconButton(
                    icon = WorkspaceAction.Edit.iconEnabled,
                    contentDescription = WorkspaceAction.Edit.resId
                ) { onAction(WorkspaceAction.Edit) }

                DragonIconButton(
                    icon = WorkspaceAction.Delete.iconEnabled,
                    contentDescription = WorkspaceAction.Delete.resId,
                    isCancel = true
                ) { onAction(WorkspaceAction.Delete) }
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
