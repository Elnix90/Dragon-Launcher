@file:Suppress("NOTHING_TO_INLINE")

package org.elnix.dragonlauncher.ktx

import androidx.compose.ui.util.fastRoundToInt
import kotlin.math.PI
import kotlin.math.abs
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


/**
 * Snaps the value to the nearest integer if it crosses the threshold.
 * @param threshold The distance from an integer to trigger snapping
 */
public fun Int.snapToRound(snapTo: Int, threshold: Int): Int {
    return if (abs(this - snapTo) <= threshold) snapTo else this
}

/**
 * Snaps the value to the nearest integer if it crosses the threshold.
 * @param threshold The distance from an integer to trigger snapping
 */
public fun Float.snapToRound(snapTo: Float, threshold: Float): Float {
    return if (abs(this - snapTo) <= threshold) snapTo else this
}


/**
 * Rounds this value to the nearest multiple of [gridSize].
 *
 * Useful for aligning coordinates to a virtual grid. For example, with a grid size of 10f,
 * the value 27f rounds to 30f, and -23f rounds to -20f.
 *
 * @param gridSize The spacing between grid lines in pixels. Must be positive.
 * @return This value rounded to the nearest grid multiple.
 *
 * @throws IllegalArgumentException if [gridSize] is not positive.
 */
public fun Float.snapToGrid(gridSize: Float): Float {
    if (gridSize <= 0f) throw IllegalArgumentException("gridSize must be > 0")
    return (this / gridSize).fastRoundToInt() * gridSize
}

