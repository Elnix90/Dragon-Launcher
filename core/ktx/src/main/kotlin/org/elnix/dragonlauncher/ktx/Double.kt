package org.elnix.dragonlauncher.ktx

/** Normalize angle into [0,360) */
public fun Double.normalizeAngle(): Double {
    val v = this % 360.0
    return if (v < 0) v + 360.0 else v
}