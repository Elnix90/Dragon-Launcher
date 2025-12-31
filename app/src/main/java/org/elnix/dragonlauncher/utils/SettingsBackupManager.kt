package org.elnix.dragonlauncher.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.data.DataStoreName
import org.elnix.dragonlauncher.data.SwipeJson
import org.elnix.dragonlauncher.data.allStores
import org.elnix.dragonlauncher.data.stores.BackupSettingsStore
import org.elnix.dragonlauncher.data.stores.SwipeSettingsStore.savePoints
import org.json.JSONObject
import java.io.FileOutputStream

object SettingsBackupManager {

    /**
     * Automatic backup to pre-selected file
     */
    suspend fun triggerBackup(ctx: Context) {
        if (!BackupSettingsStore.getAutoBackupEnabled(ctx).first()) {
            Log.w(BACKUP_TAG, "Auto-backup disabled")
            return
        }

        try {
            val uriString = BackupSettingsStore.getAutoBackupUri(ctx).first()
            if (uriString.isNullOrBlank()) {
                Log.w(BACKUP_TAG, "No backup URI set")
                return
            }

            val uri = uriString.toUri()
            val path = getFilePathFromUri(ctx, uri)

            if (!ctx.hasUriReadWritePermission(uri)) {
                Log.e(BACKUP_TAG, "URI permission expired!")
                ctx.showToast("Auto-backup URI expired. Please reselect file.")
                return
            }

            val selectedStores = BackupSettingsStore.getBackupStores(ctx).first()
                .mapNotNull { storeValue -> DataStoreName.entries.find { it.value == storeValue } }

            exportSettings(ctx, uri, selectedStores)

            BackupSettingsStore.setLastBackupTime(ctx)
            Log.i(BACKUP_TAG, "Auto-backup completed to $path")

        } catch (e: Exception) {
            Log.e(BACKUP_TAG, "Auto-backup failed", e)
            if (e.message?.contains("permission") == true) {
                ctx.showToast("URI permission lost. Reselect backup file.")
            }
        }
    }


    suspend fun writeJson(ctx: Context, uri: Uri, json: JSONObject) {
        withContext(Dispatchers.IO) {
            ctx.contentResolver.openFileDescriptor(uri, "wt")?.use { pfd ->
                FileOutputStream(pfd.fileDescriptor).use { fos ->
                    fos.channel.truncate(0) // Ensure file is cleared before writing
                    fos.write(json.toString(2).toByteArray()) // Pretty print with 2 spaces
                    fos.flush()
                }
            } ?: run {
                Log.e(
                    BACKUP_TAG,
                    "Failed to open FileDescriptor - URI permission expired!"
                )
                throw IllegalStateException("Cannot write to URI - permission expired")
            }
        }
    }


    /**
     * Exports only the requested stores.
     * @param requestedStores List of DataStoreName objects
     */
    suspend fun exportSettings(
        ctx: Context,
        uri: Uri,
        requestedStores: List<DataStoreName>
    ) {
        val json = JSONObject()

        allStores.forEach { store ->
            if (store.backupKey in requestedStores.map { it.backupKey }) {
                store.store.exportForBackup(ctx)?.let {
                    json.put(store.backupKey, it)
                }
            }
        }

        writeJson(ctx, uri, json)
    }

    /**
     * Imports app settings from a JSON object directly, without reading a file.
     *
     * This method supports both the current store-based backup system and the legacy
     * "actions" JSON array format. For each requested store, if the JSON contains
     * a corresponding object, it will be passed to the store's `importFromBackup`.
     *
     * @param ctx Context used for accessing DataStores
     * @param json Parsed JSONObject containing backup data
     * @param requestedStores List of DataStoreName objects specifying which stores to restore
     */
    suspend fun importSettingsFromJson(
        ctx: Context,
        json: JSONObject,
        requestedStores: List<DataStoreName>
    ) {
        Log.d(BACKUP_TAG, json.toString())

        allStores.forEach { store ->
            val key = store.backupKey
            if (key in requestedStores.map { it.backupKey }) {
                json.optJSONObject(key)?.let {
                    store.store.importFromBackup(ctx, it)
                }
            }
        }

        // LEGACY format: fallback for "actions" array
        json.optJSONArray("actions")?.let { actionsArray ->
            Log.d(BACKUP_TAG, "Fallback to legacy system")
            val legacyPoints = SwipeJson.decode(actionsArray.toString())
            savePoints(ctx, legacyPoints)
        }
    }
}
