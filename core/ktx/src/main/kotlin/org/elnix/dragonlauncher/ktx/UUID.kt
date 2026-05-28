package org.elnix.dragonlauncher.ktx

import java.nio.ByteBuffer
import java.util.UUID

fun UUID.toBytes(): ByteArray {
    val bytes = ByteArray(16)
    val buffer = ByteBuffer.wrap(bytes)
    buffer.putLong(mostSignificantBits)
    buffer.putLong(leastSignificantBits)
    return buffer.array()
}