package org.elnix.dragonlauncher.enumsui.toggle

import org.elnix.dragonlauncher.enumsui.ToggleButtonOption
import org.elnix.dragonlauncher.i18n.R


enum class DefaultThemes(
    override val resId: Int,
    override val iconEnabled: Int? = null,
    override val iconDisabled: Int? = null
) : ToggleButtonOption {
    Light(R.string.light_theme),
    Dark(R.string.dark_theme),
    Amoled(R.string.amoled_theme),
    System(R.string.system_theme),
    Custom(R.string.custom_theme)
}
