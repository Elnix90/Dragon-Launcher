package org.elnix.dragonlauncher.base.model.serializables.serializers

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.elnix.dragonlauncher.ktx.toColor
import org.elnix.dragonlauncher.ktx.toHexWithAlpha

@OptIn(ExperimentalSerializationApi::class)
public object ColorSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Color) {
        encoder.encodeString(value.toHexWithAlpha)
    }

    override fun deserialize(decoder: Decoder): Color = decoder.decodeString().toColor()
}
