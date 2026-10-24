package org.elnix.dragonlauncher.base.model.enumsui.select

import io.github.elnix90.shared.ui.base.model.SelectButtonOption
import org.elnix.dragonlauncher.i18n.R

enum class AngleObject(
	override val resId: Int,
	override val iconResId: Int? = null
) : SelectButtonOption {
	Line(R.string.line),
	Angle(R.string.angle),
	Start(R.string.start),
	End(R.string.end)
}
