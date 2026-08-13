package org.elnix.dragonlauncher.ui.settings.debug

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import org.elnix.dragonlauncher.base.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.models.DragonLogViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.AnimatedFab
import org.elnix.dragonlauncher.ui.helpers.MonospaceScrollableText
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import java.io.File

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun LogsViewerScreen(
    filename: String,
    dragonLogViewModel: DragonLogViewModel = activityViewModel()
) {
    val ctx = LocalContext.current

    val file = File(ctx.filesDir, "logs/$filename")
    var logs: String by remember(filename) { mutableStateOf("") }
    LaunchedEffect(Unit) {
        logs = dragonLogViewModel.readLogFile(file)
    }
    val lines by remember(logs) { derivedStateOf { logs.lines() } }

    SettingsScaffold(
        title = filename,
        helpText = "Viewing logs from the log file: $filename\n - ${lines.size} total lines\n - ${logs.length} total chars",
        onReset = null,
        resetText = null,
        scrollableContent = false,
        specialSettingsTitleContent = {
            AnimatedFab(
                onClick = {
                    ctx.copyToClipboard(logs)
                    ctx.showToast(ctx.getString(R.string.copied_to_clipboard))
                },
                icon = R.drawable.copy
            )
        },
    ) {
        MonospaceScrollableText(lines, useDragonLogsColoration = true)
    }
}