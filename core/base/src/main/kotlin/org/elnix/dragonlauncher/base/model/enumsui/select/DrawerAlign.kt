package org.elnix.dragonlauncher.base.model.enumsui.select

import androidx.compose.ui.Alignment
import org.elnix.dragonlauncher.base.model.enumsui.SelectButtonOption
import org.elnix.dragonlauncher.i18n.R

public enum class DrawerAlign(
	override val resId: Int,
	override val iconResId: Int?
) : SelectButtonOption {
	Top(R.string.top, R.drawable.keyboard_arrow_up),
	Bottom(R.string.bottom, R.drawable.arrow_down);

	public fun toAlignment(): Alignment.Vertical = when (this) {
		Top -> Alignment.Top
		Bottom -> Alignment.Bottom
	}
}
