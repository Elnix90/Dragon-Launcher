@file:Suppress("NOTHING_TO_INLINE")

package org.elnix.dragonlauncher.ktx

import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.roundToInt


private const val TWO_PI_F = (2.0 * PI).toFloat()
public val Float.Companion.TWO_PI: Float
    get() = TWO_PI_F

private const val PI_F = PI.toFloat()
public val Float.Companion.PI: Float
    get() = PI_F


public inline val Float.radians: Double
    get() = this * (PI / 180)

public fun Float.round(decimals: Int): Float {
    if (decimals < 0) throw IllegalArgumentException("decimals must be >= 0")
    val factor = 10f.pow(decimals)
    return (this * factor).roundToInt() / factor
}
