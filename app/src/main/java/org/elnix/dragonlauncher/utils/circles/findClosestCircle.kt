package org.elnix.dragonlauncher.utils.circles

import androidx.compose.ui.geometry.Offset
import org.elnix.dragonlauncher.data.UiCircle
import kotlin.math.abs
import kotlin.math.hypot

fun findClosestCircle(
    circles: List<UiCircle>,
    center: Offset,
    pos: Offset
): UiCircle? {
    var best: UiCircle? = null
    var bestDiff = Float.MAX_VALUE

    circles.forEach { c ->
        val dist = hypot(pos.x - center.x, pos.y - center.y)
        val diff = abs(dist - c.radius)
        if (diff < bestDiff) {
            bestDiff = diff
            best = c
        }
    }
    return best
}