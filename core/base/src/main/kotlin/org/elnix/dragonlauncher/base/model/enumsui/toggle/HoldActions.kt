package org.elnix.dragonlauncher.base.model.enumsui.toggle

import io.github.elnix90.shared.ui.base.model.ToggleButtonOption
import org.elnix.dragonlauncher.i18n.R

enum class HoldActions(
	override val resId: Int,
	override val iconEnabled: Int,
	override val iconDisabled: Int
) : ToggleButtonOption {
	ManualMode(R.string.play, R.drawable.flash_auto, R.drawable.flash_off),
	PlayPause(R.string.manual_mode_or_auto, R.drawable.play_arrow, R.drawable.pause)
}
