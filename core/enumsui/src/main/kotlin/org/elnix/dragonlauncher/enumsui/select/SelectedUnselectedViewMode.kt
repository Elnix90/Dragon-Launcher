package org.elnix.dragonlauncher.enumsui.select

import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.enumsui.SelectButtonOption

enum class SelectedUnselectedViewMode(
    override val resId: Int,
    override val iconResId: Int? = null
) : SelectButtonOption {
    Unselected(R.string.unselected),
    Selected(R.string.selected_text)
}
