@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.settings.workspace

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.serializables.AppModel
import org.elnix.dragonlauncher.common.serializables.SwipeActionSerializable
import org.elnix.dragonlauncher.common.serializables.SwipePointSerializable.Companion.dummySwipePoint
import org.elnix.dragonlauncher.enumsui.WorkspaceViewMode
import org.elnix.dragonlauncher.settings.stores.DebugSettingsStore
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.composition.LocalAppsViewModel
import org.elnix.dragonlauncher.ui.dialogs.AppAliasesDialog
import org.elnix.dragonlauncher.ui.dialogs.AppLongPressRow
import org.elnix.dragonlauncher.ui.dialogs.AppPickerDialog
import org.elnix.dragonlauncher.ui.dialogs.IconEditorDialog
import org.elnix.dragonlauncher.ui.dialogs.RenameAppDialog
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.dragon.generic.ShowLabels
import org.elnix.dragonlauncher.ui.helpers.AppGrid
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold

@Composable
fun WorkspaceDetailScreen(
    showLabels: Boolean,
    showIcons: Boolean,
    gridSize: Int,
    workspaceId: String,
    onBack: () -> Unit,
    onLaunchAction: (SwipeActionSerializable) -> Unit
) {
    val ctx = LocalContext.current
    val appsViewModel = LocalAppsViewModel.current

    val scope = rememberCoroutineScope()

    val workspaceState by appsViewModel.state.collectAsState()
    val workspace = workspaceState.workspaces.first { it.id == workspaceId }
    val overrides = workspaceState.appOverrides

    val workspaceDebugInfos by DebugSettingsStore.workspacesDebugInfo.asState()


    var selectedView by remember { mutableStateOf(WorkspaceViewMode.DEFAULTS) }

    val getOnlyRemoved = selectedView == WorkspaceViewMode.REMOVED
    val getOnlyAdded = selectedView == WorkspaceViewMode.ADDED

    val apps by appsViewModel
        .appsForWorkspace(workspace, overrides, getOnlyAdded, getOnlyRemoved)
        .collectAsState(initial = emptyList())


    var showAppPicker by remember { mutableStateOf(false) }

    var renameTarget by remember { mutableStateOf<AppModel?>(null) }
    var renameText by remember { mutableStateOf("") }

    var showAliasDialog by remember { mutableStateOf<AppModel?>(null) }

    var iconTargetApp by remember { mutableStateOf<AppModel?>(null) }

    @Composable
    fun AppLongPressRow(app: AppModel) {
        val cacheKey = app.iconCacheKey

        AppLongPressRow(
            app = app,
            onOpen = { onLaunchAction(app.action) },
            onSettings = if (!app.isPrivateProfile && !app.isWorkProfile) {
                {
                    ctx.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = "package:${app.packageName}".toUri()
                        }
                    )
                }
            } else null,
            onUninstall = if (!app.isPrivateProfile && !app.isWorkProfile) {
                {
                    ctx.startActivity(
                        Intent(Intent.ACTION_DELETE).apply {
                            data = "package:${app.packageName}".toUri()
                        }
                    )
                }
            } else null,
            onRemoveFromWorkspace = if (
                (cacheKey !in (workspace.removedAppIds ?: emptyList())) &&
                !app.isPrivateProfile
            // Can't remove private apps from private workspace somehow cause its too
            // annoying to handle
            ) {
                {
                    workspaceId.let {
                        scope.launch {
                            appsViewModel.removeAppFromWorkspace(
                                workspaceId = it,
                                cacheKey = cacheKey
                            )
                        }
                    }
                }
            } else null,
            onAddToWorkspace = if (cacheKey in (workspace.removedAppIds ?: emptyList())) {
                {
                    workspaceId.let {
                        scope.launch {
                            appsViewModel.addAppToWorkspace(
                                workspaceId = it,
                                cacheKey = cacheKey
                            )
                        }
                    }
                }
            } else null,
            onRenameApp = {
                renameText = overrides[cacheKey]?.customName ?: app.name
                renameTarget = app
            },
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
            helpText = stringResource(R.string.workspace_detail_help),
            onReset = { appsViewModel.resetWorkspace(workspaceId) },
            resetTitle = stringResource(R.string.reset_workspace),
            resetText = stringResource(R.string.reset_this_workspace_to_default_apps),
            content = {

                MultiSelectConnectedButtonRow(
                    entries = WorkspaceViewMode.entries,
                    showLabels = ShowLabels.Selected,
                    isChecked = { it == selectedView }
                ) {
                    selectedView = it
                }

                AppGrid(
                    apps = apps.sortedBy { it.name },
                    gridSize = gridSize,
                    txtColor = Color.White,
                    showIcons = showIcons,
                    showLabels = showLabels,
                    longPressPopup = { app -> AppLongPressRow(app) },
                    onClick = null
                )
            }
        )

        FloatingActionButton(
            onClick = { showAppPicker = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, null)
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
            gridSize = gridSize,
            showIcons = showIcons,
            showLabels = showLabels,
            onDismiss = { showAppPicker = false },
            onAppSelected = { app ->
                scope.launch {
                    appsViewModel.addAppToWorkspace(workspaceId, app.iconCacheKey)
                }
            }
        )
    }



    if (renameTarget != null) {
        val app = renameTarget!!
        val cacheKey = app.iconCacheKey
        RenameAppDialog(
            title = stringResource(R.string.rename),
            name = { renameText },
            onNameChange = { renameText = it },
            onConfirm = {

                scope.launch {
                    appsViewModel.renameApp(
                        cacheKey = cacheKey,
                        customName = renameText
                    )
                }

                renameTarget = null
            },
            onReset = {

                scope.launch {
                    appsViewModel.resetAppName(cacheKey)
                }
                renameTarget = null
            },
            onDismiss = { renameTarget = null }
        )
    }

    if (iconTargetApp != null) {

        val app = iconTargetApp!!
        val pkg = app.packageName
        val cacheKey = app.iconCacheKey

        val iconOverride =
            overrides[cacheKey]?.customIcon


        val tempPoint =
            dummySwipePoint(
                action = SwipeActionSerializable.LaunchApp(
                    packageName = pkg,
                    isPrivateSpace = app.isPrivateProfile,
                    userId = app.userId
                ), id = pkg
            ).copy(
                customIcon = iconOverride
            )

        LaunchedEffect(iconOverride) {
            if (iconOverride == null) {
                scope.launch {
                    appsViewModel.reloadPointIcon(tempPoint)
                }
            }
        }

        IconEditorDialog(
            point = tempPoint,
            onDismiss = { iconTargetApp = null }
        ) {
            scope.launch {
                if (it != null) {
                    appsViewModel.setAppIcon(
                        cacheKey = cacheKey,
                        customIcon = it
                    )
                } else {
                    appsViewModel.resetAppIcon(cacheKey)
                }
            }
            iconTargetApp = null
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
