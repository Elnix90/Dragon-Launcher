package org.elnix.dragonlauncher.ui.remembers

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import io.github.elnix90.logging.BACKUP_TAG
import io.github.elnix90.logging.logE
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.models.BackupResult
import org.elnix.dragonlauncher.models.BackupViewModel
import org.elnix.dragonlauncher.settings.stores.map.BackupSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel

@Composable
fun rememberAutoBackupLauncher(
    backupViewModel: BackupViewModel = activityViewModel(),
): ManagedActivityResultLauncher<String, Uri?> {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    return rememberLauncherForActivityResult(
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
                    BackupSettingsStore.autoBackupUri.set(ctx, uri.toString())
                    BackupSettingsStore.autoBackupEnabled.set(ctx, true)
                }
                backupViewModel.result.value = BackupResult(
                    export = true,
                    error = false,
                    title = "Auto-backup enabled"
                )
            } catch (e: SecurityException) {
                // Fallback: Store non-persistable URI or notify user
                backupViewModel.result.value = BackupResult(
                    export = true,
                    error = true,
                    title = "Backup saved (limited persistence)"
                )

                logE(BACKUP_TAG, e) { "Persistable permission not available for URI: $uri" }
            }
        }
    }
}