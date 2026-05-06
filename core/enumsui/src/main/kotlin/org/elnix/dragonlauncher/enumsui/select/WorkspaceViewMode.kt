package org.elnix.dragonlauncher.enumsui.select

import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.enumsui.SelectButtonOption

enum class WorkspaceViewMode(
    override val resId: Int,
    override val iconResId: Int? = null
) : SelectButtonOption {
    Default(R.string.workspace_defaults),
    Added(R.string.workspace_added),
    Removed(R.string.workspace_removed)
}
