package org.elnix.dragonlauncher.utils.circles

import org.elnix.dragonlauncher.data.UiSwipePoint
import org.elnix.dragonlauncher.ui.MIN_ANGLE_GAP

fun randomFreeAngle(list: List<UiSwipePoint>): Double {
    if (list.isEmpty()) return (0..359).random().toDouble()

    repeat(200) {
        val a = (0..359).random().toDouble()
        if (list.none { absAngleDiff(it.angleDeg, a) < MIN_ANGLE_GAP }) return a
    }

    // fallback: pick biggest gap
    val sorted = list.map { it.angleDeg }.sorted()
    var bestA = 0.0
    var bestDist = -1.0

    for (i in sorted.indices) {
        val a1 = sorted[i]
        val a2 = sorted[(i + 1) % sorted.size]
        val gap = ((a2 - a1 + 360) % 360)
        if (gap > bestDist) {
            bestDist = gap
            bestA = (a1 + gap / 2) % 360
        }
    }
    return bestA
}