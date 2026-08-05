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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
fun WorkspaceListScreen(drawerViewModel: DrawerViewModel = activityViewModel()) {
    val navigator = LocalNavigator.current

    val workspaceManager = drawerViewModel.workspaceManager
    val workspaces by workspaceManager.workspaces.asState()

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

    // This function is really annoying, I can't really explain why it works here, but without the behavior ir really strange,
    // I remembered this function could be used, and by magic it nw works correctly!!
    // Comment for me later: do not remove, it doesn't cost much and works!!!!!
    val objectsOther by rememberUpdatedState(objects)

    Box(modifier = Modifier.fillMaxSize()) {
        SettingsScaffold(
            title = stringResource(R.string.workspaces),
            helpText = stringResource(R.string.workspace_help),
            resetText = stringResource(R.string.reset_workspaces),
            onReset = { workspaceManager.resetWorkspaces() },
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
                            onClick = { navigator.navigate(NavigationRoute.WorkspaceDetail(ws.id)) },
                            onCheck = { workspaceManager.setWorkspaceEnabled(ws.id, it) },
                            onAction = { action ->
                                when (action) {
                                    WorkspaceAction.Edit -> {
                                        renameTarget = ws
                                        nameBuffer = ws.id
                                    }

                                    WorkspaceAction.Delete -> {
                                        showDeleteConfirm = ws
                                    }
                                }
                            }
                        ) {
                            workspaceManager.setWorkspaceOrder(objectsOther)
                        }
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
            workspaceManager.createWorkspace(nameBuffer.trim(), selectedType)
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
            if (targetId != null && nameBuffer.isNotBlank() && nameBuffer !in workspaces.map { it.id }) {
                workspaceManager.editWorkspace(
                    oldId = targetId.id,
                    newId = nameBuffer.trim(),
                    type = selectedType
                )
            }
            renameTarget = null
        },
        onDismiss = { renameTarget = null }
    )

    if (showDeleteConfirm != null) {
        val workSpaceToDelete = showDeleteConfirm!!
        UserValidation(
            title = stringResource(R.string.delete_workspace),
            message = "${stringResource(R.string.are_you_sure_to_delete_workspace)} '${workSpaceToDelete.id}' ?",
            onDismiss = { showDeleteConfirm = null }
        ) {
            workspaceManager.deleteWorkspace(workSpaceToDelete.id)
            showDeleteConfirm = null
        }
    }
}
