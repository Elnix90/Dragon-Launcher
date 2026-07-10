package org.elnix.dragonlauncher.enumsui.select

import org.elnix.dragonlauncher.enumsui.SelectButtonOption
import org.elnix.dragonlauncher.i18n.R

public enum class ColorSelectorModes(
    override val resId: Int,
    override val iconResId: Int? = null
) : SelectButtonOption {
    Normal(R.string.normal_colors),
    Custom(R.string.custom_colors)
}