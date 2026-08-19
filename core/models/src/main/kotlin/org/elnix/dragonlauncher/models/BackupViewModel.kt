package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.elnix90.core.SettingsBackupManager.exportSettings
import org.elnix.dragonlauncher.BACKUP_TAG
import org.elnix.dragonlauncher.SETTINGS_TAG
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
import org.elnix.dragonlauncher.migration.MigrationResult
import org.elnix.dragonlauncher.migration.SettingsMigrationService
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.points.PointsService
import org.elnix.dragonlauncher.settings.backupableStores
import org.elnix.dragonlauncher.settings.stores.map.BackupSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import org.elnix.dragonlauncher.settings.toSettingsStoreList
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@HiltViewModel
public class BackupViewModel @Inject constructor(
    application: Application,
    private val migrationService: SettingsMigrationService,
    private val pointsService: PointsService
) : AndroidViewModel(application) {

    public val result: SettingFlow<BackupResult?> = SettingFlow(null)
    public val migrationResult: SettingFlow<MigrationResult?> = SettingFlow(null)
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

    public fun commandBackup() {
        _backupTrigger.tryEmit(Unit)
    }

    /**
     * Reads a JSON file from the given URI and migrates it if it's a legacy 3.2.2 backup.
     */
    public fun migrateFromLegacyBackup(content: String) {
        viewModelScope.launch {
            migrationResult.value = null
            try {
                val result = migrationService.migrateFromBackupJson(
                    ctx = application,
                    legacyJson = content
                )
                migrationResult.value = result
                migrationService.logResult(result)

                if (result.success) {
                    pointsService.load()
                    PrivateSettingsStore.hasInitialized.set(application, true)
                    PrivateSettingsStore.hasSeenWelcome.set(application, true)
                }
            } catch (e: Exception) {
                logE(BACKUP_TAG, e) { "Legacy migration failed" }
                migrationResult.value = MigrationResult.failure(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Checks whether the given JSON content is a legacy 3.2.2 backup.
     */
    public fun isLegacyBackup(jsonContent: String): Boolean {
        return migrationService.isLegacyBackup(jsonContent)
    }

    public fun isMigrationNeeded(): Boolean {
        return migrationService.isMigrationNeeded(application)
    }

    public fun attemptAutoMigration() {
        viewModelScope.launch {
            migrationService.attemptAutoMigration(
                ctx = application,
                onComplete = { result ->
                    migrationResult.value = result

                    if (result.success) {
                        pointsService.load()
                        logI(SETTINGS_TAG) { "Migration completed: ${result.message}" }
                    } else {
                        logI(SETTINGS_TAG) { "Migration skipped or failed: ${result.message}" }
                    }
                }
            )
        }
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
        val selectedStores = BackupSettingsStore.backupStores
            .get(application)
            .takeIf { it.isNotEmpty() }
            ?: backupableStores.mapTo(mutableSetOf()) { it.name }

        try {
            exportSettings(application, uri, selectedStores.toSettingsStoreList())
            logI(BACKUP_TAG) { "Auto-backup completed!" }
        } catch (e: Throwable) {
            logE(BACKUP_TAG, e) { "Auto-backup failed" }
            application.showToast("Auto backup failed: $e")
        }
    }
}

public data class BackupResult(
    val export: Boolean,
    val error: Boolean,
    val title: String,
    val message: String = ""
)
