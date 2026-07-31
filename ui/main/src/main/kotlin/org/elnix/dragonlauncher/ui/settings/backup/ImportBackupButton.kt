package org.elnix.dragonlauncher.ui.settings.backup

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import io.github.elnix90.core.SettingsBackupManager
import io.github.elnix90.core.stores.SettingsStore
import io.github.elnix90.logging.BACKUP_TAG
import io.github.elnix90.logging.logE
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.BackupResult
import org.elnix.dragonlauncher.models.BackupViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.dialogs.ImportSettingsDialog
import org.elnix.dragonlauncher.ui.dialogs.LegacyMigrationDialog
import org.elnix.dragonlauncher.ui.remembers.rememberSettingsImportLauncher
import org.json.JSONObject

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun ImportBackupButton(
    onConfirm: (suspend () -> Unit)? = null,
    backupViewModel: BackupViewModel = activityViewModel(),
    content: @Composable (onImport: () -> Unit) -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedStoresForImport by remember { mutableStateOf(setOf<SettingsStore<*, *>>()) }
    var importJson by remember { mutableStateOf<JSONObject?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showLegacyMigrationDialog by remember { mutableStateOf(false) }
    var legacyJsonString by remember { mutableStateOf<String?>(null) }

    val settingsImportLauncher = rememberSettingsImportLauncher(
        onJsonReady = { json ->
            if (backupViewModel.isLegacyBackup(json.toString())) {
                legacyJsonString = json.toString()
                showLegacyMigrationDialog = true
            } else {
                importJson = json
                showImportDialog = true
            }
        }
    )

    if (showLegacyMigrationDialog && legacyJsonString != null) {
        LegacyMigrationDialog(
            legacyJsonString = legacyJsonString!!,
            onDismiss = {
                showLegacyMigrationDialog = false
                legacyJsonString = null
            }
        )
    }

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
                            SettingsBackupManager.importSettingsFromJson(
                                ctx,
                                json,
                                selectedStoresForImport
                            )
                            backupViewModel.result.value = BackupResult(
                                export = false,
                                error = false,
                                title = ctx.getString(R.string.import_successful)
                            )

                            importJson = null
                        } catch (e: Exception) {
                            logE(BACKUP_TAG, e) { "Import failed" }
                            backupViewModel.result.value = BackupResult(
                                export = false,
                                error = true,
                                title = ctx.getString(R.string.import_failed),
                                message = e.message ?: ""
                            )
                        }
                        onConfirm?.invoke()
                    }

                }
            )
        }
    }

    content {
        settingsImportLauncher.launch(
            arrayOf(
                "application/json",
                "text/plain",
                "application/octet-stream",
                "*/*"
            )
        )
    }
}