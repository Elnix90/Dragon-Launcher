package org.elnix.dragonlauncher.ktx

import kotlin.math.PI
import kotlin.math.ceil

public fun Float.ceilToInt(): Int {
    return ceil(this).toInt()
}

private const val TWO_PI_F = (2.0 * PI).toFloat()
public val Float.Companion.TWO_PI: Float
    get() = TWO_PI_F

private const val PI_F = PI.toFloat()
public val Float.Companion.PI: Float
    get() = PI_F
