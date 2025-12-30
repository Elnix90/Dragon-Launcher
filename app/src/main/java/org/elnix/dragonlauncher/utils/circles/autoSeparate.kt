package org.elnix.dragonlauncher.utils.circles

import org.elnix.dragonlauncher.data.UiCircle
import org.elnix.dragonlauncher.data.UiSwipePoint
import org.elnix.dragonlauncher.ui.minAngleGapForCircle
import kotlin.math.abs
import kotlin.math.min

fun autoSeparate(
    points: MutableList<UiSwipePoint>,
    nestId: Int,
    circle: UiCircle?,
    draggedPoint: UiSwipePoint
) {
    val circleNumber = circle?.id ?: return

    repeat(20) {
        val pts = points
            .filter { it.nestId == nestId && it.circleNumber == circleNumber }
            .sortedBy { normalizeAngle(it.angleDeg) }

        if (pts.size <= 1) return

        var adjusted = false

        for (i in 0 until pts.size) {
            for (j in i + 1 until pts.size) {
                val p1 = pts[i]
                val p2 = pts[j]

                val a = normalizeAngle(p1.angleDeg)
                val b = normalizeAngle(p2.angleDeg)

                val diff = absAngleDiff(a, b)
                if (diff < minAngleGapForCircle(circle.radius)) {

                    if (draggedPoint.id == p1.id || draggedPoint.id == p2.id) {
                        // Check if dragged point crossed midpoint
                        val mid = normalizeAngle(a + signedAngleDiff(a, b) / 2.0)
                        val draggedAngle = normalizeAngle(draggedPoint.angleDeg)

                        val shouldSwap = if (draggedPoint.id == p1.id) {
                            // p1 was dragged - swap only if it crossed rightward past p2
                            signedAngleDiff(a, b) > 0 && draggedAngle > mid
                        } else {
                            // p2 was dragged - swap only if it crossed leftward past p1
                            signedAngleDiff(a, b) < 0 && draggedAngle < mid
                        }

                        if (shouldSwap) {
                            val temp = p1.angleDeg
                            p1.angleDeg = p2.angleDeg
                            p2.angleDeg = temp
                            adjusted = true
                            continue  // Skip normal separation for this pair
                        }
                    }

                    // Normal separation (no swap, just push apart)
                    val signed = signedAngleDiff(a, b)
                    val mid = normalizeAngle(a + signed / 2.0)
                    val halfGap = minAngleGapForCircle(circle.radius) / 2.0

                    p1.angleDeg = normalizeAngle(mid - halfGap)
                    p2.angleDeg = normalizeAngle(mid + halfGap)

                    adjusted = true
                }
            }
        }

        if (!adjusted) return
    }
}


/** Normalize angle into [0,360) */
fun normalizeAngle(a: Double): Double {
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
