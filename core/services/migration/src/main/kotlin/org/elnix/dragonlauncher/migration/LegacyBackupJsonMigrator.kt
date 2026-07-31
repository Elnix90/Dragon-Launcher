package org.elnix.dragonlauncher.migration

import android.content.Context
import androidx.compose.ui.unit.Density
import io.github.elnix90.core.stores.JsonArraySettingsStore
import io.github.elnix90.core.stores.JsonObjectSettingsStore
import io.github.elnix90.core.stores.MapSettingsStore
import io.github.elnix90.core.stores.SettingsStore
import io.github.elnix90.logging.BACKUP_TAG
import io.github.elnix90.logging.logD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.settings.AllStores
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Migrates settings from a legacy 3.2.2 backup JSON file to the current 4.0.0 store format.
 *
 * This migrator handles structural changes between versions:
 * - Store renames (e.g., `color_mode` -> `color_modes`)
 * - Key renames (e.g., `primary_color` -> `primaryColor`)
 * - Store splits (e.g., `new_actions` -> `points` + `nests`)
 * - Store removals (e.g., `floating_apps`)
 * - Value format changes (e.g., drawer enum casing)
 *
 * The migration follows these steps:
 * 1. First, the `new_actions` store is pre-processed by [PointsAndNestsMigrator]
 *    to convert points/nests from UUID-based to integer-based IDs.
 * 2. Then, each remaining store is processed via its [StoreMapping], which
 *    applies key renames, value transformations, and store splits.
 */
public class LegacyBackupJsonMigrator {

