package org.elnix.dragonlauncher.migration

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import io.github.elnix90.core.util.dataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Maps old (3.2.2) DataStore file names to their corresponding backup key and new prefix.
 *
 * Each entry represents one old DataStore file that should be migrated. Files whose
 * `newPrefix` is `null` have no direct 1:1 mapping and are skipped during auto-migration
 * (they may be handled by [LegacyBackupJsonMigrator] instead).
 *
 * @property oldBackupKey The backup key used in the old preference system.
 * @property newPrefix The prefix used in the new unified DataStore key, or `null` if not auto-migratable.
 */
private data class OldStoreInfo(
    val oldBackupKey: String,
    val newPrefix: String?
)

private val OLD_DATASTORE_FILES: Map<String, OldStoreInfo> = mapOf(
    "uiDatastore" to OldStoreInfo("ui", "ui"),
    "colorModeDatastore" to OldStoreInfo("color_mode", "color_modes"),
    "colorDatastore" to OldStoreInfo("color", "color"),
    "drawerDatastore" to OldStoreInfo("drawer", "drawer"),
    "behaviorDatastore" to OldStoreInfo("behavior", "behavior"),
    "wellbeingDatastore" to OldStoreInfo("wellbeing", "wellbeing"),
    "languageDatastore" to OldStoreInfo("language", "language"),
    "backupDatastore" to OldStoreInfo("backup", "backup"),
    "statusDatastore" to OldStoreInfo("status_bar", "status_bar"),
    "debugDatastore" to OldStoreInfo("debug", "debug"),
    "widgetsDatastore" to OldStoreInfo("widgets", "widgets"),
    "workspacesDataStore" to OldStoreInfo("workspaces", "workspaces"),
    "privateSettingsStore" to OldStoreInfo("private", "private"),
    "privateAppsDatastore" to OldStoreInfo("private_apps", null),
    "floatingAppsDatastore" to OldStoreInfo("floating_apps", null),
    "swipePointsDatastore" to OldStoreInfo("new_actions", null),
    "swipeMapDataStore" to OldStoreInfo("swipe_map", null),
    "statusBarJsonDataStore" to OldStoreInfo("status_bar_json", null),
    "AngleLineDatastore" to OldStoreInfo("angle_line", null),
    "HoldTOActivateDatastore" to OldStoreInfo("hold_to_activate", null)
)

/**
 * Silent auto-migrator that reads old 3.2.2 DataStore files and writes their
 * values into the new unified [AppDatastore][io.github.elnix90.core.util.dataStore].
 *
 * This runs on app startup when old DataStore files are detected and the new
 * DataStore is empty or missing (see [isMigrationNeeded]).
 *
 * Stores that require structural transformation (e.g. `new_actions` -> points/nests)
 * are skipped here and handled by [LegacyBackupJsonMigrator] during manual import.
 */
public class DataStoreMigrator {

    /**
     * Reads every old DataStore file that has a 1:1 mapping, applies key/prefix
     * transformations, and writes the result into the new unified DataStore.
     *
     * Stores with structural changes (`splitInto`, `newBackupKey == null`) are
     * skipped – they must be migrated via [LegacyBackupJsonMigrator].
     *
     * @param ctx Android context used to locate old DataStore files.
     * @return [MigrationResult] summary of what was migrated, skipped, or errored.
     */
    public suspend fun migrateFromOldDataStores(ctx: Context): MigrationResult =
        withContext(Dispatchers.IO) {
            val migrated = mutableSetOf<String>()
            val skipped = mutableSetOf<String>()
            val errors = mutableListOf<String>()

            for ((fileName, info) in OLD_DATASTORE_FILES) {
                try {
                    val mapping = OldToNewStoreMapping.mappings[info.oldBackupKey]
                    if (mapping == null || mapping.splitInto.isNotEmpty() || info.newPrefix == null) {
                        skipped.add(fileName)
                        continue
                    }

                    val oldFile = File(ctx.filesDir, "datastore/${fileName}.preferences_pb")
                    if (!oldFile.exists()) {
                        skipped.add(fileName)
                        continue
                    }

                    val oldDataStore = PreferenceDataStoreFactory.create { oldFile }
                    val prefs = try {
                        oldDataStore.data.first()
                    } catch (_: Exception) {
                        emptyPreferences()
                    }

                    if (prefs.asMap().isEmpty()) {
                        skipped.add(fileName)
                        continue
                    }

                    ctx.dataStore.edit { newPrefs ->
                        for ((key, value) in prefs.asMap()) {
                            val newKey = mapping.keyMappings[key.name] ?: key.name
                            val prefixedKey = "${info.newPrefix}_$newKey"
                            val transformed = mapping.valueTransformers[newKey]
                                ?: mapping.valueTransformers[key.name]
                                ?: value
                            @Suppress("UNCHECKED_CAST")
                            newPrefs[keyForType(prefixedKey, transformed)] = transformed
                        }
                    }

                    migrated.add(fileName)

                } catch (e: Exception) {
                    errors.add("[${fileName}] ${e.message}")
                }
            }

            if (errors.isEmpty()) {
                MigrationResult.success(migrated, skipped)
            } else {
                MigrationResult(
                    success = migrated.isNotEmpty(),
                    migratedStores = migrated,
                    skippedStores = skipped,
                    errors = errors,
                    message = "DataStore migration: ${migrated.size} stores migrated, ${errors.size} errors"
                )
            }
        }

    /**
     * Checks whether any old 3.2.2 DataStore files still exist on disk.
     *
     * @param ctx Android context.
     * @return `true` if at least one old DataStore file is present.
     */
    public fun hasOldDataStoreFiles(ctx: Context): Boolean {
        return OLD_DATASTORE_FILES.keys.any { name ->
            File(ctx.filesDir, "datastore/${name}.preferences_pb").exists()
        }
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

/**
 * Resolves the appropriate [Preferences.Key] type for a given value.
 *
 * This enables type-safe storage of mixed-type values read from the old DataStore
 * into the new unified DataStore.
 *
 * @param name The preference key name.
 * @param value The value to infer the key type from.
 * @return A [Preferences.Key] of the matching type.
 */
@Suppress("UNCHECKED_CAST")
private fun <T> keyForType(name: String, value: T): Preferences.Key<T> {
    return when (value) {
        is Boolean -> booleanPreferencesKey(name) as Preferences.Key<T>
        is Int -> intPreferencesKey(name) as Preferences.Key<T>
        is Long -> longPreferencesKey(name) as Preferences.Key<T>
        is Float -> floatPreferencesKey(name) as Preferences.Key<T>
        is Double -> doublePreferencesKey(name) as Preferences.Key<T>
        is String -> stringPreferencesKey(name) as Preferences.Key<T>
        is Set<*> -> stringSetPreferencesKey(name) as Preferences.Key<T>
        else -> stringPreferencesKey(name) as Preferences.Key<T>
    }
}
