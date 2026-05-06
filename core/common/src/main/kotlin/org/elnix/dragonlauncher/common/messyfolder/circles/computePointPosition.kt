package org.elnix.dragonlauncher.common.messyfolder.circles

import androidx.compose.ui.geometry.Offset
import org.elnix.dragonlauncher.common.serializables.SwipePointSerializable
import org.elnix.dragonlauncher.common.messyfolder.UiCircle
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

fun computePointPosition(
    point: SwipePointSerializable,
    circles: List<UiCircle>,
    center: Offset
): Offset {
    // Find the circle this point belongs to
    val circle = circles.find { it.id == point.circleNumber } ?: return center

    return computePointPositionInternal(
        angleDeg = point.angleDeg,
        radius = circle.radius,
        center = center
    )
}


fun computePointPosition(
    point: SwipePointSerializable,
    radius: Float,
    center: Offset
): Offset {

    return computePointPositionInternal(
        angleDeg = point.angleDeg,
        radius = radius,
        center = center
    )
}
