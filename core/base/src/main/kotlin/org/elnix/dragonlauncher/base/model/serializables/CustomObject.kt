package org.elnix.dragonlauncher.base.model.serializables

import androidx.annotation.IntRange
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.serializables.serializers.ColorSerializer
import org.elnix.dragonlauncher.base.model.serializables.serializers.DpSerializer


@Serializable
@SerialName("CustomObject")
public data class CustomObject(
    /**
     * Stroke of the object.
     *
     * The value it **ALWAYS** interpreted as raw dp, then converted into pixels using [androidx.compose.ui.unit.Density]
     */
    @Serializable(with = DpSerializer::class)
    val stroke: Dp,

    /**
     * The color uses by the shape.
     * Defaults to `null` -> uses the given circle color or RGB driven color from angle
     */
    @Serializable(with = ColorSerializer::class)
    val color: Color?,
    /**
     * Optional [CustomGlow] for the shape.
     * Depending on the context it may or may not be used in drawing
     */
    val glow: CustomGlow?,


    val shape: IconShape,

    /**
     * The size of the object, pretty self-explanatory
     * Depending on the context this could mean different things, but it is used as a size discriminant factor for the [CustomObject]
     */
    @Serializable(with = DpSerializer::class)
    val size: Dp,

    /**
     * When the [CustomObject] is using the [shape] parameter, it often uses also the rotation, which is uses to correctly place the shape like the user wants
     */
    @IntRange(from = -1, to = 360)
    val rotation: Int,

    /**
     * Some shapes are drawn in the wrong way, use this to mirror them vertically (and play with [rotation]) to achieve what you want
     */
    val mirror: Boolean = false,

    /**
     * Whether to use the [androidx.compose.ui.graphics.BlendMode.Clear] option when drawing to erase what's under the same canvas
     */
    val eraseBackground: Boolean = false,

    /**
     * In some cases, this can be used to align the shape direction with the angle of the drag to achieve cleaner results
     */
    val alignsWithDragAngle: Boolean = false

) {
    public companion object {

        public val defaultLineCustomObject: CustomObject = CustomObject(
            stroke = 2.dp,
            color = null, // RGB Color according to the angle
            glow = CustomGlow(
                radius = 10.dp
            ),
            shape = IconShape.Circle,
            size = 0.dp, // Unused for line (only stroke)
            rotation = 0, // No rotation for line, (it's nullable, but I use nul here to indicate that the rotation isn't available)

            eraseBackground = false,
            alignsWithDragAngle = false
        )


        public val defaultAngleCustomObject: CustomObject = CustomObject(
            stroke = 2.dp,
            color = null, // RGB Color according to the angle
            glow = CustomGlow(
                radius = 20.dp
            ),
            shape = IconShape.Circle,
            size = 50.dp,
            rotation = 0,
            eraseBackground = false,
            alignsWithDragAngle = false
        )

        public val defaultStartCustomObject: CustomObject = CustomObject(
            stroke = 4.dp,
            color = null, // RGB Color according to the angle
            glow = CustomGlow(
                radius = 32.dp
            ),
            shape = IconShape.Circle,
            size = 30.dp,
            rotation = 0,
            eraseBackground = true,
            alignsWithDragAngle = false
        )

        public val defaultEndCustomObject: CustomObject = CustomObject(
            stroke = (-.5f).dp,
            color = null, // RGB Color according to the angle
            glow = CustomGlow(
                radius = 12.dp
            ),
            shape = IconShape.Circle,
            size = 8.dp,
            rotation = 0,
            eraseBackground = false,
            alignsWithDragAngle = false
        )


        public val defaultHoldCustomObject: CustomObject = CustomObject(
            stroke = 4.dp,
            color = null,
            glow = CustomGlow(
                radius = 12.dp
            ),
            shape = IconShape.Cookie12Sided,
            size = 100.dp,
            rotation = -1,
            eraseBackground = false,
            alignsWithDragAngle = false // Unused for hold
        )


        public data class CustomObjectBlockProperties(
            val allowStrokeCustomization: Boolean = true,
            val allowColorCustomization: Boolean = true,
            val allowShapeCustomization: Boolean = true,
            val allowedShapes: Set<IconShape> = IconShape.allShapes,
            val allowSizeCustomization: Boolean = true,
            val allowEraseBackgroundCustomization: Boolean = true,
            val allowAlignCustomization: Boolean = true,
            val allowMirrorCustomization: Boolean = true,
            val allowRotationCustomization: Boolean = true,

            val allowGlowCustomization: Boolean = true
        )
    }
}
