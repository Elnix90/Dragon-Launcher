package org.elnix.dragonlauncher.base.model.enumsui.toggle

import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.base.model.enumsui.ToggleButtonOption

public enum class MoveAroundTools(
    override val resId: Int?,
    override val iconEnabled: Int? = null,
    override val iconDisabled: Int? = null
) : org.elnix.dragonlauncher.base.model.enumsui.ToggleButtonOption {
    Center(R.string.align_center, R.drawable.center_focus_strong),
    ResetZoom(R.string.reset_zoom, R.drawable.view_real_size),
    ResetRotation(R.string.reset_rotation, R.drawable.trhee_d_rotation)
}
