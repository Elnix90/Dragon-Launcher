package org.elnix.dragonlauncher.enumsui.toggle

import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.enumsui.ToggleButtonOption

enum class MoveAroundTools(
    override val resId: Int?,
    override val iconEnabled: Int? = null,
    override val iconDisabled: Int? = null
) : ToggleButtonOption {
    ToggleMoveAround(R.string.move_around_mode, R.drawable.pan_zoom, R.drawable.pan_tool),
    Center(R.string.align_center, R.drawable.center_focus_strong),
    ResetZoom(R.string.reset_zoom, R.drawable.view_real_size),
    ResetRotation(R.string.reset_rotation, R.drawable.trhee_d_rotation)
}
