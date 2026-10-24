package org.elnix.dragonlauncher.base.model.enumsui.select

import io.github.elnix90.shared.ui.base.model.SelectButtonOption
import org.elnix.dragonlauncher.i18n.R

enum class ColorPickerMode(
	override val resId: Int,
	override val iconResId: Int? = null
) : SelectButtonOption {
	Default(R.string.default_text),
	Slider(R.string.sliders),
	Gradient(R.string.gradient)
}
