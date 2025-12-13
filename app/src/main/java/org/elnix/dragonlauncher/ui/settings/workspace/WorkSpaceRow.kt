package org.elnix.dragonlauncher.ui.settings.workspace

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.scale
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
    onCheck: (Boolean) -> Unit,
    onAction: (WorkspaceAction) -> Unit
) {
    val enabled = workspace.enabled
    val elevation = if (isDragging) 8.dp else 0.dp
    val scale = if (isDragging) 1.05f else 1f

    Card(
        colors = AppObjectsColors.cardColors(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(elevation),
        modifier = Modifier
            .scale(scale)
            .clickable { onClick() }
    ){
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
