package org.elnix.dragonlauncher.data.stores


import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.data.privateSettingsStore

object PrivateSettingsStore {

    // ---------------------------------------------------------
    // Backup structure
    // ---------------------------------------------------------
    private data class PrivateSettingsBackup(
        val hasSeenWelcome: Boolean = false,
        val hasInitialized: Boolean = false,
        val showSetDefaultLauncherBanner: Boolean = true,
        val useAccessibilityInsteadOfContextToExpandActionPanel: Boolean = false,
        val showMethodAsking: Boolean = true,
    )

    private val defaults = PrivateSettingsBackup()

    // ---------------------------------------------------------
    // Keys object (authoritative)
    // ---------------------------------------------------------
    private object Keys {
        const val HAS_SEEN_WELCOME = "hasSeenWelcome"
        const val HAS_INITIALIZED = "hasInitialized"
        const val SHOW_SET_DEFAULT_LAUNCHER_BANNER = "showSetDefaultLauncherBanner"
        const val USE_ACCESSIBILITY_INSTEAD_OF_CONTEXT = "useAccessibilityInsteadOfContextToExpandActionPanel"
        const val SHOW_METHOD_ASKING = "showMethodAsking"
    }


    // ---------------------------------------------------------
    // DataStore preference keys
    // ---------------------------------------------------------
    private val HAS_SEEN_WELCOME =
        booleanPreferencesKey(Keys.HAS_SEEN_WELCOME)

    private val HAS_INITIALIZED =
        booleanPreferencesKey(Keys.HAS_INITIALIZED)

    private val SHOW_SET_DEFAULT_LAUNCHER_BANNER =
        booleanPreferencesKey(Keys.SHOW_SET_DEFAULT_LAUNCHER_BANNER)

    private val USE_ACCESSIBILITY_INSTEAD_OF_CONTEXT =
        booleanPreferencesKey(Keys.USE_ACCESSIBILITY_INSTEAD_OF_CONTEXT)

    private val SHOW_METHOD_ASKING =
        booleanPreferencesKey(Keys.SHOW_METHOD_ASKING)

    // ---------------------------------------------------------
    // Accessors
    // ---------------------------------------------------------
    fun getHasSeenWelcome(ctx: Context): Flow<Boolean> =
        ctx.privateSettingsStore.data.map { prefs ->
            prefs[HAS_SEEN_WELCOME] ?: defaults.hasSeenWelcome
        }

    suspend fun setHasSeenWelcome(ctx: Context, v: Boolean) {
        ctx.privateSettingsStore.edit { it[HAS_SEEN_WELCOME] = v }
    }

    fun getHasInitialized(ctx: Context): Flow<Boolean> =
        ctx.privateSettingsStore.data.map { prefs ->
            prefs[HAS_INITIALIZED] ?: defaults.hasInitialized
        }

    suspend fun setHasInitialized(ctx: Context, v: Boolean) {
        ctx.privateSettingsStore.edit { it[HAS_INITIALIZED] = v }
    }

    fun getShowSetDefaultLauncherBanner(ctx: Context): Flow<Boolean> =
        ctx.privateSettingsStore.data.map { prefs ->
            prefs[SHOW_SET_DEFAULT_LAUNCHER_BANNER] ?: defaults.showSetDefaultLauncherBanner
        }

    suspend fun setShowSetDefaultLauncherBanner(ctx: Context, v: Boolean) {
        ctx.privateSettingsStore.edit { it[SHOW_SET_DEFAULT_LAUNCHER_BANNER] = v }
    }

    fun getUseAccessibilityInsteadOfContextToExpandActionPanel(ctx: Context): Flow<Boolean> =
        ctx.privateSettingsStore.data.map { prefs ->
            prefs[USE_ACCESSIBILITY_INSTEAD_OF_CONTEXT] ?: defaults.useAccessibilityInsteadOfContextToExpandActionPanel
        }

    suspend fun setUseAccessibilityInsteadOfContextToExpandActionPanel(ctx: Context, v: Boolean) {
        ctx.privateSettingsStore.edit { it[USE_ACCESSIBILITY_INSTEAD_OF_CONTEXT] = v }
    }

    fun getShowMethodAsking(ctx: Context): Flow<Boolean> =
        ctx.privateSettingsStore.data.map { prefs ->
            prefs[SHOW_METHOD_ASKING] ?: defaults.showMethodAsking
        }

    suspend fun setShowMethodAsking(ctx: Context, v: Boolean) {
        ctx.privateSettingsStore.edit { it[SHOW_METHOD_ASKING] = v }
    }

    // ---------------------------------------------------------
    // Reset
    // ---------------------------------------------------------
    suspend fun resetAll(ctx: Context) {
        ctx.privateSettingsStore.edit { prefs ->
            prefs.remove(HAS_SEEN_WELCOME)
            prefs.remove(HAS_INITIALIZED)
            prefs.remove(SHOW_SET_DEFAULT_LAUNCHER_BANNER)
            prefs.remove(USE_ACCESSIBILITY_INSTEAD_OF_CONTEXT)
            prefs.remove(SHOW_METHOD_ASKING)
        }
    }

    // Disabled backup / import functions as they aren't meant to be used, cause those are private settings
//    // ---------------------------------------------------------
//    // Backup export
//    // ---------------------------------------------------------
//    suspend fun getAll(ctx: Context): Map<String, String> {
//        val prefs = ctx.privateSettingsStore.data.first()
//
//        return buildMap {
//            fun putIfNonDefault(key: String, value: Any?, default: Any?) {
//                if (value != null && value != default) {
//                    put(key, value.toString())
//                }
//            }
//
//            putIfNonDefault(
//                Keys.HAS_SEEN_WELCOME,
//                prefs[HAS_SEEN_WELCOME],
//                defaults.hasSeenWelcome
//            )
//
//            putIfNonDefault(
//                Keys.HAS_INITIALIZED,
//                prefs[HAS_INITIALIZED],
//                defaults.hasInitialized
//            )
//        }
//    }
//
//    // ---------------------------------------------------------
//    // Backup import (strict, throws on wrong types)
//    // ---------------------------------------------------------
//    suspend fun setAll(ctx: Context, backup: Map<String, String>) {
//        ctx.privateSettingsStore.edit { prefs ->
//
//            fun decodeBoolean(key: String, raw: String) {
//                val cleaned = raw.trim().lowercase()
//                val value = when (cleaned) {
//                    "true" -> true
//                    "false" -> false
//                    else -> throw BackupTypeException(
//                        key = key,
//                        expected = "Boolean (true/false)",
//                        actual = raw,
//                        value = raw
//                    )
//                }
//                // commit value based on key
//                when (key) {
//                    Keys.HAS_SEEN_WELCOME -> prefs[HAS_SEEN_WELCOME] = value
//                    Keys.HAS_INITIALIZED -> prefs[HAS_INITIALIZED] = value
//                }
//            }
//
//            backup.forEach { (key, rawValue) ->
//                when (key) {
//                    Keys.HAS_SEEN_WELCOME,
//                    Keys.HAS_INITIALIZED -> decodeBoolean(key, rawValue)
//                }
//            }
//        }
//    }
}
