package org.elnix.dragonlauncher.ui.helpers.customobjects

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawTransform

fun DrawTransform.mirrorVertically(center: Offset): Unit =
    scale(
        scaleX = -1f,
        scaleY = 1f,
        pivot = center
    )
