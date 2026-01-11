package org.elnix.dragonlauncher.data

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
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.data.helpers.SwipePointSerializable
import org.elnix.dragonlauncher.utils.logs.logE
import java.lang.reflect.Type
import java.util.UUID


fun dummySwipePoint(
    action: SwipeActionSerializable? = null,
    id: String? = null
) =
    SwipePointSerializable(
        circleNumber = 0,
        angleDeg = 0.0,
        action = action ?: SwipeActionSerializable.OpenDragonLauncherSettings,
        id = id ?: UUID.randomUUID().toString(),
        nestId = 0
    )

val defaultSwipePointsValues = dummySwipePoint(null).copy(
    borderStroke = 4f,
    borderStrokeSelected = 8f,
    opacity = 1f,
    cornerRadius = null,
    paddingDp = 0,
    haptic = false
)

/**
 * New CircleNest system, where every bloc of circles is contained inside one of those*
 * This way, we can navigate across those nests, to achieve more actions, using the jump actions
 */
data class CircleNest(
    /**
     *  By default the id 0 is the first nest that is available,
     *  I'll try to make the old system importable, to avoid breaking changes like empty actions circle
     */
    @SerializedName("id") val id: Int = 0,
    /**
     * Holds the cancel zone (index -1), and the circle numbers for each drag distances
     * for all the circles in the nest (index positive integer)
     * the key is the circle number, made for allowing not ascending order drag distances
     * For the last one, the drag distance has no limit, it's not even counted
     */
    @SerializedName("dragDistances") val dragDistances: Map<Int, Int> = mapOf(
        -1 to 150,
        0 to 400,
        1 to 700,
        2 to 1000
    ),

    /**
     * The id of the nest that holds this one, used for drawing correctly the outer circles
     * And also to navigate across nests
     */
    @SerializedName("parentId") val parentId: Int = 0
)




/**
 * Swipe Actions Serializable, the core of the main gesture idea
 * Holds all the different actions the user can do
 */
@Serializable
sealed class SwipeActionSerializable {
    @Serializable data class LaunchApp(val packageName: String) : SwipeActionSerializable()
    @Serializable
    data class LaunchShortcut(
        val packageName: String,
        val shortcutId: String
    ) : SwipeActionSerializable()

    @Serializable data class OpenUrl(val url: String) : SwipeActionSerializable()
    @Serializable data class OpenFile(
        val uri: String,
        val mimeType: String? = null
    ) : SwipeActionSerializable()
    @Serializable object NotificationShade : SwipeActionSerializable()
    @Serializable object ControlPanel : SwipeActionSerializable()
    @Serializable object OpenAppDrawer : SwipeActionSerializable()
    @Serializable object  OpenDragonLauncherSettings: SwipeActionSerializable()
    @Serializable object Lock: SwipeActionSerializable()
    @Serializable object ReloadApps: SwipeActionSerializable()

    @Serializable object OpenRecentApps: SwipeActionSerializable()
    @Serializable data class OpenCircleNest(val nestId: Int): SwipeActionSerializable()
    @Serializable object GoParentNest: SwipeActionSerializable()
    @Serializable data class OpenWidget(
        val widgetId: Int,
        val provider: ComponentName
    ): SwipeActionSerializable()
}

