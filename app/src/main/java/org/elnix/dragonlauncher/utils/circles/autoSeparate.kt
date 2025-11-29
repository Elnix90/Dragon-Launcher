package org.elnix.dragonlauncher.utils.circles

import org.elnix.dragonlauncher.data.UiSwipePoint
import org.elnix.dragonlauncher.ui.MIN_ANGLE_GAP
import kotlin.collections.filter

fun autoSeparate(points: MutableList<UiSwipePoint>, circleNumber: Int) {
    val pts = points.filter { it.circleNumber == circleNumber }
    if (pts.size <= 1) return

    repeat(20) {
        var adjusted = false

        for (i in pts.indices) {
            for (j in i + 1 until pts.size) {

                val p1 = pts[i]
                val p2 = pts[j]
                val diff = absAngleDiff(p1.angleDeg, p2.angleDeg)

                if (diff < MIN_ANGLE_GAP) {
                    val mid = (p1.angleDeg + p2.angleDeg) / 2
                    p1.angleDeg = (mid - MIN_ANGLE_GAP / 2 + 360) % 360
                    p2.angleDeg = (mid + MIN_ANGLE_GAP / 2 + 360) % 360
                    adjusted = true
                }
            }
        }

        if (!adjusted) return
    }
}
