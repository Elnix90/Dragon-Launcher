package org.elnix.dragonlauncher.settings.bases.objects

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import org.elnix.dragonlauncher.logging.BACKUP_TAG
import org.elnix.dragonlauncher.logging.SETTINGS_TAG
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.logging.logV
import org.elnix.dragonlauncher.logging.logW
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.resolveDataStore
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi


/**
 * Abstract base class for strongly-typed settings persisted in [androidx.datastore.core.DataStore].
 *
 * Provides a consistent API for getting/setting individual settings with type-safe encoding/decoding,
 * reactive flows for UI observation, and change callbacks.
 *
 * @param TYPED The strongly-typed value type of this setting (e.g., `Boolean`, `String`, custom data class).
 * @param ENCODED The raw [Preferences.Key] value type stored in DataStore (e.g., `Boolean`, `String`).
 */
@OptIn(ExperimentalAtomicApi::class)
abstract class SettingObject<TYPED, ENCODED> {
    /**
     * Unique identifier for this setting.
     * It is auto inferred via the [SettingObject] builders functions during at compile-time via the [Settings compiler plugin](https://github.com/Elnix90/Settings-Plugin)
     */
    abstract val key: String

    /**
     * The title of this setting.
     * It's the ressource ID that links to an i18n string, that android resolves depending on the app language
     * Used in Compose to automatically infer title via the property instead of manually specifying them for all settings
     * Can be null for specific settings, that aren't meant to be directly toggled or changed in Compose
     */
    abstract val title: Int?

    /**
     * Same as [title] but it's the description
     */
    abstract val description: Int?

    /**
     * Target [DataStoreName] where this setting is persisted.
     */
    abstract val dataStoreName: DataStoreName

    /**
     * Fallback value when no persisted value exists.
     * Initial value that takes the object when first initialized
     * @see _cachedValue
     */
    abstract val default: TYPED

    /**
     * DataStore key used for storage/retrieval.
     */
    abstract val preferenceKey: Preferences.Key<ENCODED>

    /**
     * Converts [TYPED] → [R?] for DataStore persistence (returns `null` to remove setting).
     */
    abstract fun encode(value: TYPED): ENCODED?

    /**
     * Converts raw DataStore value → [TYPED].
     */
    abstract fun decode(raw: Any?): TYPED

    /**
     * Optional callback invoked after successful set/reset operations.
     */
    abstract var onChanged: (() -> Unit)?

    /**
     * Whether if this setting should be added to the backup or not.
     * It is always added when the parameter `forceAllKeys` is `true` during an export
     * @see org.elnix.dragonlauncher.settings.bases.stores.SettingsStore
     */
    abstract val backupable: Boolean


    /**
     * Private cache to avoid fetching value from the Datastore every time
     * The cache initializes to the [default] provided value, and should be **loaded** when someone subscribe to the [flow], or [get] the value
     *
     * Initialized **lazily** because it caused crashes when tried to be accessed in early initialization time. Using the lazy shouldn't cost much and allow the parameter ([default]) to be loaded before the [MutableStateFlow] initializes
     */
    private val _cachedValue: MutableStateFlow<TYPED> by lazy {
        MutableStateFlow(default)
    }

    /**
     * Internal value to track whether the value has been loaded from the datastore or not.
     */
    private var isInitialized = AtomicBoolean(false)


    /**
     * Internally loads the value from the datastore if not already
     *
     * @return [TYPED] value decoded from the Datastore
     */
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

        _cachedValue.value = decoded

        isInitialized.store(true)
        return _cachedValue.value
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


    /**
     * Get the value one shot for logic, no flow
     * Returns null if the value is not defined ([default])
     *
     * @return [TYPED]? decoded nullable value
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
    suspend fun get(ctx: Context): TYPED {
        return if (isInitialized.compareAndSet(expectedValue = false, newValue = true)) {
            val loaded = loadValue(ctx)
            assert(loaded != null)
            loaded
        } else {
            val loaded = _cachedValue.value
            assert(loaded != null)
            loaded
        }
    }


    /**
     * Returns the value encoded for the backup
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
    fun flow(ctx: Context): Flow<TYPED> =
        _cachedValue
            .asStateFlow()
            .onStart {
                if (isInitialized.compareAndSet(expectedValue = false, newValue = true)) {
                    loadValue(ctx)
                }
            }

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

            if (value == _cachedValue.value) {
                logV(SETTINGS_TAG) { "Value already equals to the one in settings, no need to change" }
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
     * Removes the value of the [preferenceKey] from the datastore and sets its cached value to [default]
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
