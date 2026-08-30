package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.base.modifiers.shapedClickable
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.remembers.rememberAutoBackupLauncher

/**
 * Reselect auto backup banner
 *
 * Ugly banner that shows up when app loose access to the URI for the auto backup, shouldn't appear
 * now since it auto get uri permissions on import
 */
@Composable
fun ReselectAutoBackupBanner(onHide: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val autoBackupLauncher = rememberAutoBackupLauncher()

    Row(
        modifier =
            Modifier
                .shapedClickable { autoBackupLauncher.launch("dragonlauncher-auto-backup.json") }
                .background(MaterialTheme.colorScheme.surface)
                .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.reselect_auto_backup_file),
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.weight(1f)
        )

        Icon(
            painter = painterResource(R.drawable.open_in_new),
            contentDescription = stringResource(R.string.open)
        )

        DragonIconButton(
            icon = R.drawable.close,
            contentDescription = R.string.close,
            onClick = onHide
        )
    }
}
