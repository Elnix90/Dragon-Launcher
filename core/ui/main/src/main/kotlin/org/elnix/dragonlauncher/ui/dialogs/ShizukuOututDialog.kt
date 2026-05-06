package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.ui.composition.LocalShizukuViewModel
import org.elnix.dragonlauncher.ui.dragon.dialogs.CustomAlertDialog
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription

@Composable
fun ShizukuOutputDialog() {

    val shizukuViewModel = LocalShizukuViewModel.current
    val output by shizukuViewModel.outputValue.collectAsState()

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
