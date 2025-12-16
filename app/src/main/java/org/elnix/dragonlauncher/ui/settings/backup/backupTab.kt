package org.elnix.dragonlauncher.ui.settings.backup

import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.DataStoreName
import org.elnix.dragonlauncher.ui.helpers.GradientBigButton
import org.elnix.dragonlauncher.ui.helpers.UserValidation
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsLazyHeader
import org.elnix.dragonlauncher.utils.SettingsBackupManager
import org.elnix.dragonlauncher.utils.models.BackupResult
import org.elnix.dragonlauncher.utils.models.BackupViewModel
import org.json.JSONObject

@Suppress("AssignedValueIsNeverRead")
@Composable
fun BackupTab(
    backupViewModel: BackupViewModel,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val result by backupViewModel.result.collectAsState()

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
                        message = ctx.getString(R.string.export_cancelled)
                    )
                )
                return@rememberLauncherForActivityResult
            }

            scope.launch {
                try {
                    SettingsBackupManager.exportSettings(ctx, uri, selectedStoresForExport)
                    backupViewModel.setResult(BackupResult(export = true, error = false))
                } catch (e: Exception) {
                    backupViewModel.setResult(
                        BackupResult(
                            export = true,
                            error = true,
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
                        message = ctx.getString(R.string.import_cancelled)
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
                            message = "Failed to read backup file: ${e.message}"
                        )
                    )
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
                onImport = { settingsImportLauncher.launch(arrayOf("application/json")) }
            )
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
                            backupViewModel.setResult(BackupResult(export = false, error = false))
                            importJson = null
                        } catch (e: Exception) {
                            backupViewModel.setResult(
                                BackupResult(
                                    export = false,
                                    error = true,
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
        val isExport = res.export
        val errorMessage = res.message

        UserValidation(
            title = when {
                isError && isExport -> stringResource(R.string.export_failed)
                isError && !isExport -> stringResource(R.string.import_failed)
                !isError && isExport -> stringResource(R.string.export_successful)
                else -> stringResource(R.string.import_successful)
            },
            message = when {
                isError -> errorMessage.ifBlank { stringResource(R.string.unknown_error) }
                isExport -> stringResource(R.string.export_successful)
                else -> stringResource(R.string.import_successful)
            },
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
            modifier = Modifier
        )

        GradientBigButton(
            text = stringResource(R.string.import_settings),
            onClick = onImport,
            modifier = Modifier
        )
    }
}
