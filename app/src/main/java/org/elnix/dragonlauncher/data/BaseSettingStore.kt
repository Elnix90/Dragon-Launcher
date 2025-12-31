package org.elnix.dragonlauncher.data

import android.content.Context
import org.json.JSONObject

abstract class BaseSettingsStore<T> {
    abstract val name: String

    abstract suspend fun resetAll(ctx: Context)
    abstract suspend fun getAll(ctx: Context): T
    abstract suspend fun setAll(ctx: Context, value: T)

    /**
     * Exports the store as JSONObject for backup.
     * Adapts automatically for Map<String, Any> or JSONObject types.
     */
    open suspend fun exportForBackup(ctx: Context): JSONObject? {
        val value = getAll(ctx) ?: return null
        return when (value) {
            is JSONObject -> value
            is Map<*, *> -> JSONObject().apply {
                value.forEach { (k, v) ->
                    put(k.toString(), v)
                }
            }
            else -> null
        }
    }

    /**
     * Imports the store from a JSONObject backup.
     * Adapts automatically for Map<String, Any> or JSONObject types.
     */
    open suspend fun importFromBackup(ctx: Context, json: JSONObject) {
        val value = getAll(ctx)
        @Suppress("UNCHECKED_CAST")
        when (value) {
            is JSONObject -> setAll(ctx, json as T)
            is Map<*, *> -> {
                val map = buildMap<String, Any?> {
                    json.keys().forEach { key ->
                        put(key, json.opt(key))
                    }
                }
                @Suppress("UNCHECKED_CAST")
                setAll(ctx, map as T)
            }
            else -> return
        }
    }
}
