package org.elnix.dragonlauncher.settings.bases.stores

import android.content.Context
import org.elnix.dragonlauncher.logging.BACKUP_TAG
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.objects.StringSettingObject
import org.json.JSONArray
import org.json.JSONException

/**
 * Settings store backed by a single JSON value.
 *
 * `JsonSettingsStore` is used for settings groups that are persisted as one
 * serialized JSON blob rather than as multiple independent preference keys.
 * Typical use cases include complex or hierarchical data structures such as
 * workspaces, layouts, or app configurations.
 *
 * Characteristics:
 * - All data is stored under a single `SettingObject<String>` ([jsonSetting]).
 * - The persisted value is a JSON string, decoded to/from a [JSONArray].
 * - Updates are atomic: the whole JSON payload is written at once.
 * - Import/export is a direct pass-through of the JSON structure.
 *
 * Compared to [MapSettingsStore], this approach:
 * - simplifies persistence of nested or schema-flexible data
 * - trades fine-grained updates for easier serialization
 */
abstract class JsonArraySettingsStore(
    final override val dataStoreName: DataStoreName
) : BaseSettingsStore<JSONArray?, JSONArray>(dataStoreName) {

    /**
     * Underlying setting that stores the JSON payload as a raw string.
     */
    val jsonSetting = StringSettingObject(
        key = "jsonSetting",
        default = "",
        title = null,
        description = null,
        dataStoreName = dataStoreName,
        onChanged = null
    )


    final override val ALL: List<BaseSettingObject<*, *>>
        get() = listOf(jsonSetting)

    /**
     * Reads the JSON string from DataStore and parses it into a [JSONArray].
     */
    final override suspend fun getAll(ctx: Context, forceAllKeys: Boolean): JSONArray? {
        // Skips if default value provided (no changes made), keep the backup lighter
        val raw = jsonSetting.getEncoded(ctx)?.trim() ?: return null

        return try {
            if (raw.isEmpty()) null else JSONArray(raw)
        } catch (e: JSONException) {
            logE(BACKUP_TAG, e) { "Error while creating json object of backup" }
            null
        }
    }


    /**
     * Serializes and writes the provided [JSONArray] into DataStore.
     */
    final override suspend fun setAll(ctx: Context, value: JSONArray?) {
        jsonSetting.set(ctx, value.toString())
    }

    /**
     * Exports the current JSON payload for backup.
     *
     * Since the store is already JSON-backed, this is a direct passthrough.
     */
    final override suspend fun exportForBackup(ctx: Context, forceAllKeys: Boolean): JSONArray? =
        getAll(ctx, forceAllKeys)

    /**
     * Restores the store from a JSON backup.
     *
     * The provided [JSONArray] fully replaces the current stored value.
     */
    final override suspend fun importFromBackup(ctx: Context, json: JSONArray?) {
        setAll(ctx, json)
    }
}
