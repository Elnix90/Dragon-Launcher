package org.elnix.dragonlauncher.base.model.enumsui.toggle

import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.base.model.enumsui.ToggleButtonOption

public enum class HorizontalAlignment(
    override val resId: Int?,
    override val iconEnabled: Int,
    override val iconDisabled: Int? = null
) : org.elnix.dragonlauncher.base.model.enumsui.ToggleButtonOption {
    Start(R.string.align_start,R.drawable.format_align_left),
    Center(R.string.align_center, R.drawable.format_align_justify),
    End(R.string.align_end, R.drawable.format_align_right)
}
