package org.elnix.dragonlauncher.enumsui.toggle

import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.enumsui.ToggleButtonOption

enum class WellbeingPausedAppActions(
    override val resId: Int,
    override val iconEnabled: Int,
    override val iconDisabled: Int? = null
) : ToggleButtonOption {
    Add(R.string.add_app, R.drawable.add),
    AddAll(R.string.add_social_media, R.drawable.ic_app_grid),
    ClearAll(R.string.clear_all, R.drawable.reset)
}
