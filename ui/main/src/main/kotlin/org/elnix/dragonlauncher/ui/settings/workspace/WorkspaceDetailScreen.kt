package org.elnix.dragonlauncher.ui.settings.workspace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.elnix.dragonlauncher.base.model.enumsui.select.LocalWorkspaceViewMode
import org.elnix.dragonlauncher.base.model.enumsui.select.WorkspaceViewMode
import org.elnix.dragonlauncher.base.model.serializables.Profile
import org.elnix.dragonlauncher.base.model.serializables.WorkspaceType
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.models.ProfilesViewModel
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.components.AnimatedFab
import org.elnix.dragonlauncher.ui.dialogs.AppPickerSheet
import org.elnix.dragonlauncher.ui.dragon.generic.SingleSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.helpers.DebugZone
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.helpers.workspace.AppGrid
import org.elnix.dragonlauncher.ui.helpers.workspace.WorkspaceLockedContent
import org.elnix.dragonlauncher.ui.helpers.workspace.WorkspaceUnavailableContent
import kotlin.time.Duration.Companion.seconds

@Composable
fun WorkspaceDetailScreen(
    workspaceId: String,
    drawerViewModel: DrawerViewModel = activityViewModel(),
    profilesViewModel: ProfilesViewModel = activityViewModel()
) {
    val ctx = LocalContext.current
    val workspaceManager = drawerViewModel.workspaceManager
    val workspaces by workspaceManager.workspaces.asState()
    val workspace = workspaces.first { it.id == workspaceId }

    var workspaceViewMode by remember { mutableStateOf(WorkspaceViewMode.Default) }
    var showAppPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        drawerViewModel.clearSearchQuery()

        // This way, the remove from workspace and add to workspace will work, otherwise they add and remove apps to the real last workspace used
        DrawerSettingsStore.lastWorkspaceUsed.set(ctx, workspaceId)
    }

    val apps by drawerViewModel
        .search(workspace, workspaceViewMode)
        .collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        delay(1.seconds)
        val apps = null
    }

    Box(Modifier.fillMaxSize()) {
        SettingsScaffold(
            title = "${stringResource(R.string.workspace)}: ${workspace.id}",
            scrollableContent = false,
            helpText = stringResource(R.string.workspace_detail_help),
            onReset = { workspaceManager.resetWorkspace(workspaceId) },
            resetTitle = stringResource(R.string.reset_workspace),
            resetText = stringResource(R.string.reset_this_workspace_to_default_apps)
        ) {
            Box(Modifier.fillMaxSize()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    SingleSelectConnectedButtonRow(
                        entries = WorkspaceViewMode.entries,
                        checked = { it == workspaceViewMode }
                    ) { workspaceViewMode = it }

                    val profiles by profilesViewModel.profiles.collectAsState(emptyList())
                    val profileStates by profilesViewModel.profileStates.collectAsState(emptyList())

                    val workspace = workspaces.first { it.id == workspaceId }

                    val workspaceProfileType =
                        when (workspace.type) {
                            WorkspaceType.Work -> Profile.Type.Work
                            WorkspaceType.Private -> Profile.Type.Private
                            else -> Profile.Type.Personal
                        }

                    val workspaceProfile = profiles.find { it?.type == workspaceProfileType }

                    val workspaceLocked =
                        when (workspaceProfileType) {
                            Profile.Type.Personal -> false
                            Profile.Type.Work -> profileStates.getOrNull(1)?.locked ?: true
                            Profile.Type.Private -> profileStates.getOrNull(2)?.locked ?: true
                        }

                    when {
                        workspaceProfile == null -> {
                            WorkspaceUnavailableContent(workspace.type)
                        }

                        workspaceLocked -> {
                            WorkspaceLockedContent(workspaceProfile, true)
                        }

                        else -> {
                            CompositionLocalProvider(LocalWorkspaceViewMode provides workspaceViewMode) {
                                AppGrid(
                                    apps = apps.sortedBy { it.label },
                                    longPressPopup = true,
                                    onClick = null
                                )
                            }
                        }
                    }
                }

                AnimatedFab(
                    icon = R.drawable.add,
                    onClick = { showAppPicker = true },
                    minSize = 70.dp,
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                )
                DebugZone(DebugSettingsStore.workspacesDebugInfo) {
                    Text(workspace.toString())
                }
            }
        }
    }

    if (showAppPicker) {
        AppPickerSheet(
            onDismiss = { showAppPicker = false },
            onAppSelected = { app ->
                workspaceManager.addAppToWorkspace(workspaceId, app.key)
            }
        )
    }
}
