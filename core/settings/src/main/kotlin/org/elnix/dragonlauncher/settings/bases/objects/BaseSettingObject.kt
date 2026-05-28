package org.elnix.dragonlauncher.settings.bases.objects

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.BACKUP_TAG
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.logging.logV
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.DatastoreProvider
import org.elnix.dragonlauncher.settings.resolveDataStore


/**
 * Abstract base class for strongly-typed settings persisted in [androidx.datastore.core.DataStore].
 *²
 * Provides a consistent API for getting/setting individual settings with type-safe encoding/decoding,
 * reactive flows for UI observation, and change callbacks. Extends [AnySettingObject] for use in
 * heterogeneous collections like [org.elnix.dragonlauncher.settings.bases.stores.BaseSettingsStore.ALL].
 *
 * @param T The strongly-typed value type of this setting (e.g., `Boolean`, `String`, custom data class).
 * @param R The raw [Preferences.Key] value type stored in DataStore (e.g., `Boolean`, `String`).
 * @param key Unique identifier for this setting.
 * @param dataStoreName Target [DataStoreName] where this setting is persisted.
 * @param default Fallback value when no persisted value exists.
 * @param preferenceKey DataStore key used for storage/retrieval.
 * @param encode Converts [T] → [R?] for DataStore persistence (returns `null` to remove setting).
 * @param decode Converts raw DataStore value → [T].
 * @param onChanged Optional callback invoked after successful set/reset operations.
 */
class BaseSettingObject<T, R>(
    override val key: String,
    val dataStoreName: DatastoreProvider,
    val default: T,
    private val preferenceKey: Preferences.Key<R>,
    val encode: (T) -> R?,
    val decode: (Any?) -> T,
    var onChanged: (() -> Unit)?
) : AnySettingObject {


    /**
     * Returns the current value of this setting in a type-erased form.
     *
     * This method is part of the type-erased settings API and allows heterogeneous
     * collections of settings to be accessed without knowing their concrete generic
     * type parameters at compile time.
     *
     * Internally, this simply delegates to [get], preserving the original value,
     * but exposes it as [Any?] so it can be used in generic containers such as
     * maps or lists of mixed setting types.
     *
     * @param ctx Android context used to access the underlying data store.
     * @return The current value of this setting, or its default value if none
     *         has been persisted yet.
     */
    override suspend fun getAny(ctx: Context) = get(ctx)

    /**
     * Sets the value of this setting using a type-erased input.
     *
     * This method exists to support bulk operations (such as restore, import,
     * or map-based updates) where the concrete generic type of the setting is
     * not known at compile time.
     *
     * The provided [value] is first cast to the raw representation type [R],
     * then converted into the setting's strongly-typed value using [decode],
     * and finally persisted via [set].
     *
     * @param ctx Android context used to access the underlying data store.
     * @param value The raw, type-erased value to apply to this setting.
     *
     * @throws ClassCastException if [value] is not of the expected raw type [R].
     */
    override suspend fun setAny(ctx: Context, value: Any?) {
        @Suppress("UNCHECKED_CAST")
        set(ctx, value as? T)
    }


    /**
     * Get the value one shot for logic, no flow
     * Returns null if the value is not defined (default)
     *
     * @param ctx
     * @return decoded nullable value of settings type [T?]
     */
    suspend fun getOrNull(ctx: Context): T? {

        val raw = ctx.applicationContext
            .resolveDataStore(dataStoreName)
            .data
            .first()[preferenceKey]

        return raw?.let {
            try {
                decode(it)
            } catch (e: Exception) {
                logE(BACKUP_TAG, e) { "FAILED decoding setting: $key" }
                null
            }
        }
    }


    /**
     * Get the value one shot for logic, no flow
     *
     * @param ctx
     * @return decoded value of settings type [T]
     */
    suspend fun get(ctx: Context): T = getOrNull(ctx) ?: default


    /**
     * Get the value one shot for logic, no flow
     *
     * @param ctx
     * @return decoded value of settings type [T]
     */
    suspend fun getEncoded(ctx: Context): R? {

        val raw = ctx.applicationContext
            .resolveDataStore(dataStoreName)
            .data
            .first()[preferenceKey]

        // Shitty but should work
        // After reviewing this, I find it even mores shitier,
        // but I really don't want to touch that, as it works.
        // if I touch this, it'll break the whole app
        return raw?.let {
            try {
                encode(decode(it))
            } catch (e: Exception) {
                logE(BACKUP_TAG, e) { "FAILED encoding setting: $key" }
                null
            }
        }
    }


    /**
     * Outputs a flow of the value, for compose
     *
     * @param ctx
     * @return [Flow] of the settings type [T]
     */
    fun flow(ctx: Context): Flow<T> {
        return ctx.applicationContext
            .resolveDataStore(dataStoreName)
            .data
            .map { prefs ->
                val raw = prefs[preferenceKey]
                raw?.let {
                    decode(it)
                } ?: default
            }
            .catch { e ->
                logE(BACKUP_TAG, e) { "FAILED reading setting: $key" }

                emit(default)
            }
    }


    /**
     * Saves the value in the datastore for persistence
     *
     * @param ctx
     * @param value either the good type or a null, to reset
     */
    suspend fun set(ctx: Context, value: T?) {
        try {
            ctx.applicationContext
                .resolveDataStore(dataStoreName).edit {

                    if (value != null) {
                        val encoded = encode(value)
                        encoded?.let { encodedNotNull ->
                            it[preferenceKey] = encodedNotNull
                        } ?: it.remove(preferenceKey)
                    } else {
                        it.remove(preferenceKey)
                    }
                }

            logV(BACKUP_TAG) { "Setting changed: $key" }
            onChanged?.invoke()

        } catch (e: Exception) {
            logE(BACKUP_TAG, e) { "FAILED persisting setting: $key" }
        }
    }


    /**
     * Removes the value of the [preferenceKey] from the datastore
     * it will use the default value since nothing will be found in the datastore
     *
     * @param ctx
     */
    suspend fun reset(ctx: Context) {
        ctx.resolveDataStore(dataStoreName).edit {
            it.remove(preferenceKey)
        }

        logV(BACKUP_TAG) { "Setting has been reset: $key" }
        onChanged?.invoke()
    }
}
