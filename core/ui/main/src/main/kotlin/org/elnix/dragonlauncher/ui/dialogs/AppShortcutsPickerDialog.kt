package org.elnix.dragonlauncher.ui.dialogs

import android.content.pm.ShortcutInfo
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.serializables.Action.LaunchShortcut.Companion.toAction
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.actions.AppIcon
import org.elnix.dragonlauncher.ui.actions.ShortcutIcon
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.helpers.workspace.AppDrawerSearch

private fun ShortcutInfo.matchesAppShortcutSearch(appName: String, q: String): Boolean {
    if (q.isBlank()) return true
    return appName.contains(q, ignoreCase = true) ||
        `package`.contains(q, ignoreCase = true) ||
        (shortLabel?.toString()?.contains(q, ignoreCase = true) == true) ||
        (longLabel?.toString()?.contains(q, ignoreCase = true) == true) ||
        id.contains(q, ignoreCase = true)
}

@Composable
fun AppShortcutPickerDialog(
    app: Application,
    shortcuts: List<ShortcutInfo>,
    onDismiss: () -> Unit,
    onShortcutSelected: (shortcut: ShortcutInfo) -> Unit,
    onOpenApp: () -> Unit
) {
    val appName = app.label
    var searchQuery by remember { mutableStateOf("") }

    val filteredShortcuts = remember(searchQuery, shortcuts, appName) {
        if (searchQuery.isBlank()) shortcuts
        else shortcuts.filter { it.matchesAppShortcutSearch(appName, searchQuery) }
    }

    val openAppLabel = stringResource(R.string.just_open_app, appName)
    val showOpenAppRow = searchQuery.isBlank() ||
        appName.contains(searchQuery, ignoreCase = true) ||
        openAppLabel.contains(searchQuery, ignoreCase = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_shortcut_action_title, appName)) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                AppDrawerSearch(
                    searchQuery = searchQuery,
                    onSearchChanged = { searchQuery = it },
                    placeholderText = stringResource(R.string.search_shortcuts),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.search),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    painter = painterResource(R.drawable.close),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                )

                if (shortcuts.isEmpty()) {
                    Text(
                        stringResource(R.string.no_extra_shortcuts),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else if (filteredShortcuts.isEmpty() && searchQuery.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.no_search_match),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    filteredShortcuts.forEach { shortcut ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(DragonShape)
                                .clickable {
                                    onShortcutSelected(shortcut)
                                }
                                .padding(8.dp)
                        ) {
                            ShortcutIcon(
                                shortcut = shortcut.toAction(),
                                size = 35.dp
                            )
                            Text(
                                text = shortcut.shortLabel?.toString() ?: "Unnamed",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                if (showOpenAppRow) {
                    Spacer(12.dp)
                    HorizontalDivider()
                    Spacer(8.dp)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(DragonShape)
                            .clickable { onOpenApp() }
                            .padding(8.dp)
                    ) {

                        AppIcon(app, size = 30.dp)
                        Spacer(8.dp)

                        Text(
                            text = openAppLabel,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {},
        containerColor = MaterialTheme.colorScheme.surface,
        shape = DragonShape
    )
}
