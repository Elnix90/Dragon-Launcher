package org.elnix.dragonlauncher.ui.remembers

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.models.BackupResult
import org.elnix.dragonlauncher.models.BackupViewModel
import org.elnix.dragonlauncher.settings.SettingsBackupManager
import org.elnix.dragonlauncher.settings.bases.DatastoreProvider
import org.elnix.dragonlauncher.ui.activityViewModel

@Composable
fun rememberSettingsExportLauncher(
    selectedStoresForExport: Set<DatastoreProvider>,
    backupViewModel: BackupViewModel = activityViewModel()
): ManagedActivityResultLauncher<String, Uri?> {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val exportCancelledText = stringResource(R.string.export_cancelled)
    val exportSuccessfulText = stringResource(R.string.export_successful)
    val exportFailedText = stringResource(R.string.export_failed)

    val settingsExportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            if (uri == null) {
                backupViewModel.setResult(
                    BackupResult(
                        export = true,
                        error = true,
                        title = exportCancelledText
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
                            title = exportSuccessfulText
                        )
                    )
                } catch (e: Exception) {
                    backupViewModel.setResult(
                        BackupResult(
                            export = true,
                            error = true,
                            title = exportFailedText,
                            message = e.message ?: ""
                        )
                    )
                }
            }
        }
    return settingsExportLauncher
}

@Composable
fun rememberSafeSettingsExportLauncher(
    selectedStoresForExport: Set<DatastoreProvider>
): ManagedActivityResultLauncher<String, Uri?> {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val exportCancelledText = stringResource(R.string.export_cancelled)
    val exportSuccessfulText = stringResource(R.string.export_successful)
    val exportFailedText = stringResource(R.string.export_failed)


    val settingsExportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            if (uri == null) {
                ctx.showToast(exportCancelledText)
                return@rememberLauncherForActivityResult
            }

            scope.launch {
                try {
                    SettingsBackupManager.exportSettings(ctx, uri, selectedStoresForExport)
                    ctx.showToast(exportSuccessfulText)
                } catch (e: Exception) {
                    ctx.showToast("$exportFailedText: $e")
                }
            }
        }
    return settingsExportLauncher
}