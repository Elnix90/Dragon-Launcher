package org.elnix.dragonlauncher.enumsui.select

import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.enumsui.SelectButtonOption

public enum class PointFeaturePanel(
    override val resId: Int,
    override val iconResId: Int? = null
) : SelectButtonOption {
    LiveNest(R.string.live_nest),
    CycleActions(R.string.cycle_actions),
    HoldAndRun(R.string.hold_and_run)
}
