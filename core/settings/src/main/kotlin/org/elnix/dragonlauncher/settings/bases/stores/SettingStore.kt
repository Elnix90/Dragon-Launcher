package org.elnix.dragonlauncher.settings.bases.stores

import android.content.Context
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.SettingObject
import org.json.JSONArray
import org.json.JSONObject

/**
 * Base abstract class for a collection of related settings in a [androidx.datastore.core.DataStore].
 *
 * A `BaseSettingsStore` represents a **group of settings** (a settings **Store**) and provides a consistent API to:
 *   - access all settings in the store,
 *   - reset them,
 *   - get/set the entire store as a single object,
 *   - import/export for backup.
 *
 * @param T The aggregate type representing the values of the entire store.
 * @param B The aggregate type representing the backup type,I use 2 backup types: [JSONObject], [JSONArray], which is used across the app to store conveniently data
 * @param dataStoreName Identifier of the [androidx.datastore.core.DataStore] used to persist these settings.
 *
 * ### Responsibilities
 * - Defines a list of all contained settings via [ALL], auto inferred by the [Settings compiler plugin](https://github.com/Elnix90/Settings-Plugin)
 * - Provides utility methods to reset all settings to their defaults.
 * - Requires concrete implementations to provide methods for reading/writing all settings at once ([getAll], [setAll]).
 * - Supports backup and restore via ([exportForBackup], [importFromBackup]).
 *
 */
sealed class SettingsStore<T, B>(
    open val dataStoreName: DataStoreName
) {
    /**
     * Lambda use to detect if a setting was changed, and redirect them to the backup manager, in order to trigger a backup.
     * The value is constructed by applying all [SettingObject.onChanged] lambdas to this one.
     * This way, on any settings changed, this lambda is triggered and I don't need to list ALL the settings in the app
     */
    var onAnySettingChanged: (() -> Unit)? = null
        set(value) {
            field = value
            ALL.forEach {
                // Skips settings who have their onChange already defined, in order to prevent the backup to trigger when they do
                if (it.onChanged == null) {
                    it.onChanged = value
                }
            }
        }

    /**
     * List of all individual settings in this store.
     *
     * Each item must be a concrete instance of [SettingObject].
     * This list is used for operations like [resetAll].
     */
    @Suppress("PropertyName")
    abstract val ALL: List<SettingObject<*, *>>

    /**
     * Resets all settings in this store to their default values.
     *
     * @param ctx The Android [Context] required to access the underlying DataStore.
     */
    open suspend fun resetAll(ctx: Context) {
        ALL.forEach { it.reset(ctx) }
    }

    /**
     * Reads the current state of all settings in this store and returns it in the form of the store's backup type [T]
     *
     * @param ctx The Android [Context] required to access the underlying DataStore.
     * @param forceAllKeys whether to get the settings that haven't been changed in the backup, the defaults
     * @return The aggregate state of type [T].
     */
    abstract suspend fun getAll(ctx: Context, forceAllKeys: Boolean): T

    /**
     * Writes the given aggregate state to all settings in this store.
     *
     * @param ctx The Android [Context] required to access the underlying DataStore.
     * @param value The new state to write to all settings.
     */
    abstract suspend fun setAll(ctx: Context, value: T)

    /**
     * Exports the current state of all settings as a [B] type object for backup purposes.
     *
     * @param ctx The Android [Context] required to access the underlying DataStore.
     * @return A [B] representing all settings in the store's type, or `null` if nothing to export.
     */
    abstract suspend fun exportForBackup(ctx: Context, forceAllKeys: Boolean): B?

    /**
     * Imports settings from a [B] type backup.
     *
     * @param ctx The Android [Context] required to access the underlying DataStore.
     * @param json The [B] containing backup values.
     */
    abstract suspend fun importFromBackup(ctx: Context, json: B?)
}
