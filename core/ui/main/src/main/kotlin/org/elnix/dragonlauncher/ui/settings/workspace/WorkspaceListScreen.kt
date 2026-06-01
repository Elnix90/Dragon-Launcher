@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.settings.workspace

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.serializables.Workspace
import org.elnix.dragonlauncher.base.model.serializables.WorkspaceType
import org.elnix.dragonlauncher.enumsui.toggle.WorkspaceAction
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.AppsViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.AnimatedFab
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dialogs.CreateOrEditWorkspaceDialog
import org.elnix.dragonlauncher.ui.dragon.dialogs.UserValidation
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState


@Composable
fun WorkspaceListScreen(
    appsViewModel: AppsViewModel = activityViewModel(),
    onOpenWorkspace: (String) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val workspaceManager = appsViewModel.workspaceManager
    val workspaceState by workspaceManager.workspacesState.collectAsState()
    val workspaces = workspaceState.workspaces

    var showCreateDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<Workspace?>(null) }
    var nameBuffer by remember { mutableStateOf("") }

    var showDeleteConfirm by remember { mutableStateOf<Workspace?>(null) }

    // Local mutable list synced with ViewModel state
    val uiList = remember { mutableStateListOf<Workspace>() }
    LaunchedEffect(workspaces) {
        if (workspaces != uiList) {
            uiList.clear()
            uiList.addAll(workspaces)
        }
    }

    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            if (from.index in uiList.indices && to.index in uiList.indices) {
                val tmp = uiList.toMutableList()
                val item = tmp.removeAt(from.index)
                tmp.add(to.index, item)
                uiList.clear()
                uiList.addAll(tmp)
            }
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        SettingsScaffold(
            title = stringResource(R.string.workspaces),
            onBack = onBack,
            helpText = stringResource(R.string.workspace_help),
            onReset = {
                scope.launch { workspaceManager.resetWorkspaces() }
            },
            bottomContent = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp),
                ) {
                    Spacer()
                    AnimatedFab(
                        onClick = {
                            nameBuffer = ""
                            showCreateDialog = true
                        },
                        icon = R.drawable.add
                    )
                }
            },
            lazyContent = {
                items(uiList, key = { it.id }) { ws ->
                    ReorderableItem(state = reorderState, key = ws.id) { isDragging ->
                        WorkspaceRow(
                            workspace = ws,
                            isDragging = isDragging,
                            onClick = {
                                if (ws.type != WorkspaceType.PRIVATE) {
                                    onOpenWorkspace(ws.id)
                                }
                            },
                            onCheck = { scope.launch { workspaceManager.setWorkspaceEnabled(ws.id, it) } },
                            onAction = { action ->
                                when (action) {
                                    WorkspaceAction.Edit -> {
                                        renameTarget = ws
                                        nameBuffer = ws.name
                                    }

                                    WorkspaceAction.Delete -> {
                                        if (ws.type != WorkspaceType.PRIVATE) {
                                            showDeleteConfirm = ws
                                        }
                                    }
                                }
                            },
                            onDragEnd = {
                                scope.launch { workspaceManager.setWorkspaceOrder(uiList) }
                            }
                        )
                    }
                }
            }
        )
    }

    CreateOrEditWorkspaceDialog(
        visible = showCreateDialog,
        title = stringResource(R.string.create_workspace),
        name = nameBuffer,
        type = WorkspaceType.CUSTOM,
        onNameChange = { nameBuffer = it },
        onConfirm = { selectedType ->
            scope.launch { workspaceManager.createWorkspace(nameBuffer.trim(), selectedType) }
            showCreateDialog = false
        },
        onDismiss = { showCreateDialog = false }
    )

    CreateOrEditWorkspaceDialog(
        visible = renameTarget != null,
        title = stringResource(R.string.edit_workspace),
        name = nameBuffer,
        type = renameTarget?.type,
        onNameChange = { nameBuffer = it },
        onConfirm = { selectedType ->
            val targetId = renameTarget
            if (targetId != null && nameBuffer.isNotBlank()) {
                scope.launch {
                    workspaceManager.editWorkspace(
                        targetId.id,
                        nameBuffer.trim(),
                        selectedType
                    )
                }
            }
            renameTarget = null
        },
        onDismiss = { renameTarget = null }
    )

    if (showDeleteConfirm != null) {
        val workSpaceToDelete = showDeleteConfirm!!
        UserValidation(
            title = stringResource(R.string.delete_workspace),
            message = "${stringResource(R.string.are_you_sure_to_delete_workspace)} '${workSpaceToDelete.name}' ?",
            onDismiss = { showDeleteConfirm = null }
        ) {
            scope.launch {
                workspaceManager.deleteWorkspace(workSpaceToDelete.id)
                showDeleteConfirm = null
            }
        }
    }
}
