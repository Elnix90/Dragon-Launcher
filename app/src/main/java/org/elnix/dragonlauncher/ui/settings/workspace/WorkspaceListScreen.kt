@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.settings.workspace

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.ui.drawer.Workspace
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsLazyHeader
import org.elnix.dragonlauncher.utils.workspace.WorkspaceAction
import org.elnix.dragonlauncher.utils.workspace.WorkspaceViewModel


@Composable
fun WorkspaceListScreen(
    workspaceViewModel: WorkspaceViewModel,
    onOpenWorkspace: (String) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val state by workspaceViewModel.state.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<String?>(null) }
    var nameBuffer by remember { mutableStateOf("") }

    // Local mutable list synced with ViewModel state
    val uiList = remember { mutableStateListOf<Workspace>() }
    LaunchedEffect(state.workspaces) {
        if (state.workspaces != uiList) {
            uiList.clear()
            uiList.addAll(state.workspaces)
        }
    }

    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to ->
            if (from.index in uiList.indices && to.index in 0..uiList.size) {
                val tmp = uiList.toMutableList()
                val item = tmp.removeAt(from.index)
                tmp.add(to.index, item)
                uiList.clear()
                uiList.addAll(tmp)
            }
        },
        onDragEnd = { _, _ ->
            // Commit changes to ViewModel
            scope.launch { workspaceViewModel.setWorkspaceOrder(uiList) }
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        SettingsLazyHeader(
            title = stringResource(R.string.workspaces),
            onBack = onBack,
            helpText = stringResource(R.string.workspace_help),
            onReset = {
                scope.launch { workspaceViewModel.resetWorkspacesAndOverrides() }
            },
            reorderState = reorderState
        ) {
            items(uiList, key = { it.id }) { ws ->
                ReorderableItem(state = reorderState, key = ws.id) { isDragging ->
                    WorkspaceRow(
                        workspace = ws,
                        reorderState = reorderState,
                        isDragging = isDragging,
                        onClick = { onOpenWorkspace(ws.id) },
                        onCheck = { scope.launch { workspaceViewModel.setWorkspaceEnabled(ws.id, it) } },
                        onAction = { action ->
                            when (action) {
                                WorkspaceAction.Rename -> {
                                    renameTarget = ws.id
                                    nameBuffer = ws.name
                                }
                                WorkspaceAction.Delete -> {
                                    scope.launch { workspaceViewModel.deleteWorkspace(ws.id) }
                                }
                            }
                        }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = {
                nameBuffer = ""
                showCreateDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, null)
        }
    }

    CreateOrRenameWorkspaceDialog(
        visible = showCreateDialog,
        title = stringResource(R.string.create_workspace),
        name = nameBuffer,
        onNameChange = { nameBuffer = it },
        onConfirm = {
            scope.launch { workspaceViewModel.createWorkspace(nameBuffer.trim()) }
            showCreateDialog = false
        },
        onDismiss = { showCreateDialog = false }
    )

    CreateOrRenameWorkspaceDialog(
        visible = renameTarget != null,
        title = stringResource(R.string.rename_workspace),
        name = nameBuffer,
        onNameChange = { nameBuffer = it },
        onConfirm = {
            val targetId = renameTarget
            if (targetId != null && nameBuffer.isNotBlank()) {
                scope.launch { workspaceViewModel.renameWorkspace(targetId, nameBuffer.trim()) }
            }
            renameTarget = null
        },
        onDismiss = { renameTarget = null }
    )
}
