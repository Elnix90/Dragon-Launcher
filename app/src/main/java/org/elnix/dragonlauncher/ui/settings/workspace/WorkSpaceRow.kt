package org.elnix.dragonlauncher.ui.settings.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorder
import org.elnix.dragonlauncher.ui.drawer.Workspace
import org.elnix.dragonlauncher.utils.colors.AppObjectsColors
import org.elnix.dragonlauncher.utils.workspace.WorkspaceAction

@Composable
fun WorkspaceRow(
    workspace: Workspace,
    reorderState: ReorderableLazyListState,
    isDragging: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onCheck: (Boolean) -> Unit,
    onAction: (WorkspaceAction) -> Unit
) {
    val enabled = workspace.enabled
    val elevation = if (isDragging) 8.dp else 0.dp

    Card(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface),
        colors = AppObjectsColors.cardColors(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(elevation)
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
//                .clip(RoundedCornerShape(12.dp))
                .padding(16.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
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
                listOf(
                    WorkspaceAction.Rename,
                    WorkspaceAction.Delete
                ).forEach { action ->
                    IconButton(onClick = { onAction(action) }) {
                        Icon(action.icon, action.label)
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.DragIndicator,
                contentDescription = "Drag",
                tint = if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.detectReorder(reorderState)
            )
        }
    }
}
