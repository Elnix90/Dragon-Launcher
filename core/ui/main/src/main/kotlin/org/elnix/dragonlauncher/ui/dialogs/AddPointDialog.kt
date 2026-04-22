package org.elnix.dragonlauncher.ui.dialogs

import android.content.pm.ShortcutInfo
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.serializables.AppModel
import org.elnix.dragonlauncher.common.serializables.SwipeActionSerializable
import org.elnix.dragonlauncher.common.utils.BluetoothADBCommands
import org.elnix.dragonlauncher.common.utils.Constants
import org.elnix.dragonlauncher.common.utils.Constants.Actions.defaultChoosableActions
import org.elnix.dragonlauncher.common.utils.DataADBCommands
import org.elnix.dragonlauncher.common.utils.PackageManagerCompat
import org.elnix.dragonlauncher.common.utils.WifiADBCommands
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.settings.stores.BehaviorSettingsStore
import org.elnix.dragonlauncher.settings.stores.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.UiSettingsStore
import org.elnix.dragonlauncher.ui.actions.ActionIcon
import org.elnix.dragonlauncher.ui.actions.actionColor
import org.elnix.dragonlauncher.ui.actions.actionLabel
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.composition.LocalShowLabelsInAddPointDialog
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonRow
import org.elnix.dragonlauncher.ui.dragon.components.DragonTooltip
import org.elnix.dragonlauncher.ui.dragon.dialogs.CustomAlertDialog
import org.elnix.dragonlauncher.ui.dragon.text.AutoResizeableText

