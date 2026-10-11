@file:Suppress("NOTHING_TO_INLINE")

package org.elnix.dragonlauncher.base.model.serializables

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.DragonJson
import org.elnix.dragonlauncher.base.model.serializables.serializers.ColorSerializer
import org.elnix.dragonlauncher.base.model.serializables.serializers.DpSerializer
import org.elnix.dragonlauncher.base.model.serializables.serializers.OffsetSerializer
import org.elnix.dragonlauncher.base.theme.ExtraColors
import org.elnix.dragonlauncher.ktx.rect
import org.elnix.dragonlauncher.ktx.round
import org.elnix.dragonlauncher.ktx.unless

/**
 * Shape that can be present in any [Nest] to intersect with the points that uses their Ids
 */
@Immutable
@Serializable
@SerialName("IntersectionShape")
data class IntersectionShape(
	val id: Int,
	val shape: IconShape? = null,
	@FloatRange(from = 0.0)
	val scale: Float? = null,
	@IntRange(from = 0, to = 360)
	val rotation: Int? = null,
	@Serializable(with = OffsetSerializer::class)
	val offset: Offset? = null,
	val haptic: CustomHapticFeedback? = null,
	@Serializable(with = DpSerializer::class)
	val borderStroke: Dp? = null,
	@Serializable(with = ColorSerializer::class)
	val color: Color? = null,
	val glow: CustomGlow? = null,
	/**
	 * Whether if when moved, the points offsets are adapted to match their original angle relative to this shape,
	 * or if they aren't taken into account and that the shape moves regardless of the points in it.
	 * When its `false` the points might rotate around their hidden underlying offset aas the shape moves
	 */
	val pointsKeepTheirRelativePosition: Boolean? = null
) {
//    fun scaledBy(scale: Float, defaultIntersectionShape: IntersectionShape): IntersectionShape = this.copy(scale = this.getScale(defaultIntersectionShape) * scale)

	/**
	 * Returns the size of this [IntersectionShape], computed with the pixel density
	 */
	inline fun getSize(
		density: Float,
		defaultIntersectionShape: IntersectionShape,
		isDefaultEditing: Boolean
	): Size =
		Size.rect(
			(this.scale ?: (defaultIntersectionShape.scale unless isDefaultEditing) ?: defaultScale) *
				defaultSize.dp.value *
				density
		)

	inline fun getOffset(defaultIntersectionShape: IntersectionShape, isDefaultEditing: Boolean): Offset =
		this.offset ?: (defaultIntersectionShape.offset unless isDefaultEditing) ?: defaultOffset

	inline fun getOffsetX(defaultIntersectionShape: IntersectionShape, isDefaultEditing: Boolean): Float =
		(this.offset?.x ?: defaultIntersectionShape.offset?.x.unless(isDefaultEditing) ?: defaultOffset.x).round(2)

	inline fun getOffsetY(defaultIntersectionShape: IntersectionShape, isDefaultEditing: Boolean): Float =
		(this.offset?.y ?: defaultIntersectionShape.offset?.y.unless(isDefaultEditing) ?: defaultOffset.y).round(2)

	inline fun getScale(defaultIntersectionShape: IntersectionShape, isDefaultEditing: Boolean): Float =
		(this.scale ?: (defaultIntersectionShape.scale unless isDefaultEditing) ?: defaultScale).round(2)

	inline fun getRotation(defaultIntersectionShape: IntersectionShape, isDefaultEditing: Boolean): Int =
		this.rotation ?: (defaultIntersectionShape.rotation unless isDefaultEditing) ?: defaultRotation

	inline fun getColor(defaultIntersectionShape: IntersectionShape, extraColors: ExtraColors, isDefaultEditing: Boolean): Color =
		this.color ?: (defaultIntersectionShape.color unless isDefaultEditing) ?: extraColors.shapes

	inline fun getShape(defaultIntersectionShape: IntersectionShape, isDefaultEditing: Boolean): IconShape =
		this.shape ?: (defaultIntersectionShape.shape unless isDefaultEditing) ?: defaultShape

	inline fun getBorderStroke(defaultIntersectionShape: IntersectionShape, isDefaultEditing: Boolean): Dp =
		this.borderStroke ?: (defaultIntersectionShape.borderStroke unless isDefaultEditing) ?: defaultBorderStroke

	inline fun getGlow(defaultIntersectionShape: IntersectionShape, isDefaultEditing: Boolean): CustomGlow =
		this.glow ?: (defaultIntersectionShape.glow unless isDefaultEditing) ?: defaultGlow

	inline fun getHapticFeedback(defaultIntersectionShape: IntersectionShape, isDefaultEditing: Boolean): CustomHapticFeedback =
		this.haptic ?: (defaultIntersectionShape.haptic unless isDefaultEditing) ?: defaultHapticFeedback

	inline fun getPointsKeepTheirRelativePosition(defaultIntersectionShape: IntersectionShape, isDefaultEditing: Boolean): Boolean =
		this.pointsKeepTheirRelativePosition
			?: (defaultIntersectionShape.pointsKeepTheirRelativePosition unless isDefaultEditing)
			?: defaultPointsKeepTheirRelativePosition

	@Suppress("ConstPropertyName")
	companion object {
		/**
		 * Used to derive a  size from the scale, the main parameter that defines the size of the shape stays the [scale]
		 */
		const val defaultSize: Float = 300f
		val defaultBorderStroke: Dp = 2.dp

		val defaultGlow: CustomGlow =
			CustomGlow(
				color = null,
				radius = 5.dp
			)

		const val defaultScale: Float = 1f
		const val defaultRotation: Int = 0

		val defaultOffset: Offset = Offset.Zero
		val defaultShape: IconShape = IconShape.Circle
		const val defaultEraseBackground: Boolean = true
		const val defaultPointsKeepTheirRelativePosition: Boolean = true

		val defaultHapticFeedback: CustomHapticFeedback = CustomHapticFeedback.singleTap

		val emptyIntersectionShape: IntersectionShape = IntersectionShape(-1)

		@Suppress("NOTHING_TO_INLINE")
		inline fun IntersectionShape.highlightedIfSelected(selected: Boolean, color: Color): IntersectionShape =
			if (selected) this.copy(glow = CustomGlow(color = color, radius = 30.dp)) else this

		inline val IntersectionShape.isDefault: Boolean
			get() =
				this.shape == null &&
					this.scale == null &&
					this.rotation == null &&
					this.offset == null &&
					this.haptic == null &&
					this.glow == null &&
					this.color == null &&
					this.pointsKeepTheirRelativePosition == null
		inline val IntersectionShape.isNotDefault: Boolean
			get() = !isDefault

		object DefaultShapeJson : DragonJson<IntersectionShape>()
	}
}
