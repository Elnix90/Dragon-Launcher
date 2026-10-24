package org.elnix.dragonlauncher.base.model.enumsui.select

import io.github.elnix90.shared.ui.base.model.SelectButtonOption
import org.elnix.dragonlauncher.i18n.R

enum class PointFeaturePanel(
	override val resId: Int,
	override val iconResId: Int? = null
) : SelectButtonOption {
	LiveNest(R.string.live_nest),
	CycleActions(R.string.cycle_actions),
	HoldAndRun(R.string.hold_and_run)
}
