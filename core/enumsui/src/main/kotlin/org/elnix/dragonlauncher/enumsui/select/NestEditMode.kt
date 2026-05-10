package org.elnix.dragonlauncher.enumsui.select

import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.enumsui.SelectButtonOption

enum class NestEditMode(
    override val iconResId: Int,
    override val resId: Int? = null
) : SelectButtonOption {
    Drag(R.drawable.drag_handle),
    Haptic(R.drawable.haptic),
    MinAngle(R.drawable.polyline),
    Radius(R.drawable.radar),
    Other(R.drawable.more_horiz)
}