@Suppress("AssignedValueIsNeverRead")
@Composable
fun AddPointDialog(
    actions: Set<SwipeActionSerializable> = defaultChoosableActions,
    onNewNest: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    onActionSelected: ((SwipeActionSerializable) -> Unit)? = null,
    onMultipleActionsSelected: ((List<SwipeActionSerializable>, Boolean) -> Unit)? = null
) {
    require((onActionSelected != null).xor(onMultipleActionsSelected != null))

    val ctx = LocalContext.current
    val pm = ctx.packageManager
    val packageManagerCompat = PackageManagerCompat(pm, ctx)
    val scope = rememberCoroutineScope()

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

    val actualActions = remember(showKillLauncherActionInActionPicker, actions) {
        if (showKillLauncherActionInActionPicker) actions.toMutableSet().apply {
            add(SwipeActionSerializable.KillLauncher)
        } else actions
    }

    val promptForShortcuts by BehaviorSettingsStore.promptForShortcutsWhenAddingApp.asState()
    val showTooltipsOnAddPointDialog = LocalShowLabelsInAddPointDialog.current


    var selectedApp by remember { mutableStateOf<AppModel?>(null) }
    var shortcutDialogVisible by remember { mutableStateOf(false) }
    var shortcuts by remember { mutableStateOf<List<ShortcutInfo>>(emptyList()) }


    fun onActionPicked(action: SwipeActionSerializable) {
        if (onMultipleActionsSelected != null) {
            onMultipleActionsSelected(listOf(action), false)
        } else {
            onActionSelected!!(action)
        }
    }

    CustomAlertDialog(
        scroll = false,
        alignment = Alignment.Center,
        modifier = Modifier.padding(16.dp),
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = {
            DragonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.choose_action))
                DragonIconButton(
                    onClick = {
                        scope.launch {
                            UiSettingsStore.showTooltipsOnAddPointDialog.set(ctx, !showTooltipsOnAddPointDialog)
                        }
                    },
                    imageVector = if (showTooltipsOnAddPointDialog) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = stringResource(R.string.show_tooltips)
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                if (actualActions.any { it is SwipeActionSerializable.LaunchApp }) {

                    val dummyLaunchAppAction = SwipeActionSerializable.LaunchApp("", false, 0)
                    val color = actionColor(dummyLaunchAppAction, LocalExtraColors.current)


                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(DragonShape)
                            .background(color.copy(0.5f))
                            .border(1.dp, color, DragonShape)
                            .clickable { showAppPicker = true }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ActionIcon(
                            action = dummyLaunchAppAction,
                            modifier = Modifier.size(30.dp),
                            showLaunchAppVectorGrid = true
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = stringResource(R.string.open_app),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }


                LazyVerticalGrid(
                    modifier = Modifier.clip(DragonShape),
                    columns = GridCells.Fixed(if (showTooltipsOnAddPointDialog) 1 else 3),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    // Loop through all actions
                    items(actualActions.filterNot { it is SwipeActionSerializable.LaunchApp }) { action ->
                        AddPointColumn(
                            action = action,
                            showText = { showTooltipsOnAddPointDialog },
                            onSelected = {
                                when (action) {
                                    is SwipeActionSerializable.LaunchShortcut -> {
                                        showPinnedShortcutsPicker = true
                                    }

                                    is SwipeActionSerializable.OpenAppDrawer -> {
                                        showWorkspacePicker = true
                                    }

                                    is SwipeActionSerializable.OpenCircleNest -> {
                                        showNestPicker = true
                                    }

                                    is SwipeActionSerializable.OpenDragonLauncherSettings -> {
                                        showSettingsPagePicker = true
                                    }

                                    is SwipeActionSerializable.OpenFile -> {
                                        showFilePicker = true
                                    }

                                    is SwipeActionSerializable.OpenUrl -> {
                                        showUrlInput = true
                                    }

                                    is SwipeActionSerializable.RunAdbCommand -> {
                                        showAdbCommandInput = true
                                    }

                                    is SwipeActionSerializable.ToggleData -> {
                                        showDataCommandInput = true
                                    }

                                    is SwipeActionSerializable.ToggleWifi -> {
                                        showWifiCommandInput = true
                                    }

                                    is SwipeActionSerializable.ToggleBluetooth -> {
                                        showBluetoothCommandInput = true
                                    }

                                    else -> onActionPicked(action)
                                }
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    )

    if (showAppPicker) {
        AppPickerDialog(
            multiSelectEnabled = onMultipleActionsSelected != null,
            onDismiss = { showAppPicker = false },
            onAppSelected = { app ->

                logD(Constants.Logging.APP_LAUNCH_TAG) { "Selected App: $app" }

                // Try to query shortcuts, but handle crashes gracefully
                val list = if (promptForShortcuts) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            packageManagerCompat.queryAppShortcuts(app.packageName)
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
                    onActionPicked(
                        SwipeActionSerializable.LaunchApp(
                            app.packageName,
                            app.isPrivateProfile,
                            app.userId ?: 0
                        )
                    )
                }
            },
            onMultipleAppsSelected = if (onMultipleActionsSelected != null) {
                { apps, autoPlace ->
                    val actions = apps.map { SwipeActionSerializable.LaunchApp(it.packageName, it.isPrivateProfile, it.userId ?: 0) }
                    onMultipleActionsSelected(actions, autoPlace)
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
            onActionPicked(SwipeActionSerializable.ToggleWifi(command, toast))
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
            onActionPicked(SwipeActionSerializable.ToggleBluetooth(command, toast))
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
            onActionPicked(SwipeActionSerializable.ToggleData(command, toast))
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
        AppShortcutPickerDialog(
            app = selectedApp!!,
            shortcuts = shortcuts,
            onDismiss = { shortcutDialogVisible = false },
            onShortcutSelected = { pkg, id ->
                onActionPicked(SwipeActionSerializable.LaunchShortcut(pkg, id))
                shortcutDialogVisible = false
            },
            onOpenApp = {
                onActionPicked(
                    SwipeActionSerializable.LaunchApp(
                        selectedApp!!.packageName,
                        selectedApp!!.isPrivateProfile,
                        selectedApp!!.userId ?: 0
                    )
                )
                onDismiss()
            }
        )
    }

    if (showNestPicker) {
        NestManagementDialog(
            onDismissRequest = { showNestPicker = false },
            title = stringResource(R.string.pick_a_nest),
            onNewNest = onNewNest,
            onNameChange = null,
            onDelete = null,
            onSelect = {
                onActionPicked(SwipeActionSerializable.OpenCircleNest(it.id))
                showNestPicker = false
            }
        )
    }

    if (showSettingsPagePicker) {
        SettingsPagePicker(
            onDismissRequest = { showSettingsPagePicker = false }
        ) {
            onActionPicked(SwipeActionSerializable.OpenDragonLauncherSettings(it))
            showSettingsPagePicker = false
        }
    }

    if (showWorkspacePicker) {
        WorkspacePickerDialog(
            onDismiss = { showWorkspacePicker = false },
            onActionPicked = { onActionPicked(it) }
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
private fun AddPointColumn(
    action: SwipeActionSerializable,
    showText: () -> Boolean,
    onSelected: () -> Unit
) {
    val extraColors = LocalExtraColors.current

    val name = when (action) {
        /** Not verifying for open app, because it is filtered by the filter above in [AddPointColumn] */


        is SwipeActionSerializable.LaunchShortcut -> {
            if (action.packageName.isEmpty()) stringResource(R.string.pinned_shortcuts)
            else actionLabel(action)
        }

        is SwipeActionSerializable.OpenUrl -> stringResource(R.string.open_url)
        is SwipeActionSerializable.RunAdbCommand -> stringResource(R.string.run_adb_command)
        is SwipeActionSerializable.OpenFile -> stringResource(R.string.open_file)
        is SwipeActionSerializable.OpenCircleNest -> stringResource(R.string.open_nest_circle)
        else -> actionLabel(action)
    }

    val color = actionColor(action, extraColors)

    DragonTooltip(name) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(DragonShape)
                .background(color.copy(0.5f))
                .border(1.dp, color, DragonShape)
                .clickable { onSelected() }
                .padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionIcon(
                action = action,
                modifier = Modifier.size(30.dp),
                showLaunchAppVectorGrid = true
            )

            if (showText()) {
                Spacer(Modifier.width(5.dp))
                AutoResizeableText(
                    name,
                    maxLines = 2
                )
            }
        }
    }
}
