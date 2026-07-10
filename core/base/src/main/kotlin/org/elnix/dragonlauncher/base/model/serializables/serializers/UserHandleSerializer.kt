package org.elnix.dragonlauncher.base.model.serializables.serializers

import android.os.Process
import android.os.UserHandle
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import io.github.elnix90.logging.PROFILES_TAG
import io.github.elnix90.logging.logE

internal object UserHandleSerializer : KSerializer<UserHandle> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("UserHandle", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: UserHandle) {
        encoder.encodeInt(value.hashCode())
    }

    override fun deserialize(decoder: Decoder): UserHandle {
        val hashCode = decoder.decodeInt()
        return try {
            UserHandle.getUserHandleForUid(hashCode)
        } catch (e: Throwable) {
            logE(PROFILES_TAG, e) { "Unable to get UserHandle from hashcode"}
            Process.myUserHandle()
        }
    }
}