@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.settings.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.Profile
import org.elnix.dragonlauncher.base.model.serializables.WorkspaceType
import org.elnix.dragonlauncher.enumsui.select.WorkspaceViewMode
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.models.ProfilesViewModel
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.components.AnimatedFab
import org.elnix.dragonlauncher.ui.dialogs.AppPickerDialog
import org.elnix.dragonlauncher.ui.dragon.generic.SingleSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.helpers.workspace.AppGrid
import org.elnix.dragonlauncher.ui.helpers.workspace.WorkspaceLockedContent
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold

@Composable
fun WorkspaceDetailScreen(
    drawerViewModel: DrawerViewModel = activityViewModel(),
    profilesViewModel: ProfilesViewModel = activityViewModel(),
    workspaceId: String,
    onBack: () -> Unit,
) {
    val workspaceManager = drawerViewModel.workspaceManager
    val workspaceState by workspaceManager.workspacesState.collectAsState()
    val workspace = workspaceState.first { it.id == workspaceId }

    val workspaceDebugInfos by DebugSettingsStore.workspacesDebugInfo.asState()

    var selectedView by remember { mutableStateOf(WorkspaceViewMode.Default) }

    val getOnlyRemoved = selectedView == WorkspaceViewMode.Removed
    val getOnlyAdded = selectedView == WorkspaceViewMode.Added

    LaunchedEffect(Unit) {
        drawerViewModel.searchQuery.value = ""
    }

    val apps by drawerViewModel
        .search(workspace, getOnlyAdded, getOnlyRemoved)
        .collectAsState(initial = emptyList())


    var showAppPicker by remember { mutableStateOf(false) }

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

            val profiles by profilesViewModel.profiles.collectAsState(emptyList())
            val profileStates by profilesViewModel.profileStates.collectAsState(emptyList())

            val workspace = workspaceState.first { it.id == workspaceId }

            val workspaceProfileType = when (workspace.type) {
                WorkspaceType.Work -> Profile.Type.Work
                WorkspaceType.Private -> Profile.Type.Private
                else -> Profile.Type.Personal
            }

            val workspaceProfile = profiles.find { it?.type == workspaceProfileType }

            val workspaceLocked = when (workspaceProfileType) {
                Profile.Type.Work -> profileStates[1]?.locked ?: true
                Profile.Type.Private -> profileStates[2]?.locked ?: true
                Profile.Type.Personal -> false
            }

            when {
                workspaceProfile == null -> {
                    Text("No profile found in phone")
                }

                workspaceLocked -> {
                    WorkspaceLockedContent(workspaceProfile)
                }

                else -> {
                    AppGrid(
                        apps = apps.sortedBy { it.label },
                        longPressPopup = true,
                        onClick = null
                    )
                }
            }
        }

        AnimatedFab(
            icon = R.drawable.add,
            onClick = { showAppPicker = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        )

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
                workspaceManager.addAppToWorkspace(workspaceId, app.key)
            }
        )
    }
}
