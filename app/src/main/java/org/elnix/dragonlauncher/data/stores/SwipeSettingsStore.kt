package org.elnix.dragonlauncher.data.stores

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.data.BaseSettingsStore
import org.elnix.dragonlauncher.data.CircleNest
import org.elnix.dragonlauncher.data.SwipeJson
import org.elnix.dragonlauncher.data.SwipePointSerializable
import org.elnix.dragonlauncher.data.swipeDataStore
import org.json.JSONArray
import org.json.JSONObject

object SwipeSettingsStore : BaseSettingsStore<JSONObject>() {

    override val name: String = "Swipe"

    private val POINTS = stringPreferencesKey("points_json")
    private val CIRCLE_NESTS = stringPreferencesKey("nests_json")

    /* ---------- Points ---------- */

    suspend fun getPoints(ctx: Context): List<SwipePointSerializable> =
        ctx.swipeDataStore.data
            .map { prefs -> prefs[POINTS]?.let(SwipeJson::decodePoints) ?: emptyList() }
            .first()

    fun getPointsFlow(ctx: Context) =
        ctx.swipeDataStore.data.map { prefs ->
            prefs[POINTS]?.let(SwipeJson::decodePoints) ?: emptyList()
        }

    suspend fun savePoints(ctx: Context, points: List<SwipePointSerializable>) {
        ctx.swipeDataStore.edit { prefs ->
            prefs[POINTS] = SwipeJson.encodePoints(points)
        }
    }

    /* ---------- Nests ---------- */

    suspend fun getNests(ctx: Context): List<CircleNest> =
        ctx.swipeDataStore.data
            .map { prefs -> prefs[CIRCLE_NESTS]?.let(SwipeJson::decodeNests) ?: listOf(CircleNest()) }
            .first()

    fun getNestsFlow(ctx: Context) =
        ctx.swipeDataStore.data.map { prefs ->
            prefs[CIRCLE_NESTS]?.let(SwipeJson::decodeNests) ?: listOf(CircleNest())
        }

    suspend fun saveNests(ctx: Context, nests: List<CircleNest>) {
        ctx.swipeDataStore.edit { prefs ->
            prefs[CIRCLE_NESTS] = SwipeJson.encodeNests(nests)
        }
    }

    /* ---------- Reset ---------- */

    override suspend fun resetAll(ctx: Context) {
        ctx.swipeDataStore.edit { prefs ->
            prefs.remove(POINTS)
            prefs.remove(CIRCLE_NESTS)
        }
    }

    override suspend fun getAll(ctx: Context): JSONObject {
        val points = getPoints(ctx)
        val nests = getNests(ctx)

        if (points.isEmpty() && nests.isEmpty()) return JSONObject()

        return JSONObject().apply {
            if (points.isNotEmpty()) {
                put("points", JSONArray(SwipeJson.encodePointsPretty(points)))
            }
            if (nests.isNotEmpty()) {
                put("nests", JSONArray(SwipeJson.encodeNestsPretty(nests)))
            }
        }
    }


    override suspend fun setAll(ctx: Context, value: JSONObject) {
        if (value.has("points") || value.has("nests")) {
            value.optJSONArray("points")?.let {
                savePoints(ctx, SwipeJson.decodePoints(it.toString()))
            }
            value.optJSONArray("nests")?.let {
                saveNests(ctx, SwipeJson.decodeNests(it.toString()))
            }
            return
        }
    }
}
