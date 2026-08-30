package org.elnix.dragonlauncher.base.model.enumsui.toggle

import org.elnix.dragonlauncher.base.model.enumsui.ToggleButtonOption
import org.elnix.dragonlauncher.i18n.R

public enum class WellbeingPausedAppActions(
    override val resId: Int,
    override val iconEnabled: Int,
    override val iconDisabled: Int? = null
) : ToggleButtonOption {
    Add(R.string.add_app, R.drawable.add),
    AddAll(R.string.add_social_media, R.drawable.apps),
    ClearAll(R.string.clear_all, R.drawable.reset)
}
