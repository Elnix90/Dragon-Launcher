package org.elnix.dragonlauncher.enumsui.select

import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.enumsui.ToggleButtonOption

enum class NestEditMode(
    override val resId: Int,
    override val iconEnabled: Int,
    override val iconDisabled: Int? = null
) : ToggleButtonOption {
    Drag(R.string.dragging_distance_selection,R.drawable.drag_handle),
    Haptic(R.string.haptic_feedback,R.drawable.haptic),
    MinAngle(R.string.min_angle_to_activate,R.drawable.polyline),
    Radius(R.string.miscellaneous,R.drawable.radar),
    Other(R.string.more,R.drawable.more_horiz)
}