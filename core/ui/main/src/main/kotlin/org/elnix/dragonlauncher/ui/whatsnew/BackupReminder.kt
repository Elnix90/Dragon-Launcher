package org.elnix.dragonlauncher.ui.whatsnew

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.common.utils.rememberVersionCode
import org.elnix.dragonlauncher.settings.stores.PrivateSettingsStore
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton

// I hate the behavior of this shitty modal sheet that force showing the system bars, even in fullscreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupReminder(onGoToSettings: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val lastSeenVersionCodeWhatsNew by PrivateSettingsStore.lastSeenVersionCodeDoABackup.asState()
    val versionCode = rememberVersionCode()

    if (lastSeenVersionCodeWhatsNew >= versionCode) return

    val msg =
        "I'm currently working on the next version, that'll be HUGE! Please create a backup of your settings, you may need to use it in order to not loose all your settings. And, as a reminder, you should enable auto backups, AND keep a separate backup"


    AlertDialog(
        onDismissRequest = {

        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        title = {
            Text(
                text = "DO A BACKUP",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Column {
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                DragonIconButton(
                    icon = R.drawable.copy,
                    contentDescription = R.string.copy
                ) {
                    ctx.copyToClipboard(msg)
                }
            }
        },
        confirmButton = {
            DragonButton(
                onClick = {
                    scope.launch {
                        PrivateSettingsStore.lastSeenVersionCodeDoABackup.set(ctx, versionCode)
                        onGoToSettings()
                    }
                }
            ) {
                Text("Go to backup settings")
            }
        }
    )
}
