package org.elnix.dragonlauncher.settings.bases

import android.content.Context
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.logging.logI
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.BACKUP_TAG
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.WORKSPACES_TAG
import org.json.JSONException
import org.json.JSONObject

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
 * - The persisted value is a JSON string, decoded to/from a [JSONObject].
 * - Updates are atomic: the whole JSON payload is written at once.
 * - Import/export is a direct pass-through of the JSON structure.
 *
 * Compared to [MapSettingsStore], this approach:
 * - simplifies persistence of nested or schema-flexible data
 * - trades fine-grained updates for easier serialization
 */
abstract class JsonObjectSettingsStore :
    BaseSettingsStore<JSONObject?, JSONObject>() {

    /**
     * Underlying setting that stores the JSON payload as a raw string.
     */
    abstract val jsonSetting: BaseSettingObject<String, String>


    /**
     * Reads the JSON string from DataStore and parses it into a [JSONObject].
     */
    override suspend fun getAll(ctx: Context): JSONObject? {
        // Skips if default value provided (no changes made), keeps the backup lighter
        val raw = jsonSetting.getEncoded(ctx)?.trim() ?: return null

//        logI(WORKSPACES_TAG) { raw }
        return try {
            if (raw.isEmpty()) null else JSONObject(raw)
        } catch (e: JSONException) {
            logE(BACKUP_TAG, e) { "Error while creating json object of backup" }
            null
        }
    }



    /**
     * Serializes and writes the provided [JSONObject] into DataStore.
     */
    override suspend fun setAll(ctx: Context, value: JSONObject?) {
        logI(WORKSPACES_TAG) { value?.toString() ?: "null" }
        jsonSetting.set(ctx, value?.toString())
    }

    /**
     * Exports the current JSON payload for backup.
     *
     * Since the store is already JSON-backed, this is a direct passthrough.
     */
    override suspend fun exportForBackup(ctx: Context): JSONObject? =
        getAll(ctx)

    /**
     * Restores the store from a JSON backup.
     *
     * The provided [JSONObject] fully replaces the current stored value.
     */
    override suspend fun importFromBackup(ctx: Context, json: JSONObject?) {
        setAll(ctx, json)
    }
}
