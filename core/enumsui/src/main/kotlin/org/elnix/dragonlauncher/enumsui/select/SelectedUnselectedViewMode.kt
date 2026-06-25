package org.elnix.dragonlauncher.enumsui.select

import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.enumsui.SelectButtonOption

public enum class SelectedUnselectedViewMode(
    override val resId: Int,
    override val iconResId: Int? = null
) : SelectButtonOption {
    Unselected(R.string.unselected),
    Selected(R.string.selected_text)
}
