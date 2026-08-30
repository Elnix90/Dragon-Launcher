package org.elnix.dragonlauncher.base.model.enumsui.toggle

import org.elnix.dragonlauncher.base.model.enumsui.ToggleButtonOption
import org.elnix.dragonlauncher.i18n.R

public enum class UndRedoEditTools(
    override val resId: Int?,
    override val iconEnabled: Int,
    override val iconDisabled: Int? = null
) : ToggleButtonOption {
    UndoAll(R.string.undo_all, R.drawable.fast_rewind),
    Undo(R.string.undo, R.drawable.undo),
    Redo(R.string.redo, R.drawable.redo),
    RedoAll(R.string.redo_all, R.drawable.fast_forward)
}
