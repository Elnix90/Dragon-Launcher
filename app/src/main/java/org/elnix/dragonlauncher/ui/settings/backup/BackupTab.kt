package org.elnix.dragonlauncher.ui.settings.backup

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.DataStoreName
import org.elnix.dragonlauncher.data.backupableStores
import org.elnix.dragonlauncher.data.stores.BackupSettingsStore
import org.elnix.dragonlauncher.ui.components.dialogs.UserValidation
import org.elnix.dragonlauncher.ui.helpers.GradientBigButton
import org.elnix.dragonlauncher.ui.helpers.SwitchRow
import org.elnix.dragonlauncher.ui.helpers.TextDivider
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsLazyHeader
import org.elnix.dragonlauncher.utils.SettingsBackupManager
import org.elnix.dragonlauncher.utils.formatDateTime
import org.elnix.dragonlauncher.utils.getFilePathFromUri
import org.elnix.dragonlauncher.utils.models.BackupResult
import org.elnix.dragonlauncher.utils.models.BackupViewModel
import org.elnix.dragonlauncher.utils.showToast
import org.json.JSONObject

@Suppress("AssignedValueIsNeverRead")
@Composable
fun BackupTab(
    backupViewModel: BackupViewModel,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val autoBackupEnabled by BackupSettingsStore.getAutoBackupEnabled(ctx).collectAsState(initial = false)
    val autoBackupUriString by BackupSettingsStore.getAutoBackupUri(ctx).collectAsState(initial = null)
    val lastBackupTime by BackupSettingsStore.getLastBackupTime(ctx).collectAsState(initial = 0L)

    val backupStores by BackupSettingsStore.getBackupStores(ctx).collectAsState(initial = emptySet())

    val result by backupViewModel.result.collectAsState()

    LaunchedEffect(lastBackupTime) {
        ctx.showToast(lastBackupTime)
    }

    val autoBackupUri: Uri? = autoBackupUriString?.takeIf { it.isNotEmpty() }?.toUri()

    val backupPath: String? = autoBackupUri?.let { uri ->
        getFilePathFromUri(ctx, uri)
    }

    var selectedStoresForExport by remember { mutableStateOf(listOf<DataStoreName>()) }
    var selectedStoresForImport by remember { mutableStateOf(listOf<DataStoreName>()) }
    var importJson by remember { mutableStateOf<JSONObject?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    // ------------------------------------------------------------
    // SETTINGS EXPORT LAUNCHER
    // ------------------------------------------------------------
    val settingsExportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            if (uri == null) {
                backupViewModel.setResult(
                    BackupResult(
                        export = true,
                        error = true,
                        title = ctx.getString(R.string.export_cancelled)
                    )
                )
                return@rememberLauncherForActivityResult
            }

            scope.launch {
                try {
                    SettingsBackupManager.exportSettings(ctx, uri, selectedStoresForExport)
                    backupViewModel.setResult(
                        BackupResult(
                            export = true,
                            error = false,
                            title = ctx.getString(R.string.export_successful)
                        ))
                } catch (e: Exception) {
                    backupViewModel.setResult(
                        BackupResult(
                            export = true,
                            error = true,
                            title = ctx.getString(R.string.export_failed),
                            message = e.message ?: ""
                        )
                    )
                }
            }
        }

    // ------------------------------------------------------------
    // SETTINGS IMPORT LAUNCHER (File Picker)
    // ------------------------------------------------------------
    val settingsImportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            Log.d("BackupManager", "File picked: $uri")

            if (uri == null) {
                backupViewModel.setResult(
                    BackupResult(
                        export = false,
                        error = true,
                        title = ctx.getString(R.string.import_cancelled)
                    )
                )
                return@rememberLauncherForActivityResult
            }

            ctx.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            // Read JSON from selected file
            scope.launch {
                try {
                    val jsonString = withContext(Dispatchers.IO) {
                        ctx.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    }

                    if (jsonString.isNullOrBlank()) {
                        backupViewModel.setResult(
                            BackupResult(
                                export = false,
                                error = true,
                                title = ctx.getString(R.string.import_failed),
                                message = "Invalid or empty backup file"
                            )
                        )
                        return@launch
                    }

                    importJson = JSONObject(jsonString)
                    showImportDialog = true

                } catch (e: Exception) {
                    backupViewModel.setResult(
                        BackupResult(
                            export = false,
                            error = true,
                            title = ctx.getString(R.string.import_failed),
                            message = "Failed to read backup file: ${e.message}"
                        )
                    )
                }
            }
        }

    val autoBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            try {
                ctx.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                // Proceed only if successful
                scope.launch {
                    BackupSettingsStore.setAutoBackupUri(ctx, uri)
                    BackupSettingsStore.setAutoBackupEnabled(ctx, true)
                }
                backupViewModel.setResult(BackupResult(export = true, error = false, title = "Auto-backup enabled"))
            } catch (e: SecurityException) {
                // Fallback: Store non-persistable URI or notify user
                backupViewModel.setResult(BackupResult(export = true, error = true, title = "Backup saved (limited persistence)"))
                Log.e("Backup", "Persistable permission not available for URI: $uri", e)
            }
        }
    }


    // ------------------------------------------------------------
    // UI
    // ------------------------------------------------------------
    SettingsLazyHeader(
        title = stringResource(R.string.backup_restore),
        onBack = onBack,
        helpText = stringResource(R.string.backup_restore_text),
        resetText = null,
        onReset = null
    ) {
        item {
            BackupButtons(
                onExport = { showExportDialog = true },
                onImport = {
                    settingsImportLauncher.launch(
                        arrayOf(
                            "application/json",
                            "text/plain",
                            "application/octet-stream",
                            "*/*"
                        )
                    )
                }
            )
        }

        item { TextDivider(stringResource(R.string.automatic_backups)) }

        item {
            SwitchRow(
                state = autoBackupEnabled,
                text = stringResource(R.string.automatic_backups)
            ) { enabled ->
                scope.launch {
                    BackupSettingsStore.setAutoBackupEnabled(ctx, enabled)
                }
            }
        }

        if (autoBackupEnabled) {
            if (backupPath != null) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Backup Path: ",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = backupPath,
                            color = MaterialTheme.colorScheme.primary,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier
                                .clickable {
                                    autoBackupUri.let { uri ->
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, "application/json")
                                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        }
                                        ctx.startActivity(
                                            Intent.createChooser(
                                                intent,
                                                "Open backup file"
                                            )
                                        )
                                    }
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            item {
                if (backupPath != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(0.5f))
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable {
                                    autoBackupLauncher.launch("dragonlauncher-auto-backup.json")
                                },

                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.change),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary)
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable {
                                    scope.launch {
                                        BackupSettingsStore.setAutoBackupUri(ctx, null)
                                    }
                                },

                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.remove),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary)
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable {
                                    scope.launch {
                                        SettingsBackupManager.triggerBackup(ctx)
                                    }
                                },

                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.trigger_backup),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(0.5f))
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable {
                                    autoBackupLauncher.launch("dragonlauncher-auto-backup.json")
                                },

                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.select_backup_file),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }


            if (backupPath != null) {
                item {
                    Text(
                        text = "${stringResource(R.string.last_backup)} : ${lastBackupTime.formatDateTime()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }



            item { TextDivider(stringResource(R.string.auto_backup_stores)) }

            item {
                Column{
                    backupableStores.forEach { store ->
                        val isSelected = backupStores.contains(store.value)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    scope.launch {
                                        val updated = if (isSelected) {
                                            backupStores - store.value
                                        } else {
                                            backupStores + store.value
                                        }
                                        BackupSettingsStore.setBackupStores(
                                            ctx,
                                            backupableStores.filter { updated.contains(it.value) })
                                    }
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = store.store.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null
                            )
                        }
                    }
                }
            }
        }
    }

    // Export Dialog
    if (showExportDialog) {
        ExportSettingsDialog(
            onDismiss = { showExportDialog = false },
            onConfirm = { selectedStores ->
                showExportDialog = false
                selectedStoresForExport = selectedStores
                settingsExportLauncher.launch("backup-${System.currentTimeMillis()}.json")
            }
        )
    }

    // Import Dialog (shows after file is picked)
    importJson?.let { json ->
        if (showImportDialog) {
            ImportSettingsDialog(
                backupJson = json,
                onDismiss = {
                    showImportDialog = false
                    importJson = null
                },
                onConfirm = { selectedStores ->
                    showImportDialog = false
                    selectedStoresForImport = selectedStores

                    scope.launch {
                        try {
                            SettingsBackupManager.importSettingsFromJson(ctx, json , selectedStoresForImport)
                            backupViewModel.setResult(
                                BackupResult(
                                    export = false,
                                    error = false,
                                    title = ctx.getString(R.string.import_successful)
                                )
                            )
                            importJson = null
                        } catch (e: Exception) {
                            backupViewModel.setResult(
                                BackupResult(
                                    export = false,
                                    error = true,
                                    title = ctx.getString(R.string.import_failed),
                                    message = e.message ?: ""
                                )
                            )
                        }
                    }
                }
            )
        }
    }

    // ------------------------------------------------------------
    // RESULT DIALOG
    // ------------------------------------------------------------
    result?.let { res ->
        val isError = res.error
        UserValidation(
            title = res.title,
            message = res.message,
            titleIcon = if (isError) Icons.Default.Warning else Icons.Default.Check,
            titleColor = if (isError) MaterialTheme.colorScheme.error else Color.Green,
            cancelText = null,
            copy = isError,
            onCancel = {},
            onAgree = { backupViewModel.setResult(null) }
        )
    }
}


// ------------------------------------------------------------
// Shared Buttons (internal)
// ------------------------------------------------------------

@Composable
fun BackupButtons(
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        GradientBigButton(
            text = stringResource(R.string.export_settings),
            onClick = onExport,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Upload,
                    contentDescription = stringResource(R.string.export_settings),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier
        )

        GradientBigButton(
            text = stringResource(R.string.import_settings),
            onClick = onImport,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = stringResource(R.string.import_settings),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier
        )
    }
}
