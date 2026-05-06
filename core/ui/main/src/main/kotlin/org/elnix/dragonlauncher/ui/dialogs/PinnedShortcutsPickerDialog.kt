package org.elnix.dragonlauncher.ui.dialogs

import android.annotation.SuppressLint
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.os.Process
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.serializables.SwipeActionSerializable
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.ICONS_TAG
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.PINNED_SHORTCUTS
import org.elnix.dragonlauncher.common.utils.ImageUtils.loadDrawableAsBitmap
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.logging.logW
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.helpers.AppDrawerSearch

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
@Composable
fun PinnedShortcutsPickerDialog(
    onDismiss: () -> Unit,
    onShortcutSelected: (SwipeActionSerializable.LaunchShortcut) -> Unit
) {
    val ctx = LocalContext.current

    val groupedShortcuts: Map<String, List<PinnedShortcutItem>> = remember {
        try {
            queryAllPinnedShortcuts(ctx)
        } catch (e: Exception) {
            logE(PINNED_SHORTCUTS, e) { "Failed to query pinned shortcuts" }
            emptyMap()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.pinned_shortcuts),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (groupedShortcuts.isEmpty()) {
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
            } else {
                var searchQuery by remember { mutableStateOf("") }
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

                Column(modifier = Modifier.fillMaxWidth()) {
                    AppDrawerSearch(
                        searchQuery = searchQuery,
                        onSearchChanged = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        placeholderText = stringResource(R.string.search_shortcuts),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    )

                    if (filteredGrouped.isEmpty() && searchQuery.isNotEmpty()) {
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
                    } else {
                        Column(
                            modifier = Modifier
                                .heightIn(max = 450.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            var isFirst = true
                            filteredGrouped.forEach { (appName, shortcuts) ->
                                if (!isFirst) {
                                    Spacer(Modifier.height(8.dp))
                                    HorizontalDivider()
                                    Spacer(Modifier.height(8.dp))
                                }
                                isFirst = false

                                Text(
                                    text = appName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )

                                shortcuts.forEach { item ->
                                    ShortcutRow(
                                        shortcut = item.shortcutInfo,
                                        onClick = {
                                            onShortcutSelected(
                                                SwipeActionSerializable.LaunchShortcut(
                                                    packageName = item.packageName,
                                                    shortcutId = item.shortcutInfo.id
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
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = DragonShape
    )
}

@Composable
private fun ShortcutRow(
    shortcut: ShortcutInfo,
    onClick: () -> Unit
) {
    val ctx = LocalContext.current

    val drawable = remember(shortcut.id, shortcut.`package`) {
        try {
            val launcherApps = ctx.getSystemService(LauncherApps::class.java)
            launcherApps?.getShortcutIconDrawable(shortcut, 0)
        } catch (_: Exception) {
            null
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(DragonShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        if (drawable != null) {
            val bitmapPainter = remember(drawable) {
                try {
                    val bmp = loadDrawableAsBitmap(drawable, 48, 48)
                    BitmapPainter(bmp)
                } catch (e: Exception) {
                    logW(ICONS_TAG, e) { "Unable to load icon via loadDrawableAsBitmap" }
                    null
                }
            }

            if (bitmapPainter != null) {
                Image(
                    painter = bitmapPainter,
                    contentDescription = shortcut.shortLabel?.toString(),
                    modifier = Modifier
                        .size(36.dp)
                        .clip(DragonShape)
                )
                Spacer(Modifier.width(12.dp))
            }
        }

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
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 11.sp
                )
            }
        }
    }
}

/**
 * Queries all pinned shortcuts across all installed apps.
 * Returns a map of appName -> list of PinnedShortcutItem.
 */
@SuppressLint("InlinedApi")
private fun queryAllPinnedShortcuts(
    ctx: android.content.Context
): Map<String, List<PinnedShortcutItem>> {
    val launcherApps = ctx.getSystemService(LauncherApps::class.java)
        ?: return emptyMap()
    val pm = ctx.packageManager
    val userHandle = Process.myUserHandle()

    // Get only launchable apps to avoid querying system packages
    val launchIntent = android.content.Intent(android.content.Intent.ACTION_MAIN, null)
    launchIntent.addCategory(android.content.Intent.CATEGORY_LAUNCHER)
    val packages = pm.queryIntentActivities(launchIntent, 0)
        .map { it.activityInfo.packageName }
        .distinct()

    val allShortcuts = mutableListOf<PinnedShortcutItem>()

    for (pkg in packages) {
        try {

            val query = LauncherApps.ShortcutQuery()
                .setPackage(pkg)
                .setQueryFlags(
                    LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED or
                            LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                            LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                            LauncherApps.ShortcutQuery.FLAG_MATCH_CACHED
                )

            val shortcuts = launcherApps.getShortcuts(query, userHandle)
            if (shortcuts.isNullOrEmpty()) continue

            val appName = try {
                pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
            } catch (_: Exception) {
                pkg
            }

            for (shortcut in shortcuts) {
                allShortcuts.add(
                    PinnedShortcutItem(
                        shortcutInfo = shortcut,
                        appName = appName,
                        packageName = pkg
                    )
                )
            }
        } catch (e: SecurityException) {
            logD(PINNED_SHORTCUTS, e) { "SecurityException for $pkg" }
        } catch (e: Exception) {
            logE(PINNED_SHORTCUTS, e) { "Error querying shortcuts for $pkg" }
        }
    }

    // Group by app name, sorted alphabetically
    return allShortcuts
        .groupBy { it.appName }
        .toSortedMap(String.CASE_INSENSITIVE_ORDER)
}
