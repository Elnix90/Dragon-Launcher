package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonRow
import org.elnix.dragonlauncher.ui.remembers.rememberAutoBackupLauncher


/**
 * Reselect auto backup banner
 *
 * Ugly banner that shows up when app loose access to the URI for the auto backup, shouldn't appear
 * now since it auto get uri permissions on import
 */
@Composable
fun ReselectAutoBackupBanner() {
    val autoBackupLauncher = rememberAutoBackupLauncher()

    DragonRow(
        onClick = { autoBackupLauncher.launch("dragonlauncher-auto-backup.json") }
    ) {
         Text(
             stringResource(R.string.reselect_auto_backup_file),
             color = MaterialTheme.colorScheme.onPrimary,
             modifier = Modifier.weight(1f)
         )

        DragonIconButton(
            icon = R.drawable.open_in_new,
            contentDescription = stringResource(R.string.open)
        ) { autoBackupLauncher.launch("dragonlauncher-auto-backup.json") }
     }
}
