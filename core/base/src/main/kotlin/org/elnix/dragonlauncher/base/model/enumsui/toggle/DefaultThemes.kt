package org.elnix.dragonlauncher.base.model.enumsui.toggle

import org.elnix.dragonlauncher.base.model.enumsui.ToggleButtonOption
import org.elnix.dragonlauncher.i18n.R

public enum class DefaultThemes(
    override val resId: Int,
    override val iconEnabled: Int? = null,
    override val iconDisabled: Int? = null
) : ToggleButtonOption {
    Light(R.string.flashbang_theme),
    Dark(R.string.dark_theme),
    Amoled(R.string.amoled_theme),
    System(R.string.system_theme),
    Custom(R.string.custom_theme)
}
