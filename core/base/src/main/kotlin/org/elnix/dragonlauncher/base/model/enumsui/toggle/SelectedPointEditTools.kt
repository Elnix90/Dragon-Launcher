package org.elnix.dragonlauncher.base.model.enumsui.toggle

import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.base.model.enumsui.ToggleButtonOption

public enum class SelectedPointEditTools(
    override val resId: Int,
    override val iconEnabled: Int,
    override val iconDisabled: Int? = null
) : ToggleButtonOption {
    Edit(R.string.edit_point, R.drawable.edit_rounded),
    Duplicate(R.string.copy_point, R.drawable.copy),
    Remove(R.string.remove_point, R.drawable.remove)
}