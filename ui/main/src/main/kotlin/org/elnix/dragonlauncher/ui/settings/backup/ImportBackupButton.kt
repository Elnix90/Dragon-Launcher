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
import io.github.elnix90.logging.logD
import io.github.elnix90.logging.logE
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.BackupResult
import org.elnix.dragonlauncher.models.BackupViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.dialogs.ImportSettingsDialog
import org.elnix.dragonlauncher.ui.dialogs.MigrationDialog
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


    var importJson by remember { mutableStateOf<JSONObject?>(null) }
    var legacyJsonString by remember { mutableStateOf<String?>(null) }

    val settingsImportLauncher = rememberSettingsImportLauncher(
        onJsonReady = { json ->
            logD(BACKUP_TAG) { "Json ready: $json" }
            if (backupViewModel.isLegacyBackup(json.toString())) {
                legacyJsonString = json.toString()
            } else {
                importJson = json
            }
        }
    )

    legacyJsonString?.let {json ->
        MigrationDialog(
            migrate = { backupViewModel.migrateFromLegacyBackup(json) },
            onDismiss = { legacyJsonString = null },
            canDisagree = true
        )
    }

    importJson?.let { json ->
        var selectedStoresForImport by remember { mutableStateOf(setOf<SettingsStore<*, *>>()) }

        ImportSettingsDialog(
            backupJson = json,
            onDismiss = {
                importJson = null
            },
            onConfirm = { selectedStores ->
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