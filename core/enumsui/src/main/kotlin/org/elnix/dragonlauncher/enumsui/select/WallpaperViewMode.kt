package org.elnix.dragonlauncher.enumsui.select

import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.enumsui.SelectButtonOption


public enum class WallpaperEditMode(
    override val resId: Int,
    override val iconResId: Int? = null
) : SelectButtonOption {
    Main(R.string.main_screen),
    Drawer(R.string.drawer_screen)
}