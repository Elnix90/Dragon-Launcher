package org.elnix.dragonlauncher.base.model.serializables.serializers

import androidx.compose.ui.geometry.Offset
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.elnix.dragonlauncher.ktx.round

@OptIn(ExperimentalSerializationApi::class)
internal object OffsetSerializer : KSerializer<Offset> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Offset", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Offset) {
        encoder.encodeString("${value.x.round(2)},${value.y.round(2)}")
    }

    override fun deserialize(decoder: Decoder): Offset {
        val parts = decoder.decodeString().split(",")
        return Offset(x = parts[0].toFloat(), y = parts[1].toFloat())
    }
}