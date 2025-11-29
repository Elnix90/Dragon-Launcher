package org.elnix.dragonlauncher.utils.circles

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import org.elnix.dragonlauncher.data.UiCircle
import java.util.UUID

fun addCircle(circles: SnapshotStateList<UiCircle>) {
    if (circles.size >= 5) return
    circles.add(
        UiCircle(
            id = circles.size + 1,
            radius = circles.last().radius + 150f,
            points = mutableStateListOf()
        )
    )
}

fun removeCircle(circles: SnapshotStateList<UiCircle>, circleId: Int) {
    if (circles.size <= 1) return
    circles.removeAll { it.id == circleId }
}
