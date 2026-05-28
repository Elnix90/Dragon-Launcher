package org.elnix.dragonlauncher.enumsui.select

import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.enumsui.SelectButtonOption

enum class ColorPickerMode(
    override val resId: Int,
    override val iconResId: Int? = null
) : SelectButtonOption {
    DEFAULTS(R.string.default_text),
    SLIDERS(R.string.sliders),
    GRADIENT(R.string.gradient)
}
