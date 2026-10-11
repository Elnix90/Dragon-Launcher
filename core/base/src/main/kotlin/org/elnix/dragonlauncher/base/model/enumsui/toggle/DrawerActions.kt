package org.elnix.dragonlauncher.base.model.enumsui.toggle

import org.elnix.dragonlauncher.base.model.enumsui.ToggleButtonOption
import org.elnix.dragonlauncher.i18n.R

enum class DrawerActions(
	override val resId: Int,
	override val iconEnabled: Int,
	override val iconDisabled: Int? = null
) : ToggleButtonOption {
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
	Disabled(R.string.disabled, R.drawable.disabled_by_default)
	;

	companion object {
		val defaultLeftDrawerAction: DrawerActions = Disabled
		val defaultRightDrawerAction: DrawerActions = Disabled
		val defaultEnterAction: DrawerActions = Clear
		val defaultHomeAction: DrawerActions = Close
		val defaultScrollDownAction: DrawerActions = Close
		val defaultScrollUpAction: DrawerActions = CloseKb
		val defaultClickSearchAction: DrawerActions = SearchWeb
		val defaultBackAction: DrawerActions = Close

		inline val DrawerActions.isUsed: Boolean
			get() = notNone && notDisabled

		inline val DrawerActions.notNone: Boolean
			get() = this != None
		inline val DrawerActions.notDisabled: Boolean
			get() = this != Disabled
	}
}
