package org.elnix.dragonlauncher.base.model.serializables

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.DragonJson

@Serializable
@SerialName("Widget")
data class Widget(
    val id: Int,
    val appWidgetId: Int? = null,
    val nestId: Int?,
    val action: Action,
    val spanX: Float = 1f,
    val spanY: Float = 1f,
    val x: Float = 0f,
    val y: Float = 0f,
    val angle: Float = 0f,
    val ghosted: Boolean? = false,
    val foreground: Boolean? = true,
    val shape: IconShape? = null
) {
    companion object {
        object WidgetsJson : DragonJson<List<Widget>>()
    }
}
