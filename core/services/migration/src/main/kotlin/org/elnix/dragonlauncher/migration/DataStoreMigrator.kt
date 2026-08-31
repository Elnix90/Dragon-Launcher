package org.elnix.dragonlauncher.migration

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

private val OLD_DATASTORE_FILES: Map<String, String> =
    mapOf(
        "uiDatastore" to "ui",
        "colorModeDatastore" to "color_mode",
        "colorDatastore" to "color",
        "drawerDatastore" to "drawer",
        "behaviorDatastore" to "behavior",
        "wellbeingDatastore" to "wellbeing",
        "languageDatastore" to "language",
        "backupDatastore" to "backup",
        "statusDatastore" to "status_bar",
        "debugDatastore" to "debug",
        "widgetsDatastore" to "widgets",
        "workspacesDataStore" to "workspaces",
        "privateSettingsStore" to "private",
        "privateAppsDatastore" to "private_apps",
        "floatingAppsDatastore" to "floating_apps",
        "swipePointsDatastore" to "new_actions",
        "swipeMapDataStore" to "swipe_map",
        "statusBarJsonDataStore" to "status_bar_json",
        "AngleLineDatastore" to "angle_line",
        "HoldTOActivateDatastore" to "hold_to_activate"
    )

/**
 * Raw DataStore preference keys of old stores whose backup JSON has a different
 * shape than a plain key/value map.
 */
private const val KEY_POINTS_JSON = "points_json"
private const val KEY_NESTS_JSON = "nests_json"
private const val KEY_DEFAULT_CIRCLE = "default_circle"
private const val KEY_WORKSPACE_STATE = "workspace_state"
private const val KEY_WIDGETS = "widgets"
private const val KEY_STATUS_BAR_JSON = "statusBarJson"

/**
 * Silent auto-migrator that reads old 3.2.2 DataStore files and feeds them through the
 * shared [LegacyBackupJsonMigrator].
 *
 * Each old DataStore file is reconstructed into the same backup-JSON shape the old app
 * would export, then the whole payload is migrated with the regular store mappings
 * (including store splits, key routes, and transformations). This runs on app startup
 * when old DataStore files are detected and the new DataStore is empty (see [isMigrationNeeded]).
 */
public class DataStoreMigrator {
    /**
     * Reconstructs the old backup JSON from every present old DataStore file and
     * migrates it via [LegacyBackupJsonMigrator].
     *
     * @param ctx Android context used to locate old DataStore files.
     * @return [MigrationResult] summary of what was migrated, skipped, or errored.
     */
    public suspend fun migrateFromOldDataStores(ctx: Context): MigrationResult =
        withContext(Dispatchers.IO) {
            val json = JSONObject()
            for ((fileName, oldBackupKey) in OLD_DATASTORE_FILES) {
                try {
                    val value = readStoreAsBackupJson(ctx, fileName, oldBackupKey) ?: continue
                    json.put(oldBackupKey, value)
                } catch (e: Exception) {
                    return@withContext MigrationResult.failure(
                        "DataStore migration failed for $fileName: ${e.message}",
                        listOfNotNull(e.message)
                    )
                }
            }

            if (json.length() == 0) {
                return@withContext MigrationResult.success(emptySet(), emptySet())
            }

            json.put("app_version", "${OldToNewStoreMapping.legacyAppVersionPrefix} (auto-migration)")
            LegacyBackupJsonMigrator().migrateFromJsonObject(ctx, json)
        }

    /**
     * Reads a single old DataStore file and rebuilds the JSON value the old app would
     * have exported for its backup key.
     */
    private suspend fun readStoreAsBackupJson(
        ctx: Context,
        fileName: String,
        oldBackupKey: String
    ): Any? {
        val oldFile = File(ctx.filesDir, "datastore/$fileName.preferences_pb")
        if (!oldFile.exists()) return null

        val oldDataStore = PreferenceDataStoreFactory.create { oldFile }
        val prefs =
            try {
                oldDataStore.data.first()
            } catch (_: Exception) {
                emptyPreferences()
            }
        if (prefs.asMap().isEmpty()) return null

        return when (oldBackupKey) {
            "new_actions" -> reconstructNewActions(prefs)
            "workspaces" -> prefs.stringValue(KEY_WORKSPACE_STATE)?.let(::parseObject)
            "widgets" -> prefs.stringValue(KEY_WIDGETS)?.let(::parseArray)
            "status_bar_json" -> prefs.stringValue(KEY_STATUS_BAR_JSON)?.let(::parseArray)
            else -> reconstructKeyValueMap(prefs)
        }
    }

    /**
     * Old `new_actions` (the swipe points/nests store) persisted points, nests and the
     * default point as JSON-array strings.
     */
    private fun reconstructNewActions(prefs: Preferences): JSONObject? {
        val obj = JSONObject()
        prefs.stringValue(KEY_POINTS_JSON)?.let { obj.put("points", JSONArray(it)) }
        prefs.stringValue(KEY_NESTS_JSON)?.let { obj.put("nests", JSONArray(it)) }
        prefs.stringValue(KEY_DEFAULT_CIRCLE)?.let { obj.put("default_point", JSONArray(it)) }
        return obj.takeIf { it.length() > 0 }
    }

    /**
     * Most old stores are plain key/value maps; rebuild them verbatim so the store
     * mappings can rename keys, apply transformers, or drop them.
     */
    private fun reconstructKeyValueMap(prefs: Preferences): JSONObject? {
        val obj = JSONObject()
        for ((key, value) in prefs.asMap()) {
            obj.put(key.name, value)
        }
        return obj.takeIf { it.length() > 0 }
    }

    private fun Preferences.stringValue(key: String): String? {
        for ((k, v) in asMap()) {
            if (k.name == key) return v as? String
        }
        return null
    }

    private fun parseArray(raw: String): JSONArray? =
        try {
            JSONArray(raw)
        } catch (_: Exception) {
            null
        }

    private fun parseObject(raw: String): JSONObject? =
        try {
            JSONObject(raw)
        } catch (_: Exception) {
            null
        }

    /**
     * Checks whether any old 3.2.2 DataStore files still exist on disk.
     *
     * @param ctx Android context.
     * @return `true` if at least one old DataStore file is present.
     */
    public fun hasOldDataStoreFiles(ctx: Context): Boolean =
        OLD_DATASTORE_FILES.keys.any { name ->
            File(ctx.filesDir, "datastore/$name.preferences_pb").exists()
        }

    /**
     * Determines whether auto-migration is needed.
     *
     * Migration is needed when old DataStore files exist **and** the new
     * unified DataStore file is either absent or empty.
     *
     * @param ctx Android context.
     * @return `true` if auto-migration should run.
     */
    public fun isMigrationNeeded(ctx: Context): Boolean {
        val hasOldFiles = hasOldDataStoreFiles(ctx)
        if (!hasOldFiles) return false
        val newFile = File(ctx.filesDir, "datastore/AppDatastore.preferences_pb")
        return !newFile.exists() || newFile.length() == 0L
    }
}
