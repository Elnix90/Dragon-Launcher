package org.elnix.dragonlauncher.ui.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.settings.stores.BackupSettingsStore
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.asStateNull
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsSwitchRow
import org.elnix.dragonlauncher.ui.helpers.GradientBigButton
import org.elnix.dragonlauncher.ui.remembers.rememberAutoBackupLauncher

@Composable
fun WelcomePageBackup() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()


    val autoBackupEnabled by BackupSettingsStore.autoBackupEnabled.asState()
    val autoBackupUriString by BackupSettingsStore.autoBackupUri.asStateNull()
    val autoBackupUri = autoBackupUriString?.toUri()

    val autoBackupLauncher = rememberAutoBackupLauncher()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.cloud_upload),
                contentDescription = stringResource(R.string.enable_backup),
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = stringResource(R.string.enable_backup),
                fontSize = 26.sp
            )
        }


        Spacer(Modifier.height(32.dp))


        SettingsSwitchRow(
            setting = BackupSettingsStore.autoBackupEnabled,
            title = stringResource(R.string.automatic_backups),
            description = stringResource(R.string.auto_backup_desc)
        ) {
            // If the user disabled the backup, also remove the uri
            if (!it) {
                scope.launch {
                    BackupSettingsStore.autoBackupUri.reset(ctx)
                }
            }
        }


        Spacer(Modifier.height(5.dp))

        GradientBigButton(
            text = if (autoBackupUri != null) {
                stringResource(R.string.choose_a_auto_backup_file)
            } else {
                stringResource(R.string.open_default_launcher_settings)
            },
            enabled = autoBackupEnabled,
            onClick = {
                autoBackupLauncher.launch("dragonlauncher-auto-backup.json")
            }
        )
    }
}
