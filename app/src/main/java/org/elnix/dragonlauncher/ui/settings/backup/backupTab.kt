package org.elnix.dragonlauncher.ui.settings.backup

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.ui.helpers.UserValidation
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsLazyHeader
import org.elnix.dragonlauncher.utils.SettingsBackupManager
import org.elnix.dragonlauncher.utils.colors.AppObjectsColors


@Composable
fun BackupTab(
    backupVm: BackupViewModel,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val result by backupVm.result.collectAsState()


    // ------------------------------------------------------------
    // SETTINGS BACKUP
    // ------------------------------------------------------------

    val settingsExportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            Log.d("BackupManager", "Started settings export 2")

            if (uri == null) {
                backupVm.setResult(
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
                    SettingsBackupManager.exportSettings(ctx, uri)
                    backupVm.setResult(BackupResult(export = true, error = false))

                } catch (e: Exception) {
                    backupVm.setResult(
                        BackupResult(
                            export = true,
                            error = true,
                            message = e.message ?: ""
                        )
                    )
                }
            }
        }

    val settingsImportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            Log.d("BackupManager", "Started settings import 2")

            if (uri == null) {
                backupVm.setResult(
                    BackupResult(
                        export = false,
                        error = true,
                        message = ctx.getString(R.string.import_cancelled)
                    )
                )
                return@rememberLauncherForActivityResult
            }

            scope.launch {
                try {
                    SettingsBackupManager.importSettings(ctx, uri)
                    backupVm.setResult(BackupResult(export = false, error = false))

                } catch (e: Exception) {
                    backupVm.setResult(
                        BackupResult(
                            export = false,
                            error = true,
                            message = e.message ?: ""
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
                exportLabel = stringResource(R.string.export_settings),
                importLabel = stringResource(R.string.import_settings),
                onExport = { settingsExportLauncher.launch("notes_settings_backup.json") },
                onImport = { settingsImportLauncher.launch(arrayOf("application/json")) }
            )
        }
    }

    // ------------------------------------------------------------
    // RESULT DIALOG
    // ------------------------------------------------------------

    if (result != null) {
        val isError = result!!.error
        val isExport = result!!.export
        val errorMessage = result!!.message

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
            onAgree = { backupVm.setResult(null) }
        )
    }
}


// ------------------------------------------------------------
// Shared Buttons (internal)
// ------------------------------------------------------------

@Composable
fun BackupButtons(
    exportLabel: String,
    importLabel: String,
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Button(
            onClick = onExport,
            colors = AppObjectsColors.buttonColors()
        ) { Text(exportLabel) }

        Button(
            onClick = onImport,
            colors = AppObjectsColors.buttonColors()
        ) { Text(importLabel) }
    }
}

