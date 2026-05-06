package org.elnix.dragonlauncher.common.serializables

import android.content.ComponentName
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.SWIPE_TAG
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.logging.logW
import java.lang.reflect.Type


// Gson type adapter for sealed class
private class SwipeActionAdapter : JsonSerializer<SwipeActionSerializable>, JsonDeserializer<SwipeActionSerializable> {
    override fun serialize(
        src: SwipeActionSerializable?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        if (src == null) return JsonNull.INSTANCE
        val obj = JsonObject()
        when (src) {

            // Those with more parameters
            is SwipeActionSerializable.LaunchApp -> {
                obj.addProperty("type", "LaunchApp")
                obj.addProperty("packageName", src.packageName)
                obj.addProperty("isPrivateSpace", src.isPrivateSpace)
                obj.addProperty("userId", src.userId)
            }

            is SwipeActionSerializable.OpenUrl -> {
                obj.addProperty("type", "OpenUrl")
                obj.addProperty("url", src.url)
            }

            is SwipeActionSerializable.OpenFile -> {
                obj.addProperty("type", "OpenFile")
                obj.addProperty("uri", src.uri)
                obj.addProperty("mimeType", src.mimeType)
            }

            is SwipeActionSerializable.LaunchShortcut -> {
                obj.addProperty("type", "LaunchShortcut")
                obj.addProperty("packageName", src.packageName)
                obj.addProperty("shortcutId", src.shortcutId)
            }

            is SwipeActionSerializable.OpenCircleNest -> {
                obj.addProperty("type", "OpenCircleNest")
                obj.addProperty("nestId", src.nestId)
            }


            is SwipeActionSerializable.OpenWidget -> {
                obj.addProperty("type", "OpenWidget")
                obj.addProperty("widgetId", "${src.widgetId}")
                obj.addProperty("providerClass", src.providerClass)
                obj.addProperty("providerPackage", src.providerPackage)

            }

            // Those with only the name as param
            is SwipeActionSerializable.NotificationShade -> {
                obj.addProperty("type", "NotificationShade")
            }

            is SwipeActionSerializable.ControlPanel -> {
                obj.addProperty("type", "ControlPanel")
            }

            is SwipeActionSerializable.OpenAppDrawer -> {
                obj.addProperty("type", "OpenAppDrawer")
                if (src.workspaceId != null) {
                    obj.addProperty("workspaceId", src.workspaceId)
                }
            }

//            is SwipeActionSerializable.OpenDragonLauncherSettings -> {
//                obj.addProperty("type", "OpenDragonLauncherSettings")
//                obj.addProperty("route", src.route)
//            }

            is SwipeActionSerializable.Lock -> {
                obj.addProperty("type", "Lock")
            }

            is SwipeActionSerializable.ReloadApps -> {
                obj.addProperty("type", "ReloadApps")
            }

            is SwipeActionSerializable.OpenRecentApps -> {
                obj.addProperty("type", "OpenRecentApps")
            }

            is SwipeActionSerializable.GoParentNest -> {
                obj.addProperty("type", "GoParentNest")
            }

            // Don't handle new actions in this legacy type adapter
            else -> {}
        }
        return obj
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): SwipeActionSerializable? {
        if (json == null || !json.isJsonObject) return null
        val obj = json.asJsonObject
        return try {
            when (obj.get("type").asString) {
                "LaunchApp" -> SwipeActionSerializable.LaunchApp(
                    packageName = obj.get("packageName").asString,
                    isPrivateSpace = obj.get("isPrivateSpace")?.asBoolean ?: false,
                    userId = obj.get("userId")?.asInt ?: 0
                )

                "OpenUrl" -> SwipeActionSerializable.OpenUrl(obj.get("url").asString)
                "OpenFile" -> SwipeActionSerializable.OpenFile(
                    uri = obj.get("uri").asString,
                    mimeType = obj.get("mimeType")?.asString
                )

                "NotificationShade" -> SwipeActionSerializable.NotificationShade
                "ControlPanel" -> SwipeActionSerializable.ControlPanel
                "OpenAppDrawer" -> SwipeActionSerializable.OpenAppDrawer(
                    obj.get("workspaceId")?.asString
                )

//                "OpenDragonLauncherSettings" -> SwipeActionSerializable.OpenDragonLauncherSettings(
//                    obj.get("route")?.asString ?: SETTINGS.ROOT
//                )

                "Lock" -> SwipeActionSerializable.Lock
                "ReloadApps" -> SwipeActionSerializable.ReloadApps
                "OpenRecentApps" -> SwipeActionSerializable.OpenRecentApps
                "LaunchShortcut" -> SwipeActionSerializable.LaunchShortcut(
                    packageName = obj.get("packageName").asString,
                    shortcutId = obj.get("shortcutId").asString
                )

                "OpenCircleNest" -> SwipeActionSerializable.OpenCircleNest(
                    obj.get("nestId").asInt
                )

                "GoParentNest" -> SwipeActionSerializable.GoParentNest
                "OpenWidget" -> {
                    val providerStr = obj.get("provider")?.asString ?: ""

                    val provider = ComponentName.unflattenFromString(providerStr)
                        ?: ComponentName("", "")

                    val widgetId = obj.get("widgetId")?.asInt ?: 0
                    SwipeActionSerializable.OpenWidget(widgetId, provider.packageName, provider.className)
                }

                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

object LegacySwipeJson {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(SwipeActionSerializable::class.java, SwipeActionAdapter())
        .registerTypeAdapter(IconShape::class.java, IconShapeAdapter())
        .create()

    private val pointsType = object : TypeToken<List<SwipePointSerializable>>() {}.type
    private val nestsType = object : TypeToken<List<CircleNest>>() {}.type


    /* ───────────── Points ───────────── */

    fun decodePoints(json: String): List<SwipePointSerializable> =
        legacyDecodeSafe(json, pointsType)

    /* ───────────── Nests ───────────── */

    fun decodeNests(json: String): List<CircleNest> =
        legacyDecodeSafe(json, nestsType)


    fun decodeAction(jsonString: String): SwipeActionSerializable? {
        if (jsonString.isBlank() || jsonString == "{}") return null
        return try {
            gson.fromJson(jsonString, SwipeActionSerializable::class.java)
        } catch (_: Throwable) {
            null
        }
    }


    private fun <T> legacyDecodeSafe(json: String, type: Type): List<T> {
        if (json.isBlank()) {
            logW(SWIPE_TAG) { "Legacy json is empty, using emptyList()" }
            return emptyList()
        }
        return try {
            gson.fromJson(json, type)
        } catch (e: Throwable) {
            logE(SWIPE_TAG, e) { "Legacy decode failed too, using emptyList()" }
            emptyList()
        }
    }
}