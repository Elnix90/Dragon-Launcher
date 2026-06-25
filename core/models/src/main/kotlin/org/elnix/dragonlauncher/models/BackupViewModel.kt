package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.elnix90.core.SettingsBackupManager.exportSettings
import io.github.elnix90.logging.BACKUP_TAG
import io.github.elnix90.logging.logE
import io.github.elnix90.logging.logI
import io.github.elnix90.logging.logV
import io.github.elnix90.logging.logW
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.SettingFlow
import org.elnix.dragonlauncher.ktx.hasUriReadWritePermission
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.settings.stores.map.BackupSettingsStore
import org.elnix.dragonlauncher.settings.toSettingsStoreList
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@HiltViewModel
class BackupViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    val result = SettingFlow<BackupResult?>(null)
    private val _backupTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    init {
        viewModelInitialized()

        viewModelScope.launch {
            _backupTrigger
                .debounce(5000.milliseconds)
                .collect {
                    performBackup()
                }
        }
    }

    fun commandBackup() {
        _backupTrigger.tryEmit(Unit)
    }

    private suspend fun performBackup() {
        if (!BackupSettingsStore.autoBackupEnabled.get(application)) {
            logV(BACKUP_TAG) { "Auto-backup disabled" }
            return
        }
        val uriString = BackupSettingsStore.autoBackupUri.get(application)
        if (uriString.isBlank()) {
            logW(BACKUP_TAG) { "No backup URI set" }
            return
        }
        val uri = uriString.toUri()
        if (!application.hasUriReadWritePermission(uri)) {
            logW(BACKUP_TAG) { "URI permission expired!" }
            application.showToast("Auto-backup URI expired. Please reselect file.")
            return
        }
        val selectedStores = BackupSettingsStore.backupStores.get(application)
        if (selectedStores.isEmpty()) {
            logW(BACKUP_TAG) { "No stores set to backup, skipping it" }
            application.showToast("No stores set to backup, skipping it")
            return
        }
        try {
            exportSettings(application, uri, selectedStores.toSettingsStoreList())
            logI(BACKUP_TAG) { "Auto-backup completed!" }
        } catch (e: Throwable) {
            logE(BACKUP_TAG, e) { "Auto-backup failed" }
            application.showToast("Auto backup failed: $e")
        }
    }
}

data class BackupResult(
    val export: Boolean,
    val error: Boolean,
    val title: String,
    val message: String = ""
)
