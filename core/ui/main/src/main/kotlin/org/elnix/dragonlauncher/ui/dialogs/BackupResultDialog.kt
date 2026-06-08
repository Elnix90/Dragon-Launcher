package org.elnix.dragonlauncher.ui.dialogs


import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.BackupViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.dragon.dialogs.UserValidation

@Composable
fun BackupResultDialog(
    backupViewModel: BackupViewModel = activityViewModel(),
) {
    val result by backupViewModel.result.asState()

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
                else -> null
            },
            titleIcon = if (isError) R.drawable.warning else R.drawable.check,
            titleColor = if (isError) MaterialTheme.colorScheme.error else Color.Green,
            copy = isError,
            onValidate = { backupViewModel.result.set(null) }
        )
    }
}