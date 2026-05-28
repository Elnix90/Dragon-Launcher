package org.elnix.dragonlauncher.enumsui.toggle

import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.enumsui.ToggleButtonOption


/**
 * Default themes
 * DO NOT RENAME THEM, their names are serialized in user's storage
 */
enum class DefaultThemes(
    override val resId: Int,
    override val iconEnabled: Int? = null,
    override val iconDisabled: Int? = null
) : ToggleButtonOption {
    LIGHT(R.string.light_theme),
    DARK(R.string.dark_theme),
    AMOLED(R.string.amoled_theme),
    SYSTEM(R.string.system_theme),
    CUSTOM(R.string.custom_theme)
}
