package org.elnix.dragonlauncher.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.data.SwipeJson
import org.elnix.dragonlauncher.data.SwipePointSerializable
import org.elnix.dragonlauncher.data.stores.ColorModesSettingsStore
import org.elnix.dragonlauncher.data.stores.DebugSettingsStore
import org.elnix.dragonlauncher.data.stores.LanguageSettingsStore
import org.elnix.dragonlauncher.data.stores.SwipeSettingsStore
import org.elnix.dragonlauncher.data.stores.UiSettingsStore
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore
import org.elnix.dragonlauncher.data.stores.DrawerSettingsStore
import org.json.JSONObject
import java.io.OutputStreamWriter

object SettingsBackupManager {

    private const val TAG = "SettingsBackupManager"

    suspend fun exportSettings(ctx: Context, uri: Uri) {
        try {


            val json = JSONObject().apply {

                fun putIfNotEmpty(key: String, obj: JSONObject) {
                    if (obj.length() > 0) put(key, obj)
                }


                putIfNotEmpty("actions", mapActionsToJson(SwipeSettingsStore.getAll(ctx)))
                putIfNotEmpty("drawer", mapToJson(DrawerSettingsStore.getAll(ctx)))
                putIfNotEmpty("color_mode", mapStringToJson(ColorModesSettingsStore.getAll(ctx)))
                putIfNotEmpty("color", mapIntToJson(ColorSettingsStore.getAll(ctx)))
                putIfNotEmpty("debug", mapToJson(DebugSettingsStore.getAll(ctx)))
                putIfNotEmpty("language", mapStringToJson(LanguageSettingsStore.getAll(ctx)))
                putIfNotEmpty("ui", mapStringToJson(UiSettingsStore.getAll(ctx)))
            }


            Log.d(TAG, "Generated JSON: $json")

            withContext(Dispatchers.IO) {
                ctx.contentResolver.openOutputStream(uri)?.use { output ->
                    OutputStreamWriter(output).use { it.write(json.toString(2)) }
                }
            }

            Log.i(TAG, "Export completed successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error during export", e)
            throw e
        }
    }

    suspend fun importSettings(ctx: Context, uri: Uri) {
        try {
            val json = withContext(Dispatchers.IO) {
                ctx.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }

            if (json.isNullOrBlank()) {
                Log.e(TAG, "Invalid or empty file")
                throw IllegalArgumentException("Invalid or empty file")
            }

            Log.d(TAG, "Loaded JSON: $json")
            val obj = JSONObject(json)


            withContext(Dispatchers.IO) {

                // ------------------ ACTIONS ------------------
                obj.optJSONObject("actions")?.let {
                    SwipeSettingsStore.setAll(ctx, jsonToActionsMap(it))
                }

                // ------------------ ACTIONS ------------------
                obj.optJSONObject("drawer")?.let {
                    DrawerSettingsStore.setAll(ctx, jsonToMap(it))
                }

                // ------------------ COLOR MODE ------------------
                obj.optJSONObject("color_mode")?.let {
                    ColorModesSettingsStore.setAll(ctx, jsonToMapString(it))
                }

                // ------------------ COLORS ------------------
                obj.optJSONObject("color")?.let {
                    ColorSettingsStore.setAll(ctx, jsonToMapInt(it))
                }

                // ------------------ DEBUG ------------------
                obj.optJSONObject("debug")?.let {
                    DebugSettingsStore.setAll(ctx, jsonToMap(it))
                }

                // ------------------ LANGUAGE ------------------
                obj.optJSONObject("language")?.let {
                    LanguageSettingsStore.setAll(ctx, jsonToMapString(it))
                }

                // ------------------ UI ------------------
                obj.optJSONObject("ui")?.let {
                    UiSettingsStore.setAll(ctx, jsonToMapString(it))
                }
            }

            Log.i(TAG, "Import completed successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error during import", e)
            throw e
        }
    }

    private fun mapToJson(map: Map<String, Boolean>) = JSONObject().apply {
        map.forEach { (key, value) -> put(key, value) }
    }

    private fun jsonToMap(obj: JSONObject) = buildMap {
        obj.keys().forEach { key -> put(key, obj.optBoolean(key, false)) }
    }

    private fun mapIntToJson(map: Map<String, Int>) = JSONObject().apply {
        map.forEach { (k, v) -> put(k, v) }
    }

    private fun jsonToMapInt(obj: JSONObject) = buildMap {
        obj.keys().forEach { key -> put(key, obj.optInt(key, 0)) }
    }

    private fun mapStringToJson(map: Map<String, String>) = JSONObject().apply {
        map.forEach { (key, value) -> put(key, value) }
    }

    private fun jsonToMapString(obj: JSONObject) = buildMap {
        obj.keys().forEach { key -> put(key, obj.optString(key, "")) }
    }


    private fun mapActionsToJson(map: Map<String, Any>): JSONObject {
        val obj = JSONObject()

        map.forEach { (key, value) ->
            when (value) {

                // Case 1: Expected List<SwipePointSerializable>
                is List<*> -> {
                    val jsonArray = org.json.JSONArray()

                    value.forEach { element ->
                        if (element is SwipePointSerializable) {
                            // Safe: Convert exactly one element to JSON object
                            val singleJson = SwipeJson.encode(listOf(element))
                            val jsonObj = JSONObject(singleJson.substring(1, singleJson.length - 1))
                            jsonArray.put(jsonObj)
                        } else {
                            // Ignore invalid element
                        }
                    }

                    // Only put valid arrays
                    if (jsonArray.length() > 0) obj.put(key, jsonArray)
                }

                // Case 2: Other primitive values (rare in actions, but still safe)
                is String, is Number, is Boolean -> obj.put(key, value)

                // Case 3: Unknown type → ignore
                else -> { /* skip */ }
            }
        }

        return obj
    }


    private fun jsonToActionsMap(obj: JSONObject): Map<String, Any> = buildMap {

        obj.keys().forEach { key ->
            when (val value = obj.opt(key)) {

                // Case: JSONArray → convert objects back to SwipePointSerializable safely
                is org.json.JSONArray -> {
                    val list = mutableListOf<SwipePointSerializable>()

                    for (i in 0 until value.length()) {
                        val element = value.optJSONObject(i)
                        if (element != null) {
                            try {
                                val json = element.toString()
                                val decoded = SwipeJson.decode("[$json]")
                                list += decoded
                            } catch (_: Exception) {
                                // ignore damaged entries
                            }
                        }
                    }

                    if (list.isNotEmpty()) put(key, list)
                }

                // Case: unexpected primitive → ignore, actions should not store these
                is String, is Number, is Boolean -> {
                    // ignore
                }

                // Case: completely invalid → ignore
                else -> {
                    // ignore
                }
            }
        }
    }

}
