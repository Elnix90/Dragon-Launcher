package org.elnix.dragonlauncher.base.model.serializables

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.serializables.serializers.ColorSerializer
import org.elnix.dragonlauncher.base.model.serializables.serializers.OffsetSerializer


/**
 * Shape that can be present in any [Nest] to intersect with the points that uses their Ids
 */
@Immutable
@Serializable
@SerialName("IntersectionShape")
public data class IntersectionShape(

    @SerialName("id")
    val id: Int,

    @SerialName("shape")
    val shape: IconShape,

    @SerialName("size")
    val scale: Float = 1f,

    @SerialName("angle")
    val angle: Float = 0f,

    @SerialName("offset")
    @Serializable(with = OffsetSerializer::class)
    val offset: Offset,

    @SerialName("haptic")
    val haptic: CustomHapticFeedback? = null,

    @SerialName("borderStroke")
    val borderStroke: Float? = null,

    @SerialName("color")
    @Serializable(with = ColorSerializer::class)
    val color: Color? = null,

    @SerialName("glow")
    val glow: CustomGlow? = CustomGlow(
        color = color,
        radius = 5f
    )
) {
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

    @Suppress("ConstPropertyName")
    public companion object {
        public const val borderStrokeDefault: Float = 2f
        public const val defaultSize: Float = 300f
    }
}

