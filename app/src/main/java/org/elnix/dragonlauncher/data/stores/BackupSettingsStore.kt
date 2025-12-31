package org.elnix.dragonlauncher.data.stores

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.data.BaseSettingsStore
import org.elnix.dragonlauncher.data.DataStoreName
import org.elnix.dragonlauncher.data.backupDatastore
import org.elnix.dragonlauncher.data.getBooleanStrict
import org.elnix.dragonlauncher.data.getStringSetStrict
import org.elnix.dragonlauncher.data.getStringStrict
import org.elnix.dragonlauncher.data.putIfNonDefault
import org.elnix.dragonlauncher.data.stores.BackupSettingsStore.Keys.AUTO_BACKUP_ENABLED
import org.elnix.dragonlauncher.data.stores.BackupSettingsStore.Keys.AUTO_BACKUP_URI
import org.elnix.dragonlauncher.data.stores.BackupSettingsStore.Keys.BACKUP_STORES

object BackupSettingsStore : BaseSettingsStore<Map<String, Any>>() {

    override val name: String = "Backup"

    private data class SettingsBackup(
        val autoBackupEnabled: Boolean = false,
        val autoBackupUri: String? = null,
        val lastBackupTime: Long = System.currentTimeMillis()
    )

    // Cause at runtime, the app crashes due to early .entries initialization
    private val defaultBackupStores: Set<String>
        get() = DataStoreName.entries
            .map { it.value }
            .toSet()


    private val defaults = SettingsBackup()

    private object Keys {
        val AUTO_BACKUP_ENABLED = booleanPreferencesKey("autoBackupEnabled")
        val AUTO_BACKUP_URI = stringPreferencesKey("autoBackupUri")
        val BACKUP_STORES = stringSetPreferencesKey("backupStores")
        val LAST_BACKUP_TIME = longPreferencesKey("lastBackupTime")
        val ALL = listOf(
            AUTO_BACKUP_ENABLED,
            AUTO_BACKUP_URI,
            BACKUP_STORES,
            LAST_BACKUP_TIME
        )
    }

    fun getAutoBackupEnabled(ctx: Context) = ctx
        .backupDatastore.data
        .map { it[AUTO_BACKUP_ENABLED] ?: defaults.autoBackupEnabled }

    suspend fun setAutoBackupEnabled(ctx: Context, enabled: Boolean) {
        ctx.backupDatastore.edit { it[AUTO_BACKUP_ENABLED] = enabled }
    }

    fun getAutoBackupUri(ctx: Context) = ctx
        .backupDatastore.data
        .map { it[AUTO_BACKUP_URI]?.ifBlank { null } }

    suspend fun setAutoBackupUri(ctx: Context, uri: Uri?) {
        ctx.backupDatastore.edit {
            it[AUTO_BACKUP_URI] = uri?.toString() ?: ""
        }
    }

    fun getBackupStores(ctx: Context) = ctx
        .backupDatastore.data
        .map { it[BACKUP_STORES] ?: defaultBackupStores }

    suspend fun setBackupStores(ctx: Context, stores: List<DataStoreName>) {
        ctx.backupDatastore.edit { prefs ->
            prefs[BACKUP_STORES] = stores.map { it.value }.toSet()
        }
    }

    fun getLastBackupTime(ctx: Context) = ctx
        .backupDatastore.data
        .map { it[Keys.LAST_BACKUP_TIME] ?: defaults.lastBackupTime }

    suspend fun setLastBackupTime(ctx: Context) {
        ctx.backupDatastore.edit { it[Keys.LAST_BACKUP_TIME] = System.currentTimeMillis() }
    }

    override suspend fun resetAll(ctx: Context) {
        ctx.backupDatastore.edit { prefs ->
            Keys.ALL.forEach { prefs.remove(it) }
        }
    }

    override suspend fun getAll(ctx: Context): Map<String, Any> {
        val prefs = ctx.backupDatastore.data.first()

        return buildMap {

            putIfNonDefault(
                AUTO_BACKUP_ENABLED,
                prefs[AUTO_BACKUP_ENABLED],
                defaults.autoBackupEnabled
            )

            putIfNonDefault(
                AUTO_BACKUP_URI,
                prefs[AUTO_BACKUP_URI],
                null
            )

            putIfNonDefault(
                BACKUP_STORES,
                prefs[BACKUP_STORES],
                defaultBackupStores
            )
        }
    }

    override suspend fun setAll(ctx: Context, value: Map<String, Any>) {
        ctx.backupDatastore.edit { prefs ->

            prefs[AUTO_BACKUP_ENABLED] =
                getBooleanStrict(value, AUTO_BACKUP_ENABLED, defaults.autoBackupEnabled)

            prefs[AUTO_BACKUP_URI] =
                getStringStrict(value, AUTO_BACKUP_URI, "")

            prefs[BACKUP_STORES] =
                getStringSetStrict(value, BACKUP_STORES, defaultBackupStores)
        }
    }
}
