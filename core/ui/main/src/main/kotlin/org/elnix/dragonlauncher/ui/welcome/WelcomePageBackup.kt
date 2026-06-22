package org.elnix.dragonlauncher.ui.welcome

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.stores.map.BackupSettingsStore
import io.github.elnix90.runtime.asState
import io.github.elnix90.runtime.asStateNull
import org.elnix.dragonlauncher.ui.base.components.Spacer
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

    WelcomePagerHeader(
        title = stringResource(R.string.enable_backup),
        icon = R.drawable.cloud_upload
    ) {
        SettingsSwitchRow(BackupSettingsStore.autoBackupEnabled) {
            // If the user disabled the backup, also remove the uri
            if (!it) {
                scope.launch {
                    BackupSettingsStore.autoBackupUri.reset(ctx)
                }
            }
        }

        Spacer(5.dp)

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
