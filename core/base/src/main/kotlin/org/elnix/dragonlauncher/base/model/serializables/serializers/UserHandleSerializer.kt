package org.elnix.dragonlauncher.base.model.serializables.serializers

import android.os.Parcel
import android.os.Process
import android.os.UserHandle
import io.github.elnix90.logging.logE
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.elnix.dragonlauncher.PROFILES_TAG

/**
 * Serializes a [UserHandle] as its raw user id (`Int`).
 *
 * [UserHandle.hashCode] returns the user id (`mHandle`), which is why it is a
 * correct serialization format here.
 *
 * Deserialization must NOT use [UserHandle.getUserHandleForUid]: that function
 * expects a full *uid* (e.g. `1010023`) and computes the user id with
 * `uid / PER_USER_RANGE`, silently mapping every user id below `100000`
 * (i.e. all of them, including private space) to the main user.
 *
 * Instead, the user id is rebuilt into a [UserHandle] through a `Parcel`
 * round-trip, which preserves the exact value using only public APIs
 * ([UserHandle.CREATOR], [Parcel.writeInt]).
 */
internal object UserHandleSerializer : KSerializer<UserHandle> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("UserHandle", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: UserHandle) {
        encoder.encodeInt(value.hashCode())
    }

    override fun deserialize(decoder: Decoder): UserHandle {
        val userId = decoder.decodeInt()
        return try {
            val parcel = Parcel.obtain()
            try {
                parcel.writeInt(userId)
                parcel.setDataPosition(0)
                return UserHandle.CREATOR.createFromParcel(parcel)
            } finally {
                parcel.recycle()
            }
        } catch (e: Throwable) {
            logE(PROFILES_TAG, e) { "Unable to get UserHandle from user id $userId" }
            Process.myUserHandle()
        }
    }
}
