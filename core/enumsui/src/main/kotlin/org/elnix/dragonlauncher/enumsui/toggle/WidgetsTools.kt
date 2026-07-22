package org.elnix.dragonlauncher.enumsui.toggle

import org.elnix.dragonlauncher.enumsui.ToggleButtonOption
import org.elnix.dragonlauncher.i18n.R

public enum class WidgetsToolsAddNestRemove(
    override val resId: Int?,
    override val iconEnabled: Int,
    override val iconDisabled: Int? = null
) : ToggleButtonOption {
    Nests(R.string.pick_a_nest, R.drawable.nest_icon),
    Remove(R.string.delete_widget, R.drawable.remove_circle)
}


public enum class WidgetsToolsCenterReset(
    override val resId: Int?,
    override val iconEnabled: Int,
    override val iconDisabled: Int? = null
) : ToggleButtonOption {
    Center(R.string.center_selected_widget, R.drawable.center_focus_strong),
    Reset(R.string.reset_widget, R.drawable.reset)
}

public enum class WidgetsToolsUpDown(
    override val resId: Int?,
    override val iconEnabled: Int,
    override val iconDisabled: Int? = null
) : ToggleButtonOption {
    Up(R.string.select_previous_widget, R.drawable.arrow_drop_up),
    Down(R.string.select_next_widget, R.drawable.arrow_drop_down)
}


public enum class WidgetsToolsMoveUpDown(
    override val resId: Int?,
    override val iconEnabled: Int,
    override val iconDisabled: Int? = null
) : ToggleButtonOption {
    MoveUp(R.string.move_selected_widget_up, R.drawable.move_up),
    MoveDown(R.string.move_selected_widget_down, R.drawable.move_down)
}

public enum class WidgetsToolsSnapping(
    override val resId: Int?,
    override val iconEnabled: Int,
    override val iconDisabled: Int? = null
) : ToggleButtonOption {
    SnapGrid(R.string.enable_snap_move, R.drawable.grid_on, R.drawable.grid_off),
    SnapResize(R.string.enable_scale_snap, R.drawable.format_size, R.drawable.format_clear),
    SnapRotation(R.string.snap_rotation, R.drawable.rotate_90_degrees_cw, R.drawable.rotate_90_degrees_ccw),
}