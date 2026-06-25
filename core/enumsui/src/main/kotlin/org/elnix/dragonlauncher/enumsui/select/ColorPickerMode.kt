package org.elnix.dragonlauncher.enumsui.select

import org.elnix.dragonlauncher.enumsui.SelectButtonOption
import org.elnix.dragonlauncher.i18n.R

public enum class ColorPickerMode(
    override val resId: Int,
    override val iconResId: Int? = null
) : SelectButtonOption {
    Default(R.string.default_text),
    Slider(R.string.sliders),
    Gradient(R.string.gradient)
}
