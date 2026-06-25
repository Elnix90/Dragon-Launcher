package org.elnix.dragonlauncher.enumsui.toggle

import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.enumsui.ToggleButtonOption

public enum class AddRemoveCircleTools(
    override val resId: Int,
    override val iconEnabled: Int,
    override val iconDisabled: Int? = null
) : ToggleButtonOption {
    Add(R.string.add_circle, R.drawable.add_circle),
    Remove(R.string.remove_circle, R.drawable.remove_circle)
}
