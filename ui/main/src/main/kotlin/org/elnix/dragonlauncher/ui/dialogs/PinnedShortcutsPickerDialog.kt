package org.elnix.dragonlauncher.ui.dialogs

import android.content.pm.ShortcutInfo
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Action.LaunchShortcut.Companion.toAction
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.ui.actions.ShortcutIcon
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.components.DragonGroupScope
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle
import org.elnix.dragonlauncher.ui.helpers.workspace.AppShortcutSearch

/**
 * Represents a pinned shortcut with extra metadata for display.
 */
private data class PinnedShortcutItem(
    val shortcutInfo: ShortcutInfo,
    val appName: String,
    val packageName: String
)

private fun PinnedShortcutItem.matchesShortcutSearch(q: String): Boolean {
    if (q.isBlank()) return true
    val s = shortcutInfo
    return appName.contains(q, ignoreCase = true) ||
            packageName.contains(q, ignoreCase = true) ||
            (s.shortLabel?.toString()?.contains(q, ignoreCase = true) == true) ||
            (s.longLabel?.toString()?.contains(q, ignoreCase = true) == true) ||
            s.id.contains(q, ignoreCase = true)
}

/**
 * Dialog that displays all pinned shortcuts from all installed apps,
 * grouped by app. Allows the user to pick one to add as a swipe action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinnedShortcutsPickerDialog(
    drawerViewModel: DrawerViewModel = activityViewModel(),
    onDismiss: () -> Unit,
    onShortcutSelected: (Action.LaunchShortcut) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val shortcuts by drawerViewModel.searchShortcuts(searchQuery).collectAsStateWithLifecycle(emptyList())
    val applications by drawerViewModel.allApps.collectAsStateWithLifecycle()

    val groupedShortcuts: Map<String, List<PinnedShortcutItem>> = remember(shortcuts, applications) {
        val allShortcuts = mutableListOf<PinnedShortcutItem>()

        for (shortcut in shortcuts) {
            val appLabel = applications.firstOrNull { it.packageName == shortcut.`package` }?.label ?: continue
            allShortcuts.add(
                PinnedShortcutItem(
                    shortcutInfo = shortcut,
                    appName = appLabel,
                    packageName = shortcut.`package`
                )
            )
        }

        allShortcuts
            .groupBy { it.appName }
            .toSortedMap(String.CASE_INSENSITIVE_ORDER)
    }

    val filteredGrouped = remember(searchQuery, groupedShortcuts) {
        if (searchQuery.isBlank()) groupedShortcuts
        else {
            val q = searchQuery
            groupedShortcuts.mapNotNull { (appName, items) ->
                val filteredItems = items.filter { it.matchesShortcutSearch(q) }
                if (filteredItems.isEmpty()) null else appName to filteredItems
            }.toMap()
        }
    }

    DragonModalBottomSheet(onDismissRequest = onDismiss, true) {
        DialogTitle(stringResource(R.string.pinned_shortcuts))
        Spacer(10.dp)

        AppShortcutSearch(searchQuery) { searchQuery = it }
        Spacer(10.dp)

        when {
            groupedShortcuts.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_pinned_shortcuts),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            filteredGrouped.isEmpty() && searchQuery.isNotEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_search_match),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .heightIn(max = 500.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    filteredGrouped.forEach { (appName, shortcuts) ->
                        DragonSettingsGroup(appName) {
                            shortcuts.forEach { item ->
                                ShortcutItem(
                                    shortcut = item.shortcutInfo,
                                    onClick = {
                                        onShortcutSelected(
                                            Action.LaunchShortcut(
                                                packageName = item.packageName,
                                                shortcutId = item.shortcutInfo.id,
                                                user = item.shortcutInfo.userHandle
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DragonGroupScope.ShortcutItem(
    shortcut: ShortcutInfo,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .dragonSettingGroup {
                clickable(onClick = onClick)
            }
    ) {
        ShortcutIcon(shortcut.toAction(), 36.dp)
        Spacer(8.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = shortcut.shortLabel?.toString() ?: shortcut.id,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            shortcut.longLabel?.toString()?.takeIf { it.isNotBlank() }?.let { longLabel ->
                Text(
                    text = longLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
