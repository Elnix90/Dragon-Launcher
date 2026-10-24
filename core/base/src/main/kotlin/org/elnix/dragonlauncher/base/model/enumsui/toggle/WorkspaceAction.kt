package org.elnix.dragonlauncher.base.model.enumsui.toggle

import io.github.elnix90.shared.ui.base.model.ToggleButtonOption
import org.elnix.dragonlauncher.i18n.R

enum class WorkspaceAction(
	override val resId: Int,
	override val iconEnabled: Int,
	override val iconDisabled: Int? = null
) : ToggleButtonOption {
	Edit(R.drawable.edit_rounded, R.drawable.edit_rounded),
	Delete(R.string.delete_workspace, R.drawable.delete_forever)
}
