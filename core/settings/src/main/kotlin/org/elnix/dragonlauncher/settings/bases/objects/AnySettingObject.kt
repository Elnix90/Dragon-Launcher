package org.elnix.dragonlauncher.settings.bases.objects

import android.content.Context

/**
 * Type-erased interface for individual settings.
 *
 * Enables polymorphic access to settings in heterogeneous collections like [org.elnix.dragonlauncher.settings.bases.stores.BaseSettingsStore.ALL].
 * Concrete implementations are [BaseSettingObject] instances providing type-safe operations
 * while exposing `Any?` variants for generic operations like backup/restore and bulk reset.
 *
 * @property key Unique identifier shared with [BaseSettingObject].
 */
internal interface AnySettingObject {
    val key: String

    /**
     * Retrieves current value as `Any?` for generic access.
     *
     * Delegates to concrete typed `get()` but erases return type for use in maps/lists.
     *
     * @param ctx Android [Context] for DataStore access.
     * @return Current value or default, as `Any?`.
     */
    suspend fun getAny(ctx: Context): Any?

    /**
     * Sets value from `Any?` input for generic mutations.
     *
     * Converts input → concrete type → persists via typed `set()`. Used for bulk operations.
     *
     * @param ctx Android [Context] for DataStore access.
     * @param value Raw value to apply (cast to concrete type internally).
     * @throws ClassCastException if value incompatible with concrete type.
     */
    suspend fun setAny(ctx: Context, value: Any?)
}