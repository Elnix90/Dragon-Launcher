package org.elnix.dragonlauncher.base.model.enumsui.select

import org.elnix.dragonlauncher.base.model.enumsui.SelectButtonOption
import org.elnix.dragonlauncher.i18n.R

public enum class SelectedUnselectedViewMode(
    override val resId: Int,
    override val iconResId: Int? = null
) : SelectButtonOption {
    Unselected(R.string.unselected),
    Selected(R.string.selected_text)
}
