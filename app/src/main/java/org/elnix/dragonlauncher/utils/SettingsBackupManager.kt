package org.elnix.dragonlauncher.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.data.DataStoreName
import org.elnix.dragonlauncher.data.SwipeJson
import org.elnix.dragonlauncher.data.stores.ColorModesSettingsStore
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore
import org.elnix.dragonlauncher.data.stores.DebugSettingsStore
import org.elnix.dragonlauncher.data.stores.DrawerSettingsStore
import org.elnix.dragonlauncher.data.stores.LanguageSettingsStore
import org.elnix.dragonlauncher.data.stores.SwipeSettingsStore
import org.elnix.dragonlauncher.data.stores.UiSettingsStore
import org.elnix.dragonlauncher.data.stores.WorkspaceSettingsStore
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter

object SettingsBackupManager {

    private const val TAG = "SettingsBackupManager"

    /**
     * Exports only the requested stores.
     * @param requestedStores List of DataStoreName objects
     */
    suspend fun exportSettings(ctx: Context, uri: Uri, requestedStores: List<DataStoreName>) {
        try {
            val json = JSONObject().apply {
                requestedStores.forEach { store ->
                    when (store) {
                        DataStoreName.SWIPE -> {
                            val pointsList = SwipeSettingsStore.getAll(ctx)
                            if (pointsList.isNotEmpty()) {
                                put(store.backupKey, JSONArray(SwipeJson.encodePretty(pointsList)))
                            }
                        }
                        DataStoreName.DRAWER -> DrawerSettingsStore.getAll(ctx).takeIf { it.isNotEmpty() }?.let {
                            put(store.backupKey, JSONObject(it))
                        }
                        DataStoreName.COLOR_MODE -> ColorModesSettingsStore.getAll(ctx).takeIf { it.isNotEmpty() }?.let {
                            put(store.backupKey, JSONObject(it))
                        }
                        DataStoreName.COLOR -> ColorSettingsStore.getAll(ctx).takeIf { it.isNotEmpty() }?.let {
                            put(store.backupKey, JSONObject(it))
                        }
                        DataStoreName.PRIVATE_SETTINGS -> DebugSettingsStore.getAll(ctx).takeIf { it.isNotEmpty() }?.let {
                            put(store.backupKey, JSONObject(it))
                        }
                        DataStoreName.LANGUAGE -> LanguageSettingsStore.getAll(ctx).takeIf { it.isNotEmpty() }?.let {
                            put(store.backupKey, JSONObject(it))
                        }
                        DataStoreName.UI -> UiSettingsStore.getAll(ctx).takeIf { it.isNotEmpty() }?.let {
                            put(store.backupKey, JSONObject(it))
                        }
                        DataStoreName.DEBUG -> DebugSettingsStore.getAll(ctx).takeIf { it.isNotEmpty() }?.let {
                            put(store.backupKey, JSONObject(it))
                        }
                        DataStoreName.WORKSPACES -> {
                            val obj = WorkspaceSettingsStore.getAll(ctx)
                            if (obj.length() > 0) {
                                put(store.backupKey, obj)
                            }
                        }

                        DataStoreName.APPS -> null
                    }
                }
            }

            Log.d(TAG, "Generated JSON: $json")

//            withContext(Dispatchers.IO) {
//                ctx.contentResolver.openOutputStream(uri)?.use { output ->
//                    OutputStreamWriter(output).use { it.write(json.toString(2)) }
//                }
//            }
            withContext(Dispatchers.IO) {
                val output = ctx.contentResolver.openOutputStream(uri)
                if (output == null) {
                    Log.e(TAG, "Failed to open OutputStream - URI permission expired!")
                    throw IllegalStateException("Cannot write to URI - permission expired")
                }
                output.use {
                    OutputStreamWriter(it).use { writer ->
                        writer.write(json.toString(2))
                    }
                }
            }


            Log.i(TAG, "Export completed successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error during export", e)
            throw e
        }
    }

