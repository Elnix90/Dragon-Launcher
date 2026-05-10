@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.settings.workspace

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.SAMSUNG_INTEGRATION_TAG
import org.elnix.dragonlauncher.common.messyfolder.SamsungWorkspaceIntegration
import org.elnix.dragonlauncher.common.messyfolder.showToast
import org.elnix.dragonlauncher.common.serializables.Workspace
import org.elnix.dragonlauncher.common.serializables.WorkspaceType
import org.elnix.dragonlauncher.enumsui.toggle.WorkspaceAction
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.logging.logI
import org.elnix.dragonlauncher.logging.logW
import org.elnix.dragonlauncher.settings.stores.PrivateSettingsStore
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.components.AnimatedFab
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.composition.LocalAppsViewModel
import org.elnix.dragonlauncher.ui.dialogs.CreateOrEditWorkspaceDialog
import org.elnix.dragonlauncher.ui.dragon.dialogs.UserValidation
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState


@Composable
fun WorkspaceListScreen(
    onOpenWorkspace: (String) -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val appsViewModel = LocalAppsViewModel.current

    val scope = rememberCoroutineScope()

    val state by appsViewModel.state.collectAsState()
    val samsungPreference by PrivateSettingsStore.samsungPreferSecureFolder.asState()
    val isSamsung = remember { SamsungWorkspaceIntegration.isSamsungDevice() }

    var showCreateDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<Workspace?>(null) }
    var nameBuffer by remember { mutableStateOf("") }
    var showSamsungSettingsDialog by remember { mutableStateOf(false) }
    var hasSecureFolder by remember { mutableStateOf(false) }

    var showDeleteConfirm by remember { mutableStateOf<Workspace?>(null) }

    // Local mutable list synced with ViewModel state
    val uiList = remember { mutableStateListOf<Workspace>() }
    LaunchedEffect(state.workspaces) {
        if (state.workspaces != uiList) {
            uiList.clear()
            uiList.addAll(state.workspaces)
        }
    }

    val privateWorkspaceEnabled = uiList.firstOrNull { it.type == WorkspaceType.PRIVATE }?.enabled == true

    LaunchedEffect(samsungPreference) {
        logI(SAMSUNG_INTEGRATION_TAG) { "Loading Samsung preference: $samsungPreference" }
    }

    LaunchedEffect(isSamsung, privateWorkspaceEnabled) {
        if (isSamsung && privateWorkspaceEnabled) {
            logD(SAMSUNG_INTEGRATION_TAG) { "Showing Samsung settings icon - Private Space toggle enabled" }
        } else {
            logD(SAMSUNG_INTEGRATION_TAG) { "Samsung settings icon hidden (not Samsung or toggle disabled)" }
        }
    }

    LaunchedEffect(isSamsung, showSamsungSettingsDialog) {
        if (isSamsung && showSamsungSettingsDialog) {
            hasSecureFolder = SamsungWorkspaceIntegration.isSecureFolderAvailable(ctx)
            if (!hasSecureFolder && samsungPreference) {
                logW(SAMSUNG_INTEGRATION_TAG) { "Secure Folder not available, disabling toggle" }
                PrivateSettingsStore.samsungPreferSecureFolder.set(ctx, false)
            }
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
                scope.launch { appsViewModel.resetWorkspacesAndOverrides() }
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
                            showSamsungSettingsIcon = ws.type == WorkspaceType.PRIVATE && isSamsung && ws.enabled,
                            onSamsungSettingsClick = {
                                showSamsungSettingsDialog = true
                            },
                            onClick = {
                                if (ws.type != WorkspaceType.PRIVATE) {
                                    onOpenWorkspace(ws.id)
                                }
                            },
                            onCheck = { scope.launch { appsViewModel.setWorkspaceEnabled(ws.id, it) } },
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
                                scope.launch { appsViewModel.setWorkspaceOrder(uiList) }
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
            scope.launch { appsViewModel.createWorkspace(nameBuffer.trim(), selectedType) }
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
                    appsViewModel.editWorkspace(
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
                appsViewModel.deleteWorkspace(workSpaceToDelete.id)
                showDeleteConfirm = null
            }
        }
    }

    if (showSamsungSettingsDialog && isSamsung) {
        val secureFolderUnavailableText = stringResource(R.string.secure_folder_unavailable)
        AlertDialog(
            onDismissRequest = { showSamsungSettingsDialog = false },
            title = {
                Text(stringResource(R.string.samsung_secure_folder_settings))
            },
            text = {
                Column {
                    Text(
                        text = if (samsungPreference)
                            stringResource(R.string.secure_folder_prefer_enabled)
                        else
                            stringResource(R.string.secure_folder_prefer_disabled)
                    )

                    Switch(
                        checked = samsungPreference,
                        enabled = hasSecureFolder,
                        onCheckedChange = { newValue ->
                            scope.launch {
                                if (newValue && !hasSecureFolder) {
                                    logW(SAMSUNG_INTEGRATION_TAG) { "Secure Folder not available, disabling toggle" }
                                    ctx.showToast(secureFolderUnavailableText)
                                    PrivateSettingsStore.samsungPreferSecureFolder.set(ctx, false)
                                } else {
                                    logI(SAMSUNG_INTEGRATION_TAG) { "User preference changed: useSecureFolder=$newValue" }
                                    PrivateSettingsStore.samsungPreferSecureFolder.set(ctx, newValue)
                                }
                            }
                        }
                    )

                    if (!hasSecureFolder) {
                        Text(
                            text = stringResource(R.string.secure_folder_unavailable),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSamsungSettingsDialog = false }) {
                    Text(text = stringResource(R.string.ok))
                }
            }
        )
    }
}
