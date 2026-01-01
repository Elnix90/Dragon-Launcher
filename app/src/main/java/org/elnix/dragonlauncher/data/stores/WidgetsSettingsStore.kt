package org.elnix.dragonlauncher.data.stores

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import org.elnix.dragonlauncher.data.BaseSettingsStore
import org.elnix.dragonlauncher.data.helpers.WidgetInfo
import org.elnix.dragonlauncher.data.widgetsDatastore
import org.elnix.dragonlauncher.utils.WIDGET_TAG
import org.json.JSONArray
import org.json.JSONObject

object WidgetSettingsStore : BaseSettingsStore<JSONObject>() {

    override val name: String = "Widgets"

    private object Keys {
        val WIDGETS_KEY = stringPreferencesKey("widgets_state")
    }

    suspend fun loadWidgets(ctx: Context): List<WidgetInfo> {
        return try {
            val allJson = getAll(ctx)
            Log.d(WIDGET_TAG, "Raw: $allJson")
            val widgetsArray = allJson.optJSONArray("widgets") ?: return emptyList()

            val widgets = mutableListOf<WidgetInfo>()
            for (i in 0 until widgetsArray.length()) {
                val obj = widgetsArray.getJSONObject(i)
                val providerStr = obj.optString("provider", "")
                val provider = if (providerStr.isNotEmpty()) {
                    val parts = providerStr.split(":")
                    ComponentName(parts[0], parts[1])
                } else ComponentName("", "")

                widgets.add(WidgetInfo(
                    id = obj.getInt("id"),
                    provider = provider,
                    spanX = obj.optDouble("spanX", 1.0).toFloat(),
                    spanY = obj.optDouble("spanY", 1.0).toFloat(),
                    x = obj.optDouble("x", 0.0).toFloat(),
                    y = obj.optDouble("y", 0.0).toFloat()
                ))
            }
            Log.d(WIDGET_TAG, "Loaded ${widgets.size} widgets")
            widgets
        } catch (e: Exception) {
            Log.e(WIDGET_TAG, "Load failed", e)
            emptyList()
        }
    }

    suspend fun saveWidget(ctx: Context, widget: WidgetInfo) {
        Log.d(WIDGET_TAG, "Saving widget ${widget.id}")

        val widgets = loadWidgets(ctx).toMutableList().apply {
            removeAll { it.id == widget.id }
            add(widget)
        }

        val widgetsArray = JSONArray().apply {
            widgets.forEach { widget ->
                put(JSONObject().apply {
                    put("id", widget.id)
                    put("provider", "${widget.provider.packageName}:${widget.provider.className}") // ✅ STRING!
                    put("spanX", widget.spanX)
                    put("spanY", widget.spanY)
                    put("x", widget.x)
                    put("y", widget.y)
                })
            }
        }

        val json = JSONObject().apply {
            put("widgets", widgetsArray)
        }

        Log.d(WIDGET_TAG, "Saved: $json")
        setAll(ctx, json)
    }

    suspend fun deleteWidget(ctx: Context, widgetId: Int) {
        val widgets = loadWidgets(ctx).filterNot { it.id == widgetId }

        val widgetsArray = JSONArray().apply {
            widgets.forEach { widget ->
                put(JSONObject().apply {
                    put("id", widget.id)
                    put("provider", "${widget.provider.packageName}:${widget.provider.className}")
                    put("spanX", widget.spanX)
                    put("spanY", widget.spanY)
                    put("x", widget.x)
                    put("y", widget.y)
                })
            }
        }

        val json = JSONObject().apply {
            put("widgets", widgetsArray)
        }

        setAll(ctx, json)
    }


    override suspend fun resetAll(ctx: Context) {
        ctx.widgetsDatastore.edit { prefs ->
            prefs.remove(Keys.WIDGETS_KEY)
        }
    }

    override suspend fun getAll(ctx: Context): JSONObject {
        val prefs = ctx.widgetsDatastore.data.first()
        val raw = prefs[Keys.WIDGETS_KEY] ?: return JSONObject()
        println(raw)
        return JSONObject(raw)
    }

    override suspend fun setAll(ctx: Context, value: JSONObject) {
        ctx.widgetsDatastore.edit { prefs ->
            prefs[Keys.WIDGETS_KEY] = value.toString()
        }
    }
}
