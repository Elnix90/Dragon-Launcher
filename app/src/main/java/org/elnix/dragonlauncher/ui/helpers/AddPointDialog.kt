package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.SwipeActionSerializable
import org.elnix.dragonlauncher.data.stores.DrawerSettingsStore
import org.elnix.dragonlauncher.ui.actionTint
import org.elnix.dragonlauncher.ui.theme.LocalExtraColors
import org.elnix.dragonlauncher.utils.models.AppDrawerViewModel
import org.elnix.dragonlauncher.utils.actions.actionColor
import org.elnix.dragonlauncher.utils.actions.actionIcon
import org.elnix.dragonlauncher.utils.actions.actionLabel
import org.elnix.dragonlauncher.utils.models.WorkspaceViewModel

@Suppress("AssignedValueIsNeverRead")
@Composable
fun AddPointDialog(
    appsViewModel: AppDrawerViewModel,
    workspaceViewModel: WorkspaceViewModel,
    onDismiss: () -> Unit,
    onActionSelected: (SwipeActionSerializable) -> Unit
) {
    val ctx = LocalContext.current

    var showAppPicker by remember { mutableStateOf(false) }
    var showUrlInput by remember { mutableStateOf(false) }
    var showFilePicker by remember { mutableStateOf(false) }

    // All actions except those requiring special sub-dialogs
    val actions = listOf(
        SwipeActionSerializable.LaunchApp(""),
        SwipeActionSerializable.OpenUrl(""),
        SwipeActionSerializable.OpenFile(""),
        SwipeActionSerializable.NotificationShade,
        SwipeActionSerializable.ControlPanel,
        SwipeActionSerializable.OpenAppDrawer,
        SwipeActionSerializable.Lock,
        SwipeActionSerializable.ReloadApps,
        SwipeActionSerializable.OpenDragonLauncherSettings,
    )

    val gridSize by DrawerSettingsStore.getGridSize(ctx)
        .collectAsState(initial = 1)
    val showIcons by DrawerSettingsStore.getShowAppIconsInDrawer(ctx)
        .collectAsState(initial = true)
    val showLabels by DrawerSettingsStore.getShowAppLabelsInDrawer(ctx)
        .collectAsState(initial = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text("Choose action") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .height(320.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                // Loop through all actions
                items(actions) { action ->
                    when (action) {

                        // Open App → requires AppPicker
                        is SwipeActionSerializable.LaunchApp -> {
                            AddPointRow(
                                action = action,
                                onSelected = { showAppPicker = true }
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        // Open URL → requires URL dialog
                        is SwipeActionSerializable.OpenUrl -> {
                            AddPointRow(
                                action = action,
                                onSelected = { showUrlInput = true }
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        // Open File picker to choose a file
                        is SwipeActionSerializable.OpenFile -> {
                            AddPointRow(
                                action = action,
                                onSelected = { showFilePicker = true }
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        // Direct actions
                        else -> {
                            AddPointRow(
                                action = action,
                                onSelected = { onActionSelected(action) }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )

    if (showAppPicker) {
        AppPickerDialog(
            appsViewModel = appsViewModel,
            workspaceViewModel = workspaceViewModel,
            gridSize = gridSize,
            showIcons = showIcons,
            showLabels = showLabels,
            onDismiss = { showAppPicker = false },
            onAppSelected = {
                onActionSelected(it)
                showAppPicker = false
            }
        )
    }

    if (showUrlInput) {
        UrlInputDialog(
            onDismiss = { showUrlInput = false },
            onUrlSelected = {
                onActionSelected(it)
                showUrlInput = false
            }
        )
    }

    if (showFilePicker) {
        FilePickerDialog(
            onDismiss = { showFilePicker = false },
            onFileSelected = {
                onActionSelected(it)
                showFilePicker = false
            }
        )
    }
}


@Composable
fun AddPointRow(
    action: SwipeActionSerializable,
    onSelected: () -> Unit
) {
    val extraColors = LocalExtraColors.current


    val icon = when(action) {
        is SwipeActionSerializable.LaunchApp -> painterResource(R.drawable.ic_app_grid)
        else -> actionIcon(action)
    }

    val name = when(action) {
        is SwipeActionSerializable.LaunchApp -> "Open App"
        is SwipeActionSerializable.OpenUrl -> "Open Url"
        is SwipeActionSerializable.OpenFile -> "Open File"
        else -> actionLabel(action)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(actionColor(action, extraColors).copy(0.5f))
            .clickable { onSelected() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            color = Color.White
        )
        Icon(
            painter = icon,
            contentDescription = action.toString(),
            tint = actionTint(action, extraColors),
            modifier = Modifier.size(30.dp)
        )
    }
}
