package org.elnix.dragonlauncher.base.model.serializables

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.serializables.serializers.ColorSerializer


@Serializable
@SerialName("CustomObject")
public data class CustomObject(
    val stroke: Float? = null,
    @Serializable(with = ColorSerializer::class)
    val color: Color? = null,
    val glow: CustomGlow? = null,
    val rotation: Int? = null,
    val shape: IconShape? = null,
    val size: Float? = null,
    val eraseBackground: Boolean? = null
) {
    public companion object {

        public val defaultLineCustomObject: CustomObject = CustomObject(
            stroke = 2f,
            color = null, // RGB Color according to the angle
            glow = CustomGlow(
                radius = 10f
            ),

            /** Not used for the line as it goes from `start` to `end` */
            shape = null,
            size = null,
            rotation = null, // No rotation for line, (it's nullable, but I use nul here to indicate that the rotation isn't available)

            eraseBackground = false
        )


        public val defaultAngleCustomObject: CustomObject = CustomObject(
            stroke = 2f,
            color = null, // RGB Color according to the angle
            glow = CustomGlow(
                radius = 20f
            ),
            shape = IconShape.Circle,
            size = 50f,
            rotation = 90,
            eraseBackground = false
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
            eraseBackground = true
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
            eraseBackground = false
        )


        public val defaultHoldCustomObject: CustomObject = CustomObject(
            stroke = 10f,
            color = Color.Red,
            glow = CustomGlow(
                radius = 12f
            ),
            shape = IconShape.Circle,
            size = 70f,
            rotation = 0,
            eraseBackground = false
        )

    }
}

@Serializable
public data class CustomGlow(
    @Serializable(with = ColorSerializer::class)
    val color: Color? = null,
    val radius: Float? = null
)



public data class CustomObjectBlockProperties(
    val allowStrokeCustomization: Boolean = true,
    val allowColorCustomization: Boolean = true,
    val allowShapeCustomization: Boolean = true,
    val allowSizeCustomization: Boolean = true,
    val allowEraseBackgroundCustomization: Boolean = true,
    val allowRotationCustomization: Boolean = true,

    val allowGlowCustomization: Boolean = true
)
