package org.elnix.dragonlauncher.ui.dialogs

import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.serializables.AppModel
import org.elnix.dragonlauncher.common.utils.ImageUtils.loadDrawableAsBitmap
import org.elnix.dragonlauncher.common.messyfolder.resolveShape
import org.elnix.dragonlauncher.ui.actions.appIcon
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.composition.LocalIconShape
import org.elnix.dragonlauncher.ui.helpers.AppDrawerSearch

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
    app: AppModel,
    shortcuts: List<ShortcutInfo>,
    onDismiss: () -> Unit,
    onShortcutSelected: (packageName: String, shortcutId: String) -> Unit,
    onOpenApp: () -> Unit
) {
    val ctx = LocalContext.current
    val iconsShape = LocalIconShape.current

    val appName = app.name
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
                        val drawable = remember(shortcut.id) {
                            val launcherApps =
                                ctx.getSystemService(LauncherApps::class.java)
                            launcherApps?.getShortcutIconDrawable(shortcut, 0)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(DragonShape)
                                .clickable {
                                    onShortcutSelected(shortcut.`package`, shortcut.id)
                                }
                                .padding(8.dp)
                        ) {
                            if (drawable != null) {
                                val bitmapPainter = remember(drawable) {
                                    try {
                                        val bmp = loadDrawableAsBitmap(drawable, 48, 48)
                                        BitmapPainter(bmp)
                                    } catch (_: Exception) {
                                        null
                                    }
                                }

                                if (bitmapPainter != null) {
                                    Image(
                                        painter = bitmapPainter,
                                        contentDescription = shortcut.shortLabel?.toString(),
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(DragonShape)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                }
                            }
                            Text(
                                text = shortcut.shortLabel?.toString() ?: "Unnamed",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                if (showOpenAppRow) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(DragonShape)
                            .clickable { onOpenApp() }
                            .padding(8.dp)
                    ) {

                        Image(
                            painter = appIcon(app),
                            contentDescription = "App icon",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(iconsShape.resolveShape())
                        )
                        Spacer(Modifier.width(8.dp))

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
