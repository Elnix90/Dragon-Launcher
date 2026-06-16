package org.elnix.dragonlauncher.settings

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.common.utils.VersionsUtils.getVersionNameAndCode
import org.elnix.dragonlauncher.ktx.getFilePathFromUri
import org.elnix.dragonlauncher.ktx.hasUriReadWritePermission
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.logging.BACKUP_TAG
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.logging.logI
import org.elnix.dragonlauncher.logging.logV
import org.elnix.dragonlauncher.logging.logW
import org.elnix.dragonlauncher.settings.bases.stores.JsonArraySettingsStore
import org.elnix.dragonlauncher.settings.bases.stores.JsonObjectSettingsStore
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.BackupSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileOutputStream

object SettingsBackupManager {

    /**
     * Automatic backup to pre-selected file
     */
    suspend fun triggerBackup(ctx: Context) {
        if (!BackupSettingsStore.autoBackupEnabled.get(ctx)) {
            logV(BACKUP_TAG) { "Auto-backup disabled" }
            return
        }

        val lastBackupTime = PrivateSettingsStore.lastBackupTime.get(ctx)
        if (System.currentTimeMillis() - lastBackupTime < 1000L) {
            logW(BACKUP_TAG) { "Auto-backup fired too quickly, canceling it" }
            return
        }

        try {
            val uriString = BackupSettingsStore.autoBackupUri.get(ctx)
            if (uriString.isBlank()) {
                logW(BACKUP_TAG) { "No backup URI set" }
                return
            }

            val uri = uriString.toUri()
            val path = ctx.getFilePathFromUri(uri)

            if (!ctx.hasUriReadWritePermission(uri)) {
                logW(BACKUP_TAG) { "URI permission expired!" }
                ctx.showToast("Auto-backup URI expired. Please reselect file.")
                return
            }

            val selectedStores = BackupSettingsStore.backupStores.get(ctx)
                .mapNotNull { storeValue ->
                    DataStoreName.entries.find { it.value == storeValue }
                }
                .toSet()


            exportSettings(ctx, uri, selectedStores)

            PrivateSettingsStore.lastBackupTime.set(ctx, System.currentTimeMillis())
            logI(BACKUP_TAG) { "Auto-backup completed to $path" }

        } catch (e: Exception) {
            logE(BACKUP_TAG, e) { "Auto-backup failed" }
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
                logW(BACKUP_TAG) {
                    "Failed to open FileDescriptor - URI permission expired!"
                }
                throw IllegalStateException("Cannot write to URI - permission expired")
            }
        }
    }


    suspend fun createJsonToExport(
        ctx: Context,
        requestedStores: Set<DataStoreName>,
        forceAllKeys: Boolean
    ): JSONObject {
        val json = JSONObject()

        allStores.forEach { entry ->
            val dataStoreName = entry.key
            val settingsStore = entry.value

            if (dataStoreName.value in requestedStores.map { it.value }) {
                settingsStore.exportForBackup(ctx, forceAllKeys)?.let {
                    json.put(dataStoreName.value, it)
                }
            }
        }

        json.put("app_version", ctx.getVersionNameAndCode())

        return json
    }


    /**
     * Exports only the requested stores.
     * @param requestedStores List of _root_ide_package_.org.elnix.dragonlauncher.settings.bases._root_ide_package_.org.elnix.dragonlauncher.settings.DataStoreName objects
     */
    suspend fun exportSettings(
        ctx: Context,
        uri: Uri,
        requestedStores: Set<DataStoreName>
    ) {
        val json = createJsonToExport(ctx, requestedStores, false)

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
     * @param requestedStores List of _root_ide_package_.org.elnix.dragonlauncher.settings.bases._root_ide_package_.org.elnix.dragonlauncher.settings.DataStoreName objects specifying which stores to restore
     */
    suspend fun importSettingsFromJson(
        ctx: Context,
        json: JSONObject,
        requestedStores: Set<DataStoreName>
    ) {
        logD(BACKUP_TAG) { json.toString() }

        allStores.forEach { entry ->
            val dataStoreName = entry.key
            val settingsStore = entry.value

            val key = dataStoreName.value
            if (key in requestedStores.map { it.value }) {

                val raw = json.opt(key) ?: return@forEach

                when (settingsStore) {
                    is JsonArraySettingsStore -> {
                        if (raw is JSONArray) {
                            settingsStore.importFromBackup(ctx, raw)
                        }
                    }

                    is MapSettingsStore -> {
                        if (raw is JSONObject) {
                            settingsStore.importFromBackup(ctx, raw)
                        }
                    }

                    is JsonObjectSettingsStore -> {
                        if (raw is JSONObject) {
                            settingsStore.importFromBackup(ctx, raw)
                        }
                    }

                    else -> {
                        /* no-op */
                    }
                }
            }
        }

        PrivateSettingsStore.hasInitialized.set(ctx, true)


        // FUCK LEGACY 🤎
//        logE(BACKUP_TAG) { json.optJSONArray("actions")?.toString() ?: "No actions" }
//
//        // LEGACY format: fallback for "actions" array
//        json.optJSONArray("actions")?.let { actionsArray ->
//            logD(BACKUP_TAG) { "Fallback to legacy system (actions)" }
//            val legacyPoints = SwipeJson.decodeLegacy(actionsArray.toString())
//            SwipeSettingsStore.savePoints(ctx, legacyPoints)
//        }
//
//
//        // NEW LEGACY format: fallback for "new_actions" (points and nests)
//        json.optJSONObject("new_actions")?.let { newActionsObj ->
//            logD(BACKUP_TAG) { "Fallback to legacy system (new_actions)" }
//
//            newActionsObj.optJSONArray("points")?.let { pointsArray ->
//                val legacyPoints = SwipeJson.decodePoints(pointsArray.toString())
//                SwipeSettingsStore.savePoints(ctx, legacyPoints)
//            }
//
//            newActionsObj.optJSONArray("nests")?.let { nestsArray ->
//                val legacyNests = SwipeJson.decodeNests(nestsArray.toString())
//                SwipeSettingsStore.saveNests(ctx, legacyNests)
//            }
//        }
    }


    /**
     * Imports theme settings from a JSON object directly, without reading a file.
     *
     * This method only imports the Colors part of the
     *
     * @param ctx Context used for accessing DataStores
     * @param json Parsed JSONObject containing backup data
     */
    suspend fun importTheme(
        ctx: Context,
        json: JSONObject
    ) {
        importSettingsFromJson(ctx, json, themeDataStores)
    }
}
