package org.elnix.dragonlauncher.enumsui.toggle

import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.enumsui.ToggleButtonOption
public enum class WorkspaceAction(
    override val resId: Int,
    override val iconEnabled: Int,
    override val iconDisabled: Int? = null
    ) : ToggleButtonOption {

    Edit(R.drawable.edit_rounded, R.drawable.edit_rounded),
    Delete(R.string.delete_workspace, R.drawable.delete_forever)
}
