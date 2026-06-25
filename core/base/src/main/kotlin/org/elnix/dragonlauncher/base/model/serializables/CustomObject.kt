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
)

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
