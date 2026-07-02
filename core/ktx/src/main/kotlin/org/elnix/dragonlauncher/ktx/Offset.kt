@file:Suppress("NOTHING_TO_INLINE")

package org.elnix.dragonlauncher.ktx

import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

///**
// * Rotates the given offset around the origin by the given angle in degrees.
// *
// * A positive angle indicates a counterclockwise rotation around the right-handed 2D Cartesian
// * coordinate system.
// *
// * See: [Rotation matrix](https://en.wikipedia.org/wiki/Rotation_matrix)
// */
//@Suppress("NOTHING_TO_INLINE")
//public inline fun Offset.rotateBy(angle: Float): Offset {
//    val angleInRadians: Double = angle * (PI / 180)
//    val cos: Double = cos(angleInRadians)
//    val sin: Double = sin(angleInRadians)
//    return Offset((x * cos - y * sin).toFloat(), (x * sin + y * cos).toFloat())
//}

///**
// * Reverses a scale transformation by dividing by the zoom factor.
// *
// * When the canvas is scaled by zoom factor (e.g., 1.5x), pointer coordinates are proportionally
// * larger. Dividing by zoom returns them to the original size.
// *
// * Example: If zoom = 2.0, a pointer at (200, 200) was originally at (100, 100).
// *
// * @return The offset in the un-scaled coordinate space
// */
//public inline fun Offset.undoScale(
//    zoom: () -> Float
//): Offset {
//    val zoom = zoom()
//    return Offset(
//        this.x / zoom,
//        this.y / zoom
//    )
//}


//public fun div(operand: Float): Offset {
//    return Offset(
//        packFloats(
//            val1 = unpackFloat1(packedValue) / operand,
//            val2 = unpackFloat2(packedValue) / operand
//        )
//    )
//}

/**
 * Reverses a translation transformation by adding the offset.
 *
 * When the canvas is translated by offset vector, pointer coordinates are shifted by that amount.
 * Adding the offset back returns them to the original position.
 *
 * Note: This adds (not subtracts) because the canvas translation works inversely:
 * if you move the canvas left by 100px (-offset.x in graphicsLayer),
 * a pointer at screen position X was actually at position X + offset.x in canvas space.
 *
 * @return The offset in the un-translated coordinate space
 */
public inline fun Offset.undoTranslation(
    translation: () -> Offset
): Offset {
    val translation = translation()
    return Offset(
        this.x + translation.x,
        this.y + translation.y
    )
}

///**
// * Undo all three previous transformations at once
// *
// * Note: ORDER MATTERS!!
// * If you put undo rotation first, it'll break the whole chain for some reason.
// */
//public inline fun Offset.undoTransformations(
//    angle: () -> Float,
//    zoom: () -> Float,
//    offset: () -> Offset
//): Offset = undoScale(zoom).undoTranslation(offset).undoRotation(angle)
//
//
///**
// * Redo all three previous transformations at once
// *
// * Note: ORDER MATTERS!!
// * If you put undo rotation first, it'll break the whole chain for some reason.
// */
//public inline fun Offset.redoTransformations(
//    angle: () -> Float,
//    zoom: () -> Float,
//    offset: () -> Offset
//): Offset = undoRotation(angle).undoTranslation(offset).undoScale(zoom)


/** Euclidean distance from [a] to [b] in pixels. */
public inline fun distance(a: Offset, b: Offset): Float =
    hypot(b.x - a.x, b.y - a.y)

/** Angle 0–360 from [offset] relative to [center] (north = 0, clockwise). */
public fun angle360FromOffset(offset: Offset): Float {
    val dx = offset.x
    val dy = offset.y
    val angleRad = atan2(dx.toDouble(), -dy.toDouble())
    var deg = Math.toDegrees(angleRad).toFloat()
    if (deg < 0f) deg += 360f
    return deg
}


///**
// * Reverses a rotation transformation by applying the inverse rotation matrix.
// *
// * When the canvas is rotated by angle θ, pointer coordinates are in the rotated space.
// * This function rotates them back by -θ to return them to the original coordinate space.
// *
// * Uses the inverse rotation matrix:
// * ```
// * [cos(θ)   sin(θ)]
// * [-sin(θ)  cos(θ)]
// * ```
// * See: [Rotation matrix](https://en.wikipedia.org/wiki/Rotation_matrix)
// *
// * @return The offset in the un-rotated coordinate space
// */
//public inline fun Offset.undoRotation(angle: Float): Offset {
//    val angleInRadians: Double = angle * (PI / 180)
//    val cos: Double = cos(angleInRadians)
//    val sin: Double = sin(angleInRadians)
//
//
//    return Offset(
//        x = ( cos * x + sin * y ).toFloat(),
//        y = (-sin * x + sin * y ).toFloat()
//    )
//}
//
//// DONT FUCKING ASK ME WHY THERE ARE 2 OF THEM, ITS BECAUSE OTHRWISE THEY WON'T WORK


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

    val angleInRadians: Double = angle * (PI / 180)
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
