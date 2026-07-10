@file:Suppress("NOTHING_TO_INLINE")

package org.elnix.dragonlauncher.ktx

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin

/** Euclidean distance from [this@distance] to [b] in pixels. */
public inline infix fun Offset.distanceTo(b: Offset): Float =
    hypot(b.x - x, b.y - y)


public fun angle360FromOffset(center: Offset, offset: Offset): Float {
    val dx = offset.x - center.x
    val dy = offset.y - center.y
    val angleRad = atan2(dx.toDouble(), -dy.toDouble())
    var deg = Math.toDegrees(angleRad).toFloat()
    if (deg < 0f) deg += 360f
    return deg
}

public inline infix fun Offset.distanceSquaredTo(b: Offset): Float =
    (b.x - x).pow(2) +  (b.y - y).pow(2)

public fun Offset.angleRad(): Float = atan2(y, x)

/** Angle 0–360 from [this] (north = 0, clockwise). */
public fun Offset.angleDeg(): Float {
    var deg = Math.toDegrees(this.angleRad().toDouble()).toFloat()
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

    val angleInRadians: Double = angle.toRadians()
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
 * Snaps the value to the nearest integer if it crosses the threshold.
 * @param threshold The distance from an integer to trigger snapping
 */
public fun Float.snapToRound(snapTo: Float, threshold: Float): Float {
    return if (abs(this - snapTo) <= threshold) snapTo else this
}

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
 * Determines whether a pointer position lies within the allowed interaction zone.
 *
 * The active zone is defined as the rectangular area of the screen obtained by
 * excluding padding margins from each edge. Any position inside this rectangle
 * is considered valid for gesture handling.
 *
 * @receiver [Offset] Pointer position in screen coordinates.
 * @param size Full size of the available surface.
 * @param left Excluded distance from the left edge.
 * @param right Excluded distance from the right edge.
 * @param top Excluded distance from the top edge.
 * @param bottom Excluded distance from the bottom edge.
 *
 * @return `true` if the position is inside the active zone, `false` otherwise.
 */
public fun Offset.isInsideActiveZone(
    size: IntSize,
    left: Int,
    right: Int,
    top: Int,
    bottom: Int
): Boolean = x >= left &&
        x <= size.width - right &&
        y >= top &&
        y <= size.height - bottom

