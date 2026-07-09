package org.elnix.dragonlauncher.ktx

import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Constraints

public fun Constraints.getCenter(): Offset {
    val centerX = maxWidth / 2f
    val centerY = maxHeight / 2f
    return Offset(centerX, centerY)
}