package org.elnix.dragonlauncher.base.model.serializables.serializers

import androidx.graphics.shapes.CornerRounding
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


@Serializable
private data class CornerRoundingData(
    val radius: Float = 0f,
    val smoothing: Float = 0f
)

internal object CornerRoundingSerializer : KSerializer<CornerRounding> {
    private val delegate = CornerRoundingData.serializer()

    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: CornerRounding) {
        val data = CornerRoundingData(value.radius, value.smoothing)
        encoder.encodeSerializableValue(delegate, data)
    }

    override fun deserialize(decoder: Decoder): CornerRounding {
        val data = decoder.decodeSerializableValue(delegate)
        return CornerRounding(data.radius, data.smoothing)
    }
}