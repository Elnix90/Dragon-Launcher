package org.elnix.dragonlauncher.data.helpers

import android.content.ComponentName

data class WidgetInfo(
    val id: Int,
    val provider: ComponentName,
    val spanX: Float = 1f,
    val spanY: Float = 1f,
    val x: Float = 0f,
    val y: Float = 0f
)
