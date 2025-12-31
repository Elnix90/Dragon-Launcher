package org.elnix.dragonlauncher.data.stores

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.data.BaseSettingsStore
import org.elnix.dragonlauncher.data.debugDatastore
import org.elnix.dragonlauncher.data.getBooleanStrict
import org.elnix.dragonlauncher.data.putIfNonDefault
import org.elnix.dragonlauncher.data.stores.DebugSettingsStore.Keys.DEBUG_ENABLED
import org.elnix.dragonlauncher.data.stores.DebugSettingsStore.Keys.DEBUG_INFOS
import org.elnix.dragonlauncher.data.stores.DebugSettingsStore.Keys.FORCE_APP_LANGUAGE_SELECTOR
import org.elnix.dragonlauncher.data.stores.DebugSettingsStore.Keys.SETTINGS_DEBUG_INFOS

object DebugSettingsStore : BaseSettingsStore<Map<String, Any?>>() {

    override val name: String = "Debug"

    // -------------------------------------------------------------------------
    // Backup data class
    // -------------------------------------------------------------------------
    private data class DebugSettingsBackup(
        val debugEnabled: Boolean = false,
        val debugInfos: Boolean = false,
        val settingsDebugInfo: Boolean = false,
        val forceAppLanguageSelector: Boolean = false
    )

    private val defaults = DebugSettingsBackup()

    // -------------------------------------------------------------------------
    // Keys object for safer reference
    // -------------------------------------------------------------------------
    private object Keys {
        val DEBUG_ENABLED = booleanPreferencesKey(DebugSettingsBackup::debugEnabled.name)
        val DEBUG_INFOS = booleanPreferencesKey(DebugSettingsBackup::debugInfos.name)
        val SETTINGS_DEBUG_INFOS = booleanPreferencesKey(DebugSettingsBackup::settingsDebugInfo.name)
        val FORCE_APP_LANGUAGE_SELECTOR = booleanPreferencesKey(DebugSettingsBackup::forceAppLanguageSelector.name)

        val ALL = listOf(
            DEBUG_ENABLED,
            DEBUG_INFOS,
            SETTINGS_DEBUG_INFOS,
            FORCE_APP_LANGUAGE_SELECTOR
        )
    }


    // -------------------------------------------------------------------------
    // Accessors + Mutators
    // -------------------------------------------------------------------------
    fun getDebugEnabled(ctx: Context): Flow<Boolean> =
        ctx.debugDatastore.data.map { prefs ->
            prefs[DEBUG_ENABLED] ?: defaults.debugEnabled
        }

    suspend fun setDebugEnabled(ctx: Context, enabled: Boolean) {
        ctx.debugDatastore.edit { it[DEBUG_ENABLED] = enabled }
    }

    fun getDebugInfos(ctx: Context): Flow<Boolean> =
        ctx.debugDatastore.data.map { prefs ->
            prefs[DEBUG_INFOS] ?: defaults.debugInfos
        }

    suspend fun setDebugInfos(ctx: Context, enabled: Boolean) {
        ctx.debugDatastore.edit { it[DEBUG_INFOS] = enabled }
    }

    fun getSettingsDebugInfos(ctx: Context): Flow<Boolean> =
        ctx.debugDatastore.data.map { prefs ->
            prefs[SETTINGS_DEBUG_INFOS] ?: defaults.settingsDebugInfo
        }

    suspend fun setSettingsDebugInfos(ctx: Context, enabled: Boolean) {
        ctx.debugDatastore.edit { it[SETTINGS_DEBUG_INFOS] = enabled }
    }

    fun getForceAppLanguageSelector(ctx: Context): Flow<Boolean> =
        ctx.debugDatastore.data.map { prefs ->
            prefs[FORCE_APP_LANGUAGE_SELECTOR] ?: defaults.forceAppLanguageSelector
        }

    suspend fun setForceAppLanguageSelector(ctx: Context, enabled: Boolean) {
        ctx.debugDatastore.edit { it[FORCE_APP_LANGUAGE_SELECTOR] = enabled }
    }

    // -------------------------------------------------------------------------
    // Reset
    // -------------------------------------------------------------------------
    override suspend fun resetAll(ctx: Context) {
        ctx.debugDatastore.edit { prefs ->
            Keys.ALL.forEach { prefs.remove(it) }
        }
    }

    // -------------------------------------------------------------------------
    // Backup export
    // -------------------------------------------------------------------------
    override suspend fun getAll(ctx: Context): Map<String, Any> {
        val prefs = ctx.debugDatastore.data.first()

        return buildMap {
            putIfNonDefault(DEBUG_ENABLED, prefs[DEBUG_ENABLED], defaults.debugEnabled)
            putIfNonDefault(DEBUG_INFOS, prefs[DEBUG_INFOS], defaults.debugInfos)
            putIfNonDefault(SETTINGS_DEBUG_INFOS, prefs[SETTINGS_DEBUG_INFOS], defaults.settingsDebugInfo)
            putIfNonDefault(FORCE_APP_LANGUAGE_SELECTOR, prefs[FORCE_APP_LANGUAGE_SELECTOR], defaults.forceAppLanguageSelector)
        }
    }

    // -------------------------------------------------------------------------
    // Backup import
    // -------------------------------------------------------------------------
    override suspend fun setAll(ctx: Context, value: Map<String, Any?>) {

        ctx.debugDatastore.edit { prefs ->

            prefs[DEBUG_ENABLED] =
                getBooleanStrict(value, DEBUG_ENABLED, defaults.debugEnabled)

            prefs[DEBUG_INFOS] =
                getBooleanStrict(value, DEBUG_INFOS, defaults.debugInfos)

            prefs[SETTINGS_DEBUG_INFOS] =
                getBooleanStrict(value, SETTINGS_DEBUG_INFOS, defaults.settingsDebugInfo)

            prefs[FORCE_APP_LANGUAGE_SELECTOR] =
                getBooleanStrict(value,FORCE_APP_LANGUAGE_SELECTOR, defaults.forceAppLanguageSelector)
        }
    }
}
