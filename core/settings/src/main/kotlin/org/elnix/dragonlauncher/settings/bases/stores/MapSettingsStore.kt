package org.elnix.dragonlauncher.settings.bases.stores

import android.content.Context
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.putIfNonDefault
import org.json.JSONObject

/**
 * Settings store backed by multiple independent DataStore keys.
 *
 * `MapSettingsStore` is the standard implementation used for most settings groups,
 * where each setting is stored under its own DataStore preference key and exposed
 * collectively as a `Map<String, Any?>`.
 *
 * Characteristics:
 * - Each entry in [ALL] represents a single persisted setting.
 * - The map key corresponds to `BaseSettingObject.key`.
 * - Values are read and written individually, not as a single blob.
 * - Import/export operates on raw values and relies on each `BaseSettingObject`
 *   to decode and validate its own type.
 * - Exports data in a [JSONObject] via [getAll]
 *
 * This design enables:
 * - fine-grained persistence (only changed keys are written)
 * - backward-compatible imports (unknown keys are ignored)
 * - safe type coercion during restore via `BaseSettingObject.decode`
 */
abstract class MapSettingsStore(
    override val dataStoreName: DataStoreName
) : BaseSettingsStore<Map<String, Any?>, JSONObject>(dataStoreName) {

    /**
     * Reads all settings from DataStore and returns them as a map.
     *
     * Missing keys fall back to each setting’s default value.
     */
    override suspend fun getAll(ctx: Context): Map<String, Any> =
        buildMap {
            ALL.forEach { setting ->
                putIfNonDefault(setting.key, setting.getEncoded(ctx), setting.default)
            }
        }

    /**
     * Writes all provided values to DataStore.
     *
     * Each value is decoded individually using the corresponding
     * `BaseSettingObject.decode` implementation before being persisted.
     *
     * Unknown or missing keys are ignored.
     */
    override suspend fun setAll(ctx: Context, value: Map<String, Any?>) {
        ALL.forEach { setting ->
            val raw = value[setting.key]
//            logI(SETTINGS_TAG) { "Raw : $raw" }
            val typedValue = setting.decode(raw)

//            logI(SETTINGS_TAG) { "Typed value : $typedValue" }
            setting.setAny(ctx, typedValue)
        }
    }

    /**
     * Exports all settings into a single [JSONObject] for backup purposes.
     */
    override suspend fun exportForBackup(ctx: Context): JSONObject? {

        val json = getAll(ctx)
        return if (json.isNotEmpty()) {
            JSONObject(json)
        } else null
    }

    /**
     * Restores settings from a [JSONObject] backup.
     *
     * Only keys present in [ALL] are applied; unknown keys are safely ignored.
     * Each value is decoded and validated by its corresponding `BaseSettingObject`.
     */
    override suspend fun importFromBackup(ctx: Context, json: JSONObject?) {
        json?.keys()?.forEach { key ->
            ALL.find { it.key == key }?.let { setting ->
                val raw = json.opt(key)
                val typedValue = setting.decode(raw)

//                logW(SETTINGS_TAG, "[IMPORT FROM BACKUP] Raw : $raw; Typed value : $typedValue")
                setting.setAny(ctx, typedValue)
            }
        }
    }
}
