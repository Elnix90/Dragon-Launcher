package org.elnix.dragonlauncher.ui.settings.debug

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.messyfolder.showToast
import org.elnix.dragonlauncher.common.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.ui.composition.LocalDragonLogViewModel
import org.elnix.dragonlauncher.ui.helpers.MonospaceScrollableText
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import java.io.File

@Composable
fun LogsViewerScreen(
    filename: String,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val dragonLogViewModel = LocalDragonLogViewModel.current

    val file = File(ctx.filesDir, "logs/$filename")

    var logs: String by remember(filename) { mutableStateOf("") }
    LaunchedEffect(Unit) {
        logs = dragonLogViewModel.readLogFile(file)
    }
    val lines by remember(logs) { derivedStateOf { logs.lines() } }

    val helpText = "Viewing logs from the log file: $filename\n - ${lines.size} total lines\n - ${logs.length} total chars"

    SettingsScaffold(
        title = filename,
        onBack = onBack,
        helpText = helpText,
        onReset = null,
        resetText = null,
        scrollableContent = false,
        otherIcons = arrayOf(
            Triple(
                { ctx.copyToClipboard(logs); ctx.showToast("Copied to clipboard") },
                R.drawable.copy,
                stringResource(R.string.copy)
            )
        )
    ) {
        MonospaceScrollableText(lines, useDragonLogsColoration = true)
    }
}