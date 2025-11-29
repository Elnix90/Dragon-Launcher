package org.elnix.dragonlauncher.utils.circles

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import org.elnix.dragonlauncher.data.UiCircle
import org.elnix.dragonlauncher.data.UiSwipePoint
import kotlin.collections.find
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot

fun updatePointPosition(
    point: UiSwipePoint,
    circles: SnapshotStateList<UiCircle>,
    center: Offset,
    pos: Offset
) {
    // 1. Compute angle for the point
    val dx = pos.x - center.x
    val dy = center.y - pos.y
    var angle = Math.toDegrees(atan2(dx.toDouble(), dy.toDouble()))
    if (angle < 0) angle += 360.0

    // 2. Apply angle
    point.angleDeg = angle

    // 3. Find circle whose radius is closest to dragged position
    val distFromCenter = hypot(dx, dy)
    val closest = circles.minByOrNull { c -> abs(c.radius - distFromCenter) }
        ?: return

    // 4. Reassign circle if needed
    if (point.circleNumber != closest.id) {

        val oldCircle = circles.find { it.id == point.circleNumber }
        oldCircle?.points?.removeIf { it.id == point.id }

        closest.points.add(point)
        point.circleNumber = closest.id
    }
}
