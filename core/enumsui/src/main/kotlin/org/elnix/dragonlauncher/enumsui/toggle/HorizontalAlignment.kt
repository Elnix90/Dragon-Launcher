package org.elnix.dragonlauncher.enumsui.toggle

import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.enumsui.ToggleButtonOption

public enum class HorizontalAlignment(
    override val resId: Int?,
    override val iconEnabled: Int,
    override val iconDisabled: Int? = null
) : ToggleButtonOption {
    Start(R.string.align_start,R.drawable.format_align_left),
    Center(R.string.align_center, R.drawable.format_align_justify),
    End(R.string.align_end, R.drawable.format_align_right)
}