    /**
     * Imports only the requested stores from the backup JSON file.
     * @param requestedStores List of DataStoreName objects to restore.
     */
    suspend fun importSettings(ctx: Context, uri: Uri, requestedStores: List<DataStoreName>) {
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
                requestedStores.forEach { store ->
                    when (store) {
                        DataStoreName.SWIPE -> obj.optJSONArray(store.backupKey)?.let { jsonArr ->
                            val pointsString = jsonArr.toString()
                            val pointsList = SwipeJson.decode(pointsString)
                            SwipeSettingsStore.save(ctx, pointsList)
                        }
                        DataStoreName.DRAWER -> obj.optJSONObject(store.backupKey)?.let {
                            DrawerSettingsStore.setAll(ctx, jsonToStringMap(it))
                        }
                        DataStoreName.COLOR_MODE -> obj.optJSONObject(store.backupKey)?.let {
                            ColorModesSettingsStore.setAll(ctx, jsonToStringMap(it))
                        }
                        DataStoreName.COLOR -> obj.optJSONObject(store.backupKey)?.let {
                            ColorSettingsStore.setAll(ctx, jsonToIntMap(it))
                        }
                        DataStoreName.PRIVATE_SETTINGS -> obj.optJSONObject(store.backupKey)?.let {
                            DebugSettingsStore.setAll(ctx, jsonToBooleanMap(it))
                        }
                        DataStoreName.LANGUAGE -> obj.optJSONObject(store.backupKey)?.let {
                            LanguageSettingsStore.setAll(ctx, jsonToStringMap(it))
                        }
                        DataStoreName.UI -> obj.optJSONObject(store.backupKey)?.let {
                            UiSettingsStore.setAll(ctx, jsonToStringMap(it))
                        }
                        DataStoreName.DEBUG -> obj.optJSONObject(store.backupKey)?.let {
                            DebugSettingsStore.setAll(ctx, jsonToBooleanMap(it))
                        }
                        DataStoreName.WORKSPACES -> obj.optJSONObject(store.backupKey)?.let {
                            WorkspaceSettingsStore.setAll(ctx, it)
                        }

                        DataStoreName.APPS -> null
                    }
                }
            }

            Log.i(TAG, "Import completed successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error during import", e)
            throw e
        }
    }

    /**
     * Imports settings directly from parsed JSON (no file needed).
     * @param requestedStores List of DataStoreName objects to restore.
     */
    suspend fun importSettingsFromJson(ctx: Context, jsonObj: JSONObject, requestedStores: List<DataStoreName>) {
        try {
            withContext(Dispatchers.IO) {
                requestedStores.forEach { store ->
                    when (store) {
                        DataStoreName.SWIPE -> jsonObj.optJSONArray(store.backupKey)?.let { jsonArr ->
//                            val pointsString = jsonArr.toString()
                            val cleanJsonString = jsonArr.toString(2) // Pretty print = clean JSON

                            Log.e("Debug", "Raw array: $jsonArr")
                            Log.e("Debug", "Clean JSON: $cleanJsonString")

                            val pointsList = SwipeJson.decode(cleanJsonString)

                            Log.e("Debug", "Decoded points: $pointsList")
                            SwipeSettingsStore.save(ctx, pointsList)
                        }
                        DataStoreName.DRAWER -> jsonObj.optJSONObject(store.backupKey)?.let {
                            DrawerSettingsStore.setAll(ctx, jsonToStringMap(it))
                        }
                        DataStoreName.COLOR_MODE -> jsonObj.optJSONObject(store.backupKey)?.let {
                            ColorModesSettingsStore.setAll(ctx, jsonToStringMap(it))
                        }
                        DataStoreName.COLOR -> jsonObj.optJSONObject(store.backupKey)?.let {
                            ColorSettingsStore.setAll(ctx, jsonToIntMap(it))
                        }
                        DataStoreName.PRIVATE_SETTINGS -> jsonObj.optJSONObject(store.backupKey)?.let {
                            DebugSettingsStore.setAll(ctx, jsonToBooleanMap(it))
                        }
                        DataStoreName.LANGUAGE -> jsonObj.optJSONObject(store.backupKey)?.let {
                            LanguageSettingsStore.setAll(ctx, jsonToStringMap(it))
                        }
                        DataStoreName.UI -> jsonObj.optJSONObject(store.backupKey)?.let {
                            UiSettingsStore.setAll(ctx, jsonToStringMap(it))
                        }
                        DataStoreName.DEBUG -> jsonObj.optJSONObject(store.backupKey)?.let {
                            DebugSettingsStore.setAll(ctx, jsonToBooleanMap(it))
                        }
                        DataStoreName.WORKSPACES -> jsonObj.optJSONObject(store.backupKey)?.let {
                            WorkspaceSettingsStore.setAll(ctx, it)
                        }

                        DataStoreName.APPS -> null
                    }
                }
            }
            Log.i(TAG, "Import from JSON completed successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error during JSON import", e)
            throw e
        }
    }


    private fun jsonToBooleanMap(obj: JSONObject) = buildMap {
        obj.keys().forEach { key -> put(key, obj.optBoolean(key)) }
    }

    private fun jsonToIntMap(obj: JSONObject) = buildMap {
        obj.keys().forEach { key -> put(key, obj.optInt(key)) }
    }

    private fun jsonToStringMap(obj: JSONObject) = buildMap {
        obj.keys().forEach { key -> put(key, obj.optString(key)) }
    }
}
