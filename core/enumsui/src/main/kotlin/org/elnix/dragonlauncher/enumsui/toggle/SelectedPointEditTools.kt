package org.elnix.dragonlauncher.enumsui.toggle

import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.enumsui.ToggleButtonOption

enum class SelectedPointEditTools(
    override val resId: Int,
    override val iconEnabled: Int,
    override val iconDisabled: Int? = null
) : ToggleButtonOption {
    Edit(R.string.edit_point, R.drawable.edit_rounded),
    Duplicate(R.string.copy_point, R.drawable.copy),
    Remove(R.string.remove_point, R.drawable.remove)
}