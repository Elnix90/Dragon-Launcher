@file:Suppress("NOTHING_TO_INLINE")

package org.elnix.dragonlauncher.ktx

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.util.fastRoundToInt
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin

/** Euclidean distance from [this@distance] to [b] in pixels. */
public inline infix fun Offset.distanceTo(b: Offset): Float =
    hypot(b.x - x, b.y - y)

/**
 * Return the distance squared to the receiver [Offset] of [other]
 * Use this to avoid computing the square root when you only need to compare offsets lengths together
 */
public inline infix fun Offset.distanceSquaredTo(other: Offset): Float =
    (other.x - x).pow(2) + (other.y - y).pow(2)


public fun angle360FromOffset(center: Offset, offset: Offset): Float {
    if (center == offset) return 0f
    return (offset - center).angleDeg()
}

public fun Offset.angleRad(): Float = atan2(y, x)

/** Angle 0..360 from [this] (east = 0, clockwise). */
public inline fun Offset.angleDeg(): Float {
    var deg = this.angleRad().degrees.toFloat()
    if (deg < 0f) deg += 360f
    return deg
}

/**
 * Rotates the given offset around the origin by the given angle in degrees.
 *
 * A positive angle indicates a counterclockwise rotation around the right-handed 2D Cartesian
 * coordinate system.
 *
 * See: [Rotation matrix](https://en.wikipedia.org/wiki/Rotation_matrix)
 */
public inline fun Offset.rotateBy(angle: Float): Offset {
    if (angle == 0f || angle == 360f) return this

    val angleInRadians: Double = angle.radians
    val cos: Double = cos(angleInRadians)
    val sin: Double = sin(angleInRadians)

    // AHAHAHAH FUCK IT, IT WORKS I SPEND TOO MUCH TIME ON THAT SHIT, THANKS MR ROUX

    return Offset(
        x = (x * cos - y * sin).toFloat(),
        y = (x * sin + y * cos).toFloat()
    )
}

/**
 * Undo all three previous transformations at once
 *
 * Note: ORDER MATTERS!!
 * If you put undo rotation first, it'll break the whole chain for some reason.
 */
public inline fun Offset.applyTransformations(
    zoom: Float,
    offset: Offset,
    angle: Float
): Offset = div(zoom).plus(offset).rotateBy(-angle)


/**
 * Redo all three previous transformations at once
 *
 * Note: ORDER MATTERS!!
 * If you put undo rotation first, it'll break the whole chain for some reason.
 */
public inline fun Offset.undoTransformations(
    angle: Float,
    zoom: Float,
    offset: Offset
): Offset = rotateBy(angle).minus(offset).times(zoom)


/**
 * Snaps both x and y of the Offset to integers if they cross the threshold.
 * @param threshold The distance from an integer to trigger snapping
 */
public fun Offset.snapToRound(snapTo: Offset, threshold: Float): Offset {
    return Offset(
        x = x.snapToRound(snapTo.x, threshold),
        y = y.snapToRound(snapTo.y, threshold)
    )
}

/**
 * Snaps both x and y of the Offset to the closest multiple of [cellSizePx].
 * @param cellSizePx The size of the virtual grid to be snapped on
 */
public fun Offset.snapToGrid(cellSizePx: Float): Offset {
    return Offset(
        x = this.x.snapToGrid(cellSizePx),
        y = this.y.snapToGrid(cellSizePx)
    )
}

/**
 * Outputs a receiver [Offset] to a string, well formatted and human-readable in the `"x ; y"` format:
 *
 * @return the formatter string
 */
public fun Offset.cleanString(): String =
    "${x.fastRoundToInt()} ; ${y.fastRoundToInt()}"

/**
 * Quick ktx to create a [Offset] whose [x][Offset.x] and [y][Offset.y] values are equals
 *
 * @param size value given to [x][Offset.x] and [y][Offset.y]
 * @return the newly created offset
 */
public inline fun Offset.Companion.rect(size: Float): Offset =
    Offset(size, size)

/**
 * Quick ktx to create a [IntOffset] whose [x][IntOffset.x] and [y][IntOffset.y] values are equals
 *
 * @param size value given to [x][IntOffset.x] and [y][IntOffset.y]
 * @return the newly created offset
 */
public inline fun IntOffset.Companion.rect(size: Int): IntOffset =
    IntOffset(size, size)