// Gson type adapter for sealed class
class SwipeActionAdapter : JsonSerializer<SwipeActionSerializable>, JsonDeserializer<SwipeActionSerializable> {
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
                obj.addProperty("provider", "${src.provider.packageName}:${src.provider.className}")

            }

            // Those with only the name as param
            is SwipeActionSerializable.NotificationShade -> { obj.addProperty("type", "NotificationShade") }
            is SwipeActionSerializable.ControlPanel -> { obj.addProperty("type", "ControlPanel") }
            is SwipeActionSerializable.OpenAppDrawer -> { obj.addProperty("type", "OpenAppDrawer") }
            is SwipeActionSerializable.OpenDragonLauncherSettings -> { obj.addProperty("type", "OpenDragonLauncherSettings") }
            is SwipeActionSerializable.Lock -> { obj.addProperty("type", "Lock") }
            is SwipeActionSerializable.ReloadApps -> { obj.addProperty("type", "ReloadApps") }
            is SwipeActionSerializable.OpenRecentApps -> { obj.addProperty("type", "OpenRecentApps") }
            is SwipeActionSerializable.GoParentNest -> { obj.addProperty("type", "GoParentNest") }

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
        return when (obj.get("type").asString) {
            "LaunchApp" -> SwipeActionSerializable.LaunchApp(obj.get("packageName").asString)
            "OpenUrl" -> SwipeActionSerializable.OpenUrl(obj.get("url").asString)
            "OpenFile" -> SwipeActionSerializable.OpenFile(
                obj.get("uri").asString,
                obj.get("mimeType")?.asString
            )
            "NotificationShade" -> SwipeActionSerializable.NotificationShade
            "ControlPanel" -> SwipeActionSerializable.ControlPanel
            "OpenAppDrawer" -> SwipeActionSerializable.OpenAppDrawer
            "OpenDragonLauncherSettings" -> SwipeActionSerializable.OpenDragonLauncherSettings
            "Lock" -> SwipeActionSerializable.Lock
            "ReloadApps" -> SwipeActionSerializable.ReloadApps
            "OpenRecentApps" -> SwipeActionSerializable.OpenRecentApps
            "LaunchShortcut" -> SwipeActionSerializable.LaunchShortcut(
                obj.get("packageName").asString,
                obj.get("shortcutId").asString
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
                SwipeActionSerializable.OpenWidget(widgetId, provider)
            }
            else -> null
        }
    }
}

object SwipeJson {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(SwipeActionSerializable::class.java, SwipeActionAdapter())
        .create()

    private val gsonPretty: Gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(SwipeActionSerializable::class.java, SwipeActionAdapter())
        .create()

    private val pointsType = object : TypeToken<List<SwipePointSerializable>>() {}.type
    private val nestsType = object : TypeToken<List<CircleNest>>() {}.type


    /* ---------- Old format, keep it for legacy support ---------- */
    private val listType = object : TypeToken<List<SwipePointSerializable>>() {}.type

    fun decode(jsonString: String): List<SwipePointSerializable> {
        if (jsonString.isBlank()) return emptyList()
        return try {
            gson.fromJson(jsonString, listType)
        } catch (e: Throwable) {
            logE("SwipeJson", "Decode failed: ${e.message}", e)
            emptyList()
        }
    }

    /* ---------- Points ---------- */

    fun encodePoints(points: List<SwipePointSerializable>): String =
        gson.toJson(points, pointsType)

    fun encodePointsPretty(points: List<SwipePointSerializable>): String =
        gsonPretty.toJson(points, pointsType)

    fun decodePoints(json: String): List<SwipePointSerializable> =
        decodeSafe(json, pointsType)

    /* ---------- Nests ---------- */

    fun encodeNests(nests: List<CircleNest>): String =
        gson.toJson(nests, nestsType)

    fun encodeNestsPretty(nests: List<CircleNest>): String =
        gsonPretty.toJson(nests, nestsType)

    fun decodeNests(json: String): List<CircleNest> =
        decodeSafe(json, nestsType)


    fun encodeAction(action: SwipeActionSerializable?): String =
        gson.toJson(action, SwipeActionSerializable::class.java)


    fun decodeAction(jsonString: String): SwipeActionSerializable? {
        if (jsonString.isBlank() || jsonString == "{}") return null
        return try {
            gson.fromJson(jsonString, SwipeActionSerializable::class.java)
        } catch (_: Throwable) {
            null
        }
    }


    private fun <T> decodeSafe(json: String, type: Type): List<T> {
        if (json.isBlank()) return emptyList()
        return try {
            gson.fromJson(json, type)
        } catch (e: Throwable) {
            logE("SwipeJson", "Decode failed: ${e.message}", e)
            emptyList()
        }
    }
}


/**
 * Used to reach the same package name, just since they are different instances, the compiler
 * complains about accessing directly .packageName
 */
fun SwipeActionSerializable.targetPackage(): String? = when (this) {
    is SwipeActionSerializable.LaunchApp -> packageName
    is SwipeActionSerializable.LaunchShortcut -> packageName
    else -> null
}
