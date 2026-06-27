package org.elnix.dragonlauncher.common.circles

import androidx.compose.ui.geometry.Offset
import org.elnix.dragonlauncher.base.model.serializables.Nests
import org.elnix.dragonlauncher.base.model.serializables.Point
import kotlin.math.cos
import kotlin.math.sin

private fun computePointPositionInternal(
    angleDeg: Double,
    radius: Float,
    center: Offset,
): Offset {

    // Convert angleDeg to radians and compute the Offset
    val angleRad = Math.toRadians(angleDeg)
    return Offset(
        x = center.x + radius * sin(angleRad).toFloat(),
        y = center.y - radius * cos(angleRad).toFloat()
    )
}

public fun Point.computePosition(
    nests: Nests,
    center: Offset
): Offset {
    // Find the circle this point belongs to
//    val circle = circles.find { it.id == circleNumber } ?: return cente

    val nest = nests.find { it.id == nestId }!!

    // ignore the shapes for now, TODO
    return this.offset + center

//    return computePointPositionInternal(
//        angleDeg = 0.0,
//        radius = 0f,
//        center = center
//    )
}
