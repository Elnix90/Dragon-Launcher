package org.elnix.dragonlauncher.base.model.enumsui.select

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import io.github.elnix90.shared.ui.base.model.SelectButtonOption
import org.elnix.dragonlauncher.i18n.R

enum class WorkspaceViewMode(
	override val resId: Int,
	override val iconResId: Int? = null
) : SelectButtonOption {
	Default(R.string.workspace_defaults),
	Added(R.string.workspace_added),
	Removed(R.string.workspace_removed)
}

val LocalWorkspaceViewMode: ProvidableCompositionLocal<WorkspaceViewMode> =
	compositionLocalOf { WorkspaceViewMode.Default }
