package org.elnix.dragonlauncher.base.model.serializables

import androidx.annotation.IntRange
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.serializables.serializers.ColorSerializer


@Serializable
@SerialName("CustomObject")
public data class CustomObject(
    /**
     * Stroke of the object.
     *
     * The value it **ALWAYS** interpreted as raw dp, then converted into pixels using [androidx.compose.ui.unit.Density]
     */
    val stroke: Float,
    @Serializable(with = ColorSerializer::class)

    /**
     * The color uses by the shape.
     * Defaults to `null` -> uses the given circle color or RGB driven color from angle
     */
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
     * The value it **ALWAYS** interpreted as raw dp, then converted into pixels using [androidx.compose.ui.unit.Density]
     */
    val size: Float,

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
            stroke = 2f,
            color = null, // RGB Color according to the angle
            glow = CustomGlow(
                radius = 10f
            ),
            shape = IconShape.Circle,
            size = 0f, // Unused for line (only stroke)
            rotation = 0, // No rotation for line, (it's nullable, but I use nul here to indicate that the rotation isn't available)

            eraseBackground = false,
            alignsWithDragAngle = false
        )


        public val defaultAngleCustomObject: CustomObject = CustomObject(
            stroke = 2f,
            color = null, // RGB Color according to the angle
            glow = CustomGlow(
                radius = 20f
            ),
            shape = IconShape.Circle,
            size = 50f,
            rotation = 0,
            eraseBackground = false,
            alignsWithDragAngle = false
        )

        public val defaultStartCustomObject: CustomObject = CustomObject(
            stroke = 4f,
            color = null, // RGB Color according to the angle
            glow = CustomGlow(
                radius = 32f
            ),
            shape = IconShape.Circle,
            size = 30f,
            rotation = 0,
            eraseBackground = true,
            alignsWithDragAngle = false
        )

        public val defaultEndCustomObject: CustomObject = CustomObject(
            stroke = 0f,
            color = null, // RGB Color according to the angle
            glow = CustomGlow(
                radius = 12f
            ),
            shape = IconShape.Circle,
            size = 8f,
            rotation = 0,
            eraseBackground = false,
            alignsWithDragAngle = false
        )


        public val defaultHoldCustomObject: CustomObject = CustomObject(
            stroke = 4f,
            color = null,
            glow = CustomGlow(
                radius = 12f
            ),
            shape = IconShape.Cookie12Sided,
            size = 70f,
            rotation = -1,
            eraseBackground = false,
            alignsWithDragAngle = false // Unused for hold
        )

    }
}

@Serializable
public data class CustomGlow(
    val radius: Float,
    @Serializable(with = ColorSerializer::class)
    val color: Color? = null
)


public data class CustomObjectBlockProperties(
    val allowStrokeCustomization: Boolean = true,
    val allowColorCustomization: Boolean = true,
    val allowShapeCustomization: Boolean = true,
    val allowSizeCustomization: Boolean = true,
    val allowEraseBackgroundCustomization: Boolean = true,
    val allowAlignCustomization: Boolean = true,
    val allowMirrorCustomization: Boolean = true,
    val allowRotationCustomization: Boolean = true,

    val allowGlowCustomization: Boolean = true
)
