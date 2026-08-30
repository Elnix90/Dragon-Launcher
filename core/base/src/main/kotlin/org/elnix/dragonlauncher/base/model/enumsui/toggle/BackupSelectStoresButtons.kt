package org.elnix.dragonlauncher.base.model.enumsui.toggle

import org.elnix.dragonlauncher.base.model.enumsui.ToggleButtonOption
import org.elnix.dragonlauncher.i18n.R

public enum class BackupSelectStoresButtons(
    override val resId: Int?,
    override val iconEnabled: Int,
    override val iconDisabled: Int? = null
) : ToggleButtonOption {
    DeselectAll(R.string.deselect_all, R.drawable.deselect),
    SelectAll(R.string.select_all, R.drawable.select_all),
    Invert(R.string.invert, R.drawable.swap_calls)
}
