package org.elnix.dragonlauncher.settings

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.common.utils.VersionsUtils.getVersionNameAndCode
import org.elnix.dragonlauncher.ktx.camelToSnakeCase
import org.elnix.dragonlauncher.logging.BACKUP_TAG
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.logging.logW
import org.elnix.dragonlauncher.settings.bases.stores.JsonArraySettingsStore
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileOutputStream

object SettingsBackupManager {
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

            if (dataStoreName.name in requestedStores.map { it.name }) {
                settingsStore.exportForBackup(ctx, forceAllKeys)?.let {
                    json.put(dataStoreName.name, it)
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

            val key = dataStoreName.name.camelToSnakeCase()
            if (key in requestedStores.map { it.name.camelToSnakeCase() }) {

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

//                    is JsonObjectSettingsStore -> {
//                        if (raw is JSONObject) {
//                            settingsStore.importFromBackup(ctx, raw)
//                        }
//                    }
                }
            }
        }

        PrivateSettingsStore.hasInitialized.set(ctx, true)
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
