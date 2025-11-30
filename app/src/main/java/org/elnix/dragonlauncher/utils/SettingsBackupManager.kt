package org.elnix.dragonlauncher.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.data.stores.ColorModesSettingsStore
import org.elnix.dragonlauncher.data.stores.DebugSettingsStore
import org.elnix.dragonlauncher.data.stores.LanguageSettingsStore
import org.elnix.dragonlauncher.data.stores.SwipeSettingsStore
import org.elnix.dragonlauncher.data.stores.UiSettingsStore
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore
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

                putIfNotEmpty("actions", mapStringToJson(SwipeSettingsStore.getAll(ctx)))
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
                    SwipeSettingsStore.setAll(ctx, jsonToMapString(it))
                }

                // ------------------ COLOR MODE ------------------
                obj.optJSONObject("color_mode")?.let {
                    ColorModesSettingsStore.setAll(ctx, jsonToMapString(it))
                }

                // ------------------ COLOR - MAYBE LATER ------------------
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
}
