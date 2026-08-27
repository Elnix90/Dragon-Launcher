package org.elnix.dragonlauncher.ui.dialogs

import android.content.pm.ShortcutInfo
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elnix90.logging.logD
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.APP_LAUNCH_TAG
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.models.Application.Companion.toLaunchApp
import org.elnix.dragonlauncher.base.model.models.BluetoothADBCommands
import org.elnix.dragonlauncher.base.model.models.DataADBCommands
import org.elnix.dragonlauncher.base.model.models.WifiADBCommands
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Action.Companion.actionColor
import org.elnix.dragonlauncher.base.model.serializables.Action.Companion.allActions
import org.elnix.dragonlauncher.base.model.serializables.Action.LaunchShortcut.Companion.toAction
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.alphaMultiplier
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.settings.stores.map.BehaviorSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.ui.actions.ActionIcon
import org.elnix.dragonlauncher.ui.actions.actionLabel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.components.VerticalScrollIndicator
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionPickerDialog(
    drawerViewModel: DrawerViewModel = activityViewModel(),
    allowWidgets: Boolean = false,
    onDismiss: () -> Unit,
    onActionSelected: ((Action) -> Unit)? = null,
    onMultipleActionsSelected: ((action: List<Action>) -> Unit)? = null
) {
    require((onActionSelected != null) xor (onMultipleActionsSelected != null)) {
        "You can either use onActionSelected or onMultipleActionsSelected but not both at the same time"
    }

    var showAppPicker by remember { mutableStateOf(false) }
    var showUrlInput by remember { mutableStateOf(false) }
    var showAdbCommandInput by remember { mutableStateOf(false) }
    var showWifiCommandInput by remember { mutableStateOf(false) }
    var showBluetoothCommandInput by remember { mutableStateOf(false) }
    var showDataCommandInput by remember { mutableStateOf(false) }
    var showFilePicker by remember { mutableStateOf(false) }
    var showNestPicker by remember { mutableStateOf(false) }
    var showWorkspacePicker by remember { mutableStateOf(false) }
    var showPinnedShortcutsPicker by remember { mutableStateOf(false) }
    var showSettingsPagePicker by remember { mutableStateOf(false) }

    val showKillLauncherActionInActionPicker by DebugSettingsStore.showKillLauncherActionInActionPicker.asState()
    val promptForShortcuts by BehaviorSettingsStore.promptForShortcutsWhenAddingApp.asState()

    val actualActions = allActions.filter {
        when (it) {
            Action.KillLauncher -> showKillLauncherActionInActionPicker
            is Action.OpenWidget -> allowWidgets
            else -> true
        }
    }

    var selectedApp by remember { mutableStateOf<Application?>(null) }
    var shortcutDialogVisible by remember { mutableStateOf(false) }
    var shortcuts by remember { mutableStateOf<List<ShortcutInfo>>(emptyList()) }


    fun onActionPicked(action: Action) {
        if (onMultipleActionsSelected != null) {
            onMultipleActionsSelected(listOf(action))
        } else {
            onActionSelected!!(action)
        }
    }

    DragonModalBottomSheet(
        onDismissRequest = onDismiss,
        skipPartiallyExpanded = true
    ) {
        DialogTitle(stringResource(R.string.choose_action))

        Spacer(10.dp)

        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.heightIn(max = 600.dp)
        ) {

            val gridState = rememberLazyGridState()

            Box {
                LazyVerticalGrid(
                    modifier = Modifier.clip(MaterialTheme.shapes.large),
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    items(actualActions) { action ->
                        ActionItem(
                            action = action,
                            onSelected = {
                                when (action) {
                                    is Action.LaunchApp -> {
                                        showAppPicker = true
                                    }

                                    is Action.LaunchShortcut -> {
                                        showPinnedShortcutsPicker = true
                                    }

                                    is Action.OpenAppDrawer -> {
                                        showWorkspacePicker = true
                                    }

                                    is Action.OpenCircleNest -> {
                                        showNestPicker = true
                                    }

                                    is Action.OpenDragonLauncherSettings -> {
                                        showSettingsPagePicker = true
                                    }

                                    is Action.OpenFile -> {
                                        showFilePicker = true
                                    }

                                    is Action.OpenUrl -> {
                                        showUrlInput = true
                                    }

                                    is Action.RunAdbCommand -> {
                                        showAdbCommandInput = true
                                    }

                                    is Action.ToggleData -> {
                                        showDataCommandInput = true
                                    }

                                    is Action.ToggleWifi -> {
                                        showWifiCommandInput = true
                                    }

                                    is Action.ToggleBluetooth -> {
                                        showBluetoothCommandInput = true
                                    }

                                    else -> onActionPicked(action)
                                }
                            }
                        )
                        this@Column.Spacer(8.dp)
                    }
                }
                VerticalScrollIndicator(gridState.canScrollForward)
            }
        }
    }


    if (showAppPicker) {
        AppPickerSheet(
            multiSelectEnabled = onMultipleActionsSelected != null,
            onDismiss = { showAppPicker = false },
            onAppSelected = { app ->

                logD(APP_LAUNCH_TAG) { "Selected App: $app" }

                // Try to query shortcuts, but handle crashes gracefully
                val list = if (promptForShortcuts) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            drawerViewModel.queryAppShortcuts(app.packageName)
                        } else {
                            emptyList()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Some apps (Contacts, Gmail) may throw SecurityException or other errors
                        emptyList()
                    }
                } else {
                    emptyList() // Skip shortcut query if disabled
                }

                if (list.isNotEmpty()) {
                    selectedApp = app
                    shortcuts = list
                    shortcutDialogVisible = true
                } else {
                    onActionPicked(app.toLaunchApp())
                }
            },
            onMultipleAppsSelected = if (onMultipleActionsSelected != null) {
                { apps ->
                    val actions = apps.map {
                        Action.LaunchApp(it.packageName, it.profile)
                    }
                    onMultipleActionsSelected(actions)
                    showAppPicker = false
                }
            } else null
        )
    }

    if (showUrlInput) {
        UrlInputDialog(
            onDismiss = { showUrlInput = false },
            onUrlSelected = {
                onActionPicked(it)
                showUrlInput = false
            }
        )
    }

    if (showAdbCommandInput) {
        AdbCommandInputDialog(
            onDismiss = { showAdbCommandInput = false },
            showLeaveEmptyNotice = true,
            onActionSelected = {
                onActionPicked(it)
                showAdbCommandInput = false
            }
        )
    }

    if (showWifiCommandInput) {
        AdbCommandPickerDialog(
            label = "${stringResource(R.string.pick_a)} WIFI ${stringResource(R.string.command)}",
            options = WifiADBCommands.entries,
            selected = { WifiADBCommands.Svc },
            onDismiss = { showWifiCommandInput = false },
        ) { command, toast ->
            onActionPicked(Action.ToggleWifi(command, toast))
            showWifiCommandInput = false
        }
    }


    if (showBluetoothCommandInput) {
        AdbCommandPickerDialog(
            label = "${stringResource(R.string.pick_a)} BLUETOOTH ${stringResource(R.string.command)}",
            options = BluetoothADBCommands.entries,
            selected = { BluetoothADBCommands.Cmd },
            onDismiss = { showBluetoothCommandInput = false },
        ) { command, toast ->
            onActionPicked(Action.ToggleBluetooth(command, toast))
            showBluetoothCommandInput = false
        }
    }


    if (showDataCommandInput) {
        AdbCommandPickerDialog(
            label = "${stringResource(R.string.pick_a)} DATA ${stringResource(R.string.command)}",
            options = DataADBCommands.entries,
            selected = { DataADBCommands.Svc },
            onDismiss = { showDataCommandInput = false },
        ) { command, toast ->
            onActionPicked(Action.ToggleData(command, toast))
            showDataCommandInput = false
        }
    }

    if (showFilePicker) {
        FilePickerDialog(
            onDismiss = { showFilePicker = false },
            onFileSelected = {
                onActionPicked(it)
                showFilePicker = false
            }
        )
    }

    if (shortcutDialogVisible && selectedApp != null) {
        val app = selectedApp!!
        AppShortcutPickerDialog(
            app = app,
            shortcuts = shortcuts,
            onDismiss = { shortcutDialogVisible = false },
            onShortcutSelected = { shortcut ->
                onActionPicked(shortcut.toAction())
                shortcutDialogVisible = false
            },
            onAppSelected = {
                onActionPicked(app.toLaunchApp())
                onDismiss()
            }
        )
    }

    if (showNestPicker) {
        NestManagementDialog(
            onDismissRequest = { showNestPicker = false },
            title = stringResource(R.string.pick_a_nest),
            onSelect = {
                onActionPicked(Action.OpenCircleNest(it.id))
                showNestPicker = false
            }
        )
    }

    if (showSettingsPagePicker) {
        SettingsPagePicker(
            onDismissRequest = { showSettingsPagePicker = false }
        ) {
            onActionPicked(Action.OpenDragonLauncherSettings(it))
            showSettingsPagePicker = false
        }
    }

    if (showWorkspacePicker) {
        WorkspacePickerDialog(
            onDismiss = { showWorkspacePicker = false },
            onActionPicked = {
                onActionPicked(it)
                showWorkspacePicker = false
            }
        )
    }

    if (showPinnedShortcutsPicker) {
        PinnedShortcutsPickerDialog(
            onDismiss = { showPinnedShortcutsPicker = false },
            onShortcutSelected = { shortcutAction ->
                onActionPicked(shortcutAction)
                showPinnedShortcutsPicker = false
            }
        )
    }
}


@Composable
private fun ActionItem(
    action: Action,
    onSelected: () -> Unit
) {
    val extraColors = LocalExtraColors.current

    val name = when (action) {
        is Action.LaunchApp -> stringResource(R.string.open_app)
        is Action.LaunchShortcut -> stringResource(R.string.pinned_shortcuts)
        is Action.OpenUrl -> stringResource(R.string.open_url)
        is Action.RunAdbCommand -> stringResource(R.string.run_adb_command)
        is Action.OpenFile -> stringResource(R.string.open_file)
        is Action.OpenCircleNest -> stringResource(R.string.open_nest)
        else -> actionLabel(action)
    }

    val color = action.actionColor(extraColors)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(color.alphaMultiplier(0.5f))
            .clickable(onClick = onSelected)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActionIcon(
            action = action,
            size = 30.dp
        )

        Text(
            text = name,
            maxLines = 2,
            modifier = Modifier.basicMarquee(Int.MAX_VALUE),
            style = MaterialTheme.typography.titleMediumEmphasized
        )
    }
}
