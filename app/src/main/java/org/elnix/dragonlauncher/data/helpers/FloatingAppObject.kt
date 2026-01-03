package org.elnix.dragonlauncher.data.helpers

import org.elnix.dragonlauncher.data.SwipeActionSerializable

data class FloatingAppObject(
    val id: Int,
    val action: SwipeActionSerializable,
    val spanX: Float = 1f,
    val spanY: Float = 1f,
    val x: Float = 0f,
    val y: Float = 0f,
    val angle: Double = 0.0,
    val ghosted: Boolean? = false,
    val foreground: Boolean? = true
)
