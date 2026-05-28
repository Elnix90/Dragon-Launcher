package org.elnix.dragonlauncher.common.serializables

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Widget")
data class Widget(
    val id: Int,
    val appWidgetId: Int? = null,
    val nestId: Int?,
    val action: SwipeAction? = null,
    val spanX: Float = 1f,
    val spanY: Float = 1f,
    val x: Float = 0f,
    val y: Float = 0f,
    val angle: Float = 0f,
    val ghosted: Boolean? = false,
    val foreground: Boolean? = true,
    val shape: IconShape? = null
)


object FloatingAppsJson: DragonJson<List<Widget>>()