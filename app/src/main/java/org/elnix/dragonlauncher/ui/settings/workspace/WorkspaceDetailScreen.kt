@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.settings.workspace

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.helpers.WorkspaceViewMode
import org.elnix.dragonlauncher.data.helpers.WorkspaceViewMode.ADDED
import org.elnix.dragonlauncher.data.helpers.WorkspaceViewMode.DEFAULTS
import org.elnix.dragonlauncher.data.helpers.WorkspaceViewMode.REMOVED
import org.elnix.dragonlauncher.data.helpers.workspaceViewMode
import org.elnix.dragonlauncher.data.stores.DebugSettingsStore
import org.elnix.dragonlauncher.ui.components.dialogs.AppPickerDialog
import org.elnix.dragonlauncher.ui.components.dialogs.RenameAppDialog
import org.elnix.dragonlauncher.ui.drawer.AppLongPressDialog
import org.elnix.dragonlauncher.ui.drawer.AppModel
import org.elnix.dragonlauncher.ui.helpers.AppGrid
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsLazyHeader
import org.elnix.dragonlauncher.utils.ImageUtils
import org.elnix.dragonlauncher.utils.actions.launchSwipeAction
import org.elnix.dragonlauncher.utils.models.AppsViewModel
import org.elnix.dragonlauncher.utils.models.WorkspaceViewModel
import org.elnix.dragonlauncher.utils.showToast

@Composable
fun WorkspaceDetailScreen(
    showLabels: Boolean,
    showIcons: Boolean,
    gridSize: Int,
    workspaceId: String,
    appsViewModel: AppsViewModel,
    workspaceViewModel: WorkspaceViewModel,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    val workspaceState by workspaceViewModel.state.collectAsState()
    val workspace = workspaceState.workspaces.first { it.id == workspaceId }

    val workspaceDebugInfos by DebugSettingsStore.getWorkspacesDebugInfos(ctx)
        .collectAsState(initial = false)

    var selectedView by remember { mutableStateOf(DEFAULTS) }

    val getOnlyRemoved = selectedView == REMOVED
    val getOnlyAdded = selectedView == ADDED

    val apps by appsViewModel
        .appsForWorkspace(workspace, workspaceState.appOverrides, getOnlyAdded, getOnlyRemoved)
        .collectAsState(initial = emptyList())

    val icons by appsViewModel.icons.collectAsState()

    var showAppPicker by remember { mutableStateOf(false) }
    var showDetailScreen by remember { mutableStateOf<AppModel?>(null) }

    var showRenameAppDialog by remember { mutableStateOf(false) }
    var renameTargetPackage by remember { mutableStateOf<String?>(null) }
    var renameText by remember { mutableStateOf("") }


    var iconTargetPackage by remember { mutableStateOf<String?>(null) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        val pkg = iconTargetPackage ?: return@rememberLauncherForActivityResult
        if (uri != null) {
            scope.launch {
                try {
                    val bitmap = ImageUtils.loadBitmap(ctx, uri)
                    val cropped = ImageUtils.cropCenterSquare(bitmap)
                    val resized = ImageUtils.resize(cropped, 192)

                    // Save as Base64 now
                    workspaceViewModel.setAppIcon(
                        pkg,
                        resized
                    )
                    // Optionally show toast
                    ctx.showToast(R.string.icon_updated)
                } catch (e: Exception) {
                    e.printStackTrace()
                    ctx.showToast(R.string.icon_update_failed)
                }
            }
        }
    }



    Box(Modifier.fillMaxSize()) {
        SettingsLazyHeader(
            title = "${stringResource(R.string.workspace)}: ${workspace.name}",
            onBack = onBack,
            helpText = stringResource(R.string.workspace_detail_help),
            onReset = { workspaceViewModel.resetWorkspace(workspaceId) },
            resetTitle = stringResource(R.string.reset_workspace),
            resetText = stringResource(R.string.reset_this_workspace_to_default_apps),
            content = {

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WorkspaceViewMode.entries.forEach { mode ->
                        val isSelected = mode == selectedView
                        Text(
                            text = workspaceViewMode(mode),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedView = mode }
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.secondary
                                    else MaterialTheme.colorScheme.surface
                                )
                                .padding(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.onSecondary
                                    else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                AppGrid(
                    apps = apps,
                    icons = icons,
                    gridSize = gridSize,
                    txtColor = Color.White,
                    showIcons = showIcons,
                    showLabels = showLabels,
                    onLongClick = { showDetailScreen = it },
                    onClick = { app -> showDetailScreen = app }
                )
            }
        )

        FloatingActionButton(
            onClick = { showAppPicker = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor =  MaterialTheme.colorScheme.primary
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
            appsViewModel = appsViewModel,
            workspaceViewModel = workspaceViewModel,
            gridSize = gridSize,
            showIcons = showIcons,
            showLabels = showLabels,
            onDismiss = { showAppPicker = false }
        ) { app ->
            scope.launch {
                workspaceViewModel.addAppToWorkspace(workspaceId, app.packageName)
            }
        }
    }

    if (showDetailScreen != null) {
        val app = showDetailScreen!!
        val hasCustomIcon =
            workspaceState.appOverrides[app.packageName]?.customIconBase64 != null

        AppLongPressDialog(
            app = app,
            onOpen = {
                launchSwipeAction(ctx, app.action)
            },
            onSettings = {
                ctx.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = "package:${app.packageName}".toUri()
                    }
                )
            },
            onUninstall = {
                ctx.startActivity(
                    Intent(Intent.ACTION_DELETE).apply {
                        data = "package:${app.packageName}".toUri()
                    }
                )
            },
            onRemoveFromWorkspace = if (app.packageName !in (workspace.removedAppIds ?: emptyList())) {
                {
                    workspaceId.let {
                        scope.launch {
                            workspaceViewModel.removeAppFromWorkspace(
                                it,
                                app.packageName
                            )
                        }
                    }
                }
            } else null,
            onAddToWorkspace = if (app.packageName in (workspace.removedAppIds ?: emptyList())) {
                {
                    workspaceId.let {
                        scope.launch {
                            workspaceViewModel.addAppToWorkspace(
                                it,
                                app.packageName
                            )
                        }
                    }
                }
            } else null,
            onRenameApp = {
                renameText = app.name
                renameTargetPackage = app.packageName
                showRenameAppDialog = true
            },
            onChangeAppIcon = {
                val pkg = app.packageName
                iconTargetPackage = pkg
                pickImageLauncher.launch(arrayOf("image/*"))
            },
            onResetAppIcon = if (hasCustomIcon) {
                {
                    val pkg = app.packageName
                    scope.launch {
                        workspaceViewModel.resetAppIcon(pkg)
                    }
                }
            } else null,
            onDismiss = { showDetailScreen = null }
        )
    }

    RenameAppDialog(
        visible = showRenameAppDialog,
        title = ctx.getString(R.string.rename_app),
        name = renameText,
        onNameChange = { renameText = it },
        onConfirm = {
            val pkg = renameTargetPackage ?: return@RenameAppDialog

            scope.launch {
                workspaceViewModel.renameApp(
                    packageName = pkg,
                    name = renameText
                )
            }

            showRenameAppDialog = false
            showDetailScreen = null
            renameTargetPackage = null
        },
        onReset = {
            val pkg = renameTargetPackage ?: return@RenameAppDialog

            scope.launch {
                workspaceViewModel.resetAppName(pkg)
            }
            showRenameAppDialog = false
            renameTargetPackage = null
        },
        onDismiss = { showRenameAppDialog = false }
    )
}
