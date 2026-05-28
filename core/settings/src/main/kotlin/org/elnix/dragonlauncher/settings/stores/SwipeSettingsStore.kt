package org.elnix.dragonlauncher.settings.stores

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.common.serializables.Nest
import org.elnix.dragonlauncher.common.serializables.SwipeJson
import org.elnix.dragonlauncher.common.serializables.Point
import org.elnix.dragonlauncher.common.serializables.Point.Companion.defaultSwipePointsValues
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.stores.JsonObjectSettingsStore
import org.elnix.dragonlauncher.settings.resolveDataStore
import org.json.JSONArray
import org.json.JSONObject

object SwipeSettingsStore : JsonObjectSettingsStore(DataStoreName.SWIPE) {

    private val POINTS = stringPreferencesKey("points_json")
    private val CIRCLE_NESTS = stringPreferencesKey("nests_json")

    private val DEFAULT_CIRCLE = stringPreferencesKey("default_circle")

    /* ───────────── Points ───────────── */

    suspend fun getPoints(ctx: Context): List<Point> =
        ctx.resolveDataStore(this.dataStoreName).data
            .map { prefs -> prefs[POINTS]?.let(SwipeJson::decodePoints) ?: emptyList() }
            .first()

    fun getPointsFlow(ctx: Context) =
        ctx.resolveDataStore(this.dataStoreName).data.map { prefs ->
            prefs[POINTS]?.let(SwipeJson::decodePoints) ?: emptyList()
        }

    suspend fun savePoints(ctx: Context, points: List<Point>) {
        ctx.resolveDataStore(this.dataStoreName).edit { prefs ->
            prefs[POINTS] = SwipeJson.encodePoints(points)
        }
    }

    /* ───────────── Nests ───────────── */

    suspend fun getNests(ctx: Context): List<Nest> =
        ctx.resolveDataStore(this.dataStoreName).data
            .map { prefs -> prefs[CIRCLE_NESTS]?.let(SwipeJson::decodeNests) ?: listOf(Nest()) }
            .first()

    fun getNestsFlow(ctx: Context) =
        ctx.resolveDataStore(this.dataStoreName).data.map { prefs ->
            prefs[CIRCLE_NESTS]?.let(SwipeJson::decodeNests) ?: listOf(Nest())
        }

    suspend fun saveNests(ctx: Context, nests: List<Nest>) {
        ctx.resolveDataStore(this.dataStoreName).edit { prefs ->
            prefs[CIRCLE_NESTS] = SwipeJson.encodeNests(nests)
        }
    }


    /* ───────────── Default circle ───────────── */

    fun getDefaultPointFlow(ctx: Context): Flow<Point> =
        ctx.resolveDataStore(this.dataStoreName).data.map { prefs ->
            prefs[DEFAULT_CIRCLE]?.let { SwipeJson.decodePoints(it).firstOrNull() } ?: defaultSwipePointsValues
        }

    suspend fun getDefaultPoint(ctx: Context): Point =
        ctx.resolveDataStore(this.dataStoreName).data.map { prefs ->
            prefs[DEFAULT_CIRCLE]?.let { SwipeJson.decodePoints(it).firstOrNull() } ?: defaultSwipePointsValues
        }.first()

    suspend fun setDefaultPoint(ctx: Context, point: Point) {
        ctx.resolveDataStore(this.dataStoreName).edit { prefs ->
            prefs[DEFAULT_CIRCLE] = SwipeJson.encodePoints(listOf(point))
        }
    }

    override suspend fun getAll(ctx: Context): JSONObject {
        val points = getPoints(ctx)
        val nests = getNests(ctx)
        val defaultPoint = getDefaultPoint(ctx  )

        if (points.isEmpty() && nests.isEmpty()) return JSONObject()

        return JSONObject().apply {
            if (points.isNotEmpty()) {
                put("points", JSONArray(SwipeJson.encodePointsPretty(points)))
            }
            if (nests.isNotEmpty()) {
                put("nests", JSONArray(SwipeJson.encodeNestsPretty(nests)))
            }
            if (points.isNotEmpty()) {
                put("default_point", JSONArray(SwipeJson.encodePointsPretty(listOf(defaultPoint))))
            }
        }
    }


    override suspend fun setAll(ctx: Context, value: JSONObject?) {
        if (value == null) return

        if (value.has("points") || value.has("nests")) {
            value.optJSONArray("points")?.let {
                savePoints(ctx, SwipeJson.decodePoints(it.toString()))
            }
            value.optJSONArray("nests")?.let {
                saveNests(ctx, SwipeJson.decodeNests(it.toString()))
            }
            value.optJSONArray("default_point")?.let {
                setDefaultPoint(ctx, SwipeJson.decodePoints(it.toString()).first())
            }
            return
        }
    }

    // Overrides the default resetAll cause ALL has no elements
    override suspend fun resetAll(ctx: Context) {
        ctx.resolveDataStore(this.dataStoreName).edit { prefs->
            prefs.remove(POINTS)
            prefs.remove(CIRCLE_NESTS)
            prefs.remove(DEFAULT_CIRCLE)
        }
    }
}
