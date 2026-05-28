package org.elnix.dragonlauncher.common.serializables

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


@Serializable
@SerialName("CustomObject")
data class CustomObject(
    val stroke: Float? = null,
    @Contextual val color: Color? = null,
    val glow: CustomGlow? = null,
    val rotation: Int? = null,
    val shape: IconShape? = null,
    val size: Float? = null,
    val eraseBackground: Boolean? = null
)

@Serializable
data class CustomGlow(
    @Contextual val color: Color? = null,
    val radius: Float? = null // null -> Size * 1.5
)

@OptIn(ExperimentalSerializationApi::class)
object ColorSerializer : KSerializer<Color> {
    override val descriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Color) {
        encoder.encodeInt(value.toArgb()) // ARGB as Long
    }

    override fun deserialize(decoder: Decoder): Color {
        return Color(decoder.decodeInt())
    }
}

data class CustomObjectBlockProperties(
    val allowStrokeCustomization: Boolean = true,
    val allowColorCustomization: Boolean = true,
    val allowShapeCustomization: Boolean = true,
    val allowSizeCustomization: Boolean = true,
    val allowEraseBackgroundCustomization: Boolean = true,
    val allowRotationCustomization: Boolean = true,

    val allowGlowCustomization: Boolean = true
)
