package org.elnix.dragonlauncher.data

import com.google.gson.*
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

// Keep the same data classes, no @Serializable needed
data class SwipePointSerializable(
    @SerializedName("a") val circleNumber: Int,
    @SerializedName("b") val angleDeg: Double,
    @SerializedName("c") val action: SwipeActionSerializable? = null,
    @SerializedName("d") val id: String? = null
)

// Use sealed class for actions
sealed class SwipeActionSerializable {
    data class LaunchApp(val packageName: String) : SwipeActionSerializable()
    data class OpenUrl(val url: String) : SwipeActionSerializable()
    object NotificationShade : SwipeActionSerializable()
    object ControlPanel : SwipeActionSerializable()
    object OpenAppDrawer : SwipeActionSerializable()
    object  OpenDragonLauncherSettings: SwipeActionSerializable()
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
            is SwipeActionSerializable.LaunchApp -> {
                obj.addProperty("type", "LaunchApp")
                obj.addProperty("packageName", src.packageName)
            }
            is SwipeActionSerializable.OpenUrl -> {
                obj.addProperty("type", "OpenUrl")
                obj.addProperty("url", src.url)
            }
            SwipeActionSerializable.NotificationShade -> obj.addProperty("type", "NotificationShade")
            SwipeActionSerializable.ControlPanel -> obj.addProperty("type", "ControlPanel")
            SwipeActionSerializable.OpenAppDrawer -> obj.addProperty("type", "OpenAppDrawer")
            SwipeActionSerializable.OpenDragonLauncherSettings -> obj.addProperty("type", "OpenDragonLauncherSettings")
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
            "NotificationShade" -> SwipeActionSerializable.NotificationShade
            "ControlPanel" -> SwipeActionSerializable.ControlPanel
            "OpenAppDrawer" -> SwipeActionSerializable.OpenAppDrawer
            "OpenDragonLauncherSettings" -> SwipeActionSerializable.OpenDragonLauncherSettings
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

    private val listType = object : TypeToken<List<SwipePointSerializable>>() {}.type

    fun encode(points: List<SwipePointSerializable>): String = gson.toJson(points, listType)

    fun encodePretty(points: List<SwipePointSerializable>): String = gsonPretty.toJson(points, listType)

    fun decode(jsonString: String): List<SwipePointSerializable> {
        if (jsonString.isBlank()) return emptyList()
        return try {
            gson.fromJson(jsonString, listType)
        } catch (_: Throwable) {
            emptyList()
        }
    }
}
