package org.elnix.dragonlauncher.enumsui.toggle

import org.elnix.dragonlauncher.enumsui.ToggleButtonOption
import org.elnix.dragonlauncher.i18n.R

public enum class ColorPickerButtonAction(
    override val iconEnabled: Int,
    override val resId: Int? = null,
    override val iconDisabled: Int? = null
) : ToggleButtonOption {
    Random(R.drawable.shuffle),
    Reset(R.drawable.reset),
    Copy(R.drawable.copy),
    Paste(R.drawable.paste)
}
