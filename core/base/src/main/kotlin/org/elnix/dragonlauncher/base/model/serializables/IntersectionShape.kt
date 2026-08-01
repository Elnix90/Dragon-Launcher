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
import org.elnix.dragonlauncher.ktx.takeIfNot


/**
 * Shape that can be present in any [Nest] to intersect with the points that uses their Ids
 */
@Immutable
@Serializable
@SerialName("IntersectionShape")
public data class IntersectionShape(
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
//    public fun scaledBy(scale: Float, defaultIntersectionShape: IntersectionShape): IntersectionShape = this.copy(scale = this.getScale(defaultIntersectionShape) * scale)

    /**
     * Returns the size of this [IntersectionShape], computed with the pixel density
     */
    public inline fun getSize(density: Float, defaultIntersectionShape: IntersectionShape, defaultEditing: Boolean = false): Size =
        Size.rect((this.scale ?: defaultIntersectionShape.scale.takeIfNot(defaultEditing) ?: defaultScale) * defaultSize.dp.value * density)

    public inline fun getOffset(defaultIntersectionShape: IntersectionShape, defaultEditing: Boolean = false): Offset =
        this.offset ?: defaultIntersectionShape.offset.takeIfNot(defaultEditing) ?: defaultOffset

    public inline fun getOffsetX(defaultIntersectionShape: IntersectionShape, defaultEditing: Boolean = false): Float =
        (this.offset?.x ?: defaultIntersectionShape.offset?.x.takeIfNot(defaultEditing) ?: defaultOffset.x).round(2)

    public inline fun getOffsetY(defaultIntersectionShape: IntersectionShape, defaultEditing: Boolean = false): Float =
        (this.offset?.y ?: defaultIntersectionShape.offset?.y.takeIfNot(defaultEditing) ?: defaultOffset.y).round(2)


    public inline fun getScale(defaultIntersectionShape: IntersectionShape, defaultEditing: Boolean = false): Float =
        (this.scale ?: defaultIntersectionShape.scale.takeIfNot(defaultEditing) ?: defaultScale).round(2)

    public inline fun getRotation(defaultIntersectionShape: IntersectionShape, defaultEditing: Boolean = false): Int =
        this.rotation ?: defaultIntersectionShape.rotation.takeIfNot(defaultEditing) ?: defaultRotation

    public inline fun getColor(defaultIntersectionShape: IntersectionShape, extraColors: ExtraColors, defaultEditing: Boolean = false): Color =
        this.color ?: defaultIntersectionShape.color.takeIfNot(defaultEditing) ?: extraColors.shapes

    public inline fun getShape(defaultIntersectionShape: IntersectionShape, defaultEditing: Boolean = false): IconShape =
        this.shape ?: defaultIntersectionShape.shape.takeIfNot(defaultEditing) ?: defaultShape

    public inline fun getBorderStroke(defaultIntersectionShape: IntersectionShape, defaultEditing: Boolean = false): Dp =
        this.borderStroke ?: defaultIntersectionShape.borderStroke.takeIfNot(defaultEditing) ?: defaultBorderStroke

    public inline fun getGlow(defaultIntersectionShape: IntersectionShape, defaultEditing: Boolean = false): CustomGlow =
        this.glow ?: defaultIntersectionShape.glow.takeIfNot(defaultEditing) ?: defaultGlow

    public inline fun getHapticFeedback(defaultIntersectionShape: IntersectionShape, defaultEditing: Boolean = false): CustomHapticFeedback =
        this.haptic ?: defaultIntersectionShape.haptic.takeIfNot(defaultEditing) ?: defaultHapticFeedback

    public inline fun getPointsKeepTheirRelativePosition(defaultIntersectionShape: IntersectionShape, defaultEditing: Boolean = false): Boolean =
        this.pointsKeepTheirRelativePosition
            ?: defaultIntersectionShape.pointsKeepTheirRelativePosition.takeIfNot(defaultEditing)
            ?: defaultPointsKeepTheirRelativePosition

    @Suppress("ConstPropertyName")
    public companion object {
        /**
         * Used to derive a  size from the scale, the main parameter that defines the size of the shape stays the [scale]
         */
        public const val defaultSize: Float = 300f
        public val defaultBorderStroke: Dp = 2.dp

        public val defaultGlow: CustomGlow = CustomGlow(
            color = null,
            radius = 5.dp
        )

        public const val defaultScale: Float = 1f
        public const val defaultRotation: Int = 0

        public val defaultOffset: Offset = Offset.Zero
        public val defaultShape: IconShape = IconShape.Circle
        public const val defaultEraseBackground: Boolean = true
        public const val defaultPointsKeepTheirRelativePosition: Boolean = true

        public val defaultHapticFeedback: CustomHapticFeedback = CustomHapticFeedback.singleTap

        public val emptyIntersectionShape: IntersectionShape = IntersectionShape(-1)

        @Suppress("NOTHING_TO_INLINE")
        public inline fun IntersectionShape.highlightedIfSelected(selected: Boolean, color: Color): IntersectionShape =
            if (selected) this.copy(glow = CustomGlow(color = color, radius = 30.dp)) else this


        public inline val IntersectionShape.isDefault: Boolean
            get() = this.shape == null &&
                    this.scale == null &&
                    this.rotation == null &&
                    this.offset == null &&
                    this.haptic == null &&
                    this.glow == null &&
                    this.color == null &&
                    this.pointsKeepTheirRelativePosition == null
        public inline val IntersectionShape.isNotDefault: Boolean
            get() = !isDefault

        public object DefaultShapeJson : DragonJson<IntersectionShape>()
    }
}
