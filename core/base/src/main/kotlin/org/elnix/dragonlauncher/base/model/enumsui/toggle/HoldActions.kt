package org.elnix.dragonlauncher.base.model.enumsui.toggle

import org.elnix.dragonlauncher.base.model.enumsui.ToggleButtonOption
import org.elnix.dragonlauncher.i18n.R

public enum class HoldActions(
    override val resId: Int,
    override val iconEnabled: Int,
    override val iconDisabled: Int
) : org.elnix.dragonlauncher.base.model.enumsui.ToggleButtonOption {
    ManualMode(R.string.play, R.drawable.flash_auto, R.drawable.flash_off),
    PlayPause(R.string.manual_mode_or_auto, R.drawable.play_arrow, R.drawable.pause),
}
