package org.elnix.dragonlauncher.base.model.enumsui.toggle

import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.base.model.enumsui.ToggleButtonOption

public enum class ColorActions(
    override val resId: Int,
    override val iconEnabled: Int,
    override val iconDisabled: Int? = null
) : ToggleButtonOption {
    Reset(R.string.reset, R.drawable.reset),
    Random(R.string.random, R.drawable.shuffle),
    Copy(R.string.copy, R.drawable.copy),
    Paste(R.string.paste, R.drawable.paste),
}
