package org.elnix.dragonlauncher.enumsui.select

import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.enumsui.SelectButtonOption

enum class ColorSelectorModes(
    override val resId: Int,
    override val iconResId: Int? = null
) : SelectButtonOption {
    NORMAL(R.string.normal_colors),
    CUSTOM(R.string.custom_colors)
}