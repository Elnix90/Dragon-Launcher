package org.elnix.dragonlauncher.base.model.enumsui.select

import org.elnix.dragonlauncher.base.model.enumsui.SelectButtonOption
import org.elnix.dragonlauncher.i18n.R

public enum class PointFeaturePanel(
    override val resId: Int,
    override val iconResId: Int? = null
) : SelectButtonOption {
    LiveNest(R.string.live_nest),
    CycleActions(R.string.cycle_actions),
    HoldAndRun(R.string.hold_and_run)
}