    /**
     * Migrates settings from a legacy backup JSON string.
     *
     * Parses the JSON string and delegates to [migrateFromJsonObject].
     *
     * @param ctx Android context for DataStore access.
     * @param legacyJson The raw JSON string from a 3.2.2 backup file.
     * @return [MigrationResult] describing what was migrated.
     */
    public suspend fun migrateFromJson(
        ctx: Context,
        legacyJson: String
    ): MigrationResult = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject(legacyJson)
            migrateFromJsonObject(ctx, json)
        } catch (e: Exception) {
            MigrationResult.failure("Failed to parse legacy JSON: ${e.message}", listOfNotNull(e.message))
        }
    }

    /**
     * Migrates settings from a parsed legacy backup JSON object.
     *
     * Orchestrates the full migration:
     * 1. Pre-processes `new_actions` via [PointsAndNestsMigrator] and writes results.
     * 2. Iterates all [OldToNewStoreMapping] entries, applying transformations and
     *    writing to the new store system.
     *
     * @param ctx Android context for DataStore access.
     * @param json The parsed JSON object from a 3.2.2 backup.
     * @return [MigrationResult] describing what was migrated.
     */
    public suspend fun migrateFromJsonObject(
        ctx: Context,
        json: JSONObject
    ): MigrationResult = withContext(Dispatchers.IO) {
        val migrated = mutableSetOf<String>()
        val skipped = mutableSetOf<String>()
        val errors = mutableListOf<String>()

        val density = Density(ctx.resources.displayMetrics.density)

        val newStores = AllStores.associateBy { it.name }

        val oldNewActions = json.optJSONObject("new_actions")
        if (oldNewActions != null) {
            try {
                val migratedData = PointsAndNestsMigrator.migrate(oldNewActions, density)
                newStores["nests"]?.let { writeToStore(ctx, it, migratedData.newNests) }
                newStores["points"]?.let { writeToStore(ctx, it, migratedData.newPoints) }
                migratedData.newDefaultPoint?.let { dp ->
                    newStores["default_point"]?.let { writeToStore(ctx, it, dp) }
                }
                migrated.add("new_actions")
            } catch (e: Exception) {
                errors.add("[new_actions] Points/Nests migration failed: ${e.message}")
            }
        }

        for (mapping in OldToNewStoreMapping.mappings.values) {
            try {
                val oldData = json.optJSONObject(mapping.oldBackupKey)
                    ?: json.optJSONArray(mapping.oldBackupKey)
                    ?: run {
                        if (mapping.splitInto.isNotEmpty()) {
                            json.optJSONObject(mapping.oldBackupKey)
                        } else null
                    }

                if (oldData == null) {
                    skipped.add(mapping.oldBackupKey)
                    continue
                }

                if (mapping.splitInto.isNotEmpty()) {
                    for ((newKey, extractor) in mapping.splitInto) {
                        val extractedValue = when (oldData) {
                            is JSONObject -> extractor(oldData)
                            is JSONArray -> {
                                if (newKey == mapping.oldBackupKey) oldData else null
                            }

                            else -> null
                        }
                        if (extractedValue != null) {
                            val targetStore = newStores[newKey]
                            if (targetStore != null) {
                                writeToStore(ctx, targetStore, extractedValue)
                                migrated.add("${mapping.oldBackupKey}->$newKey")
                            }
                        }
                    }
                    continue
                }

                val newBackupKey = mapping.newBackupKey ?: run {
                    skipped.add(mapping.oldBackupKey)
                    continue
                }

                val targetStore = newStores[newBackupKey]
                if (targetStore == null) {
                    skipped.add(mapping.oldBackupKey)
                    continue
                }

                val transformedData = when (oldData) {
                    is JSONObject -> applyKeyMapping(oldData, mapping)
                    is JSONArray -> oldData
                    else -> oldData
                }

                writeToStore(ctx, targetStore, transformedData)
                migrated.add(mapping.oldBackupKey)

            } catch (e: Exception) {
                errors.add("[${mapping.oldBackupKey}] ${e.message}")
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
                message = "Migrated ${migrated.size} stores with ${errors.size} errors"
            )
        }
    }

    /**
     * Applies key mappings and value transformations to a single store's JSON object.
     *
     * - Keys listed in [StoreMapping.skipKeys] are dropped.
     * - Keys listed in [StoreMapping.keyMappings] are renamed.
     * - Keys listed in [StoreMapping.splitInto] are excluded (handled separately).
     * - Values are optionally transformed via [StoreMapping.valueTransformers].
     *
     * @param source The old store JSON object.
     * @param mapping The mapping definition for this store.
     * @return A new JSON object with transformed keys and values.
     */
    private fun applyKeyMapping(
        source: JSONObject,
        mapping: StoreMapping
    ): JSONObject {
        val result = JSONObject()

        for (key in source.keys()) {
            if (key in mapping.skipKeys) continue

            val newKey = mapping.keyMappings[key] ?: key
            val value = source.get(key)

            if (key in mapping.splitInto) continue

            val transformed = mapping.valueTransformers[newKey]?.invoke(value)
                ?: mapping.valueTransformers[key]?.invoke(value)
                ?: value

            result.put(newKey, transformed)
        }

        return result
    }

    /**
     * Writes a migrated value to the appropriate [SettingsStore].
     *
     * Dispatches based on the store type:
     * - [MapSettingsStore]: imports a `JSONObject` directly.
     * - [JsonArraySettingsStore]: wraps the value into a `JSONArray` if needed.
     * - [JsonObjectSettingsStore]: wraps the value into a `JSONObject` if needed
     *   (arrays are placed under a `"data"` key).
     *
     * @param ctx Android context.
     * @param store The target store to write to.
     * @param value The value to import (JSONObject, JSONArray, or null).
     */
    private suspend fun writeToStore(
        ctx: Context,
        store: SettingsStore<*, *>,
        value: Any?
    ) {
        @Suppress("UNCHECKED_CAST")
        when (store) {
            is MapSettingsStore -> {
                if (value is JSONObject) {
                    logD(BACKUP_TAG) { "Importing ${value.length()}\n$value\n keys to ${store.name}"}
                    store.importFromBackup(ctx, value)
                } else {
                    logD(BACKUP_TAG) { "Value should have been JsonObject but is ${if (value==null) null else value::class.simpleName} to ${store.name}"}
                }
            }

            is JsonArraySettingsStore -> {
                val array = when (value) {
                    is JSONArray -> value
                    is JSONObject -> {
                        if (value.length() > 0) {
                            val arr = JSONArray()
                            arr.put(value)
                            arr
                        } else null
                    }

                    else -> null
                }
                if (array != null) {
                    logD(BACKUP_TAG) { "Importing an array of length ${array.length()}\n$value\n to ${store.name}"}
                    store.importFromBackup(ctx, array)
                } else {
                    logD(BACKUP_TAG) { "Value should have been JSONArray but is ${if (value==null) null else value::class.simpleName} to ${store.name}"}
                }
            }

            is JsonObjectSettingsStore -> {
                val obj = when (value) {
                    is JSONObject -> if (value.length() > 0) value else null
                    is JSONArray -> {
                        if (value.length() > 0) {
                            val o = JSONObject()
                            o.put("data", value)
                            o
                        } else null
                    }

                    else -> null
                }
                if (obj != null) {
                    logD(BACKUP_TAG) { "Importing an object to ${store.name}"}
                    store.importFromBackup(ctx, obj)
                } else {
                    logD(BACKUP_TAG) { "Value should have been JSONObject but is ${if (value==null) null else value::class.simpleName} to ${store.name}"}
                }
            }
        }
    }

    /**
     * Reads a legacy backup file from the app's assets.
     *
     * @param ctx Android context.
     * @param assetPath Path to the asset file (e.g., `"backup-3.2.2-witness.json"`).
     * @return The file content as a string.
     */
    public suspend fun readFromAssets(ctx: Context, assetPath: String): String = withContext(Dispatchers.IO) {
        ctx.assets.open(assetPath).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).readText()
        }
    }

    /**
     * Checks whether the given JSON object represents a legacy 3.2.2 backup.
     *
     * Detection logic:
     * 1. If `app_version` starts with `"3.2.2"` — it's a legacy backup.
     * 2. If it has `"new_actions"` or `"swipe_map"` keys — it's a legacy backup.
     *
     * @param json The parsed backup JSON.
     * @return `true` if this appears to be a legacy 3.2.2 backup.
     */
    public fun isLegacyBackup(json: JSONObject): Boolean {
        val version = json.optString("app_version", "")
        if (version.startsWith(OldToNewStoreMapping.legacyAppVersionPrefix)) return true
        return json.has("new_actions") || json.has("swipe_map")
    }
}
