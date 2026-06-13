package org.elnix.dragonlauncher.settings.bases.objects

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import org.elnix.dragonlauncher.logging.BACKUP_TAG
import org.elnix.dragonlauncher.logging.SETTINGS_TAG
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.logging.logV
import org.elnix.dragonlauncher.logging.logW
import org.elnix.dragonlauncher.logging.logWtf
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.resolveDataStore


/**
 * Abstract base class for strongly-typed settings persisted in [androidx.datastore.core.DataStore].
 *
 * Provides a consistent API for getting/setting individual settings with type-safe encoding/decoding,
 * reactive flows for UI observation, and change callbacks.
 *
 * @param TYPED The strongly-typed value type of this setting (e.g., `Boolean`, `String`, custom data class).
 * @param ENCODED The raw [Preferences.Key] value type stored in DataStore (e.g., `Boolean`, `String`).
 * @param key Unique identifier for this setting.
 * @param dataStoreName Target [DataStoreName] where this setting is persisted.
 * @param default Fallback value when no persisted value exists.
 * @param preferenceKey DataStore key used for storage/retrieval.
 * @param encode Converts [TYPED] → [R?] for DataStore persistence (returns `null` to remove setting).
 * @param decode Converts raw DataStore value → [TYPED].
 * @param onChanged Optional callback invoked after successful set/reset operations.
 */
sealed class BaseSettingObject<TYPED, ENCODED> {
    abstract val key: String
    abstract val title: Int?
    abstract val description: Int?
    abstract val dataStoreName: DataStoreName
    abstract val default: TYPED
    abstract val preferenceKey: Preferences.Key<ENCODED>
    abstract fun encode(value: TYPED): ENCODED?
    abstract fun decode(raw: Any?): TYPED
    abstract var onChanged: (() -> Unit)?

    //    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _cachedValue: MutableStateFlow<TYPED> = MutableStateFlow(default)
    val value: TYPED
        get() = _cachedValue.value ?: default

    private suspend fun loadValue(ctx: Context): TYPED {

        val raw: ENCODED? = ctx
            .applicationContext
            .resolveDataStore(dataStoreName)
            .data
            .first()[preferenceKey]

        val decoded: TYPED = raw?.let {
            try {
                decode(it)
            } catch (e: Exception) {
                logE(BACKUP_TAG, e) { "FAILED decoding setting: $key" }
                null
            }
        } ?: default

        logWtf(SETTINGS_TAG) { "Decoded value for $key: $decoded" }
        requireNotNull(decoded) { "Decoded value for $key must not be null!" }
        _cachedValue.value = decoded
        return decoded
    }

    /**
     * Sets the value of this setting using a type-erased input.
     *
     * This method exists to support bulk operations (such as restore, import,
     * or map-based updates) where the concrete generic type of the setting is
     * not known at compile time.
     *
     * The provided [value] is first cast to the raw representation type [ENCODED],
     * then converted into the setting's strongly-typed value using [decode],
     * and finally persisted via [set].
     *
     * @param ctx Android context used to access the underlying data store.
     * @param value The raw, type-erased value to apply to this setting.
     *
     * @throws ClassCastException if [value] is not of the expected raw type [ENCODED].
     */
    suspend fun setAny(ctx: Context, value: Any?) {
        @Suppress("UNCHECKED_CAST")
        val value = value as? TYPED
        if (value != null) {
            set(ctx, value)
        } else {
            reset(ctx)
        }
    }

    init {
//            requireNotNull(key) { "Key for $this is null"}
//            requireNotNull(default) { "StringSettingObject $key initialized with null default: $default" }
        logWtf(SETTINGS_TAG) { "StringSettingObject $key initialized with null default: $default" }
    }

    /**
     * Get the value one shot for logic, no flow
     * Returns null if the value is not defined (default)
     *
     * @return `T?` decoded nullable value
     */
    suspend fun getOrNull(ctx: Context): TYPED? {
        val value = get(ctx)
        return if (value != default) value else null
    }

    /**
     * Get the value one shot for logic, no flow
     *
     * @param ctx
     * @return decoded value of settings type [TYPED]
     */
    suspend fun get(ctx: Context): TYPED =
        _cachedValue.value ?: run {
            error("FUCKING CACHED VALUE WAS NULLL WHYYYY")
//                loadValue(ctx)
            default
        }

    /**
     * Returns the value encoded for the
     *
     * @param ctx
     * @return decoded value of settings type [TYPED]
     */
    suspend fun getEncoded(ctx: Context): ENCODED? =
        get(ctx)?.let {
            try {
                encode(it)
            } catch (e: Exception) {
                logE(BACKUP_TAG, e) { "FAILED encoding setting: $key" }
                null
            }
        }

    /**
     * Outputs a flow of the value, for compose
     *
     * @return [Flow] of the settings type [TYPED]
     */
//        fun flow(ctx: Context): Flow<T> =
//            _cachedValue
//                .map { it ?: default }
//                .distinctUntilChanged()


    /**
     * Returns a StateFlow that emits whenever the value changes.
     * Load happens on first access to the flow.
     */
    fun flow(ctx: Context): StateFlow<TYPED> = _cachedValue.asStateFlow()

    /**
     * Saves the value in the datastore for persistence
     *
     * @param ctx
     * @param value
     */
    suspend fun set(ctx: Context, value: TYPED?) {
        try {
            if (value == null) {
                logV(SETTINGS_TAG) { "Null setting received, resetting it" }
                reset(ctx)
                return
            }

            val encoded = encode(value)
            if (encoded == null) {
                logW(SETTINGS_TAG) { "FAILED to encode value, resetting it" }
                reset(ctx)
                return
            }

            ctx.resolveDataStore(dataStoreName).edit {
                it[preferenceKey] = encoded
            }

            logV(SETTINGS_TAG) { "Setting changed: $key" }

            _cachedValue.value = value
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
        try {
            ctx.resolveDataStore(dataStoreName).edit {
                it.remove(preferenceKey)
            }
            _cachedValue.value = default
            onChanged?.invoke()
        } catch (e: Exception) {
            logE(BACKUP_TAG, e) { "FAILED resetting setting: $key" }
        }
    }
}
