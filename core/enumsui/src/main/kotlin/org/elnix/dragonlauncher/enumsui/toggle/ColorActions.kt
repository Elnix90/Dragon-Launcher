package org.elnix.dragonlauncher.enumsui.toggle

import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.enumsui.ToggleButtonOption
enum class ColorActions(
    override val resId: Int,
    override val iconEnabled: Int,
    override val iconDisabled: Int? = null
    ) : ToggleButtonOption {
    Reset(R.string.reset, R.drawable.reset),
    Random(R.string.random, R.drawable.shuffle),
}
