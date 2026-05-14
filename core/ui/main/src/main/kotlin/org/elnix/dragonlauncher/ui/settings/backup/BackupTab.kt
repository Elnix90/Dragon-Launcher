package org.elnix.dragonlauncher.ui.settings.backup

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.BACKUP_TAG
import org.elnix.dragonlauncher.common.messyfolder.getFilePathFromUri
import org.elnix.dragonlauncher.common.messyfolder.showToast
import org.elnix.dragonlauncher.common.utils.DateUtils.formatDateTime
import org.elnix.dragonlauncher.common.utils.DateUtils.today
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.models.BackupResult
import org.elnix.dragonlauncher.settings.SettingsBackupManager
import org.elnix.dragonlauncher.settings.backupableStores
import org.elnix.dragonlauncher.settings.bases.DatastoreProvider
import org.elnix.dragonlauncher.settings.stores.BackupSettingsStore
import org.elnix.dragonlauncher.settings.stores.PrivateSettingsStore
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.modifiers.shapedClickable
import org.elnix.dragonlauncher.ui.composition.LocalBackupViewModel
import org.elnix.dragonlauncher.ui.dialogs.ExportSettingsDialog
import org.elnix.dragonlauncher.ui.dialogs.ImportSettingsDialog
import org.elnix.dragonlauncher.ui.dialogs.SelectedActionRow
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsSwitchRow
import org.elnix.dragonlauncher.ui.helpers.GradientBigButton
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.remembers.rememberAutoBackupLauncher
import org.elnix.dragonlauncher.ui.remembers.rememberSettingsExportLauncher
import org.elnix.dragonlauncher.ui.remembers.rememberSettingsImportLauncher
import org.json.JSONObject

@SuppressLint("LocalContextGetResourceValueCall")
@Suppress("AssignedValueIsNeverRead")
@Composable
fun BackupTab(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val backupViewModel = LocalBackupViewModel.current


    val autoBackupEnabled by BackupSettingsStore.autoBackupEnabled.asState()
    val autoBackupUriString by BackupSettingsStore.autoBackupUri.asState()
    val lastBackupTime by PrivateSettingsStore.lastBackupTime.asState()
    val backupStores by BackupSettingsStore.backupStores.asState()


    val selectedStores = remember(backupStores) {
        mutableStateMapOf<DatastoreProvider, Boolean>().apply {
            backupableStores.forEach { put(it.key, it.value.dataStoreName.value in backupStores) }
        }
    }

    fun save() {
        logD(BACKUP_TAG) { "Setting: ${selectedStores.size} stores" }
        scope.launch {
            if (selectedStores.size == backupableStores.size) {
                BackupSettingsStore.backupStores.reset(ctx)
            } else {
                BackupSettingsStore.backupStores.set(ctx, selectedStores.map { it.key.value }.toSet())
            }
        }
    }

    LaunchedEffect(lastBackupTime) {
        ctx.showToast(lastBackupTime)
    }

    val autoBackupUri: Uri? = autoBackupUriString.takeIf { it.isNotEmpty() }?.toUri()

    val backupPath: String? = autoBackupUri?.let { uri ->
        getFilePathFromUri(ctx, uri)
    }

    var selectedStoresForExport by remember { mutableStateOf(setOf<DatastoreProvider>()) }
    var selectedStoresForImport by remember { mutableStateOf(setOf<DatastoreProvider>()) }
    var importJson by remember { mutableStateOf<JSONObject?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var hasTriggeredManualAutoBackup by remember { mutableStateOf(false) }


    val settingsExportLauncher = rememberSettingsExportLauncher(selectedStoresForExport)
    val autoBackupLauncher = rememberAutoBackupLauncher()

    val settingsImportLauncher = rememberSettingsImportLauncher(
        onJsonReady = { json ->
            importJson = json
            showImportDialog = true
        }
    )


    // ──────── UI ───────────────────────────────────────
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


            if (autoBackupEnabled) {
                AnimatedContent(backupPath == null) { state ->
                    if (state) {
                        SettingsItem(
                            title = stringResource(R.string.backup_location),
                            description = stringResource(R.string.backup_location_desc),
                            icon = R.drawable.folder_open,
                            onClick = { autoBackupLauncher.launch("dragonlauncher-auto-backup.json") }
                        )
                    } else {
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
                    }
                }


                if (backupPath != null) {
                    SettingsItem(
                        title = stringResource(R.string.last_backup),
                        description = lastBackupTime.formatDateTime(),
                        icon = R.drawable.reset,
                        enabled = !hasTriggeredManualAutoBackup,
                        onClick = {
                            hasTriggeredManualAutoBackup = true
                            scope.launch {
                                SettingsBackupManager.triggerBackup(ctx)
                                ctx.showToast(ctx.getString(R.string.backup_triggered))
                                delay(1000)
                                hasTriggeredManualAutoBackup = false
                            }
                        }
                    )
                }
            }
        }

        DragonSettingsGroup(R.string.auto_backup_stores) {
            if (autoBackupEnabled) {

                SelectedActionRow(selectedStores, backupableStores.size) { save() }

                selectedStores.entries.forEach { (datastoreName, isSelected) ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shapedClickable() {
                                selectedStores[datastoreName] = !isSelected
                                save()
                            }
                            .padding(5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = backupableStores[datastoreName]!!.name,
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
            onDismiss = { showExportDialog = false },
            onConfirm = { selectedStores ->
                showExportDialog = false
                selectedStoresForExport = selectedStores.keys
                settingsExportLauncher.launch("backup-${today()}.json")
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
                    selectedStoresForImport = selectedStores.keys

                    scope.launch {
                        try {
                            SettingsBackupManager.importSettingsFromJson(
                                ctx,
                                json,
                                selectedStoresForImport
                            )
                            backupViewModel.setResult(
                                BackupResult(
                                    export = false,
                                    error = false,
                                    title = ctx.getString(R.string.import_successful)
                                )
                            )
                            importJson = null
                        } catch (e: Exception) {
                            logE(BACKUP_TAG, e) { "Import failed" }
                            backupViewModel.setResult(
                                BackupResult(
                                    export = false,
                                    error = true,
                                    title = ctx.getString(R.string.import_failed),
                                    message = e.message ?: ""
                                )
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
