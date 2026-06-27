package org.elnix.dragonlauncher.base.model.serializables

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.serializables.serializers.ColorSerializer
import org.elnix.dragonlauncher.base.model.serializables.serializers.OffsetSerializer


/**
 * Shape that can be present in any [Nest] to intersect with the points that uses their Ids
 */
@Immutable
@SerialName("IntersectionShape")
@Serializable
public data class IntersectionShape(

    @SerialName("id")
    val id: Int,

    @SerialName("shape")
    val shape: IconShape,

    @SerialName("size")
    val size: Float,

    @SerialName("angle")
    val angle: Int,

    @SerialName("centerOffset")
    @Serializable(with = OffsetSerializer::class)
    val centerOffset: Offset,

    @SerialName("haptic")
    val haptic: CustomHapticFeedback? = null,

    @SerialName("borderStroke")
    val borderStroke: Int? = null,

    @SerialName("color")
    @Serializable(with = ColorSerializer::class)
    val color: Color? = null
) {
    public infix fun scaledBy(scale: Float): IntersectionShape = this.copy(size = this.size * scale)

    public companion object {
        public object Defaults {
            public val borderStrokeDefault: Dp = 2.dp
        }
    }
}

