package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.settings.backupableStores
import org.elnix.dragonlauncher.ui.helpers.MonospaceScrollableText
import org.elnix.dragonlauncher.ui.remembers.rememberSafeSettingsExportLauncher


@Composable
fun CrashScreen(
    stackTrace: String,
    onDismiss: () -> Unit
) {
    val ctx = LocalContext.current
    val lines = remember(stackTrace) { stackTrace.lines() }
    val settingsExportLauncher = rememberSafeSettingsExportLauncher(backupableStores.keys)

    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.safeContent)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color.Red
                )

                Text(
                    text = stringResource(R.string.crash),
                    color = Color.Red,
                    fontSize = 30.sp
                )
            }

            IconButton(
                onClick = { ctx.copyToClipboard(stackTrace) },
            ) {
                Icon(
                    painter = painterResource(R.drawable.copy),
                    contentDescription = stringResource(R.string.copy),
                    tint = Color.White
                )
            }
        }

        CompositionLocalProvider(
            LocalContentColor provides Color.White
        ) {
            Box(Modifier.weight(1f)) {
                MonospaceScrollableText(lines)
            }


            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    settingsExportLauncher.launch("panic_backup.json")
                }
            ) {
                Text(stringResource(R.string.export_settings))
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.access_app))
            }
        }
    }
}