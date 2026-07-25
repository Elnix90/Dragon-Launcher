@file:Suppress("NOTHING_TO_INLINE")

package org.elnix.dragonlauncher.ktx

import androidx.compose.ui.util.fastRoundToInt
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
    get() = Math.toRadians(this.toDouble())

public inline val Float.degrees: Double
    get() = Math.toDegrees(this.toDouble())

public inline val Int.radians: Double
    get() = Math.toRadians(this.toDouble())

public inline val Int.degrees: Double
    get() = Math.toDegrees(this.toDouble())

public inline val Double.radians: Double
    get() = Math.toRadians(this)

public inline val Double.degrees: Double
    get() = Math.toDegrees(this)

public fun Float.round(decimals: Int): Float {
    if (decimals < 0) throw IllegalArgumentException("decimals must be >= 0")
    val factor = 10f.pow(decimals)
    return (this * factor).roundToInt() / factor
}


public val Float.to255: Int
    get() = (this.coerceIn(0f, 1f) * 255).fastRoundToInt()
