package org.elnix.dragonlauncher.enumsui.toggle

import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.enumsui.ToggleButtonOption

enum class DrawerToolbar(
    override val resId: Int,
    override val iconEnabled: Int,
    override val iconDisabled: Int? = null
) : ToggleButtonOption {
    Spacer(R.string.spacer, R.drawable.height),
    RecentlyUsed(R.string.recently_used_apps, R.drawable.reset),
    SearchBar(R.string.search_bar, R.drawable.search)
}

