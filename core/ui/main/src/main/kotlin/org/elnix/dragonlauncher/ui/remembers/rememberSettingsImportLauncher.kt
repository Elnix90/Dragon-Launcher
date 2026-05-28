package org.elnix.dragonlauncher.ui.remembers

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.BACKUP_TAG
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.models.BackupResult
import org.elnix.dragonlauncher.models.BackupViewModel
import org.elnix.dragonlauncher.ui.activityViewModel
import org.json.JSONObject

@Composable
fun rememberSettingsImportLauncher(
    backupViewModel: BackupViewModel = activityViewModel(),
    onJsonReady: (JSONObject) -> Unit
): ManagedActivityResultLauncher<Array<String>, Uri?> {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val importCancelledText = stringResource(R.string.import_cancelled)
    val importFailedText = stringResource(R.string.import_failed)


    fun onError(msg: String) {
        backupViewModel.setResult(
            BackupResult(
                export = false,
                error = true,
                title = importFailedText,
                message = msg
            )
        )
    }

    return rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->

        logD(BACKUP_TAG) { "File picked: $uri" }

        if (uri == null) {
            backupViewModel.setResult(
                BackupResult(
                    export = false,
                    error = true,
                    title = importCancelledText
                )
            )

            return@rememberLauncherForActivityResult
        }

        ctx.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )

        scope.launch {
            try {
                val jsonString = withContext(Dispatchers.IO) {
                    ctx.contentResolver
                        .openInputStream(uri)
                        ?.bufferedReader()
                        ?.use { it.readText() }
                }

                if (jsonString.isNullOrBlank()) {
                    onError("Invalid or empty backup file")
                    return@launch
                }

                onJsonReady(JSONObject(jsonString))

            } catch (e: Exception) {
                onError("Failed to read backup file: $e")
            }
        }
    }
}
