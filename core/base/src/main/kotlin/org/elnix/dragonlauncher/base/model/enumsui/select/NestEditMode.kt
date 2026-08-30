package org.elnix.dragonlauncher.base.model.enumsui.select

import org.elnix.dragonlauncher.base.model.enumsui.ToggleButtonOption
import org.elnix.dragonlauncher.i18n.R

public enum class NestEditMode(
    override val resId: Int,
    override val iconEnabled: Int,
    override val iconDisabled: Int? = null
) : ToggleButtonOption {
    Drag(R.string.dragging_distance_selection, R.drawable.drag_handle),
    Haptic(R.string.haptic_feedback, R.drawable.haptic),
    Radius(R.string.miscellaneous, R.drawable.radar),
    Other(R.string.more, R.drawable.more_horiz)
}
