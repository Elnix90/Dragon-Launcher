package org.elnix.dragonlauncher.base.model.enumsui.select

import io.github.elnix90.shared.ui.base.model.SelectButtonOption
import org.elnix.dragonlauncher.i18n.R

enum class SelectedUnselectedViewMode(
	override val resId: Int,
	override val iconResId: Int? = null
) : SelectButtonOption {
	Unselected(R.string.unselected),
	Selected(R.string.selected_text)
}
