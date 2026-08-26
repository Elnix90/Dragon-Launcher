package org.elnix.dragonlauncher.base.model.enumsui.toggle

import org.elnix.dragonlauncher.base.model.enumsui.ToggleButtonOption
import org.elnix.dragonlauncher.i18n.R


public enum class DrawerActions(
    override val resId: Int,
    override val iconEnabled: Int,
    override val iconDisabled: Int? = null
) : org.elnix.dragonlauncher.base.model.enumsui.ToggleButtonOption {
    Close(R.string.close_app_drawer, R.drawable.close),
    Clear(R.string.drawer_action_clear, R.drawable.close),
    ToggleKb(R.string.toggle_kb, R.drawable.keyboard),
    CloseKb(R.string.close_kb, R.drawable.keyboard_off),
    OpenKb(R.string.open_kb, R.drawable.keyboard),
    SearchWeb(R.string.drawer_action_search_web, R.drawable.web),
    OpenFirstApp(R.string.drawer_action_open_first_app, R.drawable.open_in_new),

    /**
     * When you want some extra padding on the left or right but no click action.
     */
    None(R.string.none, R.drawable.circle),

    /**
     * No action at all. For the width actions, the paddings aren't even showed
     */
    Disabled(R.string.disabled, R.drawable.disabled_by_default);

    public companion object {
        public val defaultLeftDrawerAction: DrawerActions = Disabled
        public val defaultRightDrawerAction: DrawerActions = Disabled
        public val defaultEnterAction: DrawerActions = Clear
        public val defaultHomeAction: DrawerActions = Close
        public val defaultScrollDownAction: DrawerActions = Close
        public val defaultScrollUpAction: DrawerActions = CloseKb
        public val defaultBackAction: DrawerActions = Close


        public inline val DrawerActions.isUsed: Boolean
            get() = notNone && notDisabled

        public inline val DrawerActions.notNone: Boolean
            get() = this != None
        public inline val DrawerActions.notDisabled: Boolean
            get() = this != Disabled
    }
}
