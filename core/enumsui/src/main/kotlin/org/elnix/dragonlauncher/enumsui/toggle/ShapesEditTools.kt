package org.elnix.dragonlauncher.enumsui.toggle

import org.elnix.dragonlauncher.enumsui.ToggleButtonOption
import org.elnix.dragonlauncher.i18n.R

public enum class ShapesEditTools(
    override val resId: Int,
    override val iconEnabled: Int,
    override val iconDisabled: Int
) : ToggleButtonOption {
    SnapOffset(R.string.snap_shapes_offset, R.drawable.grid_on, R.drawable.grid_off),
    SnapCenter(R.string.snap_shapes_center, R.drawable.center_focus_strong, R.drawable.crop_free),
//    SnapScale(R.string.snap_shapes_scale, R.drawable.format_size, R.drawable.view_real_size),
    SnapAngle(R.string.snap_shapes_angle, R.drawable.rotate_90_degrees_cw, R.drawable.trhee_d_rotation)
}