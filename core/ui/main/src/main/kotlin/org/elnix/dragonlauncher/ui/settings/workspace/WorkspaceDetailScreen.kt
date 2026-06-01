@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.settings.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.enumsui.select.WorkspaceViewMode
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.AppsViewModel
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.dialogs.AppAliasesDialog
import org.elnix.dragonlauncher.ui.dialogs.AppIconEditor
import org.elnix.dragonlauncher.ui.dialogs.AppLongPressRowImpl
import org.elnix.dragonlauncher.ui.dialogs.AppPickerDialog
import org.elnix.dragonlauncher.ui.dialogs.TextEditorDialog
import org.elnix.dragonlauncher.ui.dragon.generic.SingleSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.helpers.AppGrid
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold

@Composable
fun WorkspaceDetailScreen(
    appsViewModel: AppsViewModel = activityViewModel(),
    workspaceId: String,
    onBack: () -> Unit,
    onLaunchAction: (Action.LaunchApp) -> Unit
) {
    val scope = rememberCoroutineScope()

    val workspaceManager = appsViewModel.workspaceManager
    val workspaceState by workspaceManager.workspacesState.collectAsState()
    val workspace = workspaceState.workspaces.first { it.id == workspaceId }

    val appOverridesManager = appsViewModel.appOverrideManager

    val workspaceDebugInfos by DebugSettingsStore.workspacesDebugInfo.asState()


    var selectedView by remember { mutableStateOf(WorkspaceViewMode.Default) }

    val getOnlyRemoved = selectedView == WorkspaceViewMode.Removed
    val getOnlyAdded = selectedView == WorkspaceViewMode.Added

    val apps by appsViewModel
        .appsForWorkspace(workspace, getOnlyAdded, getOnlyRemoved)
        .collectAsState(initial = emptyList())


    var showAppPicker by remember { mutableStateOf(false) }

    var renameAppTarget by remember { mutableStateOf<Application?>(null) }

    var showAliasDialog by remember { mutableStateOf<Application?>(null) }

    var iconTargetApp by remember { mutableStateOf<Application?>(null) }

    @Composable
    fun AppLongPressRow(app: Application) {
        val cacheKey = app.key

        AppLongPressRowImpl(
            app = app,
            onLaunch = { onLaunchAction(app.action) },
            onRemoveFromWorkspace = {
                workspaceId.let {
                    scope.launch {
                        workspaceManager.removeAppFromWorkspace(
                            id = it,
                            cacheKey = cacheKey
                        )
                    }
                }
            },
            onAddToWorkspace = if (cacheKey in (workspace.removedAppIds ?: emptyList())) {
                {
                    workspaceId.let {
                        scope.launch {
                            workspaceManager.addAppToWorkspace(
                                id = it,
                                cacheKey = cacheKey
                            )
                        }
                    }
                }
            } else null,
            onRenameApp = { renameAppTarget = app },
            onChangeAppIcon = {
                iconTargetApp = app
            },
            onAliases = { showAliasDialog = app }
        )
    }


    Box(Modifier.fillMaxSize()) {
        SettingsScaffold(
            title = "${stringResource(R.string.workspace)}: ${workspace.name}",
            onBack = onBack,
            scrollableContent = false,
            helpText = stringResource(R.string.workspace_detail_help),
            onReset = { workspaceManager.resetWorkspace(workspaceId) },
            resetTitle = stringResource(R.string.reset_workspace),
            resetText = stringResource(R.string.reset_this_workspace_to_default_apps)
        ) {
            SingleSelectConnectedButtonRow(
                entries = WorkspaceViewMode.entries,
                checked = { it == selectedView }
            ) { selectedView = it }

            AppGrid(
                apps = apps.sortedBy { it.label },
                longPressPopup = { app -> AppLongPressRow(app) },
                onClick = null
            )
        }

        FloatingActionButton(
            onClick = { showAppPicker = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                painter = painterResource(R.drawable.add),
                contentDescription = null
            )
        }

        if (workspaceDebugInfos) {
            Column(
                modifier = Modifier.background(Color.DarkGray.copy(0.5f))
            ) {
                Text(workspace.toString())
            }
        }
    }

    if (showAppPicker) {
        AppPickerDialog(
            onDismiss = { showAppPicker = false },
            onAppSelected = { app ->
                scope.launch {
                    workspaceManager.addAppToWorkspace(workspaceId, app.key)
                }
            }
        )
    }


    if (renameAppTarget != null) {
        val app = renameAppTarget!!
        val cacheKey = app.key

        TextEditorDialog(
            title = { stringResource(R.string.rename) },
            placeHolder = { app.label },
            onDismiss = { renameAppTarget = null },
            initialText = app.label
        ) {
            if (it != "") {
                appOverridesManager.renameApp(
                    cacheKey = cacheKey,
                    customName = it
                )
            } else {
                appOverridesManager.renameApp(cacheKey, "")
            }
            renameAppTarget = null
        }
    }

    if (iconTargetApp != null) {

        val app = iconTargetApp!!
        val cacheKey = app.key

        AppIconEditor(
            app = app,
            onDismiss = { iconTargetApp = null }
        ) {
            scope.launch {
                if (it != null) {
                    appOverridesManager.setAppIcon(
                        cacheKey = cacheKey,
                        customIcon = it
                    )
                } else {
                    appOverridesManager.setAppIcon(cacheKey, null)
                }
                iconTargetApp = null
            }
        }
    }

    if (showAliasDialog != null) {
        val app = showAliasDialog!!

        AppAliasesDialog(
            app = app,
            onDismiss = { showAliasDialog = null }
        )
    }
}
