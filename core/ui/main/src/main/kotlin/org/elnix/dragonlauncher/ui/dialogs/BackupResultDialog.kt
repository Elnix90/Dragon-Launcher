package org.elnix.dragonlauncher.ui.dialogs


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.ui.composition.LocalAppsViewModel
import org.elnix.dragonlauncher.ui.composition.LocalBackupViewModel
import org.elnix.dragonlauncher.ui.dragon.dialogs.UserValidation

@Composable
fun BackupResultDialog() {
    val appsViewModel = LocalAppsViewModel.current
    val backupViewModel = LocalBackupViewModel.current
    val scope = rememberCoroutineScope()
    val result by backupViewModel.result.collectAsState()

    /* ───────────── RESULT DIALOG ( IMPORT / EXPORT ) ───────────── */
    result?.let { res ->
        val isError = res.error
        val isExport = res.export
        val errorMessage = res.message

        // Reload the whole viewModel data after restore
        LaunchedEffect(res) {
            scope.launch(Dispatchers.IO) {
                appsViewModel.loadAll()
            }
        }

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
                else -> null
            },
            titleIcon = if (isError) Icons.Default.Warning else Icons.Default.Check,
            titleColor = if (isError) MaterialTheme.colorScheme.error else Color.Green,
            copy = isError,
            onValidate = { backupViewModel.setResult(null) }
        )
    }
}