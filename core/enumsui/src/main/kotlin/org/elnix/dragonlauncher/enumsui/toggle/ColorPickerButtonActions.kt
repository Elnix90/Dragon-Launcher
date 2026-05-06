package org.elnix.dragonlauncher.enumsui.toggle

import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.enumsui.ToggleButtonOption

enum class ColorPickerButtonAction(
    override val iconEnabled: Int,
    override val resId: Int? = null,
    override val iconDisabled: Int? = null
) : ToggleButtonOption {
    RANDOM(R.drawable.shuffle),
    RESET(R.drawable.reset),
    COPY(R.drawable.copy),
    PASTE(R.drawable.paste)
}
