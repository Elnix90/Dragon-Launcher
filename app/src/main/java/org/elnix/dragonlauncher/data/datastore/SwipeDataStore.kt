package org.elnix.dragonlauncher.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.data.SwipeJson
import org.elnix.dragonlauncher.data.SwipePointSerializable

val Context.swipeDataStore by preferencesDataStore("swipe_points")

object SwipeDataStore {
    private val POINTS = stringPreferencesKey("points_json")

    fun getPointsFlow(ctx: Context) = ctx.swipeDataStore.data.map { prefs ->
        prefs[POINTS]?.let { json ->
            SwipeJson.decode(json)
        } ?: emptyList()
    }

    suspend fun save(ctx: Context, points: List<SwipePointSerializable>) {
        ctx.swipeDataStore.edit { prefs ->
            prefs[POINTS] = SwipeJson.encode(points)
        }
    }
}
