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
import androidx.compose.runtime.getValue
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
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute
import org.elnix.dragonlauncher.enumsui.toggle.WorkspaceAction
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.components.AnimatedFab
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.compositionslocals.LocalNavigator
import org.elnix.dragonlauncher.ui.dialogs.CreateOrEditWorkspaceDialog
import org.elnix.dragonlauncher.ui.dragon.dialogs.UserValidation
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState


@Composable
public fun WorkspaceListScreen(drawerViewModel: DrawerViewModel = activityViewModel()) {
    val navigator = LocalNavigator.current
    val scope = rememberCoroutineScope()

    val workspaceManager = drawerViewModel.workspaceManager
    val workspaces by workspaceManager.workspacesState.asState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<Workspace?>(null) }

    var renameTarget by remember { mutableStateOf<Workspace?>(null) }
    var nameBuffer by remember { mutableStateOf("") }

    var objects by remember(workspaces) { mutableStateOf(workspaces) }

    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            objects = objects.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        }
    )

    fun save() {
        scope.launch { workspaceManager.setWorkspaceOrder(objects) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SettingsScaffold(
            title = stringResource(R.string.workspaces),
            helpText = stringResource(R.string.workspace_help),
            resetText = stringResource(R.string.reset_workspaces),
            onReset = {
                scope.launch { workspaceManager.resetWorkspaces() }
            },
            lasyListState = lazyListState,
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
                items(objects, key = { it.id }) { ws ->
                    ReorderableItem(state = reorderState, key = ws.id) { isDragging ->
                        WorkspaceRow(
                            workspace = ws,
                            isDragging = isDragging,
                            modifier = Modifier.longPressDraggableHandle(
                                onDragStopped = ::save
                            ),
                            onClick = {
                                if (ws.type != WorkspaceType.Private) {
                                    navigator.navigate(NavigationRoute.WorkspaceDetail(ws.id))
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
                                        if (ws.type != WorkspaceType.Private) {
                                            showDeleteConfirm = ws
                                        }
                                    }
                                }
                            },
                            onDragEnd = ::save
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
        type = WorkspaceType.Custom,
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
