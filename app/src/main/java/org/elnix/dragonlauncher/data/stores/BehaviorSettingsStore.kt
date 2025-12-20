package org.elnix.dragonlauncher.data.stores

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.data.BaseSettingsStore
import org.elnix.dragonlauncher.data.SwipeActionSerializable
import org.elnix.dragonlauncher.data.SwipeJson
import org.elnix.dragonlauncher.data.behaviorDataStore

object BehaviorSettingsStore : BaseSettingsStore() {
    override val name: String = "Ui"

    private data class UiSettingsBackup(
        val backAction: SwipeActionSerializable? = null,
        val doubleClickAction: SwipeActionSerializable? = null,
        val keepScreenOn: Boolean = false
    )

    private val defaults = UiSettingsBackup()

    /*private*/ object Keys {
        val BACK_ACTION = stringPreferencesKey("backAction")
        val DOUBLE_CLICK_ACTION = stringPreferencesKey("doubleClickAction")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keepScreenOn")
        val ALL = listOf(
            BACK_ACTION,
            DOUBLE_CLICK_ACTION,
            KEEP_SCREEN_ON
        )
    }

    fun getBackAction(ctx: Context): Flow<SwipeActionSerializable?> =
        ctx.behaviorDataStore.data.map { json ->
            json[Keys.BACK_ACTION]?.takeIf { it.isNotBlank() }?.let { SwipeJson.decodeAction(it) }
        }

    fun getDoubleClickAction(ctx: Context): Flow<SwipeActionSerializable?> =
        ctx.behaviorDataStore.data.map { json ->
            json[Keys.DOUBLE_CLICK_ACTION]?.takeIf { it.isNotBlank() }?.let { SwipeJson.decodeAction(it) }
        }

    suspend fun setBackAction(ctx: Context, value: SwipeActionSerializable?) {
        ctx.behaviorDataStore.edit {
            if (value != null) {
                it[Keys.BACK_ACTION] = SwipeJson.encodeAction(value)
            } else {
                it.remove(Keys.BACK_ACTION)
            }
        }
    }

    suspend fun setDoubleClickAction(ctx: Context, value: SwipeActionSerializable?) {
        ctx.behaviorDataStore.edit {
            println(value)
            if (value != null) {
                println("Encoded: " + SwipeJson.encodeAction(value))
                it[Keys.DOUBLE_CLICK_ACTION] = SwipeJson.encodeAction(value)
                print(it[Keys.DOUBLE_CLICK_ACTION])
            } else {
                it.remove(Keys.DOUBLE_CLICK_ACTION)
            }
        }
    }


    fun getKeepScreenOn(ctx: Context): Flow<Boolean> =
        ctx.behaviorDataStore.data.map { it[Keys.KEEP_SCREEN_ON] ?: defaults.keepScreenOn }

    suspend fun setKeepScreenOn(ctx: Context, value: Boolean) {
        ctx.behaviorDataStore.edit { it[Keys.KEEP_SCREEN_ON] = value }
    }


//     --------------------------------
//     BACKUP / RESTORE / RESET
//     --------------------------------


    override suspend fun resetAll(ctx: Context) {
        ctx.behaviorDataStore.edit { prefs ->
            Keys.ALL.forEach { prefs.remove(it) }
        }
    }

    suspend fun getAll(ctx: Context): Map<String, Any> {
        val prefs = ctx.behaviorDataStore.data.first()

        return buildMap {

            fun putIfChanged(key: Preferences.Key<Boolean>, default: Boolean) {
                val v = prefs[key]
                if (v != null && v != default) put(key.name, v)
            }

            fun putIfChanged(key: Preferences.Key<String>, default: String?) {
                val v = prefs[key]
                if (v != null && v != default) put(key.name, v)
            }

            putIfChanged(Keys.BACK_ACTION, null)
            putIfChanged(Keys.DOUBLE_CLICK_ACTION, null)
            putIfChanged(Keys.KEEP_SCREEN_ON, defaults.keepScreenOn)
        }
    }


    suspend fun setAll(ctx: Context, backup: Map<String, Any?>) {
        ctx.behaviorDataStore.edit { prefs ->
            fun applyString(key: Preferences.Key<String>) {
                val raw = backup[key.name] ?: return
                val stringValue = when (raw) {
                    is String -> raw
                    else -> raw.toString()
                }
                prefs[key] = stringValue
            }

            fun applyBoolean(key: Preferences.Key<Boolean>) {
                val raw = backup[key.name] ?: return
                val boolValue = when (raw) {
                    is Boolean -> raw
                    is String -> raw.toBooleanStrictOrNull() ?: return
                    else -> return
                }
                prefs[key] = boolValue
            }

            applyString(Keys.BACK_ACTION)
            applyString(Keys.DOUBLE_CLICK_ACTION)
            applyBoolean(Keys.KEEP_SCREEN_ON)
        }
    }
}
