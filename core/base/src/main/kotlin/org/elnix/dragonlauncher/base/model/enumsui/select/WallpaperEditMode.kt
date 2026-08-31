package org.elnix.dragonlauncher.base.model.enumsui.select

import org.elnix.dragonlauncher.base.model.enumsui.SelectButtonOption
import org.elnix.dragonlauncher.i18n.R

public enum class WallpaperEditMode(
    override val resId: Int,
    override val iconResId: Int? = null
) : SelectButtonOption {
    Main(R.string.main_screen),
    Drawer(R.string.drawer_screen)
}
