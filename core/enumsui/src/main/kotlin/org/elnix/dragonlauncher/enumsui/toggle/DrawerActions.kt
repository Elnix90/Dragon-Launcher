package org.elnix.dragonlauncher.enumsui.toggle

import org.elnix.dragonlauncher.enumsui.ToggleButtonOption
import org.elnix.dragonlauncher.i18n.R


enum class DrawerActions(
    override val resId: Int,
    override val iconEnabled: Int,
    override val iconDisabled: Int? = null
) : ToggleButtonOption {
    CLOSE(R.string.close_app_drawer, R.drawable.close),
    CLEAR(R.string.drawer_action_clear, R.drawable.close),
    TOGGLE_KB(R.string.toggle_kb, R.drawable.keyboard),
    CLOSE_KB(R.string.close_kb, R.drawable.keyboard_off),
    OPEN_KB(R.string.open_kb, R.drawable.keyboard),
    SEARCH_WEB(R.string.drawer_action_search_web, R.drawable.web),
    OPEN_FIRST_APP(R.string.drawer_action_open_first_app, R.drawable.open_in_new),
    NONE(R.string.none, R.drawable.circle),
    DISABLED(R.string.disabled, R.drawable.disabled_by_default);

    companion object {
        val defaultLeftDrawerAction = DISABLED
        val defaultRightDrawerAction = DISABLED
        val defaultEnterAction = CLEAR
        val defaultHomeAction = CLOSE
        val defaultScrollDownAction = CLOSE
        val defaultScrollUpAction = CLOSE_KB
        val defaultBackAction = CLOSE


        inline val DrawerActions.isUsed: Boolean
            get() = this != DISABLED && this != NONE

        inline val DrawerActions.notNone: Boolean
            get() = this != NONE
        inline val DrawerActions.notDisabled: Boolean
            get() = this != DISABLED
    }
}
