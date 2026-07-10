package org.elnix.dragonlauncher.ui.settings.backup

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import io.github.elnix90.core.SettingsBackupManager
import io.github.elnix90.core.stores.SettingsStore
import io.github.elnix90.logging.BACKUP_TAG
import io.github.elnix90.logging.logE
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.utils.DateUtils
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.getFilePathFromUri
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.models.BackupResult
import org.elnix.dragonlauncher.models.BackupViewModel
import org.elnix.dragonlauncher.settings.AllStores
import org.elnix.dragonlauncher.settings.backupableStores
import org.elnix.dragonlauncher.settings.stores.map.BackupSettingsStore
import org.elnix.dragonlauncher.settings.toSettingsStoreList
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.modifiers.shapedClickable
import org.elnix.dragonlauncher.ui.dialogs.ExportSettingsDialog
import org.elnix.dragonlauncher.ui.dialogs.ImportSettingsDialog
import org.elnix.dragonlauncher.ui.dialogs.SelectedActionRow
import org.elnix.dragonlauncher.ui.dragon.components.DragonColumn
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.settings.Setting
import org.elnix.dragonlauncher.ui.helpers.GradientBigButton
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.remembers.rememberAutoBackupLauncher
import org.elnix.dragonlauncher.ui.remembers.rememberSettingsExportLauncher
import org.elnix.dragonlauncher.ui.remembers.rememberSettingsImportLauncher
import org.json.JSONObject
import kotlin.time.Duration.Companion.seconds

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
public fun BackupTab(
    onBack: () -> Unit,
    backupViewModel: BackupViewModel = activityViewModel()
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val autoBackupEnabled by BackupSettingsStore.autoBackupEnabled.asState()
    val autoBackupUriString by BackupSettingsStore.autoBackupUri.asState()
    val backupStores by BackupSettingsStore.backupStores.asState()


    val selectedStores = remember(backupStores) {
        mutableStateMapOf<SettingsStore<*, *>, Boolean>().apply {
            AllStores.forEach { put(it, backupStores.isEmpty() || it in backupStores.toSettingsStoreList()) }
        }
    }

    fun save() {
        scope.launch {
            if (selectedStores.size == backupableStores.size) {
                BackupSettingsStore.backupStores.reset(ctx)
            } else {
                BackupSettingsStore.backupStores.set(ctx, selectedStores.keys.mapTo(mutableSetOf()) { it.name })
            }
        }
    }

    val autoBackupUri: Uri? = autoBackupUriString.takeIf { it.isNotEmpty() }?.toUri()

    val backupPath: String? = autoBackupUri?.let { uri ->
        ctx.getFilePathFromUri(uri)
    }

    var selectedStoresForExport by remember { mutableStateOf(setOf<SettingsStore<*, *>>()) }
    var selectedStoresForImport by remember { mutableStateOf(setOf<SettingsStore<*, *>>()) }
    var importJson by remember { mutableStateOf<JSONObject?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }


    val settingsExportLauncher = rememberSettingsExportLauncher(selectedStoresForExport)
    val autoBackupLauncher = rememberAutoBackupLauncher()

    val settingsImportLauncher = rememberSettingsImportLauncher(
        onJsonReady = { json ->
            importJson = json
            showImportDialog = true
        }
    )

    SettingsScaffold(
        title = ctx.getString(R.string.backup_restore),
        onBack = onBack,
        helpText = ctx.getString(R.string.backup_restore_text),
        onReset = {
            scope.launch {
                BackupSettingsStore.resetAll(ctx)
            }
        }
    ) {
        BackupButtons(
            onExport = { showExportDialog = true },
            onImport = {
                settingsImportLauncher.launch(
                    arrayOf(
                        "application/json",
                        "text/plain",
                        "application/octet-stream",
                        "*/*"
                    )
                )
            }
        )

        DragonSettingsGroup(R.string.automatic_backups) {
            Setting(BackupSettingsStore.autoBackupEnabled) {
                // If the user disabled the backup, also remove the uri
                if (!it) {
                    scope.launch {
                        BackupSettingsStore.autoBackupUri.reset(ctx)
                    }
                }
            }

            AnimatedVisibility(autoBackupEnabled) {
                AnimatedContent(backupPath == null) { state ->
                    if (state) {
                        SettingsItem(
                            title = stringResource(R.string.backup_location),
                            description = stringResource(R.string.backup_location_desc),
                            icon = R.drawable.folder_open,
                            onClick = { autoBackupLauncher.launch("dragonlauncher-auto-backup.json") }
                        )
                    } else {
                        DragonColumn {
                            SettingsItem(
                                title = stringResource(R.string.backup_location),
                                description = backupPath
                                    ?: stringResource(R.string.backup_location_desc),
                                icon = R.drawable.folder_open,
                                onClick = {
                                    autoBackupLauncher.launch("dragonlauncher-auto-backup.json")
                                },
                                trailingIcon = R.drawable.open_in_new,
                                onExternalClick = {
                                    autoBackupUri.let { uri ->
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, "application/json")
                                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        }
                                        ctx.startActivity(
                                            Intent.createChooser(
                                                intent,
                                                "Open backup file"
                                            )
                                        )
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
                                enabled = !hasTriggeredManualAutoBackup,
                                onClick = {
                                    backupViewModel.commandBackup()
                                    ctx.showToast(ctx.getString(R.string.backup_triggered))
                                    hasTriggeredManualAutoBackup = true
                                }
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(autoBackupEnabled) {
            DragonSettingsGroup(
                title = R.string.auto_backup_stores,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp)
            ) {
                SelectedActionRow(selectedStores, backupableStores.size) { save() }

                selectedStores.entries.forEach { (settingsStore, isSelected) ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shapedClickable {
                                selectedStores[settingsStore] = !isSelected
                                save()
                            }
                            .padding(5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = settingsStore.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Checkbox(checked = isSelected, onCheckedChange = null)
                    }
                }
            }
        }
    }


    if (showExportDialog) {
        ExportSettingsDialog(
            title = R.string.pick_stores_to_display,
            onDismiss = { showExportDialog = false },
            onConfirm = { selectedStores ->
                showExportDialog = false
                selectedStoresForExport = selectedStores
                settingsExportLauncher.launch("backup-${DateUtils.nowFormattedDateTime()}.json")
            }
        )
    }

    importJson?.let { json ->
        if (showImportDialog) {
            ImportSettingsDialog(
                backupJson = json,
                onDismiss = {
                    showImportDialog = false
                    importJson = null
                },
                onConfirm = { selectedStores ->
                    showImportDialog = false
                    selectedStoresForImport = selectedStores

                    scope.launch {
                        try {
                            SettingsBackupManager.importSettingsFromJson(
                                ctx,
                                json,
                                selectedStoresForImport
                            )
                            backupViewModel.result.value = BackupResult(
                                export = false,
                                error = false,
                                title = ctx.getString(R.string.import_successful)
                            )

                            importJson = null
                        } catch (e: Exception) {
                            logE(BACKUP_TAG, e) { "Import failed" }
                            backupViewModel.result.value = BackupResult(
                                export = false,
                                error = true,
                                title = ctx.getString(R.string.import_failed),
                                message = e.message ?: ""
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun BackupButtons(
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        GradientBigButton(
            text = stringResource(R.string.export_settings),
            onClick = onExport,
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.cloud_upload),
                    contentDescription = stringResource(R.string.export_settings),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier
        )

        GradientBigButton(
            text = stringResource(R.string.import_settings),
            onClick = onImport,
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.download),
                    contentDescription = stringResource(R.string.import_settings),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier
        )
    }
}
