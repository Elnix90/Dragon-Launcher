package org.elnix.dragonlauncher.utils.circles

import kotlin.math.abs
import kotlin.math.min

fun absAngleDiff(a: Double, b: Double): Double {
    val diff = abs(a - b)
    return min(diff, 360 - diff)
}
