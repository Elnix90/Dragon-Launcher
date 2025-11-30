package org.elnix.dragonlauncher.data.stores

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.data.debugDatastore

object DebugSettingsStore {

    data class DebugSettingsBackup(
        val debugEnabled: Boolean = false,
        val debugInfos: Boolean = false,
        val forceAppLanguageSelector: Boolean = false
    )

    private val DEBUG_INFOS = booleanPreferencesKey("debug_infos")
    private val DEBUG_ENABLED = booleanPreferencesKey("debug_enabled")
    private val FORCE_APP_LANGUAGE_SELECTOR = booleanPreferencesKey("force_app_language_selector")

    fun getDebugInfos(ctx: Context): Flow<Boolean> =
        ctx.debugDatastore.data.map { it[DEBUG_INFOS] ?: false }

    suspend fun setDebugInfos(ctx: Context, enabled: Boolean) {
        ctx.debugDatastore.edit { it[DEBUG_INFOS] = enabled }
    }

    fun getDebugEnabled(ctx: Context): Flow<Boolean> =
        ctx.debugDatastore.data.map { it[DEBUG_ENABLED] ?: false }

    suspend fun setDebugEnabled(ctx: Context, enabled: Boolean) {
        ctx.debugDatastore.edit { it[DEBUG_ENABLED] = enabled }
    }

    fun getForceAppLanguageSelector(ctx: Context): Flow<Boolean> =
        ctx.debugDatastore.data.map { it[FORCE_APP_LANGUAGE_SELECTOR] ?: false }

    suspend fun setForceAppLanguageSelector(ctx: Context, enabled: Boolean) {
        ctx.debugDatastore.edit { it[FORCE_APP_LANGUAGE_SELECTOR] = enabled }
    }

    suspend fun resetAll(ctx: Context) {
        ctx.debugDatastore.edit { prefs ->
            prefs.remove(DEBUG_INFOS)
            prefs.remove(DEBUG_ENABLED)
            prefs.remove(FORCE_APP_LANGUAGE_SELECTOR)
        }
    }

    suspend fun getAll(ctx: Context): Map<String, Boolean> {
        val prefs = ctx.debugDatastore.data.first()
        val defaults = DebugSettingsBackup()
        return buildMap {
            fun putIfNonDefault(key: String, value: Boolean?, default: Any?) {
                if (value != null && value != default) {
                    put(key, value)
                }
            }

            putIfNonDefault(DEBUG_INFOS.name, prefs[DEBUG_INFOS], defaults.debugInfos)
            putIfNonDefault(DEBUG_ENABLED.name, prefs[DEBUG_ENABLED], defaults.debugEnabled)
            putIfNonDefault(FORCE_APP_LANGUAGE_SELECTOR.name, prefs[FORCE_APP_LANGUAGE_SELECTOR], defaults.forceAppLanguageSelector)
        }
    }

    suspend fun setAll(ctx: Context, backup: Map<String, Boolean>) {
        ctx.debugDatastore.edit { prefs ->
            backup[DEBUG_ENABLED.name]?.let {
                prefs[DEBUG_ENABLED] = it
            }

            backup[DEBUG_INFOS.name]?.let {
                prefs[DEBUG_INFOS] = it
            }

            backup[FORCE_APP_LANGUAGE_SELECTOR.name]?.let {
                prefs[FORCE_APP_LANGUAGE_SELECTOR] = it
            }
        }
    }
}
