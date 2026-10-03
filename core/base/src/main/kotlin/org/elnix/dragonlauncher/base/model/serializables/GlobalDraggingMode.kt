package org.elnix.dragonlauncher.base.model.serializables

import androidx.annotation.FloatRange
import androidx.compose.runtime.Stable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.elnix.dragonlauncher.base.model.DragonJson
import org.elnix.dragonlauncher.i18n.R

/**
 * The Launcher-wide dragging mode.
 */
@Serializable
@SerialName("GlobalDraggingMode")
@Stable
public sealed class GlobalDraggingMode {
	public abstract val title: Int
	public abstract val description: Int
// 	public abstract val icon: Int

	/**
	 * The Default dragging mode: you drop your finger at a specified position and start dragging to open the nests
	 * It's the original dragging method of Dragon-Launcher
	 */
	@Serializable
	@SerialName("Normal")
	public data object Normal : GlobalDraggingMode() {
		@Transient
		override val title: Int = R.string.dragging_mode_normal

		@Transient
		override val description: Int = R.string.dragging_mode_normal_desc
	}

	/**
	 * A new dragging method: the current nest is positioned in a fixed area on your screen.
	 * You can drag by touching the screen anywhere, and it will behave as if the start pos is the Fixed position defined
	 */
	@Serializable
	@SerialName("Fixed")
	public data class Fixed(
		@FloatRange(0.0, 1.0)
		val xRatio: Float = 0.5f,
		@FloatRange(0.0, 1.0)
		val yRatio: Float = 0.5f
	) : GlobalDraggingMode() {
		@Transient
		override val title: Int = R.string.dragging_mode_fixed

		@Transient
		override val description: Int = R.string.dragging_mode_fixed_desc
	}

	public companion object {
		public val default: GlobalDraggingMode by lazy {
			Normal
		}

		public val DraggingModeList: List<GlobalDraggingMode> by lazy {
			listOf(
				Normal,
				Fixed()
			)
		}

		/**
		 * Returns the same dragging mode but
		 */
		public fun GlobalDraggingMode.toDummy(): GlobalDraggingMode = when (this) {
			is Fixed -> Fixed(0f, 0f)
			Normal -> Normal
		}

		public object GlobalDraggingModeJson : DragonJson<GlobalDraggingMode>()
	}
}
