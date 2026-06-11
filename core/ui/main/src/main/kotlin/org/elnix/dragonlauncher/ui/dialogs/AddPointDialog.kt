package org.elnix.dragonlauncher.ui.dialogs

import android.content.pm.ShortcutInfo
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.models.Application.Companion.toLaunchApp
import org.elnix.dragonlauncher.base.model.models.BluetoothADBCommands
import org.elnix.dragonlauncher.base.model.models.DataADBCommands
import org.elnix.dragonlauncher.base.model.models.PointApp
import org.elnix.dragonlauncher.base.model.models.WifiADBCommands
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Action.Companion.actionColor
import org.elnix.dragonlauncher.base.model.serializables.Action.Companion.defaultChoosableActions
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.logging.APP_LAUNCH_TAG
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.settings.stores.map.BehaviorSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.actions.ActionIcon
import org.elnix.dragonlauncher.ui.actions.AppIcon
import org.elnix.dragonlauncher.ui.actions.actionLabel
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.composition.LocalShowLabelsInAddPointDialog
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonRow
import org.elnix.dragonlauncher.ui.dragon.components.DragonTooltip
import org.elnix.dragonlauncher.ui.dragon.dialogs.CustomAlertDialog
import org.elnix.dragonlauncher.ui.dragon.text.AutoResizeableText

@Suppress("AssignedValueIsNeverRead")
@Composable
fun AddPointDialog(
    actions: List<Action> = defaultChoosableActions,
    drawerViewModel: DrawerViewModel = activityViewModel(),
    onDismiss: () -> Unit,
    onActionSelected: ((Action) -> Unit)? = null,
    onMultipleActionsSelected: ((List<Action>, Boolean) -> Unit)? = null
) {
    require((onActionSelected != null) xor (onMultipleActionsSelected != null)) {
        "You can either use onActionSelected or onMultipleActionsSelected but not both at the same time"
    }


    val ctx = LocalContext.current
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
        if (showKillLauncherActionInActionPicker) actions.toMutableList().apply {
            add(Action.KillLauncher)
        } else actions
    }

    val promptForShortcuts by BehaviorSettingsStore.promptForShortcutsWhenAddingApp.asState()
    val showTooltipsOnAddPointDialog = LocalShowLabelsInAddPointDialog.current


    var selectedApp by remember { mutableStateOf<Application?>(null) }
    var shortcutDialogVisible by remember { mutableStateOf(false) }
    var shortcuts by remember { mutableStateOf<List<ShortcutInfo>>(emptyList()) }


    fun onActionPicked(action: Action) {
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
                    icon = if (showTooltipsOnAddPointDialog) R.drawable.arrow_drop_down else R.drawable.arrow_drop_up,
                    contentDescription = stringResource(R.string.show_tooltips)
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                if (actualActions.any { it is Action.LaunchApp }) {

                    val dummyLaunchAppAction = Action.LaunchApp.dummy()
                    val dummyPoint = Point.dummySwipePoint(dummyLaunchAppAction)
                    val fakeApp = PointApp(dummyPoint)
                    val color = dummyLaunchAppAction.actionColor(LocalExtraColors.current)


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
                        AppIcon(fakeApp)
                        Spacer(5.dp)
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
                    items(actualActions.filterNot { it is Action.LaunchApp }) { action ->
                        AddPointColumn(
                            action = action,
                            showText = { showTooltipsOnAddPointDialog },
                            onSelected = {
                                when (action) {
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
                        Spacer(8.dp)
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

                logD(APP_LAUNCH_TAG) { "Selected App: $app" }

                // Try to query shortcuts, but handle crashes gracefully
                val list = if (promptForShortcuts) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            drawerViewModel.appsRepository.queryAppShortcuts(app.packageName)
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
                { apps, autoPlace ->
                    val actions = apps.map {
                        Action.LaunchApp(it.packageName, it.profile)
                    }
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
            onShortcutSelected = { pkg, id ->
                onActionPicked(Action.LaunchShortcut(pkg, id))
                shortcutDialogVisible = false
            },
            onOpenApp = {
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
    action: Action,
    showText: () -> Boolean,
    onSelected: () -> Unit
) {
    val extraColors = LocalExtraColors.current

    val name = when (action) {
        /** Not verifying for open app, because it is filtered by the filter above in [AddPointColumn] */


        is Action.LaunchShortcut -> {
            if (action.packageName.isEmpty()) stringResource(R.string.pinned_shortcuts)
            else actionLabel(action)
        }

        is Action.OpenUrl -> stringResource(R.string.open_url)
        is Action.RunAdbCommand -> stringResource(R.string.run_adb_command)
        is Action.OpenFile -> stringResource(R.string.open_file)
        is Action.OpenCircleNest -> stringResource(R.string.open_nest_circle)
        else -> actionLabel(action)
    }

    val color = action.actionColor(extraColors)

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
                size = 30.dp
            )

            if (showText()) {
                Spacer(5.dp)
                AutoResizeableText(
                    name,
                    maxLines = 2
                )
            }
        }
    }
}
