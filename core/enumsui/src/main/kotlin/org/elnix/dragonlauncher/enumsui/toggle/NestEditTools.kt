package org.elnix.dragonlauncher.enumsui.toggle

import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.enumsui.ToggleButtonOption

enum class NestEditTools(
    override val resId: Int,
    override val iconEnabled: Int,
    override val iconDisabled: Int? = null
) : ToggleButtonOption {
    NestManagement(R.string.edit_nests, R.drawable.edit_nest),
    GoParentNest(R.string.go_parent_nest, R.drawable.fullscreen_exit),
    EnterNest(R.string.open_nest_circle, R.drawable.fullscreen),
}
