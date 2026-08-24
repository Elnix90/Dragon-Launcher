package org.elnix.dragonlauncher.ui.settings.backup

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import io.github.elnix90.core.stores.SettingsStore
import io.github.elnix90.logging.logE
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.BACKUP_TAG
import org.elnix.dragonlauncher.base.utils.DateUtils
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.getFilePathFromUri
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.models.BackupViewModel
import org.elnix.dragonlauncher.settings.backupableStores
import org.elnix.dragonlauncher.settings.stores.map.BackupSettingsStore
import org.elnix.dragonlauncher.settings.toSettingsStoreList
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.compositionslocals.LocalNavigator
import org.elnix.dragonlauncher.ui.dialogs.importexport.ExportDialog
import org.elnix.dragonlauncher.ui.dialogs.importexport.SelectedActionRow
import org.elnix.dragonlauncher.ui.dialogs.importexport.StoreItemsNotScrollable
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.settings.Setting
import org.elnix.dragonlauncher.ui.helpers.GradientBigButton
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.remembers.rememberAutoBackupLauncher
import org.elnix.dragonlauncher.ui.remembers.rememberSettingsExportLauncher
import kotlin.time.Duration.Companion.seconds

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun BackupTab(backupViewModel: BackupViewModel = activityViewModel()) {
    val ctx = LocalContext.current
    val navigator = LocalNavigator.current
    val scope = rememberCoroutineScope()

    val autoBackupEnabled by BackupSettingsStore.autoBackupEnabled.asState()
    val autoBackupUriString by BackupSettingsStore.autoBackupUri.asState()
    val backupStores by BackupSettingsStore.backupStores.asState()


    val snapshotStateMapStores = remember(backupStores) {
        val settingStoreList = backupStores.toSettingsStoreList()
        mutableStateMapOf<SettingsStore<*, *>, Boolean>().apply {
            backupableStores.forEach { put(it, backupStores.isEmpty() || it in settingStoreList) }
        }
    }

    val autoBackupUri: Uri? = autoBackupUriString.takeIf { it.isNotEmpty() }?.toUri()

    val backupPath: String? = autoBackupUri?.let { uri ->
        ctx.getFilePathFromUri(uri)
    }

    var selectedStoresForExport by remember { mutableStateOf(setOf<SettingsStore<*, *>>()) }
    var showExportDialog by remember { mutableStateOf(false) }

    val settingsExportLauncher = rememberSettingsExportLauncher(selectedStoresForExport)
    val autoBackupLauncher = rememberAutoBackupLauncher()



    SettingsScaffold(
        title = ctx.getString(R.string.backup),
        onBack = {
            scope.launch {
                if (snapshotStateMapStores.count { it.value } == backupableStores.size) {
                    BackupSettingsStore.backupStores.reset(ctx)
                } else {
                    val final = snapshotStateMapStores
                        .filter { it.value }
                        .keys
                        .mapTo(mutableSetOf()) { it.name }

                    BackupSettingsStore.backupStores.set(ctx, final)
                }
                navigator.onBack()
            }
        },
        helpText = ctx.getString(R.string.backup_restore_text),
        resetText = stringResource(R.string.reset_backup_tab),
        onReset = {
            scope.launch {
                BackupSettingsStore.resetAll(ctx)
            }
        }
    ) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            GradientBigButton(
                text = stringResource(R.string.export_settings),
                onClick = { showExportDialog = true },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.cloud_upload),
                        contentDescription = stringResource(R.string.export_settings),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )

            ImportBackupButton {
                GradientBigButton(
                    text = stringResource(R.string.import_settings),
                    onClick = it,
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.download),
                            contentDescription = stringResource(R.string.import_settings),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
        }

        DragonSettingsGroup(R.string.automatic_backups) {
            Setting(BackupSettingsStore.autoBackupEnabled) {
                // If the user disabled the backup, also remove the uri
                if (!it) {
                    scope.launch {
                        BackupSettingsStore.autoBackupUri.reset(ctx)
                    }
                }
            }

            SettingsItem(
                title = stringResource(R.string.backup_location),
                description = backupPath ?: stringResource(R.string.backup_location_desc),
                icon = R.drawable.folder_open,
                enabled = autoBackupEnabled,
                onClick = { autoBackupLauncher.launch("dragonlauncher-auto-backup.json") }
            )

            SettingsItem(
                title = stringResource(R.string.open_backup_file),
                icon = R.drawable.open_in_new,
                enabled = autoBackupEnabled && backupPath != null,
                onClick = {
                    autoBackupUri.let { uri ->
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/json")
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                        try {
                            ctx.startActivity(
                                Intent.createChooser(
                                    intent,
                                    "Open backup file"
                                )
                            )
                        } catch (e: Exception) {
                            ctx.showToast("Failed to open backup (probably no app available)")
                            logE(BACKUP_TAG, e) { "Failed to open backup (probably no app available)" }
                        }
                    }
                }
            )

            var hasTriggeredManualAutoBackup by remember { mutableStateOf(false) }
            LaunchedEffect(hasTriggeredManualAutoBackup) {
                if (hasTriggeredManualAutoBackup) {
                    delay(10.seconds)
                    hasTriggeredManualAutoBackup = false
                }
            }

            SettingsItem(
                title = stringResource(R.string.trigger_backup),
                icon = R.drawable.reset,
                enabled = autoBackupEnabled && backupPath != null && !hasTriggeredManualAutoBackup,
                onClick = {
                    backupViewModel.commandBackup()
                    ctx.showToast(ctx.getString(R.string.backup_triggered))
                    hasTriggeredManualAutoBackup = true
                }
            )
        }

        AnimatedVisibility(autoBackupEnabled) {
            DragonSettingsGroup(R.string.auto_backup_stores) {
                SelectedActionRow(snapshotStateMapStores)
                StoreItemsNotScrollable(snapshotStateMapStores)
            }
        }
    }

    if (showExportDialog) {
        ExportDialog(
            onDismiss = { showExportDialog = false },
            onConfirm = { selectedStores ->
                showExportDialog = false
                selectedStoresForExport = selectedStores
                settingsExportLauncher.launch("backup-${DateUtils.nowFormattedDateTime()}.json")
            }
        )
    }
}

