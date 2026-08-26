package org.elnix.dragonlauncher.base.model.enumsui.select

import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.base.model.enumsui.SelectButtonOption

public enum class PointFeaturePanel(
    override val resId: Int,
    override val iconResId: Int? = null
) : org.elnix.dragonlauncher.base.model.enumsui.SelectButtonOption {
    LiveNest(R.string.live_nest),
    CycleActions(R.string.cycle_actions),
    HoldAndRun(R.string.hold_and_run)
}
