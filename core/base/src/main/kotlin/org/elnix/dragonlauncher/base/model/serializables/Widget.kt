package org.elnix.dragonlauncher.base.model.serializables

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.DragonJson

@Suppress("ConstPropertyName")
@Serializable
@Immutable
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
		const val defaultGhosted: Boolean = false
		const val defaultForeground: Boolean = true

		const val MIN_SIZE: Float = 1.5f

		object WidgetsJson : DragonJson<List<Widget>>()
	}
}
