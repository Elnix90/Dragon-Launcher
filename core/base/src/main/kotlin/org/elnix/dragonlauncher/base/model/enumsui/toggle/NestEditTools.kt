package org.elnix.dragonlauncher.base.model.enumsui.toggle

import org.elnix.dragonlauncher.base.model.enumsui.ToggleButtonOption
import org.elnix.dragonlauncher.i18n.R

public enum class NestEditTools(
    override val resId: Int,
    override val iconEnabled: Int,
    override val iconDisabled: Int? = null
) : ToggleButtonOption {
    NestManagement(R.string.manage_nests, R.drawable.nest_icon),
    GoParentNest(R.string.go_parent_nest, R.drawable.fullscreen_exit),
    EnterNest(R.string.open_nest, R.drawable.fullscreen),
}
