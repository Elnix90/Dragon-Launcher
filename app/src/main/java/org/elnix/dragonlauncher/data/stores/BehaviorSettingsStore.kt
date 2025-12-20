package org.elnix.dragonlauncher.data.stores

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.data.BackupTypeException
import org.elnix.dragonlauncher.data.BaseSettingsStore
import org.elnix.dragonlauncher.data.uiDatastore

object BehaviorSettingsStore : BaseSettingsStore() {
    override val name: String = "Ui"

    private data class UiSettingsBackup(
        val requirePressingBackTwiceToExit: Boolean = true,
        val doubleBackFeedback: Boolean = false,
        val keepScreenOn: Boolean = false
    )

    private val defaults = UiSettingsBackup()

    private object Keys {
//        val REQUIRE_PRESSING_BACK_TWICE_TO_EXIT = booleanPreferencesKey("requirePressingBackTwiceToExit")
//        val DOUBLE_BACK_FEEDBACK = booleanPreferencesKey("doubleBackFeedback")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keepScreenOn")
        val ALL = listOf(
//            REQUIRE_PRESSING_BACK_TWICE_TO_EXIT,
//            DOUBLE_BACK_FEEDBACK,
            KEEP_SCREEN_ON
        )
    }

//    fun getRequirePressingBackTwiceToExit(ctx: Context): Flow<Boolean> =
//        ctx.uiDatastore.data.map { it[Keys.REQUIRE_PRESSING_BACK_TWICE_TO_EXIT] ?: defaults.requirePressingBackTwiceToExit }
//
//    suspend fun setRequirePressingBackTwiceToExit(ctx: Context, value: Boolean) {
//        ctx.uiDatastore.edit { it[Keys.REQUIRE_PRESSING_BACK_TWICE_TO_EXIT] = value }
//    }
//
//    fun getDoubleBackFeedback(ctx: Context): Flow<Boolean> =
//        ctx.uiDatastore.data.map { it[Keys.DOUBLE_BACK_FEEDBACK] ?: defaults.requirePressingBackTwiceToExit }
//
//    suspend fun setDoubleBackFeedback(ctx: Context, value: Boolean) {
//        ctx.uiDatastore.edit { it[Keys.DOUBLE_BACK_FEEDBACK] = value }
//    }

    fun getKeepScreenOn(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[Keys.KEEP_SCREEN_ON] ?: defaults.keepScreenOn }

    suspend fun setKeepScreenOn(ctx: Context, value: Boolean) {
        ctx.uiDatastore.edit { it[Keys.KEEP_SCREEN_ON] = value }
    }


    // --------------------------------
    // BACKUP / RESTORE / RESET
    // --------------------------------


    override suspend fun resetAll(ctx: Context) {
        ctx.uiDatastore.edit { prefs ->
            Keys.ALL.forEach { prefs.remove(it) }
        }
    }

    suspend fun getAll(ctx: Context): Map<String, Any> {
        val prefs = ctx.uiDatastore.data.first()

        return buildMap {

            fun putIfChanged(key: Preferences.Key<Boolean>, default: Boolean) {
                val v = prefs[key]
                if (v != null && v != default) put(key.name, v)
            }

            fun putIfChanged(key: Preferences.Key<Int>, default: Int) {
                val v = prefs[key]
                if (v != null && v != default) put(key.name, v)
            }

//            putIfChanged(Keys.REQUIRE_PRESSING_BACK_TWICE_TO_EXIT, defaults.requirePressingBackTwiceToExit)
//            putIfChanged(Keys.DOUBLE_BACK_FEEDBACK, defaults.doubleBackFeedback)
            putIfChanged(Keys.KEEP_SCREEN_ON, defaults.keepScreenOn)
        }
    }


    suspend fun setAll(ctx: Context, backup: Map<String, Any?>) {
        ctx.uiDatastore.edit { prefs ->

            fun applyBoolean(key: Preferences.Key<Boolean>) {
                val raw = backup[key.name] ?: return

                val boolValue = when (raw) {
                    is Boolean -> raw
                    is String -> raw.toBooleanStrictOrNull()
                        ?: throw BackupTypeException(
                            key.name,
                            expected = "Boolean",
                            actual = "String",
                            value = raw
                        )
                    else -> throw BackupTypeException(
                        key.name,
                        expected = "Boolean",
                        actual = raw::class.simpleName,
                        value = raw
                    )
                }

                prefs[key] = boolValue
            }

            fun applyInt(key: Preferences.Key<Int>) {
                val raw = backup[key.name] ?: return

                val intValue = when (raw) {
                    is Int -> raw
                    is String -> raw.toIntOrNull()
                        ?: throw BackupTypeException(
                            key.name,
                            expected = "Int",
                            actual = "String",
                            value = raw
                        )
                    else -> throw BackupTypeException(
                        key.name,
                        expected = "Int",
                        actual = raw::class.simpleName,
                        value = raw
                    )
                }

                prefs[key] = intValue
            }

//            applyBoolean(Keys.REQUIRE_PRESSING_BACK_TWICE_TO_EXIT)
//            applyBoolean(Keys.DOUBLE_BACK_FEEDBACK)
            applyBoolean(Keys.KEEP_SCREEN_ON)
        }
    }
}
