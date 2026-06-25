package org.elnix.dragonlauncher.common.circles

import androidx.compose.ui.geometry.Offset
import org.elnix.dragonlauncher.base.model.models.UiCircle
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
    circles: List<UiCircle>,
    center: Offset
): Offset {
    // Find the circle this point belongs to
    val circle = circles.find { it.id == circleNumber } ?: return center

    return computePointPositionInternal(
        angleDeg = angleDeg,
        radius = circle.radius,
        center = center
    )
}


public fun Point.computePosition(
    radius: Float,
    center: Offset
): Offset {

    return computePointPositionInternal(
        angleDeg = angleDeg,
        radius = radius,
        center = center
    )
}
