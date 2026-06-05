package org.elnix.dragonlauncher.common.circles

import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.model.models.UiCircle
import kotlin.collections.map


fun randomFreeAngle(circle: UiCircle?, list: Set<Point>): Double? {

    val circleRadius = circle?.radius ?: return null

    if (list.isEmpty()) return (0..359).random().toDouble()

    repeat(200) {
        val a = (0..359).random().toDouble()
        if (list.none { absAngleDiff(it.angleDeg, a) < minAngleGapForCircle(circleRadius) }) return a
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
