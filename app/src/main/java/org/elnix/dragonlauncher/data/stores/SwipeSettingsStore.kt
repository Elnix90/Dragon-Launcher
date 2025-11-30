package org.elnix.dragonlauncher.data.stores

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.data.SwipeJson
import org.elnix.dragonlauncher.data.SwipePointSerializable
import org.elnix.dragonlauncher.data.swipeDataStore

object SwipeSettingsStore {

    data class SwipeBackup(
        val pointsJson: String? = null
    )

    private val POINTS = stringPreferencesKey("points_json")

    suspend fun getPoints(ctx: Context): List<SwipePointSerializable> {
        return ctx.swipeDataStore.data
            .map { prefs ->
                prefs[POINTS]?.let { SwipeJson.decode(it) } ?: emptyList()
            }
            .first()
    }

    fun getPointsFlow(ctx: Context) =
        ctx.swipeDataStore.data.map { prefs ->
            prefs[POINTS]?.let { SwipeJson.decode(it) } ?: emptyList()
        }

    suspend fun save(ctx: Context, points: List<SwipePointSerializable>) {
        ctx.swipeDataStore.edit { prefs ->
            prefs[POINTS] = SwipeJson.encode(points)
        }
    }


    suspend fun resetAll(ctx: Context) {
        ctx.swipeDataStore.edit { prefs ->
            prefs.remove(POINTS)
        }
    }

    suspend fun getAll(ctx: Context): Map<String, String> {
        val prefs = ctx.swipeDataStore.data.first()
        val defaults = SwipeBackup()

        return buildMap {
            fun putIfNonDefault(key: String, value: Any?, default: Any?) {
                if (value != null && value != default) {
                    put(key, value.toString())
                }
            }

            putIfNonDefault(POINTS.name, prefs[POINTS], defaults.pointsJson)
        }
    }

    suspend fun setAll(ctx: Context, backup: Map<String, String>) {
        ctx.swipeDataStore.edit { prefs ->
            backup[POINTS.name]?.let { json ->
                prefs[POINTS] = json
            }
        }
    }
}
