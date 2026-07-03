package org.elnix.dragonlauncher.enumsui.toggle

import org.elnix.dragonlauncher.enumsui.ToggleButtonOption
import org.elnix.dragonlauncher.i18n.R

public enum class PointsEditTools(
    override val resId: Int,
    override val iconEnabled: Int,
    override val iconDisabled: Int
) : ToggleButtonOption {
    SnapPoints(R.string.snap_points, R.drawable.grid_on, R.drawable.grid_off),
    AutoSeparate(R.string.auto_separate, R.drawable.flash_auto, R.drawable.flash_off),
    AutoMerge(R.string.auto_merge, R.drawable.merge, R.drawable.join)
}