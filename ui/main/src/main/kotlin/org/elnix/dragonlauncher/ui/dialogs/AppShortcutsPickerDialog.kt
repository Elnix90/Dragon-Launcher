package org.elnix.dragonlauncher.ui.dialogs

import android.content.pm.ShortcutInfo
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.actions.AppIcon
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle
import org.elnix.dragonlauncher.ui.helpers.workspace.AppShortcutSearch

private fun ShortcutInfo.matchesAppShortcutSearch(appName: String, q: String): Boolean {
    if (q.isBlank()) return true
    return appName.contains(q, ignoreCase = true) ||
            `package`.contains(q, ignoreCase = true) ||
            (shortLabel?.toString()?.contains(q, ignoreCase = true) == true) ||
            (longLabel?.toString()?.contains(q, ignoreCase = true) == true) ||
            id.contains(q, ignoreCase = true)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShortcutPickerDialog(
    app: Application,
    shortcuts: List<ShortcutInfo>,
    onDismiss: () -> Unit,
    onShortcutSelected: (shortcut: ShortcutInfo) -> Unit,
    onAppSelected: () -> Unit
) {
    val appName = app.label
    var searchQuery by remember { mutableStateOf("") }

    val filteredShortcuts = remember(searchQuery, shortcuts, appName) {
        if (searchQuery.isBlank()) shortcuts
        else shortcuts.filter { it.matchesAppShortcutSearch(appName, searchQuery) }
    }

    DragonModalBottomSheet(onDismissRequest = onDismiss, true) {
        DialogTitle(stringResource(R.string.select_shortcut_action_title, appName))
        Spacer(10.dp)
        Column(
            modifier = Modifier
                .heightIn(max = 600.dp)
                .verticalScroll(rememberScrollState())
        ) {
            AppShortcutSearch(searchQuery) { searchQuery = it }

            when {
                shortcuts.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.no_extra_shortcuts),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                filteredShortcuts.isEmpty() && searchQuery.isNotEmpty() -> {
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
                }

                else -> {
                    DragonSettingsGroup(R.string.pinned_shortcuts) {
                        filteredShortcuts.forEach { shortcut ->
                            ShortcutItem(shortcut) {
                                onShortcutSelected(shortcut)
                            }
                        }
                    }
                }
            }

            Spacer(10.dp)
            DragonSettingsGroup {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .dragonSettingGroup {
                            clickable(onClick = onAppSelected)
                        }
                ) {
                    AppIcon(app, size = 30.dp)
                    Spacer(8.dp)
                    Text(
                        text = stringResource(R.string.just_open_app, appName),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
