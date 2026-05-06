package org.elnix.dragonlauncher.common.serializables

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.FLOATING_APPS_TAG
import org.elnix.dragonlauncher.logging.logE

@Serializable
data class FloatingAppObject(
    val id: Int,
    val appWidgetId: Int? = null,
    val nestId: Int?,
    val action: SwipeActionSerializable,
    val spanX: Float = 1f,
    val spanY: Float = 1f,
    val x: Float = 0f,
    val y: Float = 0f,
    val angle: Float = 0f,
    val ghosted: Boolean? = false,
    val foreground: Boolean? = true,
    val shape: IconShape? = null
)


object FloatingAppsJson {
    private val jsonConfig = Json {
        explicitNulls = false
        ignoreUnknownKeys = true
    }

    fun encodeFloatingApps(floatingAppObjects: List<FloatingAppObject>): String =
        jsonConfig.encodeToString(floatingAppObjects)

    fun decodeFloatingApps(jsonString: String): List<FloatingAppObject>? {
        return try {
            jsonConfig.decodeFromString<List<FloatingAppObject>>(jsonString)
        } catch (e: Exception) {
            logE(FLOATING_APPS_TAG, e) { "Floating Apps decode failed, trying legacy" }
            null
        }
    }
}