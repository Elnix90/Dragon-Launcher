package org.elnix.dragonlauncher.base.model.enumsui.select

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import org.elnix.dragonlauncher.base.model.enumsui.SelectButtonOption
import org.elnix.dragonlauncher.i18n.R

public enum class WorkspaceViewMode(
    override val resId: Int,
    override val iconResId: Int? = null
) : SelectButtonOption {
    Default(R.string.workspace_defaults),
    Added(R.string.workspace_added),
    Removed(R.string.workspace_removed)
}

public val LocalWorkspaceViewMode: ProvidableCompositionLocal<WorkspaceViewMode> = compositionLocalOf { WorkspaceViewMode.Default }