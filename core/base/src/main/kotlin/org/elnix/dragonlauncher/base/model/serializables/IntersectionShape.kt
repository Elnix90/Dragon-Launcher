package org.elnix.dragonlauncher.base.model.serializables

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape.Companion.IntersectionShapeDefaults.defaultSize
import org.elnix.dragonlauncher.base.model.serializables.serializers.ColorSerializer
import org.elnix.dragonlauncher.base.model.serializables.serializers.OffsetSerializer


/**
 * Shape that can be present in any [Nest] to intersect with the points that uses their Ids
 */
@Immutable
@Serializable
@SerialName("IntersectionShape")
public data class IntersectionShape(
    val id: Int,
    val shape: IconShape = IntersectionShapeDefaults.defaultShape,
    val scale: Float = IntersectionShapeDefaults.defaultScale,
    val angle: Float = IntersectionShapeDefaults.defaultAngle,
    @Serializable(with = OffsetSerializer::class)
    val offset: Offset = IntersectionShapeDefaults.defaultOffset,
    val haptic: CustomHapticFeedback? = null,
    val borderStroke: Float? = null,
    @Serializable(with = ColorSerializer::class)
    val color: Color? = null,
    val glow: CustomGlow? = IntersectionShapeDefaults.defaultGlow
) : Comparable<IntersectionShape> {
    public infix fun scaledBy(scale: Float): IntersectionShape = this.copy(scale = this.scale * scale)

//    /**
//     * Shape scale computed from its size and the default size
//     */
//    public fun getScale(): Float = this.size / defaultSize
    /**
     * Returns the size of this [IntersectionShape], computed with the pixel density
     */
    public fun getSize(density: Float): Size {
        val sidePx = this.scale * defaultSize.dp.value * density
        return Size(sidePx, sidePx)
    }

//    /**
//     * Used by the path cache resolver to not recompute twice the same instance of a [androidx.compose.ui.graphics.Path]
//     */
//    override fun hashCode(): Int {
//        return (shape.hashCode()  + angle)
//    }

    override fun compareTo(other: IntersectionShape): Int = id

    @Suppress("ConstPropertyName")
    public companion object {

        public object IntersectionShapeDefaults {
            public const val borderStrokeDefault: Float = 2f
            public const val defaultSize: Float = 300f

            public val defaultGlow: CustomGlow =  CustomGlow(
                color = null,
                radius = 5f
            )

            public const val defaultScale: Float = 1f
            public const val defaultAngle: Float = 0f

            public val defaultOffset: Offset = Offset.Zero
            public val defaultShape: IconShape = IconShape.Circle
            public const val defaultEraseBackground: Boolean = true

            public val defaultHapticFeedback: CustomHapticFeedback = CustomHapticFeedback.singleTap
        }

        @Suppress("NOTHING_TO_INLINE")
        public inline fun IntersectionShape.highlightedIfSelected(selected: Boolean, color: Color): IntersectionShape =
            if (selected) this.copy(glow = CustomGlow(color = color, radius = 30f)) else this
    }
}

