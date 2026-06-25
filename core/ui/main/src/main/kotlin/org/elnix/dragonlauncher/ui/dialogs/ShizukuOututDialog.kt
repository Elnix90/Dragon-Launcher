package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.ShizukuViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.dragon.dialogs.CustomAlertDialog
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription

@Composable
fun ShizukuOutputDialog(
    shizukuViewModel: ShizukuViewModel = activityViewModel()
) {
    val output by shizukuViewModel.outputValue.asState()

    output?.let { output ->
        CustomAlertDialog(
            scroll = false,
            onDismissRequest = {
                shizukuViewModel.clearOutput()
            },
            title = { Text(stringResource(R.string.command_output)) },
            text = {
                Column {
                    val errorText = if (output.isError) "Error occurred" else null

                    TextWithDescription(
                        text = output.text,
                        description = errorText
                    )
                }
            }
        )
    }
}
