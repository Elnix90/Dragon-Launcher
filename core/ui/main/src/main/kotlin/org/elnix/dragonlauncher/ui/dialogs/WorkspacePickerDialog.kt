package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.i18n.R

import org.elnix.dragonlauncher.models.AppsViewModel
import org.elnix.dragonlauncher.ui.activityViewModel
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape

@Composable
fun WorkspacePickerDialog(
    appsViewModel: AppsViewModel = activityViewModel(),
    onDismiss: () -> Unit,
    onActionPicked: (Action.OpenAppDrawer) -> Unit
) {
    val workspaces by appsViewModel.enabledState.collectAsState()
    val availableWorkspaces = workspaces.workspaces

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text(stringResource(R.string.select_default_workspace)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = stringResource(R.string.select_workspace_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                )
                Spacer(Modifier.height(8.dp))

                // "Default" option (no specific workspace)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(DragonShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            onActionPicked(Action.OpenAppDrawer())
                        }
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.workspace_default),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Specific workspaces
                availableWorkspaces.forEach { workspace ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(DragonShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable {
                                onActionPicked(
                                    Action.OpenAppDrawer(workspace.id)
                                )
                            }
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = workspace.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = DragonShape
    )
}