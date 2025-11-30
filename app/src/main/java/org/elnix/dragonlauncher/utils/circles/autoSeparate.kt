package org.elnix.dragonlauncher.utils.circles

import org.elnix.dragonlauncher.data.UiSwipePoint
import org.elnix.dragonlauncher.ui.MIN_ANGLE_GAP
import kotlin.collections.filter
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

fun autoSeparate(
    points: MutableList<UiSwipePoint>,
    circleNumber: Int
) {
    repeat(20) {
        val pts = points
            .filter { it.circleNumber == circleNumber }
            .sortedBy { it.angleDeg }

        if (pts.size <= 1) return

        var adjusted = false

        for (i in 0 until pts.size) {
            for (j in i + 1 until pts.size) {
                val p1 = pts[i]
                val p2 = pts[j]

                val a = normalizeAngle(p1.angleDeg)
                val b = normalizeAngle(p2.angleDeg)

                val diff = absAngleDiff(a, b)
                if (diff < MIN_ANGLE_GAP) {

                    val signed = signedAngleDiff(a, b)
                    val mid = normalizeAngle(a + signed / 2.0)
                    val halfGap = MIN_ANGLE_GAP / 2.0

                    val leftAngle  = normalizeAngle(mid - halfGap)
                    val rightAngle = normalizeAngle(mid + halfGap)

                    if (signed > 0) {
                        p2.angleDeg = leftAngle
                        p1.angleDeg = rightAngle
                    } else {
                        p1.angleDeg = leftAngle
                        p2.angleDeg = rightAngle
                    }

                    adjusted = true
                }
            }
        }

        if (!adjusted) return
    }
}

/** Normalize angle into [0,360) */
private fun normalizeAngle(a: Double): Double {
    val v = a % 360.0
    return if (v < 0) v + 360.0 else v
}

/**
 * Return absolute minimal difference between two angles (0..180)
 */
fun absAngleDiff(a: Double, b: Double): Double {
    val diff = abs(a - b)
    return min(diff, 360 - diff)
}

/**
 * Signed shortest difference from a -> b in degrees, in range (-180, 180]
 * Example: a=350, b=10 -> returns +20
 *          a=10,  b=350 -> returns -20
 */
private fun signedAngleDiff(a: Double, b: Double): Double {
    // compute raw difference
    var d = (b - a) % 360.0
    if (d <= -180.0) d += 360.0
    else if (d > 180.0) d -= 360.0
    return d
}